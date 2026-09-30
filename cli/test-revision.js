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
  let lastReceivedRequestBody = null;
  let lastReceivedHeaders = null;

  const server = http.createServer((req, res) => {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      lastReceivedRequestBody = body ? JSON.parse(body) : null;
      lastReceivedHeaders = req.headers;
      res.writeHead(mockResponseStatus, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify(mockResponseBody));
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

    // 3.2 pushFamilyData includes expectedRevision from config fallback
    mockResponseStatus = 200;
    mockResponseBody = {
      success: true,
      revision: 6,
      data: { success: true, revision: 6 }
    };

    await api.pushFamilyData({ action: 'SYNC', tasks: [] }, 'ST-TEST');
    assert.strictEqual(lastReceivedRequestBody.expectedRevision, 5, 'pushFamilyData should include persisted revision');
    assert.strictEqual(config.getLastKnownServerRevision(), 6, 'pushFamilyData must persist newly returned revision 6');

    // 3.3 pushFamilyData with explicit expectedRevision overrides config
    mockResponseBody = { success: true, revision: 11, data: {} };
    await api.pushFamilyData({ action: 'SYNC', expectedRevision: 10 }, 'ST-TEST');
    assert.strictEqual(lastReceivedRequestBody.expectedRevision, 10, 'Explicit expectedRevision in payload takes precedence');
    assert.strictEqual(config.getLastKnownServerRevision(), 11);

    // 3.4 pushFamilyData with expectedRevision: null omits expectedRevision
    mockResponseBody = { success: true, revision: 15, data: {} };
    await api.pushFamilyData({ action: 'SYNC', expectedRevision: null }, 'ST-TEST');
    assert.strictEqual(lastReceivedRequestBody.expectedRevision, undefined, 'expectedRevision: null should omit expectedRevision');

    // 3.5 HTTP 409 REVISION_CONFLICT handling in pushFamilyData
    mockResponseStatus = 409;
    mockResponseBody = {
      success: false,
      error: 'REVISION_CONFLICT',
      message: 'Server state has changed.',
      currentRevision: 20
    };

    let pushError = null;
    try {
      await api.pushFamilyData({ action: 'SYNC' }, 'ST-TEST');
    } catch (err) {
      pushError = err;
    }
    assert.ok(pushError, 'pushFamilyData must throw on 409 conflict');
    assert.strictEqual(config.getLastKnownServerRevision(), 20, '409 conflict must update lastKnownServerRevision to currentRevision 20');

    // 3.6 restoreFamilyData sends expectedRevision and handles success & 409
    mockResponseStatus = 200;
    mockResponseBody = {
      success: true,
      revision: 21,
      data: { familyCode: 'ST-TEST', plan: null }
    };

    await api.restoreFamilyData('ST-TEST');
    assert.strictEqual(lastReceivedRequestBody.action, 'RESTORE');
    assert.strictEqual(lastReceivedRequestBody.expectedRevision, 20, 'restoreFamilyData should include lastKnownServerRevision 20');
    assert.strictEqual(config.getLastKnownServerRevision(), 21, 'restoreFamilyData must persist new revision 21');

    // 3.7 restoreFamilyData 409 conflict
    mockResponseStatus = 409;
    mockResponseBody = {
      success: false,
      error: 'REVISION_CONFLICT',
      currentRevision: 30
    };

    let restoreError = null;
    try {
      await api.restoreFamilyData('ST-TEST');
    } catch (err) {
      restoreError = err;
    }
    assert.ok(restoreError, 'restoreFamilyData must throw on 409');
    assert.strictEqual(config.getLastKnownServerRevision(), 30, 'restoreFamilyData 409 must update lastKnownServerRevision to 30');

    // 3.8 Backward compatibility: Legacy server without revision in response
    config.setLastKnownServerRevision(null);
    mockResponseStatus = 200;
    mockResponseBody = {
      success: true,
      data: {
        familyCode: 'ST-TEST',
        tasks: []
      }
    };

    const legacyData = await api.fetchFamilyData('ST-TEST');
    assert.strictEqual(config.getLastKnownServerRevision(), null, 'Legacy response without revision must not invent 0');
    assert.strictEqual(legacyData.revision, undefined);

    await api.pushFamilyData({ action: 'SYNC', tasks: [] }, 'ST-TEST');
    assert.strictEqual(lastReceivedRequestBody.expectedRevision, undefined, 'Legacy push without revision must omit expectedRevision');

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
