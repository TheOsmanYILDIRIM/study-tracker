/**
 * StudyTracker Sync Engine v2.0 - Segregated & Granular Architecture
 * Zero-dependency Cloudflare Worker (Multi-tenant, Role-Aware, KV Sharded)
 * Mimari: Veri Ayrıştırma (Data Segregation), Komut Deseni (Command Pattern) ve RBAC.
 */

const CORS_HEADERS = {
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Family-Code, X-Sender-Role, X-Admin-Token',
  'Content-Type': 'application/json; charset=utf-8'
};

// --- YARDIMCI FONKSİYONLAR & KV YÖNETİMİ ---
const json = (data, status = 200) => new Response(JSON.stringify(data), { status, headers: CORS_HEADERS });
const error = (msg, status = 400) => json({ success: false, error: msg }, status);

// Local test (wrangler / node test-sync) için geçici bellek hafızası
const inMemoryStore = new Map();

function isLocalTest(env) {
  return env && env.__LOCAL_TEST__ === true;
}

function requireStorage(env) {
  if (!env?.STUDY_SYNC_KV && !isLocalTest(env)) {
    throw new Error('STUDY_SYNC_KV binding is required');
  }
}

async function getKV(env, key) {
  requireStorage(env);
  if (env?.STUDY_SYNC_KV) {
    const raw = await env.STUDY_SYNC_KV.get(key);
    return raw ? JSON.parse(raw) : null;
  }
  return inMemoryStore.get(key) || null;
}

async function putKV(env, key, val, ttlSeconds = null) {
  requireStorage(env);
  if (env?.STUDY_SYNC_KV) {
    const opts = ttlSeconds ? { expirationTtl: ttlSeconds } : {};
    await env.STUDY_SYNC_KV.put(key, JSON.stringify(val), opts);
  } else {
    inMemoryStore.set(key, val);
  }
}

async function deleteKV(env, key) {
  requireStorage(env);
  if (env?.STUDY_SYNC_KV) {
    await env.STUDY_SYNC_KV.delete(key);
  } else {
    inMemoryStore.delete(key);
  }
}

async function listKV(env, prefix) {
  requireStorage(env);
  if (env?.STUDY_SYNC_KV) return await env.STUDY_SYNC_KV.list({ prefix });
  const keys = [];
  for (const k of inMemoryStore.keys()) if (k.startsWith(prefix)) keys.push({ name: k });
  return { keys };
}

function randomToken(bytes = 24) {
  const data = new Uint8Array(bytes);
  crypto.getRandomValues(data);
  return Array.from(data, b => b.toString(16).padStart(2, '0')).join('');
}

function generateFamilyCode() {
  const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  const data = new Uint8Array(16);
  crypto.getRandomValues(data);
  let body = '';
  for (const b of data) body += alphabet[b % alphabet.length];
  return 'ST-' + body.match(/.{1,4}/g).join('-');
}

function isValidFamilyCode(code) {
  return /^ST-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$/.test(code);
}

function isParentRole(role) {
  return ['PARENT', 'ADMIN', 'CLI', 'PARENTING_AI'].includes(role);
}

