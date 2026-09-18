/**
 * StudyTracker Cloudflare Worker - Family Cloud Sync & Pairing
 * Zero-dependency, KV-backed serverless sync engine.
 * Multi-tenant, role-aware authoritative plan reconciliation.
 */

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Family-Code, X-Sender-Role',
  'Content-Type': 'application/json; charset=utf-8'
};

function normalizeOccurrence(o) {
  if (!o) return null;
  const youtubeUrl = (o.youtubeUrl !== undefined && o.youtubeUrl !== null && o.youtubeUrl !== '')
    ? o.youtubeUrl
    : ((o.youtube_url !== undefined && o.youtube_url !== null && o.youtube_url !== '') ? o.youtube_url : null);

  const parentNote = (o.parentNote !== undefined && o.parentNote !== null)
    ? o.parentNote
    : ((o.parent_note !== undefined && o.parent_note !== null) ? o.parent_note : (o.warningText || ''));

  const studentNote = (o.studentNote !== undefined && o.studentNote !== null)
    ? o.studentNote
    : ((o.student_note !== undefined && o.student_note !== null) ? o.student_note : null);

  return {
    id: o.id || o.occurrenceKey || '',
    familyCode: o.familyCode || o.family_code || '',
    date: o.date || '',
    planId: o.planId || o.plan_id || '',
    subject: o.subject || o.title || '',
    topic: o.topic || o.type || 'DAILY',
    targetDurationMin: Number(o.targetDurationMin ?? o.target_duration_min ?? o.plannedMinutes ?? 30),
    targetQuestionCount: Number(o.targetQuestionCount ?? o.target_question_count ?? o.targetCount ?? 0),
    completedDurationMin: Number(o.completedDurationMin ?? o.completed_duration_min ?? o.targetMinutes ?? 0),
    completedQuestionCount: Number(o.completedQuestionCount ?? o.completed_question_count ?? o.approvedCount ?? 0),
    status: o.status || 'PENDING',
    parentNote: parentNote,
    weekId: o.weekId || o.week_id || '',
    orderIndex: Number(o.orderIndex ?? o.order_index ?? 0),
    studentNote: studentNote,
    youtubeUrl: youtubeUrl,
    updatedAt: Number(o.updatedAt ?? o.updated_at ?? Date.now())
  };
}

function normalizeSession(s) {
  if (!s) return null;
  return {
    id: s.id || s.sessionId || '',
    familyCode: s.familyCode || s.family_code || '',
    occurrenceId: s.occurrenceId || s.occurrence_id || s.occurrenceKey || '',
    startTime: Number(s.startTime ?? s.start_time ?? 0),
    endTime: (s.endTime !== undefined && s.endTime !== null) ? Number(s.endTime) : ((s.end_time !== undefined && s.end_time !== null) ? Number(s.end_time) : null),
    durationMin: Number(s.durationMin ?? s.duration_min ?? 0),
    isCompleted: Boolean(s.isCompleted ?? s.is_completed ?? false),
    notes: s.notes || s.studentNote || '',
    updatedAt: Number(s.updatedAt ?? s.updated_at ?? Date.now())
  };
}

function normalizeReview(r) {
  if (!r) return null;
  return {
    id: r.id || `rev_${r.sessionId || r.session_id}`,
    familyCode: r.familyCode || r.family_code || '',
    sessionId: r.sessionId || r.session_id || '',
    isApproved: Boolean(r.isApproved ?? r.is_approved ?? true),
    rejectionReason: r.rejectionReason ?? r.rejection_reason ?? null,
    parentRating: Number(r.parentRating ?? r.parent_rating ?? 5),
    feedbackNote: r.feedbackNote ?? r.feedback_note ?? r.reviewNote ?? '',
    reviewedAt: Number(r.reviewedAt ?? r.reviewed_at ?? Date.now())
  };
}

function normalizeScreenshot(ss) {
  if (!ss) return null;
  const id = ss.id || ss.screenshotId || ss.screenshot_id || `ss_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`;
  const familyCode = ss.familyCode || ss.family_code || '';
  const sessionId = ss.sessionId || ss.session_id || ss.occurrenceKey || '';
  const imageUrl = ss.imageUrl || ss.image_url || ss.url || '';
  const timestamp = Number(ss.timestamp ?? ss.capturedAt ?? ss.captured_at ?? Date.now());

  if (!imageUrl) return null;

  return {
    id,
    familyCode,
    sessionId,
    imageUrl,
    timestamp,
    aiAnalysisJson: ss.aiAnalysisJson || ss.ai_analysis_json || null
  };
}

