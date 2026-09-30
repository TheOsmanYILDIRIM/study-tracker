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
            currentVersion: { title: 'Gerçek Sayılar Giriş' }
          },
          {
            id: 'item_mat9_quiz17',
            displayLabel: 'Quiz 17',
            stableKey: 'mat9_quiz17',
            itemType: 'QUIZ',
            orderKey: 2000.0,
            currentVersion: { title: 'Gerçek Sayılar Test 1' }
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

        if (url.pathname === '/api/v3/catalog') {
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

    // Test 6: AI Export Context Command
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
    console.log('   ✅ handleV2Command export-context validated.');

    console.log('\n🎉 ALL V2 CLI TESTS PASSED SUCCESSFULLY!');
  } finally {
    mockServer.close();
  }
}

runCliTests().catch(err => {
  console.error('CLI Test Failed:', err);
  if (mockServer) mockServer.close();
  process.exit(1);
});
