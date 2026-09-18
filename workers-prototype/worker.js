/**
 * StudyTracker Cloudflare Worker - Family Cloud Sync & Pairing
 * Zero-dependency, KV-backed serverless sync engine.
 */

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Family-Code',
  'Content-Type': 'application/json; charset=utf-8'
};

// In-memory fallback if KV is not bound (for local testing)
const inMemoryStore = new Map();

async function getStoreData(env, key) {
  if (env && env.STUDY_SYNC_KV) {
    const data = await env.STUDY_SYNC_KV.get(key);
    return data ? JSON.parse(data) : null;
  }
  return inMemoryStore.get(key) || null;
}

async function setStoreData(env, key, value, ttlSeconds = 60 * 60 * 24 * 30) {
  if (env && env.STUDY_SYNC_KV) {
    await env.STUDY_SYNC_KV.put(key, JSON.stringify(value), { expirationTtl: ttlSeconds });
  } else {
    inMemoryStore.set(key, value);
  }
}

export default {
  async fetch(request, env = {}, ctx) {
    // Handle CORS preflight
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
          // Generate a clean 6-digit pair code (e.g. ST-7842)
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
          return new Response(JSON.stringify({ success: true, ...current }), { headers: CORS_HEADERS });
        }

        if (request.method === 'POST') {
          const incoming = await request.json();
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

          // Smart Reconcile logic:
          // 1. Plan (latest updatedAt wins)
          if (incoming.plan) {
            if (!current.plan || (incoming.plan.updatedAt >= (current.plan.updatedAt || ''))) {
              current.plan = incoming.plan;
            }
          }

          // 2. Tasks
          if (Array.isArray(incoming.tasks) && incoming.tasks.length > 0) {
            const taskMap = new Map((current.tasks || []).map(t => [t.taskId, t]));
            incoming.tasks.forEach(t => taskMap.set(t.taskId, t));
            current.tasks = Array.from(taskMap.values());
          }

          // 3. Occurrences (Status hierarchy & count merge)
          if (Array.isArray(incoming.occurrences) && incoming.occurrences.length > 0) {
            const occMap = new Map((current.occurrences || []).map(o => [o.id, o]));
            for (const remote of incoming.occurrences) {
              const local = occMap.get(remote.id);
              if (!local) {
                occMap.set(remote.id, remote);
              } else {
                // Merge status
                let resolvedStatus = remote.status || local.status;
                if (local.status === 'APPROVED' || remote.status === 'APPROVED') resolvedStatus = 'APPROVED';
                else if (local.status === 'WAITING_REVIEW' || remote.status === 'WAITING_REVIEW') resolvedStatus = 'WAITING_REVIEW';
                else if (local.status === 'ACTIVE' || remote.status === 'ACTIVE') resolvedStatus = 'ACTIVE';

                occMap.set(remote.id, {
                  ...local,
                  ...remote,
                  status: resolvedStatus,
                  completedQuestionCount: Math.max(local.completedQuestionCount || 0, remote.completedQuestionCount || 0),
                  completedDurationMin: Math.max(local.completedDurationMin || 0, remote.completedDurationMin || 0),
                  studentNote: remote.studentNote || local.studentNote,
                  youtubeUrl: remote.youtubeUrl || local.youtubeUrl
                });
              }
            }
            current.occurrences = Array.from(occMap.values());
          }

          // 4. Sessions
          if (Array.isArray(incoming.sessions) && incoming.sessions.length > 0) {
            const sessMap = new Map((current.sessions || []).map(s => [s.id, s]));
            incoming.sessions.forEach(s => sessMap.set(s.id, s));
            current.sessions = Array.from(sessMap.values());
          }

          // 5. Reviews
          if (Array.isArray(incoming.reviews) && incoming.reviews.length > 0) {
            const revMap = new Map((current.reviews || []).map(r => [r.id, r]));
            incoming.reviews.forEach(r => revMap.set(r.id, r));
            current.reviews = Array.from(revMap.values());
          }

          // 6. Quizzes
          if (Array.isArray(incoming.quizzes) && incoming.quizzes.length > 0) {
            const quizMap = new Map((current.quizzes || []).map(q => [q.quizId, q]));
            incoming.quizzes.forEach(q => quizMap.set(q.quizId, q));
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
