/**
 * StudyTracker Sync Engine v2.0 - Segregated & Granular Architecture
 * Zero-dependency Cloudflare Worker (Multi-tenant, Role-Aware, KV Sharded)
 * Mimari: Veri Ayrıştırma (Data Segregation), Komut Deseni (Command Pattern) ve RBAC.
 */

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Family-Code, X-Sender-Role',
  'Content-Type': 'application/json; charset=utf-8'
};

// --- YARDIMCI FONKSİYONLAR & KV YÖNETİMİ ---
const json = (data, status = 200) => new Response(JSON.stringify(data), { status, headers: CORS_HEADERS });
const error = (msg, status = 400) => json({ success: false, error: msg }, status);

// Local test (wrangler / node test-sync) için geçici bellek hafızası
const inMemoryStore = new Map();

async function getKV(env, key) {
  if (env && env.STUDY_SYNC_KV) {
    const raw = await env.STUDY_SYNC_KV.get(key);
    return raw ? JSON.parse(raw) : null;
  }
  return inMemoryStore.get(key) || null;
}

async function putKV(env, key, val, ttlSeconds = null) {
  if (env && env.STUDY_SYNC_KV) {
    const opts = ttlSeconds ? { expirationTtl: ttlSeconds } : {};
    await env.STUDY_SYNC_KV.put(key, JSON.stringify(val), opts);
  } else {
    inMemoryStore.set(key, val);
  }
}

async function deleteKV(env, key) {
  if (env && env.STUDY_SYNC_KV) {
    await env.STUDY_SYNC_KV.delete(key);
  } else {
    inMemoryStore.delete(key);
  }
}

async function listKV(env, prefix) {
  if (env && env.STUDY_SYNC_KV) {
    return await env.STUDY_SYNC_KV.list({ prefix });
  }
  const keys = [];
  for (const k of inMemoryStore.keys()) {
    if (k.startsWith(prefix)) keys.push({ name: k });
  }
  return { keys };
}

// --- NORMALİZASYON FONKSİYONLARI ---

function normalizeTask(t) {
  if (!t) return null;
  const id = t.id || t.occurrenceKey || t.taskId || `task_${Date.now()}`;
  const youtubeUrl = (t.youtubeUrl !== undefined && t.youtubeUrl !== null && t.youtubeUrl !== '')
    ? t.youtubeUrl
    : ((t.youtube_url !== undefined && t.youtube_url !== null && t.youtube_url !== '') ? t.youtube_url : null);
  const parentNote = t.parentNote ?? t.parent_note ?? t.warningText ?? '';
  const studentNote = t.studentNote ?? t.student_note ?? null;

  return {
    id,
    occurrenceKey: id,
    familyCode: t.familyCode || t.family_code || '',
    date: t.date || '',
    planId: t.planId || t.plan_id || id,
    subject: t.subject || t.title || 'Ders',
    title: t.subject || t.title || 'Ders',
    topic: t.topic || t.type || 'DAILY',
    type: t.topic || t.type || 'DAILY',
    targetDurationMin: Number(t.targetDurationMin ?? t.target_duration_min ?? t.plannedMinutes ?? 30),
    plannedMinutes: Number(t.targetDurationMin ?? t.target_duration_min ?? t.plannedMinutes ?? 30),
    targetQuestionCount: Number(t.targetQuestionCount ?? t.target_question_count ?? t.targetCount ?? 0),
    targetCount: Number(t.targetQuestionCount ?? t.target_question_count ?? t.targetCount ?? 0),
    completedDurationMin: Number(t.completedDurationMin ?? t.completed_duration_min ?? t.completedMin ?? 0),
    completedQuestionCount: Number(t.completedQuestionCount ?? t.completed_question_count ?? t.completedQuestions ?? 0),
    status: t.status || 'PENDING',
    parentNote,
    warningText: parentNote,
    warning: Boolean(parentNote && parentNote.length > 0),
    weekId: t.weekId || t.week_id || '',
    orderIndex: Number(t.orderIndex ?? t.order_index ?? 0),
    studentNote,
    youtubeUrl,
    updatedAt: Number(t.updatedAt ?? t.updated_at ?? Date.now())
  };
}