async function hasAdminAuth(request, env, familyCode) {
  const meta = await getKV(env, 'family:' + familyCode + ':meta');
  const expected = meta?.adminToken;
  const provided = request.headers.get('X-Admin-Token') || '';
  return Boolean(expected && provided && expected === provided);
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
    approvedCount: Number(t.approvedCount ?? t.approved_count ?? 0),
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
    activeDurationSeconds: Number(s.activeDurationSeconds ?? s.active_duration_seconds ?? ((s.durationMin ?? s.duration_min ?? 0) * 60)),
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
    sessionId: r.sessionId || r.session_id || r.taskId || r.occurrenceKey || '',
    occurrenceKey: r.occurrenceKey || r.occurrenceId || r.taskId || r.sessionId || '',
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
    const familyCode = (url.searchParams.get('code') || request.headers.get('X-Family-Code') || '').toUpperCase().trim();
    const role = (request.headers.get('X-Sender-Role') || 'CLIENT').toUpperCase().trim();

    // 1. Health / Ping Check
    if (path === '/' || path === '/api/ping' || path === '/api/health') {
      return json({
        status: 'ok',
        version: '3.0-hardened',
        engine: 'StudyTracker Multi-tenant Sharded Sync Engine',
        storageConfigured: Boolean(env?.STUDY_SYNC_KV) || isLocalTest(env),
        revision: env?.BUILD_REVISION || 'unknown',
        timestamp: Date.now()
      });
    }

    // 2. Family Pairing
    if (path === '/api/pair' && method === 'POST') {
      return await handlePair(request, env, familyCode);
    }

    if (!familyCode || !isValidFamilyCode(familyCode)) {
      return error('Geçerli X-Family-Code / ?code zorunludur.', 400);
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
  let code = (providedCode || body.familyCode || '').toUpperCase().trim();
  if (!code) code = generateFamilyCode();
  if (!isValidFamilyCode(code)) return error('Geçersiz aile kodu formatı.', 400);

  const prefix = 'family:' + code + ':';
  let meta = await getKV(env, prefix + 'meta');
  let created = false;
  let issuedToken = null;

  if (!meta) {
    issuedToken = randomToken();
    meta = { createdAt: Date.now(), updatedAt: Date.now(), resetAt: 0, wipedAt: 0, tombstones: [], revision: 1, adminToken: issuedToken };
    await putKV(env, prefix + 'meta', meta);
    await putKV(env, prefix + 'plan', { templates: [], tasks: [], plan: null, updatedAt: Date.now() });
    await putKV(env, prefix + 'messages', []);
    created = true;
  } else if (!meta.adminToken) {
    return error('Legacy aile kodu güvenli yönetici anahtarına sahip değil. Veli uygulamasından yeni güvenli aile kodu oluşturun.', 409);
  } else {
    const providedToken = request.headers.get('X-Admin-Token') || body.adminToken || '';
    if (providedToken && providedToken === meta.adminToken) issuedToken = meta.adminToken;
  }

  return json({ success: true, familyCode: code, adminToken: issuedToken, created });
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
  const templates = Array.isArray(planData?.templates) ? planData.templates : [];
  const tombstones = new Set(meta?.tombstones || []);
  const normalizedTasks = rawTasks
    .map(normalizeTask)
    .filter(Boolean)
    .filter(t => !tombstones.has(t.id) && !tombstones.has(t.occurrenceKey));

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

  const rawMeta = meta || { resetAt: 0, wipedAt: 0, tombstones: [], planSource: 'Cloud Master' };
  const { adminToken: _privateAdminToken, ...finalMeta } = rawMeta;

  const unifiedData = {
    familyCode,
    updatedAt: planData?.updatedAt || Date.now(),
    planSource: finalMeta.planSource || planData?.source || 'Veli / Bulut Masası',
    plan: planData?.plan || null,
    tasks: templates,
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
    tasks: templates,
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

  const privilegedAction = ['RESTORE', 'UNDO_RESET', 'WIPE', 'RESET', 'RESET_ALL_PROGRESS', 'PATCH_TASK', 'DELETE_TASK'].includes(action);
  if ((isParentRole(senderRole) || privilegedAction) && !(await hasAdminAuth(request, env, familyCode))) {
    return error('Veli/Admin yetkisi için geçerli X-Admin-Token zorunludur.', 401);
  }
  if (senderRole === 'CLIENT' && action === 'SYNC') {
    return await handleSync(env, familyCode);
  }

  // 1-3. RESTORE / WIPE / RESET use complete snapshots.
  if (action === 'RESTORE' || action === 'UNDO_RESET') {
    const snapshot = await getKV(env, `${prefix}snapshot_prev`);
    if (!snapshot) return error('Geri yüklenecek önceki durum yedeği bulunamadı.', 404);
    const currentMeta = (await getKV(env, `${prefix}meta`)) || {};
    const list = await listKV(env, prefix);
    for (const k of list.keys) if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);

    const restoredMeta = { ...(snapshot.meta || {}), adminToken: currentMeta.adminToken, updatedAt: Date.now() };
    await putKV(env, `${prefix}meta`, restoredMeta);
    await putKV(env, `${prefix}plan`, {
      templates: snapshot.tasks || [],
      tasks: snapshot.occurrences || [],
      plan: snapshot.plan || null,
      source: snapshot.planSource || 'RESTORE',
      updatedAt: Date.now()
    });
    for (const [taskId, prog] of Object.entries(snapshot.progress || {})) await putKV(env, `${prefix}progress:${taskId}`, prog);
    await putKV(env, `${prefix}sessions`, snapshot.sessions || []);
    await putKV(env, `${prefix}screenshots`, snapshot.screenshots || []);
    await putKV(env, `${prefix}reviews`, snapshot.reviews || []);
    await putKV(env, `${prefix}messages`, snapshot.messages || []);
    await putKV(env, `${prefix}quizzes`, snapshot.quizzes || []);
    return await handleSync(env, familyCode);
  }

  if (action === 'WIPE') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400);
    const currentMeta = (await getKV(env, `${prefix}meta`)) || {};
    const list = await listKV(env, prefix);
    for (const k of list.keys) if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);

    const meta = { createdAt: Date.now(), updatedAt: Date.now(), resetAt: Date.now(), wipedAt: Date.now(), tombstones: [], revision: 1, adminToken: currentMeta.adminToken };
    await putKV(env, `${prefix}meta`, meta);
    await putKV(env, `${prefix}plan`, { templates: [], tasks: [], plan: null, updatedAt: Date.now() });
    await putKV(env, `${prefix}messages`, []);
    return await handleSync(env, familyCode);
  }

  if (action === 'RESET' || action === 'RESET_ALL_PROGRESS') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400);

    let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [] };
    meta.resetAt = Date.now();
    meta.updatedAt = Date.now();
    await putKV(env, `${prefix}meta`, meta);

    const list = await listKV(env, `${prefix}progress:`);
    for (const k of list.keys) await deleteKV(env, k.name);
    await putKV(env, `${prefix}sessions`, []);
    await putKV(env, `${prefix}screenshots`, []);
    await putKV(env, `${prefix}reviews`, []);
    const quizzes = (await getKV(env, `${prefix}quizzes`)) || [];
    await putKV(env, `${prefix}quizzes`, quizzes.map(q => ({
      ...q, completed: false, submittedAt: null, studentAnswers: {}, studentDurationSeconds: 0,
      correctCount: 0, wrongCount: 0, emptyCount: 0, studentNote: null
    })));
    return await handleSync(env, familyCode);
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
    return error('Görev bulunamadı; tam plan senkronuna düşülmedi.', 404);
  }

  // 4.5. Tekil Ders Silme (DELETE_TASK)
  if (action === 'DELETE_TASK' || incoming.deleteTaskId) {
    const targetId = incoming.deleteTaskId || incoming.taskId || (incoming.occurrences && incoming.occurrences[0]?.id);
    if (targetId) {
      let planData = (await getKV(env, `${prefix}plan`)) || { tasks: [] };
      planData.tasks = (planData.tasks || []).filter(t => (t.id || t.occurrenceKey) !== targetId && t.planId !== targetId && (t.id || '').replace(/^.*_/, '') !== targetId);
      planData.updatedAt = Date.now();
      await putKV(env, `${prefix}plan`, planData);

      let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [] };
      if (!meta.tombstones) meta.tombstones = [];
      if (!meta.tombstones.includes(targetId)) meta.tombstones.push(targetId);
      await putKV(env, `${prefix}meta`, meta);

      await deleteKV(env, `${prefix}progress:${targetId}`);
      return await handleSync(env, familyCode);
    }
  }

  // 5. Standart Sync Gövdesi İşleme
  const isAdminOrParent = isParentRole(senderRole);
  let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [], resetAt: 0, revision: 0 };
  let planData = (await getKV(env, `${prefix}plan`)) || { templates: [], tasks: [], plan: null };

  if (isAdminOrParent && (incoming.plan || Array.isArray(incoming.tasks) || Array.isArray(incoming.occurrences))) {
    if (Array.isArray(incoming.tasks)) {
      planData.templates = incoming.tasks.filter(t => t && t.taskId);
    }

    if (Array.isArray(incoming.occurrences) && incoming.occurrences.length > 0) {
      const incomingTasks = incoming.occurrences.map(normalizeTask).filter(Boolean);
      const incomingIds = new Set(incomingTasks.map(t => t.id));
      const oldIds = (planData.tasks || []).map(t => t.id || t.occurrenceKey);

      for (const oldId of oldIds) {
        if (!incomingIds.has(oldId) && !meta.tombstones.includes(oldId)) meta.tombstones.push(oldId);
      }
      for (const incId of incomingIds) {
        meta.tombstones = (meta.tombstones || []).filter(id => id !== incId);
      }
      planData.tasks = incomingTasks;
    }

    if (incoming.plan !== undefined) planData.plan = incoming.plan;
    if (incoming.planSource) meta.planSource = incoming.planSource;
    planData.updatedAt = Date.now();
    planData.source = senderRole;
    meta.revision = Number(meta.revision || 0) + 1;
    meta.updatedAt = Date.now();

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
        // Parent occurrence snapshots can be stale. Status authority comes from reviews/commands,
        // not from a bulk plan snapshot.
        if (occ.completedDurationMin !== undefined || occ.completedMin !== undefined) {
          prog.completedMin = Math.max(prog.completedMin || 0, Number(occ.completedDurationMin ?? occ.completedMin ?? 0));
        }
        if (occ.completedQuestionCount !== undefined || occ.completedQuestions !== undefined) {
          prog.completedQuestions = Math.max(prog.completedQuestions || 0, Number(occ.completedQuestionCount ?? occ.completedQuestions ?? 0));
        }
        if (occ.warningText !== undefined || occ.parentNote !== undefined) {
          prog.parentNote = occ.warningText || occ.parentNote;
        }
        if (occ.studentNote !== undefined) {
          prog.studentNote = occ.studentNote;
        }
      }

      prog.updatedAt = Date.now();
      await putKV(env, progKey, prog);
    }
  }

  // Sessions: true upsert so ACTIVE -> completed updates are not lost.
  if (Array.isArray(incoming.sessions) && incoming.sessions.length > 0) {
    let list = (await getKV(env, `${prefix}sessions`)) || [];
    const byId = new Map(list.map(s => [s.id, s]));
    for (const raw of incoming.sessions) {
      const v = normalizeSession(raw);
      if (!v) continue;
      const old = byId.get(v.id);
      if (!old || Number(v.updatedAt || 0) >= Number(old.updatedAt || 0) || (v.isCompleted && !old.isCompleted)) byId.set(v.id, { ...old, ...v });
    }
    list = Array.from(byId.values()).sort((a, b) => Number(a.updatedAt || 0) - Number(b.updatedAt || 0)).slice(-200);
    await putKV(env, `${prefix}sessions`, list);
  }

  if (Array.isArray(incoming.screenshots) && incoming.screenshots.length > 0) {
    let list = (await getKV(env, `${prefix}screenshots`)) || [];
    const byId = new Map(list.map(s => [s.id, s]));
    for (const raw of incoming.screenshots) {
      const v = normalizeScreenshot(raw);
      if (v) byId.set(v.id, { ...(byId.get(v.id) || {}), ...v });
    }
    list = Array.from(byId.values()).sort((a, b) => Number(a.timestamp || 0) - Number(b.timestamp || 0)).slice(-100);
    await putKV(env, `${prefix}screenshots`, list);
  }

  if (Array.isArray(incoming.reviews) && incoming.reviews.length > 0) {
    let list = (await getKV(env, `${prefix}reviews`)) || [];
    const byId = new Map(list.map(r => [r.id || r.sessionId, r]));
    const sessionMap = new Map((incoming.sessions || []).map(s => [s.id || s.sessionId, s.occurrenceId || s.occurrenceKey]));
    for (const raw of incoming.reviews) {
      const v = normalizeReview(raw);
      if (!v) continue;
      const key = v.id || v.sessionId;
      const old = byId.get(key);
      if (!old || Number(v.reviewedAt || 0) >= Number(old.reviewedAt || 0)) {
        byId.set(key, v);
        const targetId = v.occurrenceKey || sessionMap.get(v.sessionId) || v.sessionId;
        if (targetId) {
          const progKey = `${prefix}progress:${targetId}`;
          const prog = (await getKV(env, progKey)) || { taskId: targetId, completedMin: 0, completedQuestions: 0, history: [] };
          prog.status = v.isApproved ? 'APPROVED' : 'REJECTED';
          prog.feedbackNote = v.feedbackNote || v.rejectionReason || '';
          prog.rejectionReason = v.isApproved ? null : (v.rejectionReason || v.feedbackNote || '');
          prog.parentRating = v.parentRating;
          prog.reviewedAt = v.reviewedAt;
          prog.updatedAt = Date.now();
          await putKV(env, progKey, prog);
        }
      }
    }
    list = Array.from(byId.values()).sort((a, b) => Number(a.reviewedAt || 0) - Number(b.reviewedAt || 0)).slice(-200);
    await putKV(env, `${prefix}reviews`, list);
  }

  if (Array.isArray(incoming.quizzes) && incoming.quizzes.length > 0) {
    let list = (await getKV(env, `${prefix}quizzes`)) || [];
    const byId = new Map(list.map(q => [q.quizId, q]));
    for (const q of incoming.quizzes) {
      if (!q?.quizId) continue;
      const old = byId.get(q.quizId);
      if (senderRole === 'CHILD' && old) {
        byId.set(q.quizId, {
          ...old,
          completed: Boolean(q.completed),
          submittedAt: q.submittedAt ?? old.submittedAt,
          studentAnswers: q.studentAnswers || old.studentAnswers || {},
          studentDurationSeconds: Number(q.studentDurationSeconds || 0),
          correctCount: Number(q.correctCount || 0),
          wrongCount: Number(q.wrongCount || 0),
          emptyCount: Number(q.emptyCount || 0),
          studentNote: q.studentNote ?? old.studentNote ?? null
        });
      } else if (old?.completed && !q.completed) {
        byId.set(q.quizId, old);
      } else {
        byId.set(q.quizId, { ...(old || {}), ...q });
      }
    }
    await putKV(env, `${prefix}quizzes`, Array.from(byId.values()).slice(-100));
  }

  if (Array.isArray(incoming.messages) && incoming.messages.length > 0) {
    let list = (await getKV(env, `${prefix}messages`)) || [];
    const byId = new Map(list.map(m => [m.id, m]));
    for (const raw of incoming.messages) {
      const v = normalizeMessage(raw);
      if (v) byId.set(v.id, { ...(byId.get(v.id) || {}), ...v });
    }
    list = Array.from(byId.values()).sort((a, b) => Number(a.timestamp || 0) - Number(b.timestamp || 0)).slice(-100);
    await putKV(env, `${prefix}messages`, list);
  }
  return await handleSync(env, familyCode);
}