const inMemoryStore = new Map();

async function getStoreData(env, key) {
  if (env && env.STUDY_SYNC_KV) {
    const data = await env.STUDY_SYNC_KV.get(key);
    return data ? JSON.parse(data) : null;
  }
  return inMemoryStore.get(key) || null;
}

async function setStoreData(env, key, value, ttlSeconds = 60 * 60 * 24 * 60) {
  if (env && env.STUDY_SYNC_KV) {
    await env.STUDY_SYNC_KV.put(key, JSON.stringify(value), { expirationTtl: ttlSeconds });
  } else {
    inMemoryStore.set(key, value);
  }
}

export default {
  async fetch(request, env = {}, ctx) {
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: CORS_HEADERS, status: 204 });
    }

    const url = new URL(request.url);
    const path = url.pathname;

    try {
      // 1. Health check
      if (path === '/' || path === '/api/ping') {
        return new Response(JSON.stringify({ status: 'ok', service: 'StudyTracker Cloud Sync', timestamp: Date.now() }), {
          headers: CORS_HEADERS
        });
      }

      // 2. Family Pairing / Code Generator
      if (path === '/api/pair' && request.method === 'POST') {
        const body = await request.json().catch(() => ({}));
        let code = body.familyCode ? body.familyCode.toUpperCase().trim() : '';
        if (!code) {
          const randomNum = Math.floor(1000 + Math.random() * 9000);
          code = `ST-${randomNum}`;
        }
        
        const existing = await getStoreData(env, `family:${code}`);
        if (!existing) {
          const initialRecord = {
            familyCode: code,
            createdAt: Date.now(),
            updatedAt: Date.now(),
            plan: null,
            tasks: [],
            occurrences: [],
            sessions: [],
            screenshots: [],
            reviews: [],
            quizzes: []
          };
          await setStoreData(env, `family:${code}`, initialRecord);
        }

        return new Response(JSON.stringify({ success: true, familyCode: code }), {
          headers: CORS_HEADERS
        });
      }

      // 3. Sync Endpoint (GET / POST)
      if (path === '/api/sync') {
        const familyCode = (url.searchParams.get('code') || request.headers.get('X-Family-Code') || 'ST-2026').toUpperCase().trim();
        const storeKey = `family:${familyCode}`;

        if (request.method === 'GET') {
          const current = await getStoreData(env, storeKey);
          if (!current) {
            const initialData = {
              familyCode,
              updatedAt: Date.now(),
              plan: null,
              tasks: [],
              occurrences: [],
              sessions: [],
              screenshots: [],
              reviews: [],
              quizzes: []
            };
            return new Response(JSON.stringify({
              success: true,
              familyCode,
              data: initialData
            }), { headers: CORS_HEADERS });
          }

          current.occurrences = Array.isArray(current.occurrences) ? current.occurrences : [];
          current.tasks = Array.isArray(current.tasks) ? current.tasks : [];
          current.sessions = Array.isArray(current.sessions) ? current.sessions : [];
          current.screenshots = Array.isArray(current.screenshots) ? current.screenshots : [];
          current.reviews = Array.isArray(current.reviews) ? current.reviews : [];
          current.quizzes = Array.isArray(current.quizzes) ? current.quizzes : [];

          return new Response(JSON.stringify({ success: true, data: current }), { headers: CORS_HEADERS });
        }

        if (request.method === 'POST') {
          const incoming = await request.json();
          const senderRole = (incoming.senderRole || request.headers.get('X-Sender-Role') || 'PARENT').toUpperCase().trim();
          const action = (incoming.action || 'SYNC').toUpperCase().trim();

          let current = (await getStoreData(env, storeKey)) || {
            familyCode,
            createdAt: Date.now(),
            updatedAt: Date.now(),
            plan: null,
            tasks: [],
            occurrences: [],
            sessions: [],
            screenshots: [],
            reviews: [],
            quizzes: []
          };

          current.occurrences = Array.isArray(current.occurrences) ? current.occurrences : [];
          current.tasks = Array.isArray(current.tasks) ? current.tasks : [];
          current.sessions = Array.isArray(current.sessions) ? current.sessions : [];
          current.screenshots = Array.isArray(current.screenshots) ? current.screenshots : [];
          current.reviews = Array.isArray(current.reviews) ? current.reviews : [];
          current.quizzes = Array.isArray(current.quizzes) ? current.quizzes : [];

          // A) Tam Sıfırlama (Wipe) - SADECE ve SADECE açıkça action === 'WIPE' ise
          if (action === 'WIPE') {
            current = {
              familyCode,
              createdAt: current.createdAt || Date.now(),
              updatedAt: Date.now(),
              plan: null,
              tasks: [],
              occurrences: [],
              sessions: [],
              screenshots: [],
              reviews: [],
              quizzes: []
            };
            await setStoreData(env, storeKey, current);
            return new Response(JSON.stringify({ success: true, data: current, message: 'Tüm bulut verisi sıfırlandı' }), { headers: CORS_HEADERS });
          }

          // B) İlerleme Sıfırlama (Reset Progress)
          if (action === 'RESET') {
            for (const occ of (current.occurrences || [])) {
              occ.status = 'PENDING';
              occ.completedDurationMin = 0;
              occ.completedQuestionCount = 0;
              occ.studentNote = null;
              occ.parentNote = '';
            }
            current.sessions = [];
            current.screenshots = [];
            current.reviews = [];
            current.updatedAt = Date.now();
            await setStoreData(env, storeKey, current);
            return new Response(JSON.stringify({ success: true, data: current, message: 'Öğrenci ilerlemesi sıfırlandı' }), { headers: CORS_HEADERS });
          }

          // C) Tekil Ders Güncelleme (Patch Single Task - Zero Side-effects)
          if (action === 'PATCH_TASK' && incoming.patchTask) {
            const pt = incoming.patchTask;
            const targetKey = pt.id || pt.occurrenceKey;
            const currentOccs = (current.occurrences || []).map(normalizeOccurrence).filter(Boolean);
            const idx = currentOccs.findIndex(o => o.id === targetKey);
            if (idx !== -1) {
              const oldOcc = currentOccs[idx];
              currentOccs[idx] = {
                ...oldOcc,
                subject: pt.subject !== undefined ? pt.subject : (pt.title !== undefined ? pt.title : oldOcc.subject),
                targetDurationMin: pt.targetDurationMin !== undefined ? pt.targetDurationMin : (pt.plannedMinutes !== undefined ? pt.plannedMinutes : oldOcc.targetDurationMin),
                targetQuestionCount: pt.targetQuestionCount !== undefined ? pt.targetQuestionCount : (pt.targetCount !== undefined ? pt.targetCount : oldOcc.targetQuestionCount),
                youtubeUrl: pt.youtubeUrl !== undefined ? pt.youtubeUrl : oldOcc.youtubeUrl,
                parentNote: pt.parentNote !== undefined ? pt.parentNote : (pt.warningText !== undefined ? pt.warningText : oldOcc.parentNote),
                updatedAt: Date.now()
              };
              current.occurrences = currentOccs;
              current.updatedAt = Date.now();
              await setStoreData(env, storeKey, current);
              return new Response(JSON.stringify({ success: true, data: current, message: `Ders '${targetKey}' güncellendi` }), { headers: CORS_HEADERS });
            }
          }

          const isAdmin = senderRole === 'ADMIN' || senderRole === 'CLI' || senderRole === 'PARENTING_AI';
          const isParent = senderRole === 'PARENT';
          const isChild = senderRole === 'CHILD';

          if (incoming.plan) {
            current.planSource = incoming.planSource || (isAdmin ? 'CLI / Bilgisayar' : 'Veli Masası');
          }

          // Ensure tombstones array exists for deleted tasks
          if (!Array.isArray(current.deletedOccurrences)) {
            current.deletedOccurrences = [];
          }

          // Normalize all existing occurrences
          current.occurrences = (current.occurrences || []).map(normalizeOccurrence).filter(Boolean);
          current.sessions = (current.sessions || []).map(normalizeSession).filter(Boolean);
          current.screenshots = (current.screenshots || []).map(normalizeScreenshot).filter(Boolean);
          current.reviews = (current.reviews || []).map(normalizeReview).filter(Boolean);

          const prevOccMap = new Map(current.occurrences.map(o => [o.id, o]));
          const tombstoneSet = new Set(current.deletedOccurrences || []);

          const parseTime = (val) => {
            if (!val) return 0;
            if (typeof val === 'number') return val;
            const num = Number(val);
            if (!isNaN(num)) return num;
            const parsed = Date.parse(val);
            return isNaN(parsed) ? 0 : parsed;
          };

          const incomingOccurrences = Array.isArray(incoming.occurrences)
            ? incoming.occurrences.map(normalizeOccurrence).filter(Boolean)
            : [];

          // 1. ADMIN / CLI (Supreme Master Plan Authority: CLI > PARENT > CHILD)
          if (isAdmin) {
            current.planUpdatedAt = Date.now();
            if (incoming.plan !== undefined) current.plan = incoming.plan;
            if (Array.isArray(incoming.tasks)) current.tasks = incoming.tasks;

            if (incomingOccurrences.length > 0) {
              const incomingKeys = new Set(incomingOccurrences.map(o => o.id));

              // Find deleted occurrences and tombstone them
              for (const [oldId] of prevOccMap) {
                if (!incomingKeys.has(oldId)) {
                  tombstoneSet.add(oldId);
                }
              }

              // Merge incoming occurrences while preserving existing student progress & approval
              const mergedOccs = incomingOccurrences.map(inc => {
                const prev = prevOccMap.get(inc.id);
                tombstoneSet.delete(inc.id); // Re-added or active
                if (!prev) return inc;

                let resolvedStatus = inc.status || prev.status || 'PENDING';
                if (prev.status === 'APPROVED' || inc.status === 'APPROVED') {
                  resolvedStatus = 'APPROVED';
                } else if (prev.status === 'WAITING_REVIEW' && inc.status === 'PENDING') {
                  resolvedStatus = 'WAITING_REVIEW';
                }

                return {
                  ...prev,
                  ...inc,
                  subject: inc.subject !== undefined ? inc.subject : prev.subject,
                  date: inc.date !== undefined ? inc.date : prev.date,
                  targetDurationMin: inc.targetDurationMin !== undefined ? inc.targetDurationMin : prev.targetDurationMin,
                  targetQuestionCount: inc.targetQuestionCount !== undefined ? inc.targetQuestionCount : prev.targetQuestionCount,
                  youtubeUrl: inc.youtubeUrl !== undefined ? inc.youtubeUrl : prev.youtubeUrl,
                  parentNote: inc.parentNote !== undefined ? inc.parentNote : prev.parentNote,
                  completedDurationMin: Math.max(prev.completedDurationMin || 0, inc.completedDurationMin || 0),
                  completedQuestionCount: Math.max(prev.completedQuestionCount || 0, inc.completedQuestionCount || 0),
                  studentNote: inc.studentNote || prev.studentNote,
                  status: resolvedStatus
                };
              });

              current.occurrences = mergedOccs;
              current.deletedOccurrences = Array.from(tombstoneSet);
            }
          } else if (isParent) {
            // 2. PARENT (Veli Authority)
            const incomingPlanTime = parseTime(incoming.plan?.updatedAt);
            const currentPlanTime = parseTime(current.planUpdatedAt || current.plan?.updatedAt);

            if (action === 'DELETE_TASK' && incoming.deleteTaskId) {
              current.occurrences = current.occurrences.filter(o => o.id !== incoming.deleteTaskId);
              tombstoneSet.add(incoming.deleteTaskId);
              current.deletedOccurrences = Array.from(tombstoneSet);
            } else if (incoming.plan && Array.isArray(incoming.tasks) && incoming.tasks.length > 0 && (!current.plan || incomingPlanTime >= currentPlanTime)) {
              // Full plan overwrite from Parent (only if newer or equal to current plan timestamp, or no prior plan)
              current.plan = incoming.plan;
              current.tasks = incoming.tasks;
              current.planUpdatedAt = Date.now();
              if (incomingOccurrences.length > 0) {
                const incomingKeys = new Set(incomingOccurrences.map(o => o.id));
                for (const [oldId] of prevOccMap) {
                  if (!incomingKeys.has(oldId)) tombstoneSet.add(oldId);
                }
                current.occurrences = incomingOccurrences;
                current.deletedOccurrences = Array.from(tombstoneSet);
              }
            } else {
              // Regular Sync from Parent:
              // Update task details and reviews on existing occurrences only.
              // NEVER resurrect deleted/tombstoned occurrences!
              if (incomingOccurrences.length > 0) {
                for (const remote of incomingOccurrences) {
                  if (tombstoneSet.has(remote.id)) continue; // Do not resurrect deleted tasks

                  const local = prevOccMap.get(remote.id);
                  if (local) {
                    let resolvedStatus = remote.status || local.status || 'PENDING';
                    if (remote.status === 'APPROVED' || local.status === 'APPROVED') resolvedStatus = 'APPROVED';
                    else if (remote.status === 'WAITING_REVIEW' || local.status === 'WAITING_REVIEW') resolvedStatus = 'WAITING_REVIEW';
                    else if (remote.status === 'ACTIVE' || local.status === 'ACTIVE') resolvedStatus = 'ACTIVE';

                    prevOccMap.set(remote.id, {
                      ...local,
                      subject: remote.subject !== undefined ? remote.subject : local.subject,
                      date: remote.date !== undefined ? remote.date : local.date,
                      targetDurationMin: remote.targetDurationMin !== undefined ? remote.targetDurationMin : local.targetDurationMin,
                      targetQuestionCount: remote.targetQuestionCount !== undefined ? remote.targetQuestionCount : local.targetQuestionCount,
                      youtubeUrl: remote.youtubeUrl !== undefined ? remote.youtubeUrl : local.youtubeUrl,
                      parentNote: remote.parentNote !== undefined ? remote.parentNote : local.parentNote,
                      status: resolvedStatus,
                      completedQuestionCount: Math.max(local.completedQuestionCount || 0, remote.completedQuestionCount || 0),
                      completedDurationMin: Math.max(local.completedDurationMin || 0, remote.completedDurationMin || 0),
                      studentNote: local.studentNote || remote.studentNote
                    });
                  }
                }
                current.occurrences = Array.from(prevOccMap.values());
              }
            }
          } else {
            // 3. CHILD (Öğrenci Authority: Activity & Progress only)
            if (incomingOccurrences.length > 0) {
              for (const remote of incomingOccurrences) {
                if (tombstoneSet.has(remote.id)) continue;

                const local = prevOccMap.get(remote.id);
                if (local) {
                  let resolvedStatus = local.status;
                  if (local.status !== 'APPROVED') {
                    if (remote.status === 'WAITING_REVIEW' || remote.status === 'ACTIVE') {
                      resolvedStatus = remote.status;
                    }
                  }

                  prevOccMap.set(remote.id, {
                    ...local,
                    status: resolvedStatus,
                    completedQuestionCount: Math.max(local.completedQuestionCount || 0, remote.completedQuestionCount || 0),
                    completedDurationMin: Math.max(local.completedDurationMin || 0, remote.completedDurationMin || 0),
                    studentNote: remote.studentNote || local.studentNote
                  });
                }
              }
              current.occurrences = Array.from(prevOccMap.values());
            }
          }

          // 4. Sessions (Student -> Parent)
          if (Array.isArray(incoming.sessions) && incoming.sessions.length > 0) {
            const sessMap = new Map((current.sessions || []).map(s => [s.id, s]));
            incoming.sessions.map(normalizeSession).filter(Boolean).forEach(s => {
              const prev = sessMap.get(s.id);
              sessMap.set(s.id, prev ? { ...prev, ...s } : s);
            });
            current.sessions = Array.from(sessMap.values());
          }

          // 5. Screenshots (Student -> Parent)
          if (Array.isArray(incoming.screenshots) && incoming.screenshots.length > 0) {
            const ssMap = new Map((current.screenshots || []).map(s => [s.id, s]));
            incoming.screenshots.map(normalizeScreenshot).filter(Boolean).forEach(s => {
              const prev = ssMap.get(s.id);
              ssMap.set(s.id, prev ? { ...prev, ...s } : s);
            });
            current.screenshots = Array.from(ssMap.values()).slice(-40);
          }

          // 6. Reviews (Parent / Admin -> Student)
          if (Array.isArray(incoming.reviews) && incoming.reviews.length > 0) {
            const revMap = new Map((current.reviews || []).map(r => [r.id, r]));
            incoming.reviews.map(normalizeReview).filter(Boolean).forEach(r => revMap.set(r.id, r));
            current.reviews = Array.from(revMap.values());
          }

          // 7. Quizzes (Student Quiz Submissions)
          if (Array.isArray(incoming.quizzes) && incoming.quizzes.length > 0) {
            const quizMap = new Map((current.quizzes || []).map(q => [q.quizId, q]));
            incoming.quizzes.forEach(q => {
              const existing = quizMap.get(q.quizId);
              if (!existing) {
                quizMap.set(q.quizId, q);
              } else {
                quizMap.set(q.quizId, {
                  ...existing,
                  ...q,
                  completed: existing.completed || q.completed,
                  score: q.score !== undefined ? q.score : existing.score,
                  studentAnswers: q.studentAnswers || existing.studentAnswers,
                  studentNote: q.studentNote || existing.studentNote
                });
              }
            });
            current.quizzes = Array.from(quizMap.values());
          }

          current.updatedAt = Date.now();
          await setStoreData(env, storeKey, current);

          return new Response(JSON.stringify({ success: true, data: current }), { headers: CORS_HEADERS });
        }
      }

      return new Response(JSON.stringify({ error: 'Endpoint not found', path }), {
        status: 404,
        headers: CORS_HEADERS
      });
    } catch (err) {
      return new Response(JSON.stringify({ error: err.message, stack: err.stack }), {
        status: 500,
        headers: CORS_HEADERS
      });
    }
  }
};
