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

  // 2. Create or Get Staging D1 Database with explicit scope diagnostics
  console.log(`\n2️⃣ Staging D1 Veritabanı (${D1_DATABASE_NAME}) kontrol ediliyor...`);
  let d1List = await cfRequest('/d1/database');
  if (!d1List.success) {
    const isAuthError = d1List.errors?.some(e => e.code === 10000);
    if (isAuthError) {
      console.error('❌ [YETKİ EKSİKLİĞİ] Cloudflare D1 Authentication Error (Code 10000):');
      console.error('   Kullanılan API Token "Account -> D1 -> Edit" iznine sahip değil.');
      console.error('   Çözüm: Cloudflare Dashboard > My Profile > API Tokens > Edit Token bölümünden "Account -> D1 -> Edit" iznini ekleyin.');
    } else {
      console.error('❌ D1 veritabanı listelenemedi:', JSON.stringify(d1List.errors, null, 2));
    }
    throw new Error('Staging D1 listeleme hatası: ' + JSON.stringify(d1List.errors));
  }

  let d1Db = d1List.result?.find(db => db.name === D1_DATABASE_NAME);
  let d1Id = d1Db?.uuid;

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
      const isAuthError = createD1.errors?.some(e => e.code === 10000);
      if (isAuthError) {
        console.error('❌ [YETKİ EKSİKLİĞİ] Cloudflare D1 Authentication Error (Code 10000):');
        console.error('   Mevcut API Token "Account -> D1 -> Edit" iznine sahip değil.');
        console.error('   Çözüm: Cloudflare Dashboard > My Profile > API Tokens > Edit Token bölümünden "Account -> D1 -> Edit" iznini ekleyin.');
      } else {
        console.error('❌ D1 veritabanı oluşturulamadı:', createD1.errors);
      }
      throw new Error('Staging D1 oluşturma hatası: ' + JSON.stringify(createD1.errors));
    }
  } else {
    console.log(`   ✅ Mevcut Staging D1 UUID: ${d1Id}`);
  }

  // 3. Apply Schema Migration (0001_v2_schema.sql) to Staging D1
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

  // 4. Upload Worker Script with FormData / Multipart (ES Module + D1 Binding)
  console.log('\n4️⃣ Staging Worker scripti derlenip Cloudflare edge ağına yükleniyor...');
  const scriptContent = fs.readFileSync(path.join(workerDir, 'worker.js'), 'utf8');

  const metadata = {
    main_module: 'worker.js',
    bindings: [
      {
        type: 'd1',
        name: 'DB',
        id: d1Id
      },
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
        text: D1_DATABASE_NAME
      },
      {
        type: 'plain_text',
        name: 'BUILD_REVISION',
        text: BUILD_REVISION
      }
    ],
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
    console.error('❌ Staging Worker yükleme hatası:', JSON.stringify(uploadJson, null, 2));
    throw new Error('Staging Worker yüklenemedi: ' + JSON.stringify(uploadJson.errors));
  }
  console.log('   ✅ Staging Worker başarıyla yüklendi!');

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