async function handlePlanAndTasks(request, env, familyCode, path, role) {
  const method = request.method;
  const prefix = `family:${familyCode}:`;
  let planData = (await getKV(env, `${prefix}plan`)) || { templates: [], tasks: [], plan: null };
  let meta = (await getKV(env, `${prefix}meta`)) || { tombstones: [], revision: 0 };

  if (method === 'GET') {
    const tombstones = new Set(meta.tombstones || []);
    const occurrences = (planData.tasks || []).map(normalizeTask).filter(Boolean).filter(t => !tombstones.has(t.id));
    return json({ success: true, plan: planData.plan, tasks: planData.templates || [], occurrences });
  }

  if (!isParentRole(role)) return error('Yetki Hatası: Sadece Veli/Admin plan ve görevleri değiştirebilir.', 403);
  if (!(await hasAdminAuth(request, env, familyCode))) return error('Geçerli X-Admin-Token zorunludur.', 401);

  if (method === 'POST' || method === 'PUT') {
    const body = await request.json();
    if (Array.isArray(body.tasks)) planData.templates = body.tasks.filter(t => t && t.taskId);
    if (Array.isArray(body.occurrences) && body.occurrences.length > 0) {
      const incoming = body.occurrences.map(normalizeTask).filter(Boolean);
      const incomingIds = new Set(incoming.map(t => t.id));
      for (const old of (planData.tasks || [])) {
        const oldId = old.id || old.occurrenceKey;
        if (oldId && !incomingIds.has(oldId) && !meta.tombstones.includes(oldId)) meta.tombstones.push(oldId);
      }
      for (const id of incomingIds) meta.tombstones = meta.tombstones.filter(x => x !== id);
      planData.tasks = incoming;
    }
    if (body.plan !== undefined) planData.plan = body.plan;
    planData.updatedAt = Date.now();
    planData.source = role;
    meta.revision = Number(meta.revision || 0) + 1;
    meta.updatedAt = Date.now();
    await putKV(env, `${prefix}plan`, planData);
    await putKV(env, `${prefix}meta`, meta);
    return await handleSync(env, familyCode);
  }

  if (method === 'PATCH' && (path.includes('/tasks/') || path.includes('/occurrences/'))) {
    const taskId = path.split('/').pop();
    const body = await request.json();
    const idx = (planData.tasks || []).findIndex(t => (t.id || t.occurrenceKey) === taskId);
    if (idx === -1) return error('Görev bulunamadı', 404);
    planData.tasks[idx] = { ...planData.tasks[idx], ...normalizeTask(body), id: taskId, updatedAt: Date.now() };
    planData.updatedAt = Date.now();
    await putKV(env, `${prefix}plan`, planData);
    return await handleSync(env, familyCode);
  }

  if (method === 'DELETE' && (path.includes('/tasks/') || path.includes('/occurrences/'))) {
    const taskId = path.split('/').pop();
    const removed = (planData.tasks || []).find(t => (t.id || t.occurrenceKey) === taskId);
    planData.tasks = (planData.tasks || []).filter(t => (t.id || t.occurrenceKey) !== taskId);
    if (!meta.tombstones.includes(taskId)) meta.tombstones.push(taskId);
    if (removed?.planId && !(planData.tasks || []).some(t => t.planId === removed.planId)) {
      planData.templates = (planData.templates || []).filter(t => t.taskId !== removed.planId);
    }
    await deleteKV(env, `${prefix}progress:${taskId}`);
    await putKV(env, `${prefix}plan`, planData);
    await putKV(env, `${prefix}meta`, meta);
    return await handleSync(env, familyCode);
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
  if (!(await hasAdminAuth(request, env, familyCode))) {
    return error('Geçerli X-Admin-Token zorunludur.', 401);
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
    meta.updatedAt = Date.now();
    await putKV(env, `${prefix}meta`, meta);
    const list = await listKV(env, `${prefix}progress:`);
    for (const k of list.keys) await deleteKV(env, k.name);
    await putKV(env, `${prefix}sessions`, []);
    await putKV(env, `${prefix}screenshots`, []);
    await putKV(env, `${prefix}reviews`, []);
    const quizzes = (await getKV(env, `${prefix}quizzes`)) || [];
    await putKV(env, `${prefix}quizzes`, quizzes.map(q => ({
      ...q, completed: false, submittedAt: null, studentAnswers: {}, studentDurationSeconds: 0,
      correctCount: 0, wrongCount: 0, emptyCount: 0, studentNote: null
    })));
    return json({ success: true, message: 'Tüm öğrenci ilerlemesi sıfırlandı (24 saatlik geri alma yedeği alındı).' });
  }

  // 3. TAMAMEN SIFIRLAMA (WIPE - 24 Saat Yedekli)
  if (action === 'WIPE') {
    const currentSync = await (await handleSync(env, familyCode)).json();
    await putKV(env, `${prefix}snapshot_prev`, currentSync.data || currentSync, 86400);
    const currentMeta = (await getKV(env, `${prefix}meta`)) || {};
    const list = await listKV(env, prefix);
    for (const k of list.keys) if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);
    meta = { createdAt: Date.now(), updatedAt: Date.now(), resetAt: Date.now(), wipedAt: Date.now(), tombstones: [], revision: 1, adminToken: currentMeta.adminToken };
    await putKV(env, `${prefix}meta`, meta);
    await putKV(env, `${prefix}plan`, { templates: [], tasks: [], plan: null, updatedAt: Date.now() });
    await putKV(env, `${prefix}messages`, []);
    return json({ success: true, message: 'Sistem tamamen sıfırlandı (24 saatlik yedek alındı).' });
  }

  // 4. YEDEĞİ GERİ YÜKLEME (RESTORE)
  if (action === 'RESTORE' || action === 'UNDO_RESET') {
    const snapshot = await getKV(env, `${prefix}snapshot_prev`);
    if (!snapshot) return error('Geri yüklenecek yedek bulunamadı.', 404);
    const currentMeta = (await getKV(env, `${prefix}meta`)) || {};
    const list = await listKV(env, prefix);
    for (const k of list.keys) if (!k.name.includes(':snapshot_prev')) await deleteKV(env, k.name);
    await putKV(env, `${prefix}meta`, { ...(snapshot.meta || {}), adminToken: currentMeta.adminToken, updatedAt: Date.now() });
    await putKV(env, `${prefix}plan`, { templates: snapshot.tasks || [], tasks: snapshot.occurrences || [], plan: snapshot.plan || null, source: snapshot.planSource || 'RESTORE', updatedAt: Date.now() });
    for (const [taskId, prog] of Object.entries(snapshot.progress || {})) await putKV(env, `${prefix}progress:${taskId}`, prog);
    await putKV(env, `${prefix}sessions`, snapshot.sessions || []);
    await putKV(env, `${prefix}screenshots`, snapshot.screenshots || []);
    await putKV(env, `${prefix}reviews`, snapshot.reviews || []);
    await putKV(env, `${prefix}messages`, snapshot.messages || []);
    await putKV(env, `${prefix}quizzes`, snapshot.quizzes || []);
    return json({ success: true, message: 'Yedek başarıyla geri yüklendi.', data: snapshot });
  }
  return error('Geçersiz komut (action).', 400);
}

async function handleReview(request, env, familyCode, role) {
  if (!['PARENT', 'ADMIN', 'CLI'].includes(role)) {
    return error('Yetki Hatası: Sadece Veli inceleme/onay verebilir.', 403);
  }
  if (!(await hasAdminAuth(request, env, familyCode))) {
    return error('Geçerli X-Admin-Token zorunludur.', 401);
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
  const byId = new Map(logs.map(item => [item.id || item.sessionId || item.screenshotId, item]));

  const items = Array.isArray(body) ? body : [body];
  for (const raw of items) {
    const item = normalizer({ ...raw, familyCode });
    if (!item) continue;
    const id = item.id || item.sessionId || item.screenshotId;
    if (id) byId.set(id, { ...(byId.get(id) || {}), ...item });
  }

  logs = Array.from(byId.values()).slice(-limit);
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
    if (role !== 'SYSTEM' && !(await hasAdminAuth(request, env, familyCode))) {
      return error('Geçerli X-Admin-Token zorunludur.', 401);
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