function normalizeSession(s) {
  if (!s) return null;
  return {
    id: s.id || s.sessionId || `sess_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
    sessionId: s.id || s.sessionId || `sess_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
    familyCode: s.familyCode || s.family_code || '',
    occurrenceId: s.occurrenceId || s.occurrence_id || s.occurrenceKey || '',
    occurrenceKey: s.occurrenceId || s.occurrence_id || s.occurrenceKey || '',
    startTime: Number(s.startTime ?? s.start_time ?? 0),
    endTime: (s.endTime !== undefined && s.endTime !== null) ? Number(s.endTime) : ((s.end_time !== undefined && s.end_time !== null) ? Number(s.end_time) : null),
    durationMin: Number(s.durationMin ?? s.duration_min ?? 0),
    isCompleted: Boolean(s.isCompleted ?? s.is_completed ?? false),
    notes: s.notes || s.studentNote || '',
    studentNote: s.notes || s.studentNote || '',
    updatedAt: Number(s.updatedAt ?? s.updated_at ?? Date.now())
  };
}

function normalizeReview(r) {
  if (!r) return null;
  return {
    id: r.id || `rev_${r.sessionId || r.session_id || Date.now()}`,
    familyCode: r.familyCode || r.family_code || '',
    sessionId: r.sessionId || r.session_id || '',
    occurrenceKey: r.occurrenceKey || r.occurrenceId || r.taskId || '',
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
    screenshotId: id,
    familyCode,
    sessionId,
    occurrenceKey: ss.occurrenceKey || sessionId,
    imageUrl,
    url: imageUrl,
    timestamp,
    capturedAt: timestamp,
    aiAnalysisJson: ss.aiAnalysisJson || ss.ai_analysis_json || null
  };
}

