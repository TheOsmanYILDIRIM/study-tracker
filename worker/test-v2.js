/**
 * StudyTracker V2 Worker Integration & Domain Tests
 * Tests KV fallback, D1 SQL schema structure, versioning, puzzle reordering,
 * prerequisites, append-only idempotent attempts, analytics, and 9th grade sample fixtures.
 */

import fs from 'fs';
import path from 'path';
import worker from './worker.js';

async function mockFetch(method, path, body = null, headers = {}) {
  const url = `https://studytracker-sync.workers.dev${path}`;
  const init = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  };
  if (body) {
    init.body = typeof body === 'string' ? body : JSON.stringify(body);
  }

  const req = new Request(url, init);
  const res = await worker.fetch(req, { __LOCAL_TEST__: true }, {});
  const json = await res.json().catch(() => ({}));
  return { status: res.status, ok: res.ok, data: json };
}

function assert(condition, message) {
  if (!condition) {
    console.error(`❌ ASSERTION FAILED: ${message}`);
    throw new Error(message);
  }
}

async function runV2Tests() {
  console.log('🧪 === StudyTracker V2 Measurement & Curriculum Test Suite ===\n');

  // Test 0: Schema File Verification (Structural check)
  console.log('0️⃣ Verifying worker/schema-v2.sql structure...');
  const schemaPath = path.resolve('worker/schema-v2.sql');
  assert(fs.existsSync(schemaPath), 'schema-v2.sql must exist');
  const schemaContent = fs.readFileSync(schemaPath, 'utf8');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS courses'), 'schema must contain courses table');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS lessons'), 'schema must contain lessons table');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS learning_items'), 'schema must contain learning_items table');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS learning_item_versions'), 'schema must contain learning_item_versions table');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS item_prerequisites'), 'schema must contain item_prerequisites table');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS attempts'), 'schema must contain attempts table');
  assert(schemaContent.includes('CREATE TABLE IF NOT EXISTS quiz_answers'), 'schema must contain quiz_answers table');
  assert(schemaContent.includes('idx_attempts_idempotency'), 'schema must contain attempts idempotency index');
  console.log('   ✅ schema-v2.sql structural check passed.\n');

  // Setup Test Family
  const pair = await mockFetch('POST', '/api/pair', { familyCode: 'ST-V2TX-2026-TEST-9901' });
  const familyCode = pair.data.familyCode;
  const adminToken = pair.data.adminToken;
  const authHeaders = {
    'X-Family-Code': familyCode,
    'X-Sender-Role': 'PARENT',
    'X-Admin-Token': adminToken
  };
  const studentHeaders = {
    'X-Family-Code': familyCode,
    'X-Sender-Role': 'CLIENT'
  };

  // Test 1: Create 9th Grade Mathematics Course
  console.log('1️⃣ Creating 9th Grade Mathematics Course...');
  const courseRes = await mockFetch('POST', '/api/v3/courses', {
    id: 'course_mat_9',
    title: '9. Sınıf Matematik',
    subject: 'Matematik',
    gradeLevel: 9,
    description: 'MEB 9. Sınıf Matematik Müfredatı ve Ölçme Değerlendirme'
  }, authHeaders);
  assert(courseRes.status === 201, 'Course creation must return 201');
  assert(courseRes.data.course.id === 'course_mat_9', 'Course ID must match');
  console.log('   ✅ Course created: 9. Sınıf Matematik\n');

  // Test 2: Create Lesson / Unit "Gerçek Sayılar"
  console.log('2️⃣ Creating Lesson: Gerçek Sayılar...');
  const lessonRes = await mockFetch('POST', '/api/v3/lessons', {
    id: 'lesson_gercek_sayilar',
    courseId: 'course_mat_9',
    title: 'Gerçek Sayılar'
  }, authHeaders);
  assert(lessonRes.status === 201, 'Lesson creation must return 201');
  assert(lessonRes.data.lesson.id === 'lesson_gercek_sayilar', 'Lesson ID must match');
  console.log('   ✅ Lesson created: Gerçek Sayılar\n');

  // Test 3: Create Initial Learning Items: Video 17, Quiz 17, Video 18
  console.log('3️⃣ Creating initial items: Video 17, Quiz 17, Video 18...');
  
  // Video 17
  const vid17Res = await mockFetch('POST', '/api/v3/items', {
    id: 'item_mat9_vid17',
    lessonId: 'lesson_gercek_sayilar',
    itemType: 'VIDEO',
    displayLabel: 'Video 17',
    stableKey: 'mat9_sayilar_vid17',
    title: 'Gerçek Sayılar ve Aralık Kavramı',
    contentUrl: 'https://youtube.com/watch?v=sample_mat9_vid17',
    orderKey: 1000.0
  }, authHeaders);
  assert(vid17Res.status === 201, 'Video 17 creation must succeed');
  assert(vid17Res.data.item.currentVersionId === 'ver_item_mat9_vid17_v1', 'V1 must be created');

  // Quiz 17
  const quiz17Res = await mockFetch('POST', '/api/v3/items', {
    id: 'item_mat9_quiz17',
    lessonId: 'lesson_gercek_sayilar',
    itemType: 'QUIZ',
    displayLabel: 'Quiz 17',
    stableKey: 'mat9_sayilar_quiz17',
    title: 'Gerçek Sayılar Tarama Testi 1',
    payload: {
      questions: [
        { id: 'q1', text: 'Hangi sayı irrasyoneldir?', options: ['3/4', '√2', '0.5', '2'], answer: '√2' }
      ]
    },
    orderKey: 2000.0
  }, authHeaders);
  assert(quiz17Res.status === 201, 'Quiz 17 creation must succeed');

  // Video 18
  const vid18Res = await mockFetch('POST', '/api/v3/items', {
    id: 'item_mat9_vid18',
    lessonId: 'lesson_gercek_sayilar',
    itemType: 'VIDEO',
    displayLabel: 'Video 18',
    stableKey: 'mat9_sayilar_vid18',
    title: 'Mutlak Değer ve Özellikleri',
    contentUrl: 'https://youtube.com/watch?v=sample_mat9_vid18',
    orderKey: 3000.0
  }, authHeaders);
  assert(vid18Res.status === 201, 'Video 18 creation must succeed');
  console.log('   ✅ Initial items created (Video 17, Quiz 17, Video 18).\n');

  // Test 4: Puzzle Modular Ordering - Insert Quiz 17.2 between Quiz 17 and Video 18
  console.log('4️⃣ Puzzle Ordering: Inserting Quiz 17.2 between Quiz 17 and Video 18...');
  const quiz17_2Res = await mockFetch('POST', '/api/v3/items', {
    id: 'item_mat9_quiz17_2',
    lessonId: 'lesson_gercek_sayilar',
    itemType: 'QUIZ',
    displayLabel: 'Quiz 17.2',
    stableKey: 'mat9_sayilar_quiz17_2',
    title: 'Gerçek Sayılar Pekiştirme Testi (Ek Modül)',
    position: 'after',
    targetItemId: 'item_mat9_quiz17',
    payload: {
      questions: [
        { id: 'q17_2_1', text: '√5 sayısı hangi iki tam sayı arasındadır?', options: ['1-2', '2-3', '3-4'], answer: '2-3' }
      ]
    }
  }, authHeaders);
  assert(quiz17_2Res.status === 201, 'Quiz 17.2 creation must succeed');
  const insertedOrderKey = quiz17_2Res.data.item.orderKey;
  console.log(`   Quiz 17.2 assigned orderKey: ${insertedOrderKey} (Expected between 2000.0 and 3000.0: 2500.0)`);
  assert(insertedOrderKey > 2000.0 && insertedOrderKey < 3000.0, 'OrderKey must sit midway between 2000 and 3000');

  // Verify full sequence order
  const listItemsRes = await mockFetch('GET', '/api/v3/items?lessonId=lesson_gercek_sayilar', null, authHeaders);
  const labels = listItemsRes.data.items.map(i => i.displayLabel);
  console.log('   Ordered items sequence:', labels);
  assert(labels[0] === 'Video 17', 'First must be Video 17');
  assert(labels[1] === 'Quiz 17', 'Second must be Quiz 17');
  assert(labels[2] === 'Quiz 17.2', 'Third must be inserted Quiz 17.2');
  assert(labels[3] === 'Video 18', 'Fourth must be Video 18');
  console.log('   ✅ Puzzle ordering successfully maintained.\n');

  // Test 5: Content Versioning - Update Video 17 Content
  console.log('5️⃣ Content Versioning: Updating Video 17 to Version 2...');
  const patchRes = await mockFetch('PATCH', '/api/v3/items/item_mat9_vid17/content', {
    title: 'Gerçek Sayılar ve Sayı Kümeleri (HD Revizyon)',
    contentUrl: 'https://youtube.com/watch?v=sample_mat9_vid17_hd_remaster',
    changelog: 'Görsel kalite ve ses iyileştirildi, yeni anlatım eklendi'
  }, authHeaders);
  assert(patchRes.status === 200, 'Content update must succeed');
  assert(patchRes.data.item.id === 'item_mat9_vid17', 'Item ID must remain immutable');
  assert(patchRes.data.item.stableKey === 'mat9_sayilar_vid17', 'Stable key must remain immutable');
  assert(patchRes.data.item.currentVersionId === 'ver_item_mat9_vid17_v2', 'Current version pointer must point to v2');
  assert(patchRes.data.item.versionCount === 2, 'Version count must be 2');

  const getItemRes = await mockFetch('GET', '/api/v3/items/item_mat9_vid17', null, authHeaders);
  assert(getItemRes.data.item.versions.length === 2, 'Item must have exactly 2 versions in history');
  assert(getItemRes.data.item.versions[0].versionNumber === 1, 'Version 1 preserved');
  assert(getItemRes.data.item.versions[1].versionNumber === 2, 'Version 2 active');
  console.log('   ✅ Item ID immutable, Version 2 created, history preserved.\n');

  // Test 6: Prerequisites and Cycle Prevention
  console.log('6️⃣ Setting Prerequisites: Quiz 17.2 requires Quiz 17 (minScore: 70)...');
  const prereqRes = await mockFetch('POST', '/api/v3/prerequisites', {
    itemId: 'item_mat9_quiz17_2',
    requiredItemId: 'item_mat9_quiz17',
    minScore: 70
  }, authHeaders);
  assert(prereqRes.status === 201, 'Prerequisite addition must succeed');

  // Attempt circular prerequisite (Quiz 17 requires Quiz 17.2 -> should fail)
  const cycleRes = await mockFetch('POST', '/api/v3/prerequisites', {
    itemId: 'item_mat9_quiz17',
    requiredItemId: 'item_mat9_quiz17_2'
  }, authHeaders);
  assert(cycleRes.status === 400, 'Circular prerequisite must be rejected with 400');
  console.log('   ✅ Circular prerequisite successfully prevented.\n');

  // Test 7: Student Attempts (Append-only and Idempotency)
  console.log('7️⃣ Recording Student Attempts (Idempotency & Version Binding)...');
  
  // Student completes Video 17 referencing Version 1
  const att1Res = await mockFetch('POST', '/api/v3/attempts', {
    clientAttemptId: 'cli_att_001',
    studentId: 'student_ali',
    itemId: 'item_mat9_vid17',
    versionId: 'ver_item_mat9_vid17_v1',
    status: 'COMPLETED',
    durationSeconds: 1200
  }, studentHeaders);
  assert(att1Res.status === 201, 'Attempt 1 must succeed');
  assert(att1Res.data.attempt.versionId === 'ver_item_mat9_vid17_v1', 'Attempt must retain reference to v1');

  // Idempotent duplicate submission of att1
  const att1DupRes = await mockFetch('POST', '/api/v3/attempts', {
    clientAttemptId: 'cli_att_001',
    studentId: 'student_ali',
    itemId: 'item_mat9_vid17',
    durationSeconds: 1200
  }, studentHeaders);
  assert(att1DupRes.status === 200, 'Duplicate attempt must return 200 idempotent');
  assert(att1DupRes.data.duplicate === true, 'Duplicate flag must be true');

  // Student takes Quiz 17 (score: 80, satisfying the 70 minScore for Quiz 17.2)
  const att2Res = await mockFetch('POST', '/api/v3/attempts', {
    clientAttemptId: 'cli_att_002',
    studentId: 'student_ali',
    itemId: 'item_mat9_quiz17',
    status: 'COMPLETED',
    score: 80.0,
    durationSeconds: 450,
    quizAnswers: [
      { questionId: 'q1', questionIndex: 0, selectedOption: '√2', isCorrect: true, durationSeconds: 25 }
    ]
  }, studentHeaders);
  assert(att2Res.status === 201, 'Attempt 2 must succeed');
  assert(att2Res.data.quizAnswers.length === 1, 'Quiz answer metric recorded');
  console.log('   ✅ Idempotent attempts and quiz metrics recorded.\n');

  // Test 8: Deterministic Progress Analytics
  console.log('8️⃣ Checking Deterministic Progress Analytics & Prerequisite Unlocks...');
  const progressRes = await mockFetch('GET', '/api/v3/analytics/progress?studentId=student_ali&courseId=course_mat_9', null, authHeaders);
  assert(progressRes.status === 200, 'Progress analytics must succeed');
  const courseProg = progressRes.data.data.courses[0];
  const lessonProg = courseProg.lessons[0];
  console.log(`   Lesson Progress: ${lessonProg.completedItems}/${lessonProg.totalItems} items (${lessonProg.completionPercentage}%)`);
  
  const quiz17_2Prog = lessonProg.items.find(i => i.id === 'item_mat9_quiz17_2');
  assert(quiz17_2Prog.isUnlocked === true, 'Quiz 17.2 must be unlocked because Quiz 17 scored 80 >= 70');
  console.log('   ✅ Prerequisite condition evaluated deterministically (isUnlocked: true).\n');

  // Test 9: Context Export for External AI
  console.log('9️⃣ Testing AI Context Export (/api/v3/export-context)...');
  const exportRes = await mockFetch('GET', '/api/v3/export-context?studentId=student_ali', null, authHeaders);
  assert(exportRes.status === 200, 'Export context must succeed');
  assert(exportRes.data.context.schemaVersion === '2.0-measurement-curriculum', 'Schema version must match');
  assert(exportRes.data.context.curriculumTopology.length > 0, 'Curriculum topology must be populated');
  console.log('   ✅ AI Context Export package generated deterministically.\n');

  // Test 10: Archive Behavior (Archive does not delete or destroy attempts)
  console.log('🔟 Testing Catalog Archiving (Archiving item does not erase attempts)...');
  const archiveRes = await mockFetch('POST', '/api/v3/items/item_mat9_vid18/archive', null, authHeaders);
  assert(archiveRes.status === 200, 'Archive item must succeed');
  assert(archiveRes.data.item.isArchived === true, 'Item isArchived must be true');

  const attemptsAfterArchive = await mockFetch('GET', '/api/v3/attempts?studentId=student_ali', null, authHeaders);
  assert(attemptsAfterArchive.data.attempts.length >= 2, 'Attempts must remain completely preserved after archiving');
  console.log('   ✅ Archiving verified: attempts intact.\n');

  console.log('🎉 ==========================================================');
  console.log('🎉 ALL V2 WORKER & DOMAIN MEASUREMENT TESTS PASSED 100%!');
  console.log('🎉 ==========================================================');
}

runV2Tests().catch(err => {
  console.error('Test Suite Failed:', err);
  process.exit(1);
});
