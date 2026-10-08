/**
 * StudyTracker V2 CLI Integration Tests
 * Tests V2 CLI API helpers and CLI commands against mock server.
 */

const http = require('http');
const path = require('path');
const fs = require('fs');
const assert = require('assert');
const v2Api = require('./lib/v2-api');
const { handleV2Command } = require('./lib/v2-cli');
const { saveConfig } = require('./lib/config');

let mockServer;
let lastReceivedRequest = null;
let port = 0;

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

let activeCurriculum = mockCurriculum;

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
        } else if (url.pathname === '/api/v3/health') {
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            status: 'ok',
            version: '2.0.0',
            schemaVersion: 'v2',
            storageBackend: 'kv_fallback',
            timestamp: Date.now()
          }));
        } else if (url.pathname === '/api/v3/catalog') {
          res.writeHead(200);
          res.end(JSON.stringify({ success: true, curriculum: activeCurriculum }));
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
              id: lastReceivedRequest.body.id || 'item_mat9_quiz17_2',
              ...lastReceivedRequest.body,
              orderKey: lastReceivedRequest.body.orderKey || 2500.0,
              currentVersionId: 'ver_item_mat9_quiz17_2_v1',
              publishingStatus: lastReceivedRequest.body.publishingStatus || 'active'
            }
          }));
        } else if (url.pathname.startsWith('/api/v3/items/') && url.pathname.endsWith('/review') && req.method === 'POST') {
          const parts = url.pathname.split('/');
          const itemId = parts[4];
          const reviewAction = lastReceivedRequest.body.action;
          const pubStatus = reviewAction === 'REJECT' ? 'draft' : 'active';
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            item: {
              id: itemId,
              stableKey: itemId === 'item_mat9_vid17' ? 'mat9_vid17' : itemId,
              publishingStatus: pubStatus,
              currentVersion: {
                id: `ver_${itemId}_v2`,
                versionNumber: 2,
                title: lastReceivedRequest.body.content?.title || 'Reviewed Item Title',
                contentUrl: lastReceivedRequest.body.content?.contentUrl || 'https://youtube.com/watch?v=reviewed_vid',
                payload: {
                  provenance: {
                    reviewStatus: reviewAction === 'REJECT' ? 'rejected' : 'verified',
                    reviewedOverride: true,
                    reviewedBy: lastReceivedRequest.body.reviewer || 'reviewer',
                    reviewHistory: [
                      { action: reviewAction, reviewer: lastReceivedRequest.body.reviewer, timestamp: new Date().toISOString() }
                    ]
                  }
                }
              }
            }
          }));
        } else if (url.pathname.startsWith('/api/v3/items/') && url.pathname.endsWith('/content') && req.method === 'PATCH') {
          const parts = url.pathname.split('/');
          const itemId = parts[4];
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            item: {
              id: itemId,
              currentVersionId: `ver_${itemId}_v2`,
              currentVersion: {
                id: `ver_${itemId}_v2`,
                versionNumber: 2,
                title: lastReceivedRequest.body.title || 'Updated Title',
                payload: lastReceivedRequest.body.payload || {}
              },
              versionCount: 2
            }
          }));
        } else if (url.pathname.startsWith('/api/v3/items/') && !url.pathname.includes('/content') && !url.pathname.includes('/review') && !url.pathname.includes('/status') && req.method === 'GET') {
          const parts = url.pathname.split('/');
          const itemId = parts[4];
          res.writeHead(200);
          res.end(JSON.stringify({
            success: true,
            item: {
              id: itemId,
              stableKey: itemId === 'item_mat9_vid17' ? 'mat9_vid17' : itemId,
              displayLabel: 'Item Label',
              itemType: 'VIDEO',
              publishingStatus: 'active',
              currentVersion: {
                id: `ver_${itemId}_v1`,
                versionNumber: 1,
                title: 'Item Title',
                contentUrl: 'https://youtube.com/watch?v=sample',
                payload: {
                  provenance: {
                    reviewStatus: 'needs_review',
                    reviewHistory: []
                  }
                }
              },
              versions: [
                { id: `ver_${itemId}_v1`, versionNumber: 1, title: 'Item Title' }
              ]
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

    mockServer.listen(0, () => {
      port = mockServer.address().port;
      resolve();
    });
  });
}

async function runCliTests() {
  console.log('🧪 === StudyTracker V2 CLI Tests ===\n');

  await startMockServer();

  // Configure CLI to point to local mock server
  saveConfig({
    workerUrl: `http://127.0.0.1:${port}`,
    familyCode: 'ST-V2TX-2026-CLI1-1111',
    adminToken: 'test_admin_token'
  });

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
    assert.strictEqual(validation.stats.lessonCount, 141, 'Must have 141 lessons');
    assert.strictEqual(validation.stats.itemCount, 379, 'Must have 379 items');
    assert.strictEqual(validation.stats.videoCount, 174, 'Must have 174 video items');
    assert.strictEqual(validation.stats.ankiCount, 5, 'Must have 5 ANKI items');
    assert.strictEqual(validation.stats.quizCount, 200, 'Must have 200 deterministic quiz items (35 lesson quizzes + 165 micro-quizzes)');
    assert.strictEqual(validation.warnings.length, 0, 'Production catalog must have 0 actionable audit warnings');
    assert.strictEqual(validation.explicitExceptions.length, 30, 'Production catalog must have exactly 30 verified explicit exceptions (29 sharedSource + 1 history override)');
    console.log(`   ✅ Seed schema valid: ${validation.stats.courseCount} courses, ${validation.stats.itemCount} items, ${validation.warnings.length} audit warnings, ${validation.explicitExceptions.length} explicit exceptions.`);

    // Test 8: Canonical History Teacher & Stale Warning / Active Error / Override Exception
    console.log('8️⃣ Testing History Canonical Teacher & Stale Warning...');
    const catalog = require('../content/9-sinif-v2-catalog.json');
    const historyCourse = catalog.courses.find(c => c.id === 'course_tar_9');
    assert(historyCourse, 'History course must exist');
    historyCourse.lessons.forEach(l => {
      l.items.filter(i => i.itemType === 'VIDEO' && (i.publishingStatus || 'active') === 'active').forEach(item => {
        const isCanonical = (item.payload.teacher || '').includes('Mehmet Celal');
        const isOverride = item.payload.provenance?.reviewedOverride === true;
        assert(isCanonical || isOverride, `Active history video must be Mehmet Celal ÖZYILDIZ or have reviewedOverride: ${item.stableKey}`);
        assert.strictEqual(item.payload.provenance.reviewStatus, 'verified', 'History video items must be verified');
      });
    });

    // Test active non-canonical teacher fails validation without override
    const invalidTeacherCatalog = JSON.parse(JSON.stringify(catalog));
    invalidTeacherCatalog.courses.find(c => c.id === 'course_tar_9').lessons[0].items[0].payload.teacher = 'Ramis Hoca Stale';
    const invalidVal = validateSeed(invalidTeacherCatalog);
    assert.strictEqual(invalidVal.valid, false, 'Active non-canonical history video must fail validation');
    assert(invalidVal.errors.some(e => e.includes('Mehmet Celal ÖZYILDIZ')), 'Must error on active non-canonical teacher');

    // Test draft non-canonical teacher generates warning without override
    invalidTeacherCatalog.courses.find(c => c.id === 'course_tar_9').lessons[0].items[0].publishingStatus = 'draft';
    const invalidDraftVal = validateSeed(invalidTeacherCatalog);
    assert(invalidDraftVal.warnings.some(w => w.warning.includes('not canonical teacher Mehmet Celal ÖZYILDIZ')), 'Must flag draft non-canonical teacher with warning');

    // Test reviewedOverride on non-canonical teacher becomes explicit exception (no warning, no error)
    invalidTeacherCatalog.courses.find(c => c.id === 'course_tar_9').lessons[0].items[0].payload.provenance.reviewedOverride = true;
    invalidTeacherCatalog.courses.find(c => c.id === 'course_tar_9').lessons[0].items[0].payload.provenance.reviewedOverrideReason = 'Teacher approved for special guest lecture';
    const overrideVal = validateSeed(invalidTeacherCatalog);
    assert.strictEqual(overrideVal.valid, true, 'Reviewed override on non-canonical teacher must be valid');
    assert(!overrideVal.warnings.some(w => w.warning?.includes('not canonical teacher Mehmet Celal ÖZYILDIZ')), 'Reviewed override must not produce warning');
    assert(overrideVal.explicitExceptions.some(e => e.type === 'HISTORY_TEACHER_OVERRIDE'), 'Reviewed override must be listed as explicit exception');
    console.log('   ✅ History canonical teacher rule, stale detection, and reviewedOverride verified.');

    // Test 9: Ambiguous & Channel URL Warnings & Safe Overrides
    console.log('9️⃣ Testing Ambiguous/Channel URL audit warnings & safe overrides...');
    const testCatalogWithChannel = JSON.parse(JSON.stringify(catalog));
    testCatalogWithChannel.courses[0].lessons[0].items[0].contentUrl = 'https://www.youtube.com/@cografyaninkodlari';
    testCatalogWithChannel.courses[0].lessons[0].items[0].publishingStatus = 'draft';
    delete testCatalogWithChannel.courses[0].lessons[0].items[0].payload.current_source;
    delete testCatalogWithChannel.courses[0].lessons[0].items[0].payload.safe;
    delete testCatalogWithChannel.courses[0].lessons[0].items[0].payload.provenance.reviewedOverride;
    const channelVal = validateSeed(testCatalogWithChannel);
    const channelWarning = channelVal.warnings.find(w => w.warning.includes('Channel homepage URL'));
    assert(channelWarning, 'Must flag unreviewed channel homepage URLs with warning');

    // Test reviewedOverride on channel homepage becomes explicit exception
    testCatalogWithChannel.courses[0].lessons[0].items[0].payload.provenance.reviewedOverride = true;
    testCatalogWithChannel.courses[0].lessons[0].items[0].payload.provenance.reviewedOverrideReason = 'Official verified curriculum channel playlist';
    const channelOverrideVal = validateSeed(testCatalogWithChannel);
    assert(!channelOverrideVal.warnings.some(w => w.warning?.includes('Channel homepage URL')), 'Reviewed override must not produce warning for channel URL');
    assert(channelOverrideVal.explicitExceptions.some(e => e.type === 'AMBIGUOUS_URL_OVERRIDE'), 'Channel URL override must be listed in explicitExceptions');
    console.log('   ✅ Channel homepage and ambiguous URLs successfully flagged in audit and overridden with explicit metadata.');

    // Test 9.5: Shared Video Source Verification & Duplicate Warnings
    console.log('9️⃣.5️⃣ Testing Shared Video Source Validation & Unapproved Duplicate Warnings...');
    const dupTestCatalog = JSON.parse(JSON.stringify(catalog));
    // Introduce an unflagged duplicate video URL
    const freshUrl = 'https://www.youtube.com/watch?v=unapprovedDup123';
    dupTestCatalog.courses[0].lessons[0].items[0].contentUrl = freshUrl;
    delete dupTestCatalog.courses[0].lessons[0].items[0].payload.provenance.sharedSource;
    delete dupTestCatalog.courses[0].lessons[0].items[0].payload.provenance.sharedSourceReason;
    dupTestCatalog.courses[0].lessons[1].items[0].contentUrl = freshUrl;
    delete dupTestCatalog.courses[0].lessons[1].items[0].payload.provenance.sharedSource;
    delete dupTestCatalog.courses[0].lessons[1].items[0].payload.provenance.sharedSourceReason;

    const unflaggedDupVal = validateSeed(dupTestCatalog);
    assert(unflaggedDupVal.warnings.some(w => w.url === freshUrl && w.warning.includes('without explicit sharedSource metadata')), 'Must warn on unflagged duplicate video URL');

    // Test inconsistent sharedSource metadata (one flagged, one not)
    dupTestCatalog.courses[0].lessons[0].items[0].payload.provenance.sharedSource = true;
    dupTestCatalog.courses[0].lessons[0].items[0].payload.provenance.sharedSourceReason = 'Topic A and B shared video';
    const partialDupVal = validateSeed(dupTestCatalog);
    assert(partialDupVal.warnings.some(w => w.url === freshUrl && w.warning.includes('Inconsistent video URL reuse')), 'Must warn on inconsistent sharedSource metadata');

    // Test both properly flagged with reason -> resolves to explicit exception with 0 warnings
    dupTestCatalog.courses[0].lessons[1].items[0].payload.provenance.sharedSource = true;
    dupTestCatalog.courses[0].lessons[1].items[0].payload.provenance.sharedSourceReason = 'Topic A and B shared video';
    const fixedDupVal = validateSeed(dupTestCatalog);
    assert(!fixedDupVal.warnings.some(w => w.url === freshUrl), 'Properly flagged sharedSource must not warn');
    assert(fixedDupVal.explicitExceptions.some(e => e.url === freshUrl && e.type === 'SHARED_SOURCE_EXCEPTION'), 'Properly flagged sharedSource must be in explicitExceptions');
    console.log('   ✅ Unapproved duplicate warning, inconsistent metadata guard, and valid sharedSource exception verified.');

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

    // Test 16: Review Workflow (approve, reject, replace-content keep immutable IDs)
    console.log('1️⃣6️⃣ Testing Review Workflow (approve, reject, replace-content)...');
    const { approveReviewItem, rejectReviewItem, replaceContentReviewItem, listReviewItems, showReviewItem } = require('./lib/v2-review');
    
    // 16A: List review items
    const reviewList = await listReviewItems('ST-V2TX-2026-CLI1-1111', { status: 'all' });
    assert(reviewList.total >= 3, 'Must list items across curriculum');

    // 16B: Approve item
    const approveRes = await approveReviewItem('ST-V2TX-2026-CLI1-1111', 'mat9_vid17', { note: 'Verified by teacher' });
    assert.strictEqual(approveRes.success, true);
    assert.strictEqual(approveRes.itemId, 'item_mat9_vid17', 'Item ID must not change on approve');
    assert.strictEqual(approveRes.stableKey, 'mat9_vid17', 'stableKey must not change on approve');
    assert.strictEqual(approveRes.publishingStatus, 'active');

    // 16C: Reject item
    const rejectRes = await rejectReviewItem('ST-V2TX-2026-CLI1-1111', 'mat9_vid17', { reason: 'Outdated curriculum link' });
    assert.strictEqual(rejectRes.success, true);
    assert.strictEqual(rejectRes.itemId, 'item_mat9_vid17', 'Item ID must not change on reject');
    assert.strictEqual(rejectRes.publishingStatus, 'draft', 'Rejected item must be unpopulated to draft');

    // 16D: Replace content
    const fixturePath = path.resolve(__dirname, 'test-fixtures/content-replacement-sample.json');
    const replaceRes = await replaceContentReviewItem('ST-V2TX-2026-CLI1-1111', 'mat9_vid17', { filePath: fixturePath, note: 'Replaced with direct lecture' });
    assert.strictEqual(replaceRes.success, true);
    assert.strictEqual(replaceRes.itemId, 'item_mat9_vid17', 'Item ID must not change on replace');
    assert.strictEqual(replaceRes.versionNumber, 2, 'New version number must be created');
    console.log('   ✅ Review workflow (approve, reject, replace-content) preserves immutable IDs and stable keys.');

    // Test 17: Reviewed override survives seed diff/apply (no silent overwrite)
    console.log('1️⃣7️⃣ Testing Reviewed Overrides survive seed diff/apply...');
    const mockRemoteWithOverride = {
      curriculum: [
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
                  stableKey: 'mat9_vid17',
                  displayLabel: 'Video 17',
                  orderKey: 1000.0,
                  versionCount: 2,
                  currentVersionId: 'ver_item_mat9_vid17_v2',
                  currentVersion: {
                    id: 'ver_item_mat9_vid17_v2',
                    versionNumber: 2,
                    title: 'Manually Reviewed Title',
                    contentUrl: 'https://youtube.com/watch?v=manual_verified',
                    payload: {
                      provenance: {
                        reviewStatus: 'verified',
                        reviewedOverride: true,
                        reviewedBy: 'teacher_ali'
                      }
                    }
                  }
                }
              ]
            }
          ]
        }
      ]
    };

    // Diff against seed with reviewed override
    const seedWithMat17 = {
      schemaVersion: 'v2',
      courses: [
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
                  stableKey: 'mat9_vid17',
                  itemType: 'VIDEO',
                  displayLabel: 'Video 17',
                  orderKey: 1000.0,
                  title: 'Seed Default Title',
                  contentUrl: 'https://youtube.com/watch?v=seed_default',
                  payload: { provenance: { reviewStatus: 'needs_review' } }
                }
              ]
            }
          ]
        }
      ]
    };

    // Temporarily swap mock response for catalog fetch
    activeCurriculum = mockRemoteWithOverride.curriculum;
    const diffWithOverride = await diffSeed('ST-V2TX-2026-CLI1-1111', seedWithMat17);
    activeCurriculum = mockCurriculum;
    assert.strictEqual(diffWithOverride.summary.itemsToUpdateContent, 0, 'Reviewed item must not be overwritten by seed');
    assert(diffWithOverride.summary.reviewedOverridesPreserved >= 1, 'Reviewed override must be marked as preserved');
    console.log('   ✅ Reviewed override successfully preserved in seed diff without silent overwrite.');

    // Test 18: Quiz Schema Validations
    console.log('1️⃣8️⃣ Testing Quiz Schema Validations...');
    const { validateQuizSchema, computeQuizFingerprint } = require('./lib/v2-quiz');
    
    // 18A: Valid quiz fixture
    const quizFixturePath = path.resolve(__dirname, 'test-fixtures/quiz-mat-kumeler.json');
    const validQuizRes = validateQuizSchema(quizFixturePath);
    assert.strictEqual(validQuizRes.valid, true, 'Valid quiz fixture must pass validation');
    assert.strictEqual(validQuizRes.stats.questionCount, 3);
    assert.strictEqual(validQuizRes.stats.multipleChoiceCount, 1);
    assert.strictEqual(validQuizRes.stats.trueFalseCount, 2);
    assert(typeof validQuizRes.fingerprint === 'string' && validQuizRes.fingerprint.length === 16, 'Fingerprint must be 16-hex sha256');

    // 18B: Invalid quiz (missing prompt, bad type, MC with <2 choices, bad correctAnswer)
    const invalidQuiz = {
      quizTitle: 'Invalid Quiz Test',
      questions: [
        { id: 'q1', type: 'INVALID_TYPE', prompt: 'Bad type' },
        { id: 'q2', type: 'MULTIPLE_CHOICE', prompt: '', choices: ['A'] },
        { id: 'q3', type: 'MULTIPLE_CHOICE', prompt: 'Question 3', choices: ['A', 'B'], correctAnswer: 'C' },
        { id: 'q4', type: 'TRUE_FALSE', prompt: 'Question 4', correctAnswer: 'MAYBE' }
      ]
    };
    const invalidRes = validateQuizSchema(invalidQuiz);
    assert.strictEqual(invalidRes.valid, false, 'Invalid quiz must fail validation');
    assert(invalidRes.errors.length >= 4, 'Must report all schema errors');
    console.log('   ✅ Quiz schema validator catches type, choice, and boolean errors deterministically.');

    // Test 19: Quiz 17.2 Insert-Between Behavior
    console.log('1️⃣9️⃣ Testing Quiz 17.2 Insert-Between Behavior...');
    const { createQuizItem } = require('./lib/v2-quiz');
    const createQuizRes = await createQuizItem('ST-V2TX-2026-CLI1-1111', {
      lessonIdOrKey: 'lesson_gercek_sayilar',
      displayLabel: 'Quiz 17.2',
      stableKey: 'mat9_quiz_17_2',
      quizFilePathOrObject: quizFixturePath,
      afterTarget: 'item_mat9_quiz17'
    });
    assert.strictEqual(createQuizRes.success, true);
    assert.strictEqual(createQuizRes.item.displayLabel, 'Quiz 17.2');
    assert.strictEqual(createQuizRes.item.stableKey, 'mat9_quiz_17_2');
    assert.strictEqual(createQuizRes.item.orderKey, 2500.0, 'OrderKey must be between Quiz 17 and Video 18');
    console.log('   ✅ Quiz 17.2 created with puzzle insert-between ordering.');

    // Test 20: Quiz Attach creates new version with fingerprint
    console.log('2️⃣0️⃣ Testing Quiz Attach to existing item...');
    const { attachQuizToItem } = require('./lib/v2-quiz');
    const attachRes = await attachQuizToItem('ST-V2TX-2026-CLI1-1111', 'mat9_vid17', quizFixturePath, { note: 'Attached practice quiz' });
    assert.strictEqual(attachRes.success, true);
    assert.strictEqual(attachRes.itemId, 'item_mat9_vid17', 'Item ID must remain immutable');
    assert(attachRes.fingerprint, 'Quiz attachment must have deterministic fingerprint');
    console.log('   ✅ Quiz attach successfully updated version content with deterministic fingerprint.');

    // Test 21: V2 Doctor Diagnostic Check
    console.log('2️⃣1️⃣ Testing V2 Doctor Diagnostic Check...');
    const { runV2Doctor } = require('./lib/v2-doctor');
    const doctorRes = await runV2Doctor('ST-V2TX-2026-CLI1-1111');
    assert.strictEqual(doctorRes.checks.healthEndpoint.status, 'PASS');
    assert.strictEqual(doctorRes.checks.catalog.status, 'PASS');
    assert.strictEqual(doctorRes.checks.attempts.status, 'PASS');
    assert.strictEqual(doctorRes.storageBackend, 'kv_fallback');
    assert(doctorRes.catalogSummary.itemCount >= 3);
    console.log('   ✅ V2 doctor diagnostic output verified deterministically.');

    // Test 22: History Canonical Teacher Rule Enforced
    console.log('2️⃣2️⃣ Testing History Canonical Teacher Rule Enforced...');
    const nonCanonicalHistoryCatalog = {
      schemaVersion: 'v2',
      courses: [
        {
          id: 'course_tar_9',
          title: '9. Sınıf Tarih',
          subject: 'Tarih',
          gradeLevel: 9,
          lessons: [
            {
              id: 'lesson_tar9_test',
              title: 'Tarih Bilimi',
              items: [
                {
                  id: 'item_tar9_fake',
                  stableKey: 'tar9_fake_video',
                  itemType: 'VIDEO',
                  displayLabel: '1.1',
                  publishingStatus: 'active',
                  contentUrl: 'https://youtube.com/watch?v=fake_history_123',
                  payload: {
                    teacher: 'Sahte Öğretmen'
                  }
                }
              ]
            }
          ]
        }
      ]
    };
    const invalidHistoryRes = validateSeed(nonCanonicalHistoryCatalog);
    assert.strictEqual(invalidHistoryRes.valid, false, 'Active History video violating teacher rule must fail validation');
    assert(invalidHistoryRes.errors.some(e => e.includes('Mehmet Celal ÖZYILDIZ')), 'Error must explicitly name canonical teacher');
    console.log('   ✅ History canonical teacher rule strictly enforced.');

    // === MODULAR ARCHITECTURE & MICRO-QUIZ TESTS ===
    const { validateModularTree, compileModularCatalog, saveCompiledCatalog, decomposeCatalog } = require('./lib/v2-modular');

    // Test 23: Modular Source Tree Validation
    console.log('2️⃣3️⃣ Testing Modular Content Source Tree Validation (content/v2)...');
    const modularVal = validateModularTree(path.resolve(__dirname, '../content/v2'));
    assert.strictEqual(modularVal.valid, true, 'Modular content tree must be valid');
    assert.strictEqual(modularVal.stats.courseCount, 9, 'Must have 9 modular courses');
    assert.strictEqual(modularVal.stats.lessonCount, 141, 'Must have 141 modular lessons');
    assert.strictEqual(modularVal.stats.itemCount, 379, 'Must have 379 modular items');
    assert.strictEqual(modularVal.stats.microQuizCount, 165, 'Must have 165 transcript-grounded micro-quizzes');
    console.log('   ✅ Modular content source tree validation verified (9 courses, 141 lessons, 379 items, 165 micro-quizzes).');

    // Test 24: Modular Compilation & Deterministic Round-Trip
    console.log('2️⃣4️⃣ Testing Modular Compiler & Deterministic Artifact Consistency...');
    const compiledModular = compileModularCatalog(path.resolve(__dirname, '../content/v2'));
    const diskCatalog = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../content/9-sinif-v2-catalog.json'), 'utf8'));
    assert.strictEqual(JSON.stringify(compiledModular), JSON.stringify(diskCatalog), 'Compiled catalog from content/v2 must match 9-sinif-v2-catalog.json exactly');
    console.log('   ✅ Modular compiler produces 100% deterministic, byte-stable catalog artifact.');

    // Test 25: Modular Validation Safety Guards (Dangling refs, wrong parents, duplicate IDs)
    console.log('2️⃣5️⃣ Testing Modular Validation Safety Guards...');
    const tempTestV2Dir = path.resolve(__dirname, '../.cache/test_v2_tree');
    if (fs.existsSync(tempTestV2Dir)) fs.rmSync(tempTestV2Dir, { recursive: true, force: true });
    decomposeCatalog(diskCatalog, tempTestV2Dir);

    // Test 25a: Dangling item ref
    const sampleLessonPath = path.join(tempTestV2Dir, 'lessons/lesson_mat9_sayilar_uslu_koklu.json');
    const sampleLesson = JSON.parse(fs.readFileSync(sampleLessonPath, 'utf8'));
    sampleLesson.items.push('item_non_existent_dangling_123');
    fs.writeFileSync(sampleLessonPath, JSON.stringify(sampleLesson, null, 2), 'utf8');
    const danglingVal = validateModularTree(tempTestV2Dir);
    assert.strictEqual(danglingVal.valid, false, 'Dangling item ref must fail validation');
    assert(danglingVal.errors.some(e => e.includes('Dangling item ref')), 'Error message must specify dangling item ref');

    // Test 25b: Wrong parent ref
    sampleLesson.items = sampleLesson.items.filter(i => i !== 'item_non_existent_dangling_123');
    fs.writeFileSync(sampleLessonPath, JSON.stringify(sampleLesson, null, 2), 'utf8');
    const sampleItemPath = path.join(tempTestV2Dir, 'items/item_mat9_vid_uslu_giris.json');
    const sampleItem = JSON.parse(fs.readFileSync(sampleItemPath, 'utf8'));
    sampleItem.lessonId = 'wrong_lesson_parent_id';
    fs.writeFileSync(sampleItemPath, JSON.stringify(sampleItem, null, 2), 'utf8');
    const wrongParentVal = validateModularTree(tempTestV2Dir);
    assert.strictEqual(wrongParentVal.valid, false, 'Wrong parent ref must fail validation');
    assert(wrongParentVal.errors.some(e => e.includes('Wrong parent ref')), 'Error message must specify wrong parent ref');

    // Clean up temporary tree
    fs.rmSync(tempTestV2Dir, { recursive: true, force: true });
    console.log('   ✅ Modular safety guards (dangling refs, wrong parent refs) verified.');

    // Test 26: Micro-Quiz Metadata & Provenance Verification
    console.log('2️⃣6️⃣ Testing Transcript-Grounded Micro-Quiz Provenance & Schema...');
    const itemFiles = fs.readdirSync(path.resolve(__dirname, '../content/v2/items')).filter(f => f.endsWith('__quiz.json'));
    assert.strictEqual(itemFiles.length, 165, 'Must have exactly 165 micro-quiz items');

    let totalMicroQuestions = 0;
    for (const qFileName of itemFiles) {
      const qItemFile = path.resolve(__dirname, `../content/v2/items/${qFileName}`);
      const qItem = JSON.parse(fs.readFileSync(qItemFile, 'utf8'));

      assert.strictEqual(qItem.itemType, 'QUIZ', 'Micro-quiz must have itemType QUIZ');
      assert.strictEqual(qItem.publishingStatus, 'active', 'Micro-quiz must be active');
      assert(qItem.payload?.quiz?.questions?.length >= 3, 'Micro-quiz must have at least 3 questions');
      totalMicroQuestions += qItem.payload.quiz.questions.length;

      const prov = qItem.payload?.provenance;
      assert(prov, 'Micro-quiz must have provenance');
      assert(prov.derivedFromItemId, 'Must have derivedFromItemId');
      assert.strictEqual(prov.reviewStatus, 'verified', 'Review status must be verified');

      if (prov.groundingType === 'transcript_grounded' || prov.transcriptFingerprint) {
        assert(prov.sourceVideoUrl, `Transcript-grounded quiz ${qItem.id} must have sourceVideoUrl`);
        assert.strictEqual(prov.transcriptLanguage, 'tr', `Quiz ${qItem.id} transcriptLanguage must be "tr"`);
        assert(typeof prov.transcriptFingerprint === 'string' && prov.transcriptFingerprint.length === 16, `Quiz ${qItem.id} must have 16-hex transcriptFingerprint`);
        
        // Assert matching parent video fingerprint
        const parentVidFile = path.resolve(__dirname, `../content/v2/items/${prov.derivedFromItemId}.json`);
        assert(fs.existsSync(parentVidFile), `Parent video file must exist for ${qItem.id}`);
        const parentVid = JSON.parse(fs.readFileSync(parentVidFile, 'utf8'));
        const videoProvenance = parentVid.payload?.provenance || {};
        if (videoProvenance.quizRegroundingStatus === 'pending') {
          // A changed lecture cannot inherit an old transcript-grounded quiz.
          // Preserve its historical provenance and explicitly require review.
          assert.strictEqual(prov.sourceVideoUrl, videoProvenance.previousSourceVideoUrl,
            `Quiz ${qItem.id} must retain its historical video URL until regrounding`);
          assert.notStrictEqual(prov.sourceVideoUrl, parentVid.contentUrl,
            `Quiz ${qItem.id} must not claim grounding in the replacement video`);
          assert(!videoProvenance.transcriptFingerprint,
            `Replacement video ${parentVid.id} must not reuse the old transcript fingerprint`);
        } else {
          assert.strictEqual(prov.sourceVideoUrl, parentVid.contentUrl, `Quiz ${qItem.id} sourceVideoUrl must match parent video contentUrl`);
          assert.strictEqual(prov.transcriptFingerprint, videoProvenance.transcriptFingerprint, `Quiz ${qItem.id} fingerprint must match parent video transcriptFingerprint`);
        }
      } else {
        assert(prov.sourceRef || prov.curriculumRef, `Curriculum-grounded quiz ${qItem.id} must have sourceRef or curriculumRef`);
      }

      // Check each question structure
      qItem.payload.quiz.questions.forEach(q => {
        assert(q.id, 'Question must have id');
        assert(q.prompt && q.prompt.trim().length > 0, 'Question must have non-empty prompt');
        assert(['MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(q.type), 'Question type must be MULTIPLE_CHOICE or TRUE_FALSE');
        assert(Array.isArray(q.choices) && q.choices.length >= 2, 'Choices must be array with >= 2 options');
        assert(q.choices.includes(q.correctAnswer), 'correctAnswer must exist in choices');
        assert(q.explanation && q.explanation.trim().length > 0, 'Explanation must be present');
      });
    }
    assert.strictEqual(totalMicroQuestions, 660, 'Must have exactly 660 transcript-grounded questions across 165 micro-quizzes');
    console.log(`   ✅ Micro-quiz provenance & schema verified (165 quizzes, ${totalMicroQuestions} questions total).`);

    console.log('\n🎉 ALL V2 CLI & PHASE 4/5 MODULAR TESTS PASSED SUCCESSFULLY!');
  } finally {
    mockServer.close();
  }
}

runCliTests().catch(err => {
  console.error('CLI Test Failed:', err);
  if (mockServer) mockServer.close();
  process.exit(1);
});