function normalizeMessage(m) {
  if (!m) return null;
  return {
    id: m.id || `msg_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
    familyCode: m.familyCode || m.family_code || '',
    senderRole: (m.senderRole || m.sender_role || 'PARENT').toUpperCase().trim(),
    title: m.title || 'Bildirim',
    message: m.message || m.body || m.text || '',
    type: m.type || 'REMINDER',
    timestamp: Number(m.timestamp ?? m.createdAt ?? Date.now()),
    isRead: Boolean(m.isRead ?? m.is_read ?? false),
    targetDate: m.targetDate || m.target_date || null,
    targetTaskId: m.targetTaskId || m.targetOccurrenceId || m.target_occurrence_id || null,
    targetOccurrenceId: m.targetTaskId || m.targetOccurrenceId || m.target_occurrence_id || null
  };
}

// --- ANA ROUTER & HANDLERS ---

export default {
  async fetch(request, env = {}, ctx) {
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: CORS_HEADERS, status: 204 });
    }

    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    // Header ve URL parametrelerinden aile kodu ve rolü çek
    const familyCode = (url.searchParams.get('code') || request.headers.get('X-Family-Code') || 'ST-2026').toUpperCase().trim();
    const role = (request.headers.get('X-Sender-Role') || 'PARENT').toUpperCase().trim();

    // 1. Health / Ping Check
    if (path === '/' || path === '/api/ping' || path === '/api/health') {
      return json({
        status: 'ok',
        version: '2.0-segregated',
        engine: 'StudyTracker Multi-tenant Sharded Sync Engine',
        timestamp: Date.now()
      });
    }

    // 2. Family Pairing
    if (path === '/api/pair' && method === 'POST') {
      return await handlePair(request, env, familyCode);
    }

    try {
      // 3. READ-ONLY AGGREGATED SYNC (v1 & v2 Uyumlu)
      if ((path === '/api/v2/sync' || path === '/api/sync') && method === 'GET') {
        return await handleSync(env, familyCode);
      }

      // 4. UNIFIED WRITE SYNC (v1 Legacy Client Payload Sharding)
      if ((path === '/api/v2/sync' || path === '/api/sync') && method === 'POST') {
        return await handleLegacySyncPost(request, env, familyCode, role);
      }

      // 5. PLAN & TASKS (Veli / Admin Otoriter Yönetim)
      if (path === '/api/v2/plan' || path === '/api/plan' || path.startsWith('/api/v2/tasks') || path.startsWith('/api/tasks')) {
        return await handlePlanAndTasks(request, env, familyCode, path, role);
      }

      // 6. COMMANDS (Komut Deseni: REJECT_TASK, RESET_ALL_PROGRESS, WIPE, RESTORE)
      if (path === '/api/v2/commands' || path === '/api/commands' || path === '/api/reset' || path === '/api/wipe') {
        return await handleCommands(request, env, familyCode, role, path);
      }

      // 7. PROGRESS (Öğrenci İlerleme Kaydı)
      if (path.startsWith('/api/v2/progress') || path.startsWith('/api/progress')) {
        return await handleProgress(request, env, familyCode, path, role);
      }

      // 8. SESSIONS (Çalışma Oturum Logları)
      if (path === '/api/v2/sessions' || path === '/api/sessions') {
        if (method === 'GET') {
          const sessions = (await getKV(env, `family:${familyCode}:sessions`)) || [];
          return json({ success: true, sessions });
        }
        return await handleAppendLog(request, env, familyCode, 'sessions', 100, normalizeSession);
      }

      // 9. SCREENSHOTS (Kanıt Ekran Görüntüleri)
      if (path === '/api/v2/screenshots' || path === '/api/screenshots') {
        if (method === 'GET') {
          const screenshots = (await getKV(env, `family:${familyCode}:screenshots`)) || [];
          return json({ success: true, screenshots });
        }
        return await handleAppendLog(request, env, familyCode, 'screenshots', 50, normalizeScreenshot);
      }

      // 10. REVIEWS (Veli İnceleme & Onayları)
      if (path === '/api/v2/reviews' || path === '/api/reviews') {
        if (method === 'GET') {
          const reviews = (await getKV(env, `family:${familyCode}:reviews`)) || [];
          return json({ success: true, reviews });
        }
        return await handleReview(request, env, familyCode, role);
      }

      // 11. MESSAGES & NOTIFICATIONS (Veli <-> Öğrenci Anlık Bildirim)
      if (path === '/api/v2/messages' || path === '/api/messages' || path === '/api/notify') {
        return await handleMessages(request, env, familyCode, role, url);
      }

      return error(`Endpoint not found: ${path}`, 404);

    } catch (err) {
      console.error(`[Worker Error] ${path}:`, err);
      return error(err.message || 'Internal Server Error', 500);
    }
  }
};

// --- İŞ MANTIKLARI VE HANDLER FONKSİYONLARI ---

async function handlePair(request, env, providedCode) {
  const body = await request.json().catch(() => ({}));
  let code = (providedCode && providedCode !== 'ST-2026') ? providedCode : (body.familyCode ? body.familyCode.toUpperCase().trim() : '');
  if (!code) {
    code = `ST-${Math.floor(1000 + Math.random() * 9000)}`;
  }

  const prefix = `family:${code}:`;
  let meta = await getKV(env, `${prefix}meta`);
  if (!meta) {
    meta = { createdAt: Date.now(), resetAt: 0, wipedAt: 0, tombstones: [] };
    await putKV(env, `${prefix}meta`, meta);
    await putKV(env, `${prefix}plan`, { tasks: [], plan: null, updatedAt: Date.now() });
    await putKV(env, `${prefix}messages`, []);
  }

  return json({ success: true, familyCode: code });
}

/**
 * Parçalanmış (sharded) KV verilerini okur ve tek bir zenginleştirilmiş yanıtta birleştirir.
 */
async function handleSync(env, familyCode) {
  const prefix = `family:${familyCode}:`;

  const [meta, planData, sessions, screenshots, messages, reviews, quizzes] = await Promise.all([
    getKV(env, `${prefix}meta`),
    getKV(env, `${prefix}plan`),
    getKV(env, `${prefix}sessions`),
    getKV(env, `${prefix}screenshots`),
    getKV(env, `${prefix}messages`),
    getKV(env, `${prefix}reviews`),
    getKV(env, `${prefix}quizzes`)
  ]);

  // Progress shard'larını paralel çek
  const progressList = await listKV(env, `${prefix}progress:`);
  const progressPromises = (progressList.keys || []).map(k => getKV(env, k.name));
  const progressItems = (await Promise.all(progressPromises)).filter(Boolean);

  const progressMap = {};
  progressItems.forEach(p => {
    if (p.taskId) progressMap[p.taskId] = p;
  });

  const rawTasks = planData?.tasks || [];
  const normalizedTasks = rawTasks.map(normalizeTask).filter(Boolean);

  // Görev tanımları ile dinamik ilerleme verilerini birleştirip occurrences listesi oluştur
  const occurrences = normalizedTasks.map(task => {
    const prog = progressMap[task.id] || {};
    return {
      ...task,
      completedDurationMin: Number(prog.completedMin ?? prog.completedDurationMin ?? task.completedDurationMin ?? 0),
      completedQuestionCount: Number(prog.completedQuestions ?? prog.completedQuestionCount ?? task.completedQuestionCount ?? 0),
      status: prog.status || task.status || 'PENDING',
      studentNote: prog.studentNote !== undefined ? prog.studentNote : task.studentNote,
      rejectionReason: prog.rejectionReason || null,
      parentRating: prog.parentRating ?? null,
      feedbackNote: prog.feedbackNote ?? task.parentNote ?? '',
      reviewedAt: prog.reviewedAt ?? null
    };
  });

  const finalMeta = meta || { resetAt: 0, wipedAt: 0, tombstones: [], planSource: 'Cloud Master' };

  const unifiedData = {
    familyCode,
    updatedAt: planData?.updatedAt || Date.now(),
    planSource: finalMeta.planSource || planData?.source || 'Veli / Bulut Masası',
    plan: planData?.plan || null,
    tasks: planData?.tasks || [],
    occurrences,
    progress: progressMap,
    sessions: sessions || [],
    screenshots: screenshots || [],
    reviews: reviews || [],
    messages: messages || [],
    quizzes: quizzes || [],
    meta: finalMeta,
    deletedOccurrences: finalMeta.tombstones || [],
    resetAt: finalMeta.resetAt || 0
  };

  return json({
    success: true,
    familyCode,
    serverTime: Date.now(),
    data: unifiedData,
    meta: finalMeta,
    plan: planData?.plan || null,
    tasks: normalizedTasks,
    occurrences,
    progress: progressMap,
    sessions: sessions || [],
    screenshots: screenshots || [],
    reviews: reviews || [],
    messages: messages || [],
    quizzes: quizzes || []
  });
}

/**
 * Eski client'ların tekil POST /api/sync payload'ını KV shard'larına böler ve yazar.
 */
async function handleLegacySyncPost(request, env, familyCode, headerRole) {
  const incoming = await request.json();
  const senderRole = (incoming.senderRole || headerRole || 'PARENT').toUpperCase().trim();
  const action = (incoming.action || 'SYNC').toUpperCase().trim();

  const prefix = `family:${familyCode}:`;

  // 1. Geri Alma (RESTORE)
  if (action === 'RESTORE' || action === 'UNDO_RESET') {
    const snapshot = await getKV(env, `${prefix}snapshot_prev`);
    if (!snapshot) {
      return error('Geri yüklenebilecek önceki bir durum yedeği (snapshot) bulunamadı.', 404);
    }
    await putKV(env, `${prefix}plan`, { tasks: snapshot.tasks || [], plan: snapshot.plan || null, updatedAt: Date.now() });
    if (snapshot.progress) {
      for (const [taskId, prog] of Object.entries(snapshot.progress)) {
        await putKV(env, `${prefix}progress:${taskId}`, prog);
      }
    }
    return json({ success: true, message: 'Yedek başarıyla geri yüklendi.', data: snapshot });
  }

  // 2. Tam Sıfırlama (WIPE)
  if (action === 'WIPE') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400); // 24 saat

    const list = await listKV(env, prefix);
    for (const k of list.keys) {
      if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);
    }

    const meta = { createdAt: Date.now(), resetAt: Date.now(), wipedAt: Date.now(), tombstones: [] };
    await putKV(env, `${prefix}meta`, meta);
    await putKV(env, `${prefix}plan`, { tasks: [], plan: null, updatedAt: Date.now() });
    await putKV(env, `${prefix}messages`, []);

    return json({ success: true, message: 'Sistem tamamen sıfırlandı (24 saatlik yedek alındı).' });
  }

  // 3. İlerleme Sıfırlama (RESET)
  if (action === 'RESET' || action === 'RESET_ALL_PROGRESS') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400);

    let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [] };
    meta.resetAt = Date.now();
    await putKV(env, `${prefix}meta`, meta);

    const list = await listKV(env, `${prefix}progress:`);
    for (const k of list.keys) {
      await deleteKV(env, k.name);
    }

    return json({ success: true, message: 'Öğrenci ilerlemesi sıfırlandı.' });
  }

  // 4. Tekil Ders Güncelleme (PATCH_TASK)
  if (action === 'PATCH_TASK' && (incoming.patchTask || (incoming.occurrences && incoming.occurrences.length === 1))) {
    const pt = incoming.patchTask || incoming.occurrences[0];
    const taskId = pt.id || pt.occurrenceKey;
    let planData = (await getKV(env, `${prefix}plan`)) || { tasks: [] };
    const idx = planData.tasks.findIndex(t => (t.id || t.occurrenceKey) === taskId);

    if (idx !== -1) {
      planData.tasks[idx] = {
        ...planData.tasks[idx],
        ...normalizeTask(pt),
        id: taskId,
        updatedAt: Date.now()
      };
      planData.updatedAt = Date.now();
      await putKV(env, `${prefix}plan`, planData);
      return json({ success: true, message: `Ders '${taskId}' güncellendi.` });
    }
  }

  // 5. Standart Sync Gövdesi İşleme
  const isAdminOrParent = ['PARENT', 'ADMIN', 'CLI', 'PARENTING_AI'].includes(senderRole);
  let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [], resetAt: 0 };
  let planData = (await getKV(env, `${prefix}plan`)) || { tasks: [], plan: null };

  if (isAdminOrParent && (incoming.plan || Array.isArray(incoming.tasks) || Array.isArray(incoming.occurrences))) {
    const incomingTasks = (incoming.tasks || incoming.occurrences || []).map(normalizeTask).filter(Boolean);
    if (incomingTasks.length > 0) {
      const incomingIds = new Set(incomingTasks.map(t => t.id));
      const oldIds = (planData.tasks || []).map(t => t.id || t.occurrenceKey);

      for (const oldId of oldIds) {
        if (!incomingIds.has(oldId) && !meta.tombstones.includes(oldId)) {
          meta.tombstones.push(oldId);
        }
      }

      planData.tasks = incomingTasks;
      planData.updatedAt = Date.now();
      planData.source = senderRole;
    }
    if (incoming.plan !== undefined) planData.plan = incoming.plan;
    if (incoming.planSource) meta.planSource = incoming.planSource;

    await putKV(env, `${prefix}plan`, planData);
    await putKV(env, `${prefix}meta`, meta);
  }

  // Öğrenci veya Veli İlerleme Kayıtlarını Shard'lara Yaz
  if (Array.isArray(incoming.occurrences)) {
    for (const occ of incoming.occurrences) {
      const taskId = occ.id || occ.occurrenceKey;
      if (!taskId) continue;

      const progKey = `${prefix}progress:${taskId}`;
      let prog = (await getKV(env, progKey)) || { taskId, completedMin: 0, completedQuestions: 0, status: 'PENDING' };

      if (senderRole === 'CHILD') {
        prog.completedMin = Math.max(prog.completedMin || 0, Number(occ.completedDurationMin ?? occ.completedMin ?? 0));
        prog.completedQuestions = Math.max(prog.completedQuestions || 0, Number(occ.completedQuestionCount ?? occ.completedQuestions ?? 0));
        if (occ.studentNote !== undefined) prog.studentNote = occ.studentNote;
        if (occ.status === 'WAITING_REVIEW') prog.status = 'WAITING_REVIEW';
      } else if (isAdminOrParent) {
        if (occ.status === 'APPROVED' || occ.status === 'REJECTED') {
          prog.status = occ.status;
        }
        if (occ.warningText !== undefined || occ.parentNote !== undefined) {
          prog.parentNote = occ.warningText || occ.parentNote;
        }
      }

      prog.updatedAt = Date.now();
      await putKV(env, progKey, prog);
    }
  }

  // Sessions, Screenshots, Reviews, Messages Eklemeleri
  if (Array.isArray(incoming.sessions) && incoming.sessions.length > 0) {
    let list = (await getKV(env, `${prefix}sessions`)) || [];
    const valid = incoming.sessions.map(normalizeSession).filter(Boolean);
    const existingIds = new Set(list.map(s => s.id));
    for (const v of valid) {
      if (!existingIds.has(v.id)) list.push(v);
    }
    if (list.length > 100) list = list.slice(-100);
    await putKV(env, `${prefix}sessions`, list);
  }

  if (Array.isArray(incoming.screenshots) && incoming.screenshots.length > 0) {
    let list = (await getKV(env, `${prefix}screenshots`)) || [];
    const valid = incoming.screenshots.map(normalizeScreenshot).filter(Boolean);
    const existingIds = new Set(list.map(s => s.id));
    for (const v of valid) {
      if (!existingIds.has(v.id)) list.push(v);
    }
    if (list.length > 50) list = list.slice(-50);
    await putKV(env, `${prefix}screenshots`, list);
  }

  if (Array.isArray(incoming.messages) && incoming.messages.length > 0) {
    let list = (await getKV(env, `${prefix}messages`)) || [];
    const valid = incoming.messages.map(normalizeMessage).filter(Boolean);
    const existingIds = new Set(list.map(m => m.id));
    for (const v of valid) {
      if (!existingIds.has(v.id)) list.push(v);
    }
    if (list.length > 50) list = list.slice(-50);
    await putKV(env, `${prefix}messages`, list);
  }

  return await handleSync(env, familyCode);
}

async function handlePlanAndTasks(request, env, familyCode, path, role) {
  const method = request.method;
  const prefix = `family:${familyCode}:`;

  if (method === 'GET') {
    const planData = (await getKV(env, `${prefix}plan`)) || { tasks: [], plan: null };
    return json({ success: true, plan: planData.plan, tasks: planData.tasks });
  }

  if (!['PARENT', 'ADMIN', 'CLI', 'PARENTING_AI'].includes(role)) {
    return error('Yetki Hatası: Sadece Veli/Admin plan ve görevleri değiştirebilir.', 403);
  }

  let planData = (await getKV(env, `${prefix}plan`)) || { tasks: [], plan: null };
  let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [] };

  if (method === 'POST' || method === 'PUT') {
    const body = await request.json();
    if (body.tasks && Array.isArray(body.tasks)) {
      const incomingTasks = body.tasks.map(normalizeTask).filter(Boolean);
      const incomingIds = new Set(incomingTasks.map(t => t.id));
      const oldIds = (planData.tasks || []).map(t => t.id);

      for (const oldId of oldIds) {
        if (!incomingIds.has(oldId) && !meta.tombstones.includes(oldId)) {
          meta.tombstones.push(oldId);
        }
      }

      planData.tasks = incomingTasks;
      planData.updatedAt = Date.now();
      planData.source = role;
    }
    if (body.plan !== undefined) planData.plan = body.plan;

    await putKV(env, `${prefix}plan`, planData);
    await putKV(env, `${prefix}meta`, meta);
    return json({ success: true, message: 'Plan başarıyla güncellendi.', tasksCount: planData.tasks.length });
  }

  if (method === 'PATCH' && (path.includes('/tasks/') || path.includes('/occurrences/'))) {
    const taskId = path.split('/').pop();
    const body = await request.json();
    const idx = planData.tasks.findIndex(t => t.id === taskId);
    if (idx !== -1) {
      planData.tasks[idx] = { ...planData.tasks[idx], ...normalizeTask(body), id: taskId, updatedAt: Date.now() };
      planData.updatedAt = Date.now();
      await putKV(env, `${prefix}plan`, planData);
      return json({ success: true, message: `Görev '${taskId}' güncellendi.`, task: planData.tasks[idx] });
    }
    return error('Görev bulunamadı', 404);
  }

  if (method === 'DELETE' && (path.includes('/tasks/') || path.includes('/occurrences/'))) {
    const taskId = path.split('/').pop();
    planData.tasks = planData.tasks.filter(t => t.id !== taskId);
    if (!meta.tombstones.includes(taskId)) meta.tombstones.push(taskId);

    await putKV(env, `${prefix}plan`, planData);
    await putKV(env, `${prefix}meta`, meta);
    return json({ success: true, message: `Görev '${taskId}' silindi.` });
  }

  return error('Method not allowed', 405);
}

async function handleProgress(request, env, familyCode, path, role) {
  const method = request.method;
  const taskId = path.split('/').pop();
  const prefix = `family:${familyCode}:`;

  if (!taskId || taskId === 'progress') {
    return error('taskId gereklidir (/api/v2/progress/:taskId)', 400);
  }

  const progKey = `${prefix}progress:${taskId}`;

  if (method === 'GET') {
    const prog = await getKV(env, progKey);
    return json({ success: true, progress: prog });
  }

  if (method === 'POST' || method === 'PATCH') {
    const body = await request.json();
    let prog = (await getKV(env, progKey)) || { taskId, completedMin: 0, completedQuestions: 0, status: 'PENDING', history: [] };

    // Veli daha önce reddettiyse veya onayladıysa, öğrenci yeniden çalışmaya başlayınca IN_PROGRESS yap
    if (prog.status === 'REJECTED' || prog.status === 'APPROVED') {
      prog.status = 'IN_PROGRESS';
      prog.rejectionReason = null;
    }

    prog.completedMin = Math.max(prog.completedMin || 0, Number(body.completedMin ?? body.completedDurationMin ?? 0));
    prog.completedQuestions = Math.max(prog.completedQuestions || 0, Number(body.completedQuestions ?? body.completedQuestionCount ?? 0));
    if (body.studentNote !== undefined) prog.studentNote = body.studentNote;
    if (body.status === 'WAITING_REVIEW') prog.status = 'WAITING_REVIEW';

    prog.updatedAt = Date.now();
    await putKV(env, progKey, prog);

    return json({ success: true, progress: prog });
  }

  return error('Method not allowed', 405);
}

async function handleCommands(request, env, familyCode, role, path) {
  if (request.method !== 'POST') return error('Method not allowed', 405);
  if (!['PARENT', 'ADMIN', 'CLI'].includes(role)) {
    return error('Yetki Hatası: Sadece Veli/Admin komut verebilir.', 403);
  }

  const body = await request.json().catch(() => ({}));
  let action = body.action;
  if (!action && path.endsWith('/reset')) action = 'RESET_ALL_PROGRESS';
  if (!action && path.endsWith('/wipe')) action = 'WIPE';

  const prefix = `family:${familyCode}:`;
  let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [], resetAt: 0 };

  // 1. TEKİL GÖREV İADE / SIFIRLAMA (Parent Reject / Reset Task)
  if (action === 'REJECT_TASK' || action === 'RESET_TASK') {
    const taskId = body.taskId || body.occurrenceKey;
    if (!taskId) return error('taskId zorunludur.', 400);

    const progKey = `${prefix}progress:${taskId}`;
    let prog = (await getKV(env, progKey)) || { taskId, history: [] };

    prog.completedMin = 0;
    prog.completedQuestions = 0;
    prog.studentNote = '';
    prog.status = 'REJECTED';
    prog.rejectionReason = body.reason || 'Veli tarafından iade edildi.';
    prog.updatedAt = Date.now();

    prog.history = prog.history || [];
    prog.history.push({ action: 'PARENT_RESET', timestamp: Date.now(), note: body.reason });

    await putKV(env, progKey, prog);
    return json({ success: true, message: `Görev '${taskId}' iade edildi ve sıfırlandı.` });
  }

  // 2. TÜM İLERLEMEYİ SIFIRLAMA (Global Reset Progress)
  if (action === 'RESET_ALL_PROGRESS' || action === 'RESET') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400);

    meta.resetAt = Date.now();
    await putKV(env, `${prefix}meta`, meta);

    const list = await listKV(env, `${prefix}progress:`);
    for (const k of list.keys) {
      await deleteKV(env, k.name);
    }
    return json({ success: true, message: 'Tüm öğrenci ilerlemesi sıfırlandı (24 saatlik geri alma yedeği alındı).' });
  }

  // 3. TAMAMEN SIFIRLAMA (WIPE - 24 Saat Yedekli)
  if (action === 'WIPE') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400);

    const list = await listKV(env, prefix);
    for (const k of list.keys) {
      if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);
    }

    meta = { createdAt: Date.now(), resetAt: Date.now(), wipedAt: Date.now(), tombstones: [] };
    await putKV(env, `${prefix}meta`, meta);
    await putKV(env, `${prefix}plan`, { tasks: [], plan: null, updatedAt: Date.now() });
    await putKV(env, `${prefix}messages`, []);

    return json({ success: true, message: 'Sistem tamamen sıfırlandı (24 saatlik yedek alındı).' });
  }

  // 4. YEDEĞİ GERİ YÜKLEME (RESTORE)
  if (action === 'RESTORE' || action === 'UNDO_RESET') {
    const snapshot = await getKV(env, `${prefix}snapshot_prev`);
    if (!snapshot) return error('Geri yüklenecek yedek bulunamadı.', 404);

    const list = await listKV(env, prefix);
    for (const k of list.keys) {
      if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);
    }

    await putKV(env, `${prefix}plan`, { tasks: snapshot.tasks || [], plan: snapshot.plan || null, updatedAt: Date.now() });
    if (snapshot.progress) {
      for (const [taskId, prog] of Object.entries(snapshot.progress)) {
        await putKV(env, `${prefix}progress:${taskId}`, prog);
      }
    }
    return json({ success: true, message: 'Yedek başarıyla geri yüklendi.', data: snapshot });
  }

  return error('Geçersiz komut (action).', 400);
}

async function handleReview(request, env, familyCode, role) {
  if (!['PARENT', 'ADMIN', 'CLI'].includes(role)) {
    return error('Yetki Hatası: Sadece Veli inceleme/onay verebilir.', 403);
  }

  const body = await request.json();
  const taskId = body.taskId || body.occurrenceKey;
  if (!taskId) return error('taskId veya occurrenceKey zorunludur.', 400);

  const prefix = `family:${familyCode}:`;
  const progKey = `${prefix}progress:${taskId}`;
  let prog = (await getKV(env, progKey)) || { taskId, history: [] };

  prog.status = body.isApproved ? 'APPROVED' : 'REJECTED';
  prog.parentRating = Number(body.parentRating || 5);
  prog.feedbackNote = body.feedbackNote || body.reviewNote || '';
  prog.reviewedAt = Date.now();

  prog.history = prog.history || [];
  prog.history.push({
    action: prog.status,
    timestamp: Date.now(),
    feedbackNote: prog.feedbackNote,
    rating: prog.parentRating
  });

  await putKV(env, progKey, prog);

  // Review listesine de ekle
  let reviews = (await getKV(env, `${prefix}reviews`)) || [];
  const newRev = normalizeReview({ ...body, familyCode, occurrenceKey: taskId });
  reviews.push(newRev);
  if (reviews.length > 100) reviews = reviews.slice(-100);
  await putKV(env, `${prefix}reviews`, reviews);

  return json({ success: true, message: `Review kaydedildi (${prog.status}).`, progress: prog });
}

async function handleAppendLog(request, env, familyCode, type, limit, normalizer) {
  const body = await request.json();
  const prefix = `family:${familyCode}:`;
  const key = `${prefix}${type}`;
  let logs = (await getKV(env, key)) || [];

  const items = Array.isArray(body) ? body : [body];
  for (const raw of items) {
    const item = normalizer({ ...raw, familyCode });
    if (item) logs.push(item);
  }

  if (logs.length > limit) logs = logs.slice(-limit);
  await putKV(env, key, logs);

  return json({ success: true, addedCount: items.length, total: logs.length });
}

async function handleMessages(request, env, familyCode, role, url) {
  const method = request.method;
  const key = `family:${familyCode}:messages`;
  let messages = (await getKV(env, key)) || [];

  if (method === 'GET') {
    const unreadOnly = url.searchParams.get('unread') === 'true';
    const since = Number(url.searchParams.get('since') || 0);

    let filtered = messages;
    if (unreadOnly) {
      filtered = filtered.filter(m => !m.isRead);
    }
    if (since > 0) {
      filtered = filtered.filter(m => m.timestamp > since);
    }

    return json({ success: true, messages: filtered, total: messages.length });
  }

  if (method === 'POST') {
    if (!['PARENT', 'ADMIN', 'CLI', 'SYSTEM'].includes(role)) {
      return error('Yetki Hatası: Sadece Veli veya Sistem bildirim gönderebilir.', 403);
    }

    const body = await request.json();
    const newMsg = normalizeMessage({ ...body, familyCode, senderRole: role });

    messages.push(newMsg);
    if (messages.length > 50) messages = messages.slice(-50);
    await putKV(env, key, messages);

    return json({ success: true, message: 'Bildirim iletildi.', data: newMsg });
  }

  if (method === 'PUT') {
    const body = await request.json();
    if (body.messageId || body.id) {
      const targetId = body.messageId || body.id;
      const msg = messages.find(m => m.id === targetId);
      if (msg) msg.isRead = true;
    } else if (body.all) {
      messages.forEach(m => { m.isRead = true; });
    }

    await putKV(env, key, messages);
    return json({ success: true, message: 'Mesaj(lar) okundu olarak işaretlendi.' });
  }

  return error('Method not allowed', 405);
}
