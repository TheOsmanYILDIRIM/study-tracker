/**
 * StudyTracker V2 Worker Integration & Domain Tests
 * Tests canonical KV storage, versioning, puzzle reordering, prerequisites,
 * append-only idempotent measurements, analytics, and 9th grade sample fixtures.
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

  // Test 0: Canonical KV storage contract
  console.log('0️⃣ Verifying canonical KV storage contract...');
  const storagePath = path.resolve('worker/v2/storage.js');
  assert(fs.existsSync(storagePath), 'worker/v2/storage.js must exist');
  const storageContent = fs.readFileSync(storagePath, 'utf8');
  assert(storageContent.includes("type: 'kv'"), 'V2 storage must report KV as canonical backend');
  assert(!storageContent.includes('createD1Storage'), 'V2 runtime must not contain a D1 storage fallback');
  assert(storageContent.includes('measurement:attempt:'), 'attempts must use dedicated append-only measurement keys');
  console.log('   ✅ canonical KV storage contract verified.\n');

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

  // Test 11: GET /api/v3/catalog Verification for Android
  console.log('1️⃣1️⃣ Testing GET /api/v3/catalog with full version content...');
  const catalogRes = await mockFetch('GET', '/api/v3/catalog', null, studentHeaders);
  assert(catalogRes.status === 200, 'Catalog fetch must succeed');
  assert(catalogRes.data.curriculum.length > 0, 'Catalog must contain courses');
  const catalogCourse = catalogRes.data.curriculum[0];
  assert(catalogCourse.lessons.length > 0, 'Course must contain lessons');
  const catalogLesson = catalogCourse.lessons[0];
  assert(catalogLesson.items.length > 0, 'Lesson must contain items');
  const firstItem = catalogLesson.items[0];
  assert(firstItem.currentVersion !== null, 'Item must include currentVersion object');
  assert(firstItem.currentVersion.versionNumber > 0, 'Version number must be positive');
  console.log('   ✅ Catalog tree and version payload verified for Android consumption.\n');

  // Test 12: GET /api/v3/health endpoint
  console.log('1️⃣2️⃣ Testing GET /api/v3/health (storage backend & schema version)...');
  const healthRes = await mockFetch('GET', '/api/v3/health');
  assert(healthRes.status === 200, 'Health endpoint must return 200');
  assert(healthRes.data.success === true, 'Health must report success: true');
  assert(healthRes.data.status === 'ok', 'Health status must be ok');
  assert(healthRes.data.schemaVersion === 'v2', 'schemaVersion must be v2');
  assert(['kv_fallback', 'kv', 'd1'].includes(healthRes.data.storageBackend), 'storageBackend must be reported');
  console.log(`   ✅ Health check verified: storageBackend = ${healthRes.data.storageBackend}, schemaVersion = ${healthRes.data.schemaVersion}\n`);

  // Test 13: Publishing semantics (draft/archived hidden from student catalog by default)
  console.log('1️⃣3️⃣ Testing Publishing Semantics (Student catalog hides draft & archived)...');
  const draftItemRes = await mockFetch('POST', '/api/v3/items', {
    id: 'item_mat9_draft_test',
    lessonId: 'lesson_gercek_sayilar',
    itemType: 'VIDEO',
    displayLabel: 'Draft Video',
    stableKey: 'mat9_draft_test',
    title: 'Draft Video Item',
    contentUrl: 'https://youtube.com/watch?v=draft123',
    publishingStatus: 'draft'
  }, authHeaders);
  assert(draftItemRes.status === 201, 'Draft item creation must succeed');
  assert(draftItemRes.data.item.publishingStatus === 'draft', 'publishingStatus must be draft');

  // Student fetch (default): should NOT include draft or archived
  const studentCat = await mockFetch('GET', '/api/v3/catalog', null, studentHeaders);
  const studentLessonItems = studentCat.data.curriculum[0].lessons[0].items;
  const draftFoundInStudent = studentLessonItems.find(i => i.id === 'item_mat9_draft_test');
  const archivedFoundInStudent = studentLessonItems.find(i => i.id === 'item_mat9_vid18');
  assert(!draftFoundInStudent, 'Draft item must be HIDDEN from student catalog by default');
  assert(!archivedFoundInStudent, 'Archived item must be HIDDEN from student catalog by default');

  // Parent/Admin fetch with status=all: should include draft and archived
  const adminCat = await mockFetch('GET', '/api/v3/catalog?status=all', null, authHeaders);
  const adminLessonItems = adminCat.data.curriculum[0].lessons[0].items;
  const draftFoundInAdmin = adminLessonItems.find(i => i.id === 'item_mat9_draft_test');
  const archivedFoundInAdmin = adminLessonItems.find(i => i.id === 'item_mat9_vid18');
  assert(draftFoundInAdmin, 'Draft item must be VISIBLE in admin catalog with status=all');
  assert(archivedFoundInAdmin, 'Archived item must be VISIBLE in admin catalog with status=all');
  console.log('   ✅ Student catalog strictly hides draft/archived; admin status=all exposes them.\n');

  // Test 14: Review Workflow endpoint (APPROVE, REJECT, REPLACE_CONTENT)
  console.log('1️⃣4️⃣ Testing Item Review Workflow (Approve, Reject, Replace Content)...');
  
  // 14A: APPROVE
  const approveRes = await mockFetch('POST', '/api/v3/items/item_mat9_draft_test/review', {
    action: 'APPROVE',
    reviewer: 'teacher_fatma',
    note: 'Content reviewed and verified against MEB curriculum'
  }, authHeaders);
  assert(approveRes.status === 200, 'Approve review must return 200');
  assert(approveRes.data.item.id === 'item_mat9_draft_test', 'Item ID must remain immutable');
  assert(approveRes.data.item.stableKey === 'mat9_draft_test', 'stableKey must remain immutable');
  assert(approveRes.data.item.publishingStatus === 'active', 'Approved item must have publishingStatus: active');
  const approvedProv = approveRes.data.item.currentVersion.payload.provenance;
  assert(approvedProv.reviewStatus === 'verified', 'reviewStatus must be verified');
  assert(approvedProv.reviewedOverride === true, 'reviewedOverride flag must be set');
  assert(approvedProv.reviewHistory.length >= 1, 'reviewHistory must contain approval entry');

  // 14B: REJECT
  const rejectRes = await mockFetch('POST', '/api/v3/items/item_mat9_draft_test/review', {
    action: 'REJECT',
    reviewer: 'teacher_fatma',
    reason: 'Incorrect audio quality, unpublishing until revised'
  }, authHeaders);
  assert(rejectRes.status === 200, 'Reject review must return 200');
  assert(rejectRes.data.item.id === 'item_mat9_draft_test', 'Item ID must remain immutable');
  assert(rejectRes.data.item.publishingStatus === 'draft', 'Rejected item must be unpopulated to draft');
  const rejectedProv = rejectRes.data.item.currentVersion.payload.provenance;
  assert(rejectedProv.reviewStatus === 'rejected', 'reviewStatus must be rejected');
  assert(rejectedProv.reviewHistory.length >= 2, 'reviewHistory must accumulate actions');

  // 14C: REPLACE_CONTENT
  const replaceRes = await mockFetch('POST', '/api/v3/items/item_mat9_draft_test/review', {
    action: 'REPLACE_CONTENT',
    reviewer: 'teacher_fatma',
    note: 'Replaced with high-definition remastered lecture video',
    content: {
      title: 'Gerçek Sayılar (Remastered HD)',
      contentUrl: 'https://youtube.com/watch?v=hd_remaster_123',
      publishingStatus: 'active'
    }
  }, authHeaders);
  assert(replaceRes.status === 200, 'Replace content review must return 200');
  assert(replaceRes.data.item.id === 'item_mat9_draft_test', 'Item ID must remain immutable');
  assert(replaceRes.data.item.stableKey === 'mat9_draft_test', 'stableKey must remain immutable');
  assert(replaceRes.data.item.currentVersion.title === 'Gerçek Sayılar (Remastered HD)', 'Title must update in new version');
  assert(replaceRes.data.item.currentVersion.contentUrl === 'https://youtube.com/watch?v=hd_remaster_123', 'URL must update in new version');
  assert(replaceRes.data.item.currentVersion.versionNumber >= 3, 'New version number must increment');
  assert(replaceRes.data.item.publishingStatus === 'active', 'Replacement can set publishingStatus: active');
  console.log('   ✅ Item Review Actions (Approve, Reject, Replace Content) verified with immutable IDs and full provenance.\n');

  console.log('🎉 ==========================================================');
  console.log('🎉 ALL V2 WORKER & DOMAIN MEASUREMENT TESTS PASSED 100%!');
  console.log('🎉 ==========================================================');
}

runV2Tests().catch(err => {
  console.error('Test Suite Failed:', err);
  process.exit(1);
});

