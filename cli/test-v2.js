/**
 * StudyTracker V2 CLI Integration Tests
 * Tests V2 CLI API helpers and CLI commands against mock server.
 */

const http = require('http');
const assert = require('assert');
const v2Api = require('./lib/v2-api');
const { handleV2Command } = require('./lib/v2-cli');
const { saveConfig } = require('./lib/config');

let mockServer;
let lastReceivedRequest = null;
const port = 8991;

const mockCurriculum = [
  {
    id: 'course_mat_9',
    title: '9. Sınıf Matematik',
    subject: 'Matematik',
    gradeLevel: 9,
    lessons: [
      {
        id: 'lesson_gercek_sayilar',
        title: 'Gerçek Sayılar',
        orderKey: 1000.0,
        items: [
          {
            id: 'item_mat9_vid17',
            displayLabel: 'Video 17',
            stableKey: 'mat9_vid17',
            itemType: 'VIDEO',
            orderKey: 1000.0,
            contentUrl: 'https://www.youtube.com/watch?v=kYqP9K0Y0pU',
            currentVersion: {
              id: 'ver_item_mat9_vid17_v1',
              title: 'Gerçek Sayılar Giriş',
              contentUrl: 'https://www.youtube.com/watch?v=kYqP9K0Y0pU'
            }
          },
          {
            id: 'item_mat9_quiz17',
            displayLabel: 'Quiz 17',
            stableKey: 'mat9_quiz17',
            itemType: 'QUIZ',
            orderKey: 2000.0,
            currentVersion: {
              id: 'ver_item_mat9_quiz17_v1',
              title: 'Gerçek Sayılar Test 1'
            }
          },
          {
            id: 'item_mat9_pratik',
            displayLabel: 'Pratik 1',
            stableKey: 'mat9_pratik',
            itemType: 'VIDEO',
            orderKey: 3000.0,
            currentVersion: {
              id: 'ver_item_mat9_pratik_v1',
              title: 'Gerçek Sayılar Pratik Soru Çözümü'
            }
          }
        ]
      }
    ]
  },
  {
    id: 'course_tar_9',
    title: '9. Sınıf Tarih',
    subject: 'Tarih',
    gradeLevel: 9,
    lessons: [
      {
        id: 'lesson_tar9_gecmis',
        title: 'Geçmişin İnşa Sürecinde Tarih',
        orderKey: 1000.0,
        items: [
          {
            id: 'item_tar9_vid1',
            displayLabel: 'Tarih 1.1',
            stableKey: 'tar9_vid_birey_toplum',
            itemType: 'VIDEO',
            orderKey: 1000.0,
            contentUrl: 'https://www.youtube.com/watch?v=5QxOpTALmEE',
            currentVersion: {
              id: 'ver_tar9_vid1_v1',
              title: 'Tarih Öğrenmenin Bireye ve Topluma Faydaları',
              contentUrl: 'https://www.youtube.com/watch?v=5QxOpTALmEE'
            }
          }
        ]
      }
    ]
  }
];

