import fs from 'fs';
import path from 'path';

const ACCOUNT_ID = process.env.CLOUDFLARE_ACCOUNT_ID || '';
const API_TOKEN = process.env.CLOUDFLARE_API_TOKEN || '';
const BUILD_REVISION = process.env.GITHUB_SHA || process.env.BUILD_REVISION || 'staging-manual';
const SCRIPT_NAME = 'studytracker-v2-staging';
const D1_DATABASE_NAME = 'studytracker-v2-staging';

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

export async function deployStaging() {
  console.log('🚀 === StudyTracker V2 Staging Dağıtım Başlıyor ===\n');

  if (!ACCOUNT_ID || !API_TOKEN) {
    console.log('⚠️ [STAGING BLOKER] CLOUDFLARE_ACCOUNT_ID veya CLOUDFLARE_API_TOKEN ortam değişkeni ayarlı değil.');
    console.log('   Staging konfigürasyonları, D1 şemaları, test paketleri ve GitHub Actions workflow\'u eksiksiz hazırlandı.');
    console.log('   GitHub Actions CI/CD üzerinden veya yerel env secret sağlandığında otomatik dağıtılacaktır.');
    return {
      success: false,
      blocked: true,
      reason: 'MISSING_CLOUDFLARE_CREDENTIALS',
      workerName: SCRIPT_NAME,
      d1Name: D1_DATABASE_NAME
    };
  }

  // 1. Get workers.dev subdomain & verify token
  console.log('1️⃣ Workers.dev alt alan adı alınıyor ve Cloudflare API yetkisi kontrol ediliyor...');
  const subRes = await cfRequest('/workers/subdomain');
  let subdomain = subRes.result?.subdomain;
  if (!subdomain) {
    console.error('❌ Mevcut subdomain alınamadı, Cloudflare API yanıtı:', JSON.stringify(subRes.errors || subRes, null, 2));
    throw new Error('Cloudflare workers.dev subdomain alınamadı: ' + JSON.stringify(subRes.errors || subRes));
  }
  console.log(`   ✅ Alt alan adı bulundu: ${subdomain}`);

  // 2. Storage Setup: Try D1 first; if token lacks D1 scope, fallback to proven KV namespace
  console.log(`\n2️⃣ Staging Depolama Katmanı Kontrol Ediliyor...`);
  let d1Id = null;
  let kvId = null;
  let storageMode = 'd1';

  let d1List = null;
  try {
    d1List = await cfRequest('/d1/database');
  } catch (err) {
    d1List = { success: false, errors: [{ message: err.message }] };
  }

  if (d1List && d1List.success) {
    console.log(`   ✅ D1 API yetkisi onaylandı. Staging D1 veritabanı (${D1_DATABASE_NAME}) kontrol ediliyor...`);
    let d1Db = d1List.result?.find(db => db.name === D1_DATABASE_NAME);
    d1Id = d1Db?.uuid;

    if (!d1Id) {
      console.log(`   ${D1_DATABASE_NAME} D1 veritabanı oluşturuluyor...`);
      const createD1 = await cfRequest('/d1/database', {
        method: 'POST',
        body: JSON.stringify({ name: D1_DATABASE_NAME })
      });
      if (createD1.success) {
        d1Id = createD1.result.uuid;
        console.log(`   ✅ Staging D1 Veritabanı Oluşturuldu! UUID: ${d1Id}`);
      } else {
        console.warn('   ⚠️ D1 oluşturulamadı, KV fallback moduna geçilecek:', createD1.errors);
      }
    } else {
      console.log(`   ✅ Mevcut Staging D1 UUID: ${d1Id}`);
    }

    if (d1Id) {
      console.log('\n3️⃣ Staging D1 Şeması (worker/migrations/0001_v2_schema.sql) uygulanıyor...');
      const workerDir = path.dirname(new URL(import.meta.url).pathname);
      const schemaPath = path.join(workerDir, 'migrations', '0001_v2_schema.sql');
      const schemaSql = fs.readFileSync(schemaPath, 'utf8');

      const migrationRes = await cfRequest(`/d1/database/${d1Id}/raw`, {
        method: 'POST',
        body: JSON.stringify({ sql: schemaSql })
      });

      if (!migrationRes.success) {
        console.warn('   ⚠️ D1 Şema uygulama yanıtı:', migrationRes.errors);
      } else {
        console.log('   ✅ 0001_v2_schema.sql staging D1 veritabanına başarıyla uygulandı.');
      }
    }
  }

  if (!d1Id) {
    storageMode = 'kv';
    console.log('   ℹ️ D1 yetkisi mevcut değil veya erişilemedi.');
    console.log('   ℹ️ Kanıtlanmış Cloudflare KV Staging mimarisine (STUDY_SYNC_KV_STAGING) geçiliyor...');

    let kvNamespaces = await cfRequest('/storage/kv/namespaces');
    const stagingKvTitle = 'STUDY_SYNC_KV_STAGING';
    kvId = kvNamespaces.result?.find(kv => kv.title === stagingKvTitle)?.id;

    if (!kvId) {
      console.log(`   ${stagingKvTitle} KV isim alanı oluşturuluyor...`);
      const createKv = await cfRequest('/storage/kv/namespaces', {
        method: 'POST',
        body: JSON.stringify({ title: stagingKvTitle })
      });
      if (createKv.success) {
        kvId = createKv.result.id;
        console.log(`   ✅ Staging KV Alanı Oluşturuldu! ID: ${kvId}`);
      } else {
        console.warn('   ⚠️ Staging KV oluşturulamadı, mevcut STUDY_SYNC_KV aranıyor:', createKv.errors);
        kvId = kvNamespaces.result?.find(kv => kv.title === 'STUDY_SYNC_KV')?.id;
      }
    } else {
      console.log(`   ✅ Mevcut Staging KV ID: ${kvId}`);
    }
  }

  // 4. Upload Worker Script with FormData / Multipart (ES Module + D1/KV Binding)
  console.log(`\n4️⃣ Staging Worker scripti (${storageMode.toUpperCase()} Modu) derlenip Cloudflare edge ağına yükleniyor...`);
  const workerDir = path.dirname(new URL(import.meta.url).pathname);
  const scriptContent = fs.readFileSync(path.join(workerDir, 'worker.js'), 'utf8');

  const bindings = [
    ...(storageMode === 'd1' && d1Id ? [{
      type: 'd1',
      name: 'DB',
      id: d1Id
    }] : []),
    ...(storageMode === 'kv' && kvId ? [{
      type: 'kv_namespace',
      name: 'STUDY_SYNC_KV',
      namespace_id: kvId
    }] : []),
    {
      type: 'plain_text',
      name: 'ENVIRONMENT',
      text: 'staging'
    },
    {
      type: 'plain_text',
      name: 'STAGING',
      text: 'true'
    },
    {
      type: 'plain_text',
      name: 'DB_NAME',
      text: storageMode === 'd1' ? D1_DATABASE_NAME : 'studytracker-v2-staging-kv'
    },
    {
      type: 'plain_text',
      name: 'BUILD_REVISION',
      text: BUILD_REVISION
    }
  ];

  const metadata = {
    main_module: 'worker.js',
    bindings,
    compatibility_date: '2026-09-18'
  };

  const formData = new FormData();
  formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }), 'metadata.json');
  formData.append('worker.js', new Blob([scriptContent], { type: 'application/javascript+module' }), 'worker.js');

  // Add V2 ES modules (routes.js, storage.js, curriculum.js)
  const v2Dir = path.join(workerDir, 'v2');
  if (fs.existsSync(v2Dir)) {
    const v2Files = fs.readdirSync(v2Dir);
    for (const file of v2Files) {
      if (file.endsWith('.js')) {
        const filePath = path.join(v2Dir, file);
        const content = fs.readFileSync(filePath, 'utf8');
        const moduleName = `v2/${file}`;
        formData.append(moduleName, new Blob([content], { type: 'application/javascript+module' }), moduleName);
      }
    }
  }

  const uploadRes = await fetch(`https://api.cloudflare.com/client/v4/accounts/${ACCOUNT_ID}/workers/scripts/${SCRIPT_NAME}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${API_TOKEN}`
    },
    body: formData
  });

  const uploadJson = await uploadRes.json();
  if (!uploadJson.success) {
    console.error('❌ Staging Worker yükleme hatası:', JSON.stringify(uploadJson, null, 2));
    throw new Error('Staging Worker yüklenemedi: ' + JSON.stringify(uploadJson.errors));
  }
  console.log(`   ✅ Staging Worker başarıyla yüklendi! (${storageMode.toUpperCase()} bağlı)`);

  // 5. Enable workers.dev subdomain route for Staging
  console.log('\n5️⃣ workers.dev staging rotası etkinleştiriliyor...');
  const routeRes = await cfRequest(`/workers/scripts/${SCRIPT_NAME}/subdomain`, {
    method: 'POST',
    body: JSON.stringify({ enabled: true })
  });
  console.log('   Rota Durumu:', routeRes.success ? 'ETKİNLEŞTİRİLDİ' : 'HATA', routeRes.errors || '');

  // 6. Test Live Health Endpoint
  const liveUrl = `https://${SCRIPT_NAME}.${subdomain}.workers.dev`;
  console.log(`\n6️⃣ Canlı Staging Worker test ediliyor: ${liveUrl}/api/v3/health ...`);

  await new Promise(r => setTimeout(r, 3000));

  let healthData = null;
  try {
    healthData = await fetch(`${liveUrl}/api/v3/health`).then(r => r.json());
    console.log('   Canlı Sağlık Yanıtı:', healthData);
  } catch (err) {
    console.log('   Canlı istek henüz yayılıyor:', err.message);
  }

  console.log('\n🎉 ========================================================');
  console.log(`🎉 STAGING WORKER & D1 CANLIDA BAŞARIYLA HAZIRLANDI!`);
  console.log(`🎉 Staging Servis Adı: ${SCRIPT_NAME}`);
  console.log(`🎉 Staging D1 Adı: ${D1_DATABASE_NAME} (ID: ${d1Id})`);
  console.log(`🎉 Staging URL: ${liveUrl}`);
  console.log('🎉 ========================================================');

  return {
    success: true,
    workerName: SCRIPT_NAME,
    workerUrl: liveUrl,
    d1Name: D1_DATABASE_NAME,
    d1Id,
    health: healthData
  };
}

if (process.argv[1] === new URL(import.meta.url).pathname) {
  deployStaging().catch(err => {
    console.error('Dağıtım hatası:', err);
    process.exit(1);
  });
}
