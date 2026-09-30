const assert = require('assert');
const fs = require('fs');
const path = require('path');
const os = require('os');
const http = require('http');

// Setup isolated temp config directory for tests
const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'st-cli-test-'));
const tmpConfigFile = path.join(tmpDir, 'config.json');
process.env.STUDYTRACKER_CONFIG_DIR = tmpDir;
process.env.STUDYTRACKER_CONFIG_FILE = tmpConfigFile;

const config = require('./lib/config');
const api = require('./lib/api');

async function runTests() {
  console.log('--- 1. Testing cli/lib/config.js ---');

  // 1.1 Initial default config has lastKnownServerRevision as null
  const initialCfg = config.loadConfig();
  assert.strictEqual(initialCfg.lastKnownServerRevision, null, 'Initial revision must be null, never 0');
  assert.strictEqual(config.getLastKnownServerRevision(), null, 'getLastKnownServerRevision must return null when unknown');

  // 1.2 parseRevision pure helper
  assert.strictEqual(config.parseRevision(null), null);
  assert.strictEqual(config.parseRevision(undefined), null);
  assert.strictEqual(config.parseRevision(''), null);
  assert.strictEqual(config.parseRevision('   '), null);
  assert.strictEqual(config.parseRevision(-1), null);
  assert.strictEqual(config.parseRevision(-100), null);
  assert.strictEqual(config.parseRevision(NaN), null);
  assert.strictEqual(config.parseRevision(Infinity), null);
  assert.strictEqual(config.parseRevision(1.5), null);
  assert.strictEqual(config.parseRevision('abc'), null);
  assert.strictEqual(config.parseRevision({}), null);
  assert.strictEqual(config.parseRevision(true), null);
  assert.strictEqual(config.parseRevision(false), null);
  assert.strictEqual(config.parseRevision(0), 0, '0 is a valid non-negative revision');
  assert.strictEqual(config.parseRevision('0'), 0);
  assert.strictEqual(config.parseRevision(1), 1);
  assert.strictEqual(config.parseRevision(42), 42);
  assert.strictEqual(config.parseRevision('99'), 99);

  // 1.3 setLastKnownServerRevision and persistence
  config.setLastKnownServerRevision(10);
  assert.strictEqual(config.getLastKnownServerRevision(), 10);
  assert.strictEqual(config.loadConfig().lastKnownServerRevision, 10);

  // 1.4 Setting 0 works
  config.setLastKnownServerRevision(0);
  assert.strictEqual(config.getLastKnownServerRevision(), 0);

  // 1.5 Setting invalid negative or string does not overwrite with invalid value
  config.setLastKnownServerRevision(-5);
  assert.strictEqual(config.getLastKnownServerRevision(), 0, 'Invalid negative revision should not overwrite valid revision');

  // 1.6 Setting null clears revision
  config.setLastKnownServerRevision(null);
  assert.strictEqual(config.getLastKnownServerRevision(), null);

  // 1.7 setFamilyCode invalidates lastKnownServerRevision on code change
  config.setLastKnownServerRevision(25);
  assert.strictEqual(config.getLastKnownServerRevision(), 25);
  config.setFamilyCode('ST-FAM1-1111-2222-3333');
  assert.strictEqual(config.getLastKnownServerRevision(), null, 'Changing family code must invalidate cached revision to null');
  config.setLastKnownServerRevision(50);
  config.setFamilyCode('ST-FAM1-1111-2222-3333');
  assert.strictEqual(config.getLastKnownServerRevision(), 50, 'Setting same family code must preserve cached revision');

  console.log('✔ config.js tests passed');

  console.log('--- 2. Testing extractServerRevision pure helper in api.js ---');

  // 2.1 Top-level revision
  assert.strictEqual(api.extractServerRevision({ success: true, revision: 15 }), 15);
  // 2.2 Top-level currentRevision (409 conflict format)
  assert.strictEqual(api.extractServerRevision({ success: false, error: 'REVISION_CONFLICT', currentRevision: 8 }), 8);
  // 2.3 Meta revision
  assert.strictEqual(api.extractServerRevision({ success: true, meta: { revision: 22 } }), 22);
  // 2.4 Data revision
  assert.strictEqual(api.extractServerRevision({ success: true, data: { revision: 7 } }), 7);
  // 2.5 Data meta revision
  assert.strictEqual(api.extractServerRevision({ success: true, data: { meta: { revision: 33 } } }), 33);
  // 2.6 JSON string parsing
  assert.strictEqual(api.extractServerRevision('{"success":true,"revision":100}'), 100);
  assert.strictEqual(api.extractServerRevision('{"success":false,"currentRevision":12}'), 12);
  // 2.7 Malformed / missing / negative
  assert.strictEqual(api.extractServerRevision(null), null);
  assert.strictEqual(api.extractServerRevision(''), null);
  assert.strictEqual(api.extractServerRevision('not json'), null);
  assert.strictEqual(api.extractServerRevision({ success: true, message: 'OK' }), null);
  assert.strictEqual(api.extractServerRevision({ success: true, revision: -1 }), null);
  assert.strictEqual(api.extractServerRevision({ success: true, revision: null }), null);

  console.log('✔ extractServerRevision tests passed');

  console.log('--- 3. Testing HTTP Client Revision Flow (Mock Server) ---');

  let mockResponseStatus = 200;
  let mockResponseBody = {};
  let recordedRequests = [];

  const server = http.createServer((req, res) => {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      const record = {
        method: req.method,
        url: req.url,
        headers: req.headers,
        body: body ? (function() { try { return JSON.parse(body); } catch(_) { return body; } })() : null
      };
      recordedRequests.push(record);

      const status = typeof mockResponseStatus === 'function' ? mockResponseStatus(record) : mockResponseStatus;
      const respBody = typeof mockResponseBody === 'function' ? mockResponseBody(record) : mockResponseBody;

      res.writeHead(status, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(respBody));
    });
  });

  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const port = server.address().port;
  const mockUrl = `http://127.0.0.1:${port}`;

  // Point config to local test server
  const cfg = config.loadConfig();
  cfg.workerUrl = mockUrl;
  cfg.familyCode = 'ST-TEST';
  cfg.lastKnownServerRevision = null;
  config.saveConfig(cfg);

  try {
    // 3.1 fetchFamilyData captures server revision and updates config
    recordedRequests = [];
    mockResponseStatus = 200;
    mockResponseBody = {
      success: true,
      data: {
        familyCode: 'ST-TEST',
        revision: 5,
        tasks: [],
        occurrences: []
      }
    };

    const fetched = await api.fetchFamilyData('ST-TEST');
    assert.strictEqual(fetched.revision, 5);
    assert.strictEqual(config.getLastKnownServerRevision(), 5, 'fetchFamilyData must persist revision');

    // 3.2 pushFamilyData on clean-config automatically acquires revision before mutating
    config.setLastKnownServerRevision(null);
    recordedRequests = [];
    mockResponseStatus = (req) => 200;
    mockResponseBody = (req) => {
      if (req.method === 'GET') {
        return {
          success: true,
          data: { familyCode: 'ST-TEST', revision: 8, tasks: [], occurrences: [] }
        };
      }
      return {
        success: true,
        revision: 9,
        data: { success: true, revision: 9 }
      };
    };

    await api.pushFamilyData({ action: 'SYNC', tasks: [] }, 'ST-TEST');
    assert.strictEqual(recordedRequests.length, 2, 'Must have sent GET fetch then POST write');
    assert.strictEqual(recordedRequests[0].method, 'GET', 'First request must be GET fetchFamilyData');
    assert.strictEqual(recordedRequests[1].method, 'POST', 'Second request must be POST mutation');
    assert.strictEqual(recordedRequests[1].body.expectedRevision, 8, 'POST mutation must include acquired revision');
    assert.strictEqual(config.getLastKnownServerRevision(), 9, 'Must update to newly returned revision 9');

    // 3.3 pushFamilyData on clean-config fails without write if pre-fetch fails
    config.setLastKnownServerRevision(null);
    recordedRequests = [];
    mockResponseStatus = (req) => (req.method === 'GET' ? 500 : 200);
    mockResponseBody = (req) => (req.method === 'GET' ? { success: false, error: 'Internal Server Error' } : { success: true });

    let cleanPushError = null;
    try {
      await api.pushFamilyData({ action: 'SYNC', tasks: [] }, 'ST-TEST');
    } catch (err) {
      cleanPushError = err;
    }
    assert.ok(cleanPushError, 'pushFamilyData must fail when pre-fetch fails');
    assert.strictEqual(recordedRequests.length, 1, 'Only GET request should have been made; NO write POST');
    assert.strictEqual(recordedRequests[0].method, 'GET');

    // 3.4 restoreFamilyData on clean-config automatically acquires revision before restore write
    config.setLastKnownServerRevision(null);
    recordedRequests = [];
    mockResponseStatus = (req) => 200;
    mockResponseBody = (req) => {
      if (req.method === 'GET') {
        return {
          success: true,
          data: { familyCode: 'ST-TEST', revision: 12, tasks: [], occurrences: [] }
        };
      }
      return {
        success: true,
        revision: 13,
        data: { success: true, revision: 13 }
      };
    };

    await api.restoreFamilyData('ST-TEST');
    assert.strictEqual(recordedRequests.length, 2, 'Must have sent GET fetch then POST restore');
    assert.strictEqual(recordedRequests[0].method, 'GET');
    assert.strictEqual(recordedRequests[1].method, 'POST');
    assert.strictEqual(recordedRequests[1].body.action, 'RESTORE');
    assert.strictEqual(recordedRequests[1].body.expectedRevision, 12, 'Restore must use acquired revision 12');
    assert.strictEqual(config.getLastKnownServerRevision(), 13);

    // 3.5 restoreFamilyData on clean-config fails without write if pre-fetch fails
    config.setLastKnownServerRevision(null);
    recordedRequests = [];
    mockResponseStatus = (req) => (req.method === 'GET' ? 500 : 200);
    mockResponseBody = (req) => ({ success: false, error: 'Server error' });

    let cleanRestoreError = null;
    try {
      await api.restoreFamilyData('ST-TEST');
    } catch (err) {
      cleanRestoreError = err;
    }
    assert.ok(cleanRestoreError, 'restoreFamilyData must fail when pre-fetch fails');
    assert.strictEqual(recordedRequests.length, 1, 'Only GET request should have been made; NO write POST');

    // 3.6 pushFamilyData with known persisted revision does not need extra fetch
    config.setLastKnownServerRevision(15);
    recordedRequests = [];
    mockResponseStatus = 200;
    mockResponseBody = { success: true, revision: 16, data: {} };

    await api.pushFamilyData({ action: 'SYNC', tasks: [] }, 'ST-TEST');
    assert.strictEqual(recordedRequests.length, 1, 'Single POST mutation expected when revision is already known');
    assert.strictEqual(recordedRequests[0].method, 'POST');
    assert.strictEqual(recordedRequests[0].body.expectedRevision, 15);
    assert.strictEqual(config.getLastKnownServerRevision(), 16);

    // 3.7 pushFamilyData with explicit expectedRevision overrides config
    recordedRequests = [];
    mockResponseBody = { success: true, revision: 22, data: {} };
    await api.pushFamilyData({ action: 'SYNC', expectedRevision: 21 }, 'ST-TEST');
    assert.strictEqual(recordedRequests.length, 1);
    assert.strictEqual(recordedRequests[0].body.expectedRevision, 21);
    assert.strictEqual(config.getLastKnownServerRevision(), 22);

    // 3.8 HTTP 409 REVISION_CONFLICT in pushFamilyData persists currentRevision and surfaces failure with NO auto retry
    config.setLastKnownServerRevision(25);
    recordedRequests = [];
    mockResponseStatus = 409;
    mockResponseBody = {
      success: false,
      error: 'REVISION_CONFLICT',
      message: 'Server state has changed.',
      currentRevision: 30
    };

    let push409Error = null;
    try {
      await api.pushFamilyData({ action: 'SYNC' }, 'ST-TEST');
    } catch (err) {
      push409Error = err;
    }
    assert.ok(push409Error, 'pushFamilyData must throw on 409 conflict');
    assert.strictEqual(recordedRequests.length, 1, 'No auto-retry on 409');
    assert.strictEqual(config.getLastKnownServerRevision(), 30, '409 conflict must update lastKnownServerRevision to currentRevision 30');

    // 3.9 HTTP 409 REVISION_CONFLICT in restoreFamilyData persists currentRevision and surfaces failure with NO auto retry
    recordedRequests = [];
    mockResponseStatus = 409;
    mockResponseBody = {
      success: false,
      error: 'REVISION_CONFLICT',
      currentRevision: 35
    };

    let restore409Error = null;
    try {
      await api.restoreFamilyData('ST-TEST');
    } catch (err) {
      restore409Error = err;
    }
    assert.ok(restore409Error, 'restoreFamilyData must throw on 409');
    assert.strictEqual(recordedRequests.length, 1, 'No auto-retry on 409');
    assert.strictEqual(config.getLastKnownServerRevision(), 35, 'restoreFamilyData 409 must update lastKnownServerRevision to 35');

    // 3.10 pairFamily parses revision and persists to config
    config.setLastKnownServerRevision(null);
    recordedRequests = [];
    mockResponseStatus = 200;
    mockResponseBody = {
      success: true,
      familyCode: 'ST-NEWF-1234-5678-9012',
      adminToken: 'admin_tok_123',
      created: true,
      revision: 1
    };

    const pairRes = await api.pairFamily('ST-NEWF-1234-5678-9012');
    assert.strictEqual(pairRes.success, true);
    assert.strictEqual(pairRes.revision, 1);
    assert.strictEqual(config.getLastKnownServerRevision(), 1, 'pairFamily must persist initial revision');

    console.log('✔ HTTP Client Revision Flow tests passed');
  } finally {
    server.close();
    try {
      fs.rmSync(tmpDir, { recursive: true, force: true });
    } catch (_) {}
  }

  console.log('\n✅ All CLI revision integration tests passed successfully!');
}

runTests().catch(err => {
  console.error('❌ Test failed:', err);
  process.exit(1);
});