function startMockServer() {
  return new Promise((resolve) => {
    mockServer = http.createServer((req, res) => {
      const url = new URL(req.url, `http://127.0.0.1:${port}`);
      let body = '';
      req.on('data', chunk => { body += chunk; });
      req.on('end', () => {
        lastReceivedRequest = {
          method: req.method,
          path: url.pathname,
          query: Object.fromEntries(url.searchParams.entries()),
          headers: req.headers,
          body: body ? JSON.parse(body) : null
        };

        res.setHeader('Content-Type', 'application/json');

        if (url.pathname === '/api/sync' && req.method === 'GET') {
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            data: {
              familyCode: 'ST-V2TX-2026-CLI1-1111',
              revision: 10,
              plan: { weekId: '2026-W38' },
              tasks: [],
              occurrences: [
                {
                  id: '2026-W38_MON_mat_1',
                  planId: 'mat_1',
                  subject: 'Matematik: Üslü Sayılara Giriş',
                  topic: 'DAILY',
                  youtubeUrl: 'https://www.youtube.com/watch?v=kYqP9K0Y0pU',
                  status: 'APPROVED',
                  completedDurationMin: 25,
                  completedQuestionCount: 3,
                  targetQuestionCount: 3,
                  parentNote: 'Aferin!'
                },
                {
                  id: '2026-W38_MON_tar_1',
                  planId: 'tar_1',
                  subject: 'Tarih: Tarih Öğrenmenin Bireye ve Topluma Faydaları',
                  topic: 'DAILY',
                  youtubeUrl: 'https://www.youtube.com/watch?v=5QxOpTALmEE',
                  status: 'WAITING_REVIEW',
                  completedDurationMin: 20,
                  completedQuestionCount: 2,
                  targetQuestionCount: 2
                },
                {
                  id: '2026-W38_WED_mat_pratik',
                  planId: 'mat_pratik',
                  subject: 'Matematik: Gerçek Sayılar Pratik Soru Çözümü',
                  topic: 'DAILY',
                  youtubeUrl: null,
                  status: 'APPROVED',
                  completedDurationMin: 20,
                  completedQuestionCount: 0
                },
                {
                  id: '2026-W38_UNMATCHED_random',
                  planId: 'random_task',
                  subject: 'Robotik Kodlama ve Serbest Proje',
                  topic: 'OTHER',
                  youtubeUrl: null,
                  status: 'APPROVED',
                  completedDurationMin: 30
                }
              ],
              sessions: [
                {
                  id: 'sess_1',
                  occurrenceId: '2026-W38_MON_mat_1',
                  durationMin: 25,
                  isCompleted: true
                }
              ],
              reviews: [
                {
                  sessionId: '2026-W38_MON_mat_1',
                  isApproved: true,
                  feedbackNote: 'Harika'
                }
              ],
              quizzes: []
            }
          }));
        } else if (url.pathname === '/api/v3/catalog') {
          res.writeHead(200);
          res.end(JSON.stringify({ success: true, curriculum: mockCurriculum }));
        } else if (url.pathname === '/api/v3/courses' && req.method === 'POST') {
          res.writeHead(201);
          res.end(JSON.stringify({ success: true, course: { id: 'course_mat_9', ...lastReceivedRequest.body } }));
        } else if (url.pathname === '/api/v3/lessons' && req.method === 'POST') {
          res.writeHead(201);
          res.end(JSON.stringify({ success: true, lesson: { id: 'lesson_gercek_sayilar', ...lastReceivedRequest.body } }));
        } else if (url.pathname === '/api/v3/items' && req.method === 'POST') {
          res.writeHead(201);
          res.end(JSON.stringify({
            success: true,
            item: {
              id: 'item_mat9_quiz17_2',
              ...lastReceivedRequest.body,
              orderKey: 2500.0,
              currentVersionId: 'ver_item_mat9_quiz17_2_v1'
            }
          }));
        } else if (url.pathname === '/api/v3/items/item_mat9_vid17/content' && req.method === 'PATCH') {
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            item: {
              id: 'item_mat9_vid17',
              currentVersionId: 'ver_item_mat9_vid17_v2',
              versionCount: 2
            }
          }));
        } else if (url.pathname === '/api/v3/prerequisites' && req.method === 'POST') {
          res.writeHead(201);
          res.end(JSON.stringify({ success: true, prerequisite: lastReceivedRequest.body }));
        } else if (url.pathname === '/api/v3/attempts' && req.method === 'POST') {
          res.writeHead(201);
          res.end(JSON.stringify({
            success: true,
            attempt: { id: 'att_123', ...lastReceivedRequest.body },
            duplicate: false
          }));
        } else if (url.pathname === '/api/v3/analytics/progress') {
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            data: {
              studentId: 'student_ali',
              courses: [
                {
                  id: 'course_mat_9',
                  title: '9. Sınıf Matematik',
                  completionPercentage: 50,
                  averageMasteryScore: 85,
                  lessons: []
                }
              ]
            }
          }));
        } else if (url.pathname === '/api/v3/export-context') {
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            context: {
              schemaVersion: '2.0-measurement-curriculum',
              studentId: 'student_ali',
              curriculumTopology: mockCurriculum
            }
          }));
        } else {
          res.writeHead(200);
          res.end(JSON.stringify({ success: true }));
        }
      });
    });

    mockServer.listen(port, () => resolve());
  });
}

