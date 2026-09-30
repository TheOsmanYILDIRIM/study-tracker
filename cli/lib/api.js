const https = require('https');
const http = require('http');
const { URL } = require('url');
const {
  loadConfig,
  parseRevision,
  getLastKnownServerRevision,
  setLastKnownServerRevision
} = require('./config');

function makeRequest(targetUrl, options = {}, postData = null) {
  return new Promise((resolve, reject) => {
    const parsed = new URL(targetUrl);
    const client = parsed.protocol === 'https:' ? https : http;

    const reqOptions = {
      method: options.method || 'GET',
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        ...(options.headers || {})
      },
      timeout: options.timeout || 15000
    };

    const req = client.request(parsed, reqOptions, (res) => {
      let data = '';
      res.setEncoding('utf8');
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        try {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            const json = JSON.parse(data);
            resolve(json);
          } else {
            let parsedBody = null;
            try {
              parsedBody = JSON.parse(data);
            } catch (_) {}
            const err = new Error(`HTTP ${res.statusCode}: ${data}`);
            err.statusCode = res.statusCode;
            err.responseBody = data;
            err.data = parsedBody;
            reject(err);
          }
        } catch (e) {
          reject(new Error(`JSON Parse Hatası: ${e.message} (Cevap: ${data})`));
        }
      });
    });

    req.on('error', (err) => reject(err));
    req.on('timeout', () => {
      req.destroy();
      reject(new Error('İstek zaman aşımına uğradı (15s)'));
    });

    if (postData) {
      req.write(typeof postData === 'string' ? postData : JSON.stringify(postData));
    }
    req.end();
  });
}

function extractServerRevision(res) {
  if (!res) return null;
  let obj = res;
  if (typeof res === 'string') {
    try {
      obj = JSON.parse(res);
    } catch (_) {
      return null;
    }
  }
  if (!obj || typeof obj !== 'object') return null;
  const candidates = [
    obj.revision,
    obj.currentRevision,
    obj.meta?.revision,
    obj.data?.revision,
    obj.data?.currentRevision,
    obj.data?.meta?.revision
  ];
  for (const c of candidates) {
    const parsed = parseRevision(c);
    if (parsed !== null) {
      return parsed;
    }
  }
  return null;
}

function handleConflictError(err) {
  if (!err) return;
  const isConflict = err.statusCode === 409 ||
    (typeof err.message === 'string' && err.message.includes('409'));
  if (isConflict) {
    const conflictRev = extractServerRevision(err.data || err.responseBody || err.message);
    if (conflictRev !== null) {
      setLastKnownServerRevision(conflictRev);
    }
  }
}

/**
 * Cloudflare KV'den mevcut aile verilerini çeker
 */
async function fetchFamilyData(overrideCode = null) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  if (!code) throw new Error('Aile kodu ayarlı değil. Önce config set-code kullanın.');
  const endpoint = `${config.workerUrl}/api/sync?code=${encodeURIComponent(code)}`;

  let res;
  try {
    res = await makeRequest(endpoint, {
      method: 'GET',
      headers: { 'X-Family-Code': code }
    });
  } catch (err) {
    handleConflictError(err);
    throw err;
  }

  if (!res.success) {
    throw new Error(res.error || 'Buluttan veri çekilemedi');
  }

  const serverRev = extractServerRevision(res);
  if (serverRev !== null) {
    setLastKnownServerRevision(serverRev);
  }

  const resultData = res.data || {
    familyCode: code,
    plan: null,
    tasks: [],
    occurrences: [],
    sessions: [],
    reviews: [],
    quizzes: []
  };

  if (serverRev !== null && resultData.revision === undefined) {
    resultData.revision = serverRev;
  }

  return resultData;
}

/**
 * Cloudflare KV'ye güncel aile verilerini yükler (senderRole: "PARENT" veya "CHILD")
 */
