const https = require('https');
const http = require('http');
const { URL } = require('url');
const { loadConfig } = require('./config');

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
            reject(new Error(`HTTP ${res.statusCode}: ${data}`));
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

/**
 * Cloudflare KV'den mevcut aile verilerini çeker
 */
async function fetchFamilyData(overrideCode = null) {
  const config = loadConfig();
  const code = (overrideCode || config.familyCode || 'ST-2026').toUpperCase().trim();
  const endpoint = `${config.workerUrl}/api/sync?code=${encodeURIComponent(code)}`;

  const res = await makeRequest(endpoint, {
    method: 'GET',
    headers: { 'X-Family-Code': code }
  });

  if (!res.success) {
    throw new Error(res.error || 'Buluttan veri çekilemedi');
  }

  return res.data || {
    familyCode: code,
    plan: null,
    tasks: [],
    occurrences: [],
    sessions: [],
    reviews: [],
    quizzes: []
  };
}

/**
 * Cloudflare KV'ye güncel aile verilerini yükler (senderRole: "PARENT" veya "CHILD")
 */
async function pushFamilyData(payload, overrideCode = null, senderRole = 'PARENT') {
  const config = loadConfig();
  const code = (overrideCode || payload.familyCode || config.familyCode || 'ST-2026').toUpperCase().trim();
  const endpoint = `${config.workerUrl}/api/sync?code=${encodeURIComponent(code)}`;

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

  const res = await makeRequest(endpoint, {
    method: 'POST',
    headers: {
      'X-Family-Code': code,
      'X-Sender-Role': senderRole
    }
  }, fullPayload);

  if (!res.success) {
    throw new Error(res.error || 'Buluta yükleme başarısız');
  }

  return res.data;
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
  pairFamily
};