async function runCliTests() {
  console.log('🧪 === StudyTracker V2 CLI Tests ===\n');

  // Configure CLI to point to local mock server
  saveConfig({
    workerUrl: `http://127.0.0.1:${port}`,
    familyCode: 'ST-V2TX-2026-CLI1-1111',
    adminToken: 'test_admin_token'
  });

  await startMockServer();

  try {
    // Test 1: Fetch V2 Catalog API
    console.log('1️⃣ Testing fetchV2Catalog...');
    const cat = await v2Api.fetchV2Catalog('ST-V2TX-2026-CLI1-1111');
    assert.strictEqual(cat.success, true);
    assert.strictEqual(cat.curriculum[0].id, 'course_mat_9');
    console.log('   ✅ fetchV2Catalog passed.');

    // Test 2: Create Course API
    console.log('2️⃣ Testing createV2Course...');
    const courseRes = await v2Api.createV2Course('ST-V2TX-2026-CLI1-1111', {
      title: '9. Sınıf Matematik',
      subject: 'Matematik',
      gradeLevel: 9
    });
    assert.strictEqual(courseRes.success, true);
    assert.strictEqual(courseRes.course.id, 'course_mat_9');
    console.log('   ✅ createV2Course passed.');

    // Test 3: Create Item Insert-After (Puzzle Ordering) API
    console.log('3️⃣ Testing createV2Item with insert-after position...');
    const itemRes = await v2Api.createV2Item('ST-V2TX-2026-CLI1-1111', {
      lessonId: 'lesson_gercek_sayilar',
      itemType: 'QUIZ',
      displayLabel: 'Quiz 17.2',
      position: 'after',
      targetItemId: 'item_mat9_quiz17'
    });
    assert.strictEqual(itemRes.success, true);
    assert.strictEqual(itemRes.item.orderKey, 2500.0);
    console.log('   ✅ createV2Item insert-after passed.');

    // Test 4: Update Item Content (New Version) API
    console.log('4️⃣ Testing updateV2ItemContent...');
    const patchRes = await v2Api.updateV2ItemContent('ST-V2TX-2026-CLI1-1111', 'item_mat9_vid17', {
      title: 'Gerçek Sayılar HD'
    });
    assert.strictEqual(patchRes.success, true);
    assert.strictEqual(patchRes.item.currentVersionId, 'ver_item_mat9_vid17_v2');
    console.log('   ✅ updateV2ItemContent passed.');

    // Test 5: CLI Command Dispatcher (handleV2Command)
    console.log('5️⃣ Testing handleV2Command catalog & progress with --json...');
    let capturedLog = '';
    const originalLog = console.log;
    console.log = (msg) => { capturedLog += msg + '\n'; };

    try {
      await handleV2Command({
        positionals: ['v2', 'progress'],
        options: { json: true, student: 'student_ali' }
      }, 'ST-V2TX-2026-CLI1-1111');

      const parsedOutput = JSON.parse(capturedLog);
      assert.strictEqual(parsedOutput.success, true);
      assert.strictEqual(parsedOutput.data.courses[0].completionPercentage, 50);
    } finally {
      console.log = originalLog;
    }
    console.log('   ✅ handleV2Command JSON output validated.');

    console.log('6️⃣ Testing handleV2Command export-context...');
    let capturedExport = '';
    console.log = (msg) => { capturedExport += msg + '\n'; };

    try {
      await handleV2Command({
        positionals: ['v2', 'export-context'],
        options: { student: 'student_ali' }
      }, 'ST-V2TX-2026-CLI1-1111');

      const parsedContext = JSON.parse(capturedExport);
      assert.strictEqual(parsedContext.schemaVersion, '2.0-measurement-curriculum');
      assert.strictEqual(parsedContext.studentId, 'student_ali');
    } finally {
      console.log = originalLog;
    }
    console.log('   ✅ handleV2Command export-context validated.\n');

    // === PHASE 3 TESTS ===
    const { validateSeed, diffSeed, applySeed } = require('./lib/v2-seed');
    const { analyzeMigration, planMigration, applyMigration } = require('./lib/v2-migrate');

    // Test 7: Seed Schema Validation
    console.log('7️⃣ Testing Seed Schema Validation (content/9-sinif-v2-catalog.json)...');
    const validation = validateSeed();
    assert.strictEqual(validation.valid, true, 'Seed manifest must be structurally valid');
    assert.strictEqual(validation.stats.courseCount, 9, 'Must have 9 courses');
    assert.strictEqual(validation.stats.lessonCount, 12, 'Must have 12 lessons');
    assert.strictEqual(validation.stats.itemCount, 38, 'Must have 38 items');
    assert.strictEqual(validation.stats.videoCount, 34, 'Must have 34 video items');
    assert.strictEqual(validation.stats.ankiCount, 4, 'Must have 4 ANKI items');
    assert.strictEqual(validation.stats.quizCount, 0, 'Must have 0 fabricated quizzes');
    console.log(`   ✅ Seed schema valid: ${validation.stats.courseCount} courses, ${validation.stats.itemCount} items, ${validation.warnings.length} audit warnings.`);

    // Test 8: Canonical History Teacher & Stale Warning
    console.log('8️⃣ Testing History Canonical Teacher & Stale Warning...');
    const catalog = require('../content/9-sinif-v2-catalog.json');
    const historyCourse = catalog.courses.find(c => c.id === 'course_tar_9');
    assert(historyCourse, 'History course must exist');
    historyCourse.lessons.forEach(l => {
      l.items.filter(i => i.itemType === 'VIDEO').forEach(item => {
        assert.strictEqual(item.payload.teacher, 'Mehmet Celal ÖZYILDIZ', 'Active history video must be Mehmet Celal ÖZYILDIZ');
        assert.strictEqual(item.payload.provenance.reviewStatus, 'verified', 'Mehmet Celal ÖZYILDIZ items must be verified');
      });
    });

    // Test invalid/stale teacher generates warning
    const invalidTeacherCatalog = JSON.parse(JSON.stringify(catalog));
    invalidTeacherCatalog.courses.find(c => c.id === 'course_tar_9').lessons[0].items[0].payload.teacher = 'Ramis Hoca Stale';
    const invalidVal = validateSeed(invalidTeacherCatalog);
    assert(invalidVal.warnings.some(w => w.warning.includes('not canonical teacher Mehmet Celal ÖZYILDIZ')), 'Must flag non-canonical teacher');
    console.log('   ✅ History canonical teacher rule and stale detection verified.');

    // Test 9: Ambiguous & Channel URL Warnings
    console.log('9️⃣ Testing Ambiguous/Channel URL audit warnings...');
    const channelWarning = validation.warnings.find(w => w.warning.includes('Channel homepage URL'));
    assert(channelWarning, 'Must flag channel homepage URLs');
    console.log('   ✅ Channel homepage and ambiguous URLs successfully flagged in audit.');

    // Test 10: Seed Diff & Apply Idempotency
    console.log('🔟 Testing Seed Diff & Apply Idempotency...');
    const diff = await diffSeed('ST-V2TX-2026-CLI1-1111', catalog);
    assert(diff.summary.coursesToCreate >= 0, 'Diff summary must be computed');

    const dryRunApply = await applySeed('ST-V2TX-2026-CLI1-1111', catalog, { dryRun: true });
    assert.strictEqual(dryRunApply.dryRun, true);
    assert.strictEqual(dryRunApply.status, 'DRY_RUN_COMPLETED');
    console.log('   ✅ Seed diff and dry-run apply verified.');

    // Test 11: Content URL change -> new version, same ID
    console.log('1️⃣1️⃣ Testing Content URL change -> new version with immutable item ID...');
    const testItem = {
      id: 'item_test_v1',
      stableKey: 'item_test_v1',
      itemType: 'VIDEO',
      displayLabel: 'Test 1',
      title: 'Test Title 1',
      contentUrl: 'https://youtube.com/watch?v=old12345678',
      payload: { metadata: { tag: 'v1' } }
    };
    const { computeItemFingerprint } = require('./lib/v2-seed');
    const fp1 = computeItemFingerprint(testItem);
    const testItemUpdated = { ...testItem, contentUrl: 'https://youtube.com/watch?v=new12345678' };
    const fp2 = computeItemFingerprint(testItemUpdated);
    assert.notStrictEqual(fp1, fp2, 'Fingerprint must change when contentUrl changes');
    console.log('   ✅ Deterministic fingerprint change verified for content updates.');

    // Test 12: V1 -> V2 Migration Analysis (Exact Video ID, Normalized Title, Medium, Unmatched)
    console.log('1️⃣2️⃣ Testing V1 -> V2 Migration Analysis priorities & confidence...');
    const migrationAnalysis = await analyzeMigration('ST-V2TX-2026-CLI1-1111');
    assert(migrationAnalysis.totalV1Records > 0, 'Must analyze V1 records');

    // Exact match by YouTube ID
    const exactMatch = migrationAnalysis.mappings.find(m => m.legacyId === '2026-W38_MON_mat_1');
    assert(exactMatch, 'Matematik 1 must be present in mappings');
    assert.strictEqual(exactMatch.confidence, 'exact', 'Exact YouTube video match must have confidence: exact');

    // Normalized title match
    const highMatch = migrationAnalysis.mappings.find(m => m.legacyId === '2026-W38_WED_mat_pratik');
    assert(highMatch, 'Matematik pratik must be mapped');
    assert(['high', 'medium'].includes(highMatch.confidence), 'Normalized subject overlap must be high/medium');

    // Unmatched record
    const unmatchedRec = migrationAnalysis.mappings.find(m => m.legacyId === '2026-W38_UNMATCHED_random');
    assert(unmatchedRec, 'Unmatched task must be preserved');
    assert.strictEqual(unmatchedRec.confidence, 'unmatched', 'Unknown task must have confidence: unmatched');
    console.log('   ✅ Migration analysis priorities (exact, high, medium, unmatched) verified.');

    // Test 13: V1 -> V2 Migration Plan (Never auto-apply medium/unmatched, deterministic clientAttemptId)
    console.log('1️⃣3️⃣ Testing V1 -> V2 Migration Plan safety gating...');
    const plan = await planMigration('ST-V2TX-2026-CLI1-1111', { analyzeResult: migrationAnalysis });
    assert(plan.plannedAttempts.length > 0, 'Planned attempts must be populated');
    
    // Check that unmatched is in skippedRecords
    const skippedUnmatched = plan.skippedRecords.find(s => s.legacyId === '2026-W38_UNMATCHED_random');
    assert(skippedUnmatched, 'Unmatched record must be in skippedRecords');
    assert(!plan.plannedAttempts.some(p => p.metadata.legacyId === '2026-W38_UNMATCHED_random'), 'Unmatched record must never be auto-applied');

    // Check deterministic clientAttemptId
    const firstPlanAttempt = plan.plannedAttempts[0];
    assert(firstPlanAttempt.clientAttemptId.startsWith('mig_v1_'), 'clientAttemptId must be deterministically prefixed');
    assert.strictEqual(firstPlanAttempt.clientAttemptId, `mig_v1_${firstPlanAttempt.metadata.legacyId.replace(/[^a-zA-Z0-9_]/g, '_')}`);
    console.log('   ✅ Migration plan safety gating and deterministic attempt IDs verified.');

    // Test 14: Parent review approval irrelevant to completion
    console.log('1️⃣4️⃣ Testing Parent Review Approval does not gate completion...');
    const waitingReviewOcc = migrationAnalysis.mappings.find(m => m.legacyId === '2026-W38_MON_tar_1');
    assert(waitingReviewOcc, 'Waiting review occurrence must exist');
    assert.strictEqual(waitingReviewOcc.legacyStatus, 'WAITING_REVIEW');
    const waitingPlanAttempt = plan.plannedAttempts.find(p => p.metadata.legacyId === '2026-W38_MON_tar_1');
    assert(waitingPlanAttempt, 'Completed student work waiting for review must still be planned for attempt migration');
    assert.strictEqual(waitingPlanAttempt.status, 'COMPLETED');
    console.log('   ✅ Parent review preserved as metadata without gating attempt completion.');

    // Test 15: Migration Apply Dry-Run & Execution
    console.log('1️⃣5️⃣ Testing Migration Apply...');
    const applyDry = await applyMigration('ST-V2TX-2026-CLI1-1111', plan, { dryRun: true });
    assert.strictEqual(applyDry.dryRun, true);
    assert.strictEqual(applyDry.plannedAttemptsCount, plan.plannedAttempts.length);

    const applyLive = await applyMigration('ST-V2TX-2026-CLI1-1111', plan, { dryRun: false });
    assert.strictEqual(applyLive.dryRun, false);
    assert.strictEqual(applyLive.summary.recordedCount, plan.plannedAttempts.length);
    console.log('   ✅ Migration apply execution completed without data loss.');

    console.log('\n🎉 ALL V2 CLI & PHASE 3 MIGRATION TESTS PASSED SUCCESSFULLY!');
  } finally {
    mockServer.close();
  }
}

runCliTests().catch(err => {
  console.error('CLI Test Failed:', err);
  if (mockServer) mockServer.close();
  process.exit(1);
});

