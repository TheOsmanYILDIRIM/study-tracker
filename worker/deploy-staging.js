import fs from 'fs';
import path from 'path';

const ACCOUNT_ID = process.env.CLOUDFLARE_ACCOUNT_ID || '';
const API_TOKEN = process.env.CLOUDFLARE_API_TOKEN || '';
const BUILD_REVISION = process.env.GITHUB_SHA || process.env.BUILD_REVISION || 'staging-manual';
const SCRIPT_NAME = 'studytracker-v2-staging';
const KV_NAMESPACE_TITLE = 'STUDY_SYNC_KV_STAGING';

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
  const payload = await res.json();
  if (!res.ok || payload.success === false) {
    const message = payload.errors?.map(e => e.message).join('; ') || `HTTP ${res.status}`;
    throw new Error(message);
  }
  return payload;
}

async function ensureStagingKv() {
  const listed = await cfRequest('/storage/kv/namespaces');
  let namespace = listed.result?.find(kv => kv.title === KV_NAMESPACE_TITLE);

  if (!namespace) {
    const created = await cfRequest('/storage/kv/namespaces', {
      method: 'POST',
      body: JSON.stringify({ title: KV_NAMESPACE_TITLE })
    });
    namespace = created.result;
  }

  if (!namespace?.id) {
    throw new Error('Dedicated staging KV namespace could not be resolved');
  }

  return namespace.id;
}

export async function deployStaging() {
  console.log('🚀 === StudyTracker V2 KV Staging Deploy ===');

  if (!ACCOUNT_ID || !API_TOKEN) {
    console.log('⚠️ Cloudflare credentials are not configured; deploy skipped.');
    return {
      success: false,
      blocked: true,
      reason: 'MISSING_CLOUDFLARE_CREDENTIALS',
      workerName: SCRIPT_NAME,
      kvNamespace: KV_NAMESPACE_TITLE
    };
  }

  const subRes = await cfRequest('/workers/subdomain');
  const subdomain = subRes.result?.subdomain;
  if (!subdomain) throw new Error('Cloudflare workers.dev subdomain is unavailable');

  const kvId = await ensureStagingKv();
  console.log(`✅ Dedicated staging KV ready: ${KV_NAMESPACE_TITLE}`);

  const workerDir = path.dirname(new URL(import.meta.url).pathname);
  const scriptContent = fs.readFileSync(path.join(workerDir, 'worker.js'), 'utf8');

  const bindings = [
    {
      type: 'kv_namespace',
      name: 'STUDY_SYNC_KV',
      namespace_id: kvId
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
      name: 'STORAGE_BACKEND',
      text: 'kv'
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
  formData.append(
    'metadata',
    new Blob([JSON.stringify(metadata)], { type: 'application/json' }),
    'metadata.json'
  );
  formData.append(
    'worker.js',
    new Blob([scriptContent], { type: 'application/javascript+module' }),
    'worker.js'
  );

  const v2Dir = path.join(workerDir, 'v2');
  if (fs.existsSync(v2Dir)) {
    for (const file of fs.readdirSync(v2Dir)) {
      if (!file.endsWith('.js')) continue;
      const moduleName = `v2/${file}`;
      formData.append(
        moduleName,
        new Blob([fs.readFileSync(path.join(v2Dir, file), 'utf8')], {
          type: 'application/javascript+module'
        }),
        moduleName
      );
    }
  }

  const uploadRes = await fetch(
    `https://api.cloudflare.com/client/v4/accounts/${ACCOUNT_ID}/workers/scripts/${SCRIPT_NAME}`,
    {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${API_TOKEN}` },
      body: formData
    }
  );
  const uploadJson = await uploadRes.json();
  if (!uploadRes.ok || !uploadJson.success) {
    throw new Error(
      'Staging worker upload failed: ' +
      JSON.stringify(uploadJson.errors || uploadJson)
    );
  }

  await cfRequest(`/workers/scripts/${SCRIPT_NAME}/subdomain`, {
    method: 'POST',
    body: JSON.stringify({ enabled: true })
  });

  const liveUrl = `https://${SCRIPT_NAME}.${subdomain}.workers.dev`;
  let health = null;
  try {
    health = await fetch(`${liveUrl}/api/v3/health`).then(r => r.json());
  } catch (err) {
    console.warn('Health check is not available yet:', err.message);
  }

  if (health?.storageBackend && health.storageBackend !== 'kv') {
    throw new Error(
      `Canonical staging backend must be KV, got ${health.storageBackend}`
    );
  }

  console.log(`✅ StudyTracker V2 staging deployed with KV: ${liveUrl}`);
  return {
    success: true,
    workerName: SCRIPT_NAME,
    workerUrl: liveUrl,
    storageBackend: 'kv',
    kvNamespace: KV_NAMESPACE_TITLE,
    kvId,
    health
  };
}

if (process.argv[1] === new URL(import.meta.url).pathname) {
  deployStaging().catch(err => {
    console.error('Staging deploy failed:', err);
    process.exit(1);
  });
}