async function pushFamilyData(payload, overrideCode = null, senderRole = 'PARENT') {
  const config = loadConfig();
  const code = (overrideCode || payload.familyCode || config.familyCode || '').toUpperCase().trim();
  const endpoint = `${config.workerUrl}/api/sync?code=${encodeURIComponent(code)}`;

  let expRev;
  if (payload.expectedRevision !== undefined) {
    expRev = parseRevision(payload.expectedRevision);
  } else if (payload.revision !== undefined) {
    expRev = parseRevision(payload.revision);
  } else {
    expRev = getLastKnownServerRevision();
  }

  const fullPayload = {
    familyCode: code,
    senderRole: senderRole,
    action: payload.action || 'SYNC',
    plan: payload.plan || null,
    tasks: payload.tasks || [],
    occurrences: payload.occurrences || [],
    sessions: payload.sessions || [],
    screenshots: payload.screenshots || [],
    reviews: payload.reviews || [],
    quizzes: payload.quizzes || [],
    updatedAt: Date.now()
  };

  if (expRev !== null && expRev !== undefined) {
    fullPayload.expectedRevision = expRev;
  }

  let res;
  try {
    res = await makeRequest(endpoint, {
      method: 'POST',
      headers: {
        'X-Family-Code': code,
        'X-Sender-Role': senderRole,
        ...(config.adminToken ? { 'X-Admin-Token': config.adminToken } : {})
      }
    }, fullPayload);
  } catch (err) {
    handleConflictError(err);
    throw err;
  }

  if (!res.success) {
    throw new Error(res.error || 'Buluta yükleme başarısız');
  }

  const serverRev = extractServerRevision(res);
  if (serverRev !== null) {
    setLastKnownServerRevision(serverRev);
  }

  return res.data;
}

/**
 * Cloudflare KV'den 24 saatlik önceki durum yedeğini (snapshot) geri yükler
 */
async function restoreFamilyData(overrideCode = null, options = {}) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || '').toUpperCase().trim();
  const endpoint = `${config.workerUrl}/api/sync?code=${encodeURIComponent(code)}`;

  let expRev;
  if (typeof options === 'number' || (options && options.expectedRevision !== undefined)) {
    const rawRev = typeof options === 'number' ? options : options.expectedRevision;
    expRev = parseRevision(rawRev);
  } else if (options && options.revision !== undefined) {
    expRev = parseRevision(options.revision);
  } else {
    expRev = getLastKnownServerRevision();
  }

  const restorePayload = {
    familyCode: code,
    senderRole: 'PARENT',
    action: 'RESTORE'
  };

  if (expRev !== null && expRev !== undefined) {
    restorePayload.expectedRevision = expRev;
  }

  let res;
  try {
    res = await makeRequest(endpoint, {
      method: 'POST',
      headers: {
        'X-Family-Code': code,
        'X-Sender-Role': 'PARENT',
        ...(config.adminToken ? { 'X-Admin-Token': config.adminToken } : {})
      }
    }, restorePayload);
  } catch (err) {
    handleConflictError(err);
    throw err;
  }

  if (!res.success) {
    throw new Error(res.error || 'Geri alma başarısız');
  }

  const serverRev = extractServerRevision(res);
  if (serverRev !== null) {
    setLastKnownServerRevision(serverRev);
  }

  return res.data;
}

/**
 * Öğrenciye anlık bildirim / motivasyon mesajı gönderir
 */
async function sendNotification(overrideCode, messagePayload) {
  const config = loadConfig();
  const code = (overrideCode || messagePayload.familyCode || config.familyCode || '').toUpperCase().trim();
  const endpoint = `${config.workerUrl}/api/messages`;

  return await makeRequest(endpoint, {
    method: 'POST',
    headers: {
      'X-Family-Code': code,
      'X-Sender-Role': messagePayload.senderRole || 'PARENT',
      ...(config.adminToken ? { 'X-Admin-Token': config.adminToken } : {})
    }
  }, {
    familyCode: code,
    title: messagePayload.title || 'Ders Hatırlatması',
    message: messagePayload.message || '',
    type: messagePayload.type || 'REMINDER',
    senderRole: messagePayload.senderRole || 'PARENT'
  });
}

/**
 * Aile kodunu doğrular / oluşturur
 */
async function pairFamily(pairCode) {
  const config = loadConfig();
  const endpoint = `${config.workerUrl}/api/pair`;
  return await makeRequest(endpoint, {
    method: 'POST'
  }, { familyCode: pairCode });
}

module.exports = {
  fetchFamilyData,
  pushFamilyData,
  restoreFamilyData,
  sendNotification,
  pairFamily,
  extractServerRevision,
  handleConflictError,
  makeRequest
};

