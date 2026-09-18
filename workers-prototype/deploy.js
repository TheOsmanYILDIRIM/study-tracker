import fs from 'fs';
import path from 'path';

const ACCOUNT_ID = process.env.CLOUDFLARE_ACCOUNT_ID || '988ce42497272fb90cec4edd3c76d5a2';
const API_TOKEN = process.env.CLOUDFLARE_API_TOKEN || '';
const SCRIPT_NAME = 'studytracker-sync';

async function cfRequest(endpoint, options = {}) {
  const url = `https://api.cloudflare.com/client/v4/accounts/${ACCOUNT_ID}${endpoint}`;
  const res = await fetch(url, {
    ...options,
    headers: {
      'Authorization': `Bearer ${API_TOKEN}`,
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });
  const json = await res.json();
  return json;
}

async function main() {
  console.log('🚀 === Cloudflare Worker Dağıtım Başlıyor ===\n');

  // 1. Get workers.dev subdomain
  console.log('1️⃣ Workers.dev alt alan adı alınıyor...');
  const subRes = await cfRequest('/workers/subdomain');
  let subdomain = subRes.result?.subdomain;
  if (!subdomain) {
    console.log('   Mevcut subdomain bulunamadı, yanıt:', subRes);
  } else {
    console.log(`   ✅ Alt alan adı bulundu: ${subdomain}`);
  }

  // 2. Create or Get KV Namespace
  console.log('\n2️⃣ STUDY_SYNC_KV isim alanı kontrol ediliyor...');
  let kvNamespaces = await cfRequest('/storage/kv/namespaces');
  let kvId = kvNamespaces.result?.find(kv => kv.title === 'STUDY_SYNC_KV')?.id;

  if (!kvId) {
    console.log('   STUDY_SYNC_KV oluşturuluyor...');
    const createKv = await cfRequest('/storage/kv/namespaces', {
      method: 'POST',
      body: JSON.stringify({ title: 'STUDY_SYNC_KV' })
    });
    if (createKv.success) {
      kvId = createKv.result.id;
      console.log(`   ✅ KV Alanı Oluşturuldu! ID: ${kvId}`);
    } else {
      console.log('   ⚠️ KV oluşturulamadı:', createKv.errors);
    }
  } else {
    console.log(`   ✅ Mevcut KV ID: ${kvId}`);
  }

  // 3. Upload Worker Script with FormData / Multipart (ES Module)
  console.log('\n3️⃣ Worker scripti derlenip Cloudflare edge ağına yükleniyor...');
  const workerDir = path.dirname(new URL(import.meta.url).pathname);
  const scriptContent = fs.readFileSync(path.join(workerDir, 'worker.js'), 'utf8');

  const metadata = {
    main_module: 'worker.js',
    bindings: kvId ? [
      {
        type: 'kv_namespace',
        name: 'STUDY_SYNC_KV',
        namespace_id: kvId
      }
    ] : [],
    compatibility_date: '2026-09-18'
  };

  const formData = new FormData();
  formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }), 'metadata.json');
  formData.append('worker.js', new Blob([scriptContent], { type: 'application/javascript+module' }), 'worker.js');

  const uploadRes = await fetch(`https://api.cloudflare.com/client/v4/accounts/${ACCOUNT_ID}/workers/scripts/${SCRIPT_NAME}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${API_TOKEN}`
    },
    body: formData
  });

  const uploadJson = await uploadRes.json();
  if (!uploadJson.success) {
    console.error('❌ Worker yükleme hatası:', JSON.stringify(uploadJson, null, 2));
    process.exit(1);
  }
  console.log('   ✅ Worker başarıyla yüklendi!');

  // 4. Enable workers.dev subdomain route
  console.log('\n4️⃣ workers.dev rotası etkinleştiriliyor...');
  const routeRes = await cfRequest(`/workers/scripts/${SCRIPT_NAME}/subdomain`, {
    method: 'POST',
    body: JSON.stringify({ enabled: true })
  });
  console.log('   Rota Durumu:', routeRes.success ? 'ETKİNLEŞTİRİLDİ' : 'HATA', routeRes.errors || '');

  // 5. Test Live Endpoint
  const liveUrl = `https://${SCRIPT_NAME}.${subdomain}.workers.dev`;
  console.log(`\n5️⃣ Canlı Worker test ediliyor: ${liveUrl}/api/ping ...`);
  
  // Wait 3 seconds for Cloudflare edge propagation
  await new Promise(r => setTimeout(r, 3000));

  try {
    const livePing = await fetch(`${liveUrl}/api/ping`).then(r => r.json());
    console.log('   Canlı Yanıt:', livePing);
    console.log('\n🎉 ========================================================');
    console.log(`🎉 CLOUDFLARE WORKER CANLIDA BAŞARIYLA ÇALIŞIYOR!`);
    console.log(`🎉 Canlı Senkronizasyon URL'i: ${liveUrl}`);
    console.log('🎉 ========================================================');
  } catch (err) {
    console.log('   Canlı istek henüz yayılıyor:', err.message);
  }
}

main().catch(console.error);
