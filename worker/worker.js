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
            return new Response(JSON.stringify({
              success: true,
              familyCode,
              updatedAt: Date.now(),
              plan: null,
              tasks: [],
              occurrences: [],
              sessions: [],
              reviews: [],
              quizzes: []
            }), { headers: CORS_HEADERS });
          }
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
            reviews: [],
            quizzes: []
          };

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
            current.reviews = [];
            current.updatedAt = Date.now();
            await setStoreData(env, storeKey, current);
            return new Response(JSON.stringify({ success: true, data: current, message: 'Öğrenci ilerlemesi sıfırlandı' }), { headers: CORS_HEADERS });
          }

          const isAdmin = senderRole === 'ADMIN' || senderRole === 'CLI' || senderRole === 'PARENTING_AI';
          const isParent = senderRole === 'PARENT';
          const isChild = senderRole === 'CHILD';

          // 1. Plan & Task Templates (CLI / ADMIN has supreme master authority)
          if (isAdmin) {
            if (incoming.plan !== undefined) current.plan = incoming.plan;
            if (Array.isArray(incoming.tasks)) current.tasks = incoming.tasks;
            if (Array.isArray(incoming.occurrences)) current.occurrences = incoming.occurrences;
          } else if (isParent) {
            // Veli uygulaması: Yalnızca dolu bir plan gönderdiyse ve buluttaki plandan yeniyse günceller
            if (incoming.plan && Array.isArray(incoming.tasks) && incoming.tasks.length > 0) {
              if (!current.plan || (incoming.plan.updatedAt || 0) >= (current.plan.updatedAt || 0)) {
                current.plan = incoming.plan;
                current.tasks = incoming.tasks;
              }
            }
          } else if (isChild) {
            // Öğrenci: Asla planı veya şablonları ezemez
            if (incoming.plan && !current.plan) {
              current.plan = incoming.plan;
            }
          }

          // 2. Occurrences Mutabakatı
          const existingOccMap = new Map((current.occurrences || []).map(o => [o.id, o]));
          
          if (isAdmin) {
            // Admin occurrences were already assigned in step 1 if provided
          } else if (isParent) {
            // Veli uygulaması senkronizasyonu:
            // Eğer Veli uygulamasında yerel dersler varsa, mevcut bulut dersleriyle birleştirir
            if (Array.isArray(incoming.occurrences) && incoming.occurrences.length > 0) {
              for (const remote of incoming.occurrences) {
                const local = existingOccMap.get(remote.id);
                if (!local) {
                  existingOccMap.set(remote.id, remote);
                } else {
                  let resolvedStatus = remote.status || local.status || 'PENDING';
                  if (local.status === 'APPROVED' || remote.status === 'APPROVED') resolvedStatus = 'APPROVED';
                  else if (remote.status === 'WAITING_REVIEW' || local.status === 'WAITING_REVIEW') resolvedStatus = 'WAITING_REVIEW';
                  else if (remote.status === 'ACTIVE' || local.status === 'ACTIVE') resolvedStatus = 'ACTIVE';

                  existingOccMap.set(remote.id, {
                    ...local,
                    ...remote,
                    subject: remote.subject || local.subject,
                    date: remote.date || local.date,
                    targetDurationMin: remote.targetDurationMin || local.targetDurationMin,
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
              current.occurrences = Array.from(existingOccMap.values());
            }
            // NOT: Eğer Veli'nin yerel occurrences listesi boş ise, buluttaki master listeyi korur ve Veli'ye geri döndürür!
          } else {
            // Öğrenci ilerleme güncellemesi
            if (Array.isArray(incoming.occurrences) && incoming.occurrences.length > 0) {
              for (const remote of incoming.occurrences) {
                const existing = existingOccMap.get(remote.id);
                if (existing) {
                  let resolvedStatus = remote.status || existing.status;
                  if (existing.status === 'APPROVED') resolvedStatus = 'APPROVED';
                  else if (remote.status === 'APPROVED') resolvedStatus = 'APPROVED';
                  else if (remote.status === 'WAITING_REVIEW') resolvedStatus = 'WAITING_REVIEW';
                  else if (remote.status === 'ACTIVE') resolvedStatus = 'ACTIVE';

                  existingOccMap.set(remote.id, {
                    ...existing,
                    status: resolvedStatus,
                    completedQuestionCount: Math.max(existing.completedQuestionCount || 0, remote.completedQuestionCount || 0),
                    completedDurationMin: Math.max(existing.completedDurationMin || 0, remote.completedDurationMin || 0),
                    studentNote: remote.studentNote || existing.studentNote
                  });
                }
              }
              current.occurrences = Array.from(existingOccMap.values());
            }
          }

          // 3. Sessions (Student -> Parent)
          if (Array.isArray(incoming.sessions) && incoming.sessions.length > 0) {
            const sessMap = new Map((current.sessions || []).map(s => [s.id, s]));
            incoming.sessions.forEach(s => sessMap.set(s.id, s));
            current.sessions = Array.from(sessMap.values());
          }

          // 4. Reviews (Parent -> Student)
          if (Array.isArray(incoming.reviews) && incoming.reviews.length > 0) {
            const revMap = new Map((current.reviews || []).map(r => [r.id, r]));
            incoming.reviews.forEach(r => revMap.set(r.id, r));
            current.reviews = Array.from(revMap.values());
          }

          // 5. Quizzes
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
