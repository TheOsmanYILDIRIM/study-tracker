/**
 * StudyTracker V2 canonical KV staging test.
 *
 * Verifies the current product contract:
 * - KV is the only V2 runtime storage backend.
 * - VIDEO completion is self-reported.
 * - QUIZ / delayed recall / ANKI are measurement evidence.
 * - attempts are idempotent and persisted as append-only KV records.
 */

import assert from 'assert';
import worker from './worker.js';

function createKvShim() {
  const map = new Map();
  return {
    map,
    async get(key) {
      return map.has(key) ? map.get(key) : null;
    },
    async put(key, value) {
      map.set(key, value);
    }
  };
}

async function request(env, method, path, body = null, headers = {}) {
  const req = new Request(`https://studytracker-v2-staging.example${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    },
    body: body == null ? null : JSON.stringify(body)
  });
  const res = await worker.fetch(req, env, { waitUntil() {} });
  const data = await res.json().catch(() => ({}));
  return { status: res.status, data };
}

async function run() {
  console.log('🧪 StudyTracker V2 canonical KV staging suite');

  const kv = createKvShim();
  const env = {
    STUDY_SYNC_KV: kv,
    ENVIRONMENT: 'staging',
    STAGING: 'true',
    STORAGE_BACKEND: 'kv',
    BUILD_REVISION: 'kv-staging-test'
  };

  const familyCode = 'ST-KV01-2026-TEST-0001';
  const pair = await request(env, 'POST', '/api/pair', { familyCode });
  assert.strictEqual(pair.data.success, true);

  const adminHeaders = {
    'X-Family-Code': familyCode,
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': pair.data.adminToken
  };
  const studentHeaders = {
    'X-Family-Code': familyCode,
    'X-Sender-Role': 'CLIENT'
  };

  const health = await request(env, 'GET', '/api/v3/health', null, studentHeaders);
  assert.strictEqual(health.data.success, true);
  assert.strictEqual(health.data.storageBackend, 'kv');
  assert.strictEqual(health.data.environment, 'staging');

  const course = await request(env, 'POST', '/api/v3/courses', {
    id: 'course_math_9',
    title: '9. Sınıf Matematik',
    subject: 'Matematik',
    gradeLevel: 9
  }, adminHeaders);
  assert.strictEqual(course.status, 201);

  const lesson = await request(env, 'POST', '/api/v3/lessons', {
    id: 'lesson_numbers',
    courseId: 'course_math_9',
    title: 'Sayılar'
  }, adminHeaders);
  assert.strictEqual(lesson.status, 201);

  const items = [
    {
      id: 'item_video_17',
      lessonId: 'lesson_numbers',
      itemType: 'VIDEO',
      displayLabel: 'Video 17',
      stableKey: 'math.video.17',
      orderKey: 1000,
      title: 'Video'
    },
    {
      id: 'item_quiz_17',
      lessonId: 'lesson_numbers',
      itemType: 'QUIZ',
      displayLabel: 'Quiz 17',
      stableKey: 'math.quiz.17',
      orderKey: 2000,
      title: 'Quiz'
    },
    {
      id: 'item_anki_17',
      lessonId: 'lesson_numbers',
      itemType: 'ANKI',
      displayLabel: 'Anki 17',
      stableKey: 'math.anki.17',
      orderKey: 3000,
      title: 'Anki'
    }
  ];

  for (const item of items) {
    const res = await request(env, 'POST', '/api/v3/items', item, adminHeaders);
    assert.strictEqual(res.status, 201);
  }

  const video = await request(env, 'POST', '/api/v3/attempts', {
    clientAttemptId: 'android-video-17-1',
    studentId: 'student_default',
    itemId: 'item_video_17',
    status: 'COMPLETED',
    durationSeconds: 600,
    metadata: {
      measurementKind: 'VIDEO',
      selfCompleted: true
    }
  }, studentHeaders);
  assert.strictEqual(video.status, 201);
  assert.strictEqual(video.data.attempt.metadata.selfCompleted, true);

  const quizPayload = {
    clientAttemptId: 'android-quiz-17-1',
    studentId: 'student_default',
    itemId: 'item_quiz_17',
    status: 'COMPLETED',
    score: 80,
    durationSeconds: 180,
    metadata: {
      measurementKind: 'DELAYED_RECALL',
      recallDelayHours: 24
    },
    quizAnswers: [
      {
        questionId: 'q1',
        questionIndex: 0,
        selectedOption: 'B',
        isCorrect: true,
        durationSeconds: 20
      }
    ]
  };

  const quiz = await request(env, 'POST', '/api/v3/attempts', quizPayload, studentHeaders);
  assert.strictEqual(quiz.status, 201);

  const quizDuplicate = await request(env, 'POST', '/api/v3/attempts', quizPayload, studentHeaders);
  assert.strictEqual(quizDuplicate.status, 200);
  assert.strictEqual(quizDuplicate.data.duplicate, true);

  const anki = await request(env, 'POST', '/api/v3/attempts', {
    clientAttemptId: 'android-anki-17-1',
    studentId: 'student_default',
    itemId: 'item_anki_17',
    status: 'COMPLETED',
    durationSeconds: 240,
    metadata: {
      measurementKind: 'ANKI',
      reviewedCount: 20,
      again: 2,
      hard: 3,
      good: 10,
      easy: 5
    }
  }, studentHeaders);
  assert.strictEqual(anki.status, 201);

  const analytics = await request(
    env,
    'GET',
    '/api/v3/analytics/progress?studentId=student_default',
    null,
    studentHeaders
  );
  assert.strictEqual(analytics.data.success, true);
  assert.strictEqual(analytics.data.data.model, 'measurement-not-control');
  const measurement = analytics.data.data.courses[0].measurement;
  assert.strictEqual(measurement.averageLearningScore, 80);
  assert.strictEqual(measurement.delayedRecallAttempts, 1);
  assert.strictEqual(measurement.ankiReviews, 20);

  const attemptKeys = [...kv.map.keys()].filter(k => k.includes(':measurement:attempt:'));
  assert.strictEqual(attemptKeys.length, 3, 'three immutable attempt records must exist');

  const d1Keys = [...kv.map.keys()].filter(k => k.toLowerCase().includes('d1'));
  assert.strictEqual(d1Keys.length, 0);

  console.log('✅ canonical KV staging suite passed');
}

run().catch(err => {
  console.error('❌ canonical KV staging suite failed:', err);
  process.exit(1);
});
