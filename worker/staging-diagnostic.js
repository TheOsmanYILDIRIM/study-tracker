import process from 'process';

/**
 * Minimal Safe Cloudflare Staging Diagnostic
 * 
 * Safely verifies Cloudflare credentials and permissions without exposing secrets.
 * Outputs HTTP status, Cloudflare success boolean, error codes/messages,
 * and masked account match evidence.
 * 
 * NEVER prints token values or authorization headers.
 */
async function runDiagnostic() {
  const API_TOKEN = process.env.CLOUDFLARE_API_TOKEN || '';
  const ACCOUNT_ID = process.env.CLOUDFLARE_ACCOUNT_ID || '';

  console.log('🔍 ========================================================');
  console.log('🔍 CLOUDFLARE STAGING SAFE DIAGNOSTIC');
  console.log('🔍 ========================================================');

  const tokenConfigured = Boolean(API_TOKEN && API_TOKEN.trim().length > 0);
  const accountConfigured = Boolean(ACCOUNT_ID && ACCOUNT_ID.trim().length > 0);

  console.log('\n1️⃣ Environment Secrets Presence:');
  console.log(`   - CLOUDFLARE_API_TOKEN configured: ${tokenConfigured} (length: ${API_TOKEN.length})`);
  console.log(`   - CLOUDFLARE_ACCOUNT_ID configured: ${accountConfigured} (length: ${ACCOUNT_ID.length})`);

  if (accountConfigured) {
    const masked = ACCOUNT_ID.length >= 8
      ? `${ACCOUNT_ID.slice(0, 4)}...${ACCOUNT_ID.slice(-4)}`
      : '***';
    console.log(`   - Masked Account Identifier: ${masked}`);
  }

  if (!tokenConfigured || !accountConfigured) {
    console.log('⚠️ Credentials missing in environment, skipping API diagnostic.');
    return;
  }

  // 1. Cloudflare Token Verify Endpoint
  console.log('\n2️⃣ Cloudflare Token Verification (GET /user/tokens/verify):');
  try {
    const tokenRes = await fetch('https://api.cloudflare.com/client/v4/user/tokens/verify', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${API_TOKEN}`
      }
    });
    const tokenStatus = tokenRes.status;
    const tokenJson = await tokenRes.json().catch(() => ({}));
    console.log(`   - HTTP Status: ${tokenStatus}`);
    console.log(`   - Cloudflare Success: ${Boolean(tokenJson.success)}`);
    if (tokenJson.result) {
      console.log(`   - Token Status: ${tokenJson.result.status || 'N/A'}`);
      console.log(`   - Token ID (masked): ${tokenJson.result.id ? `${tokenJson.result.id.slice(0, 4)}...` : 'N/A'}`);
    }
    if (tokenJson.messages && tokenJson.messages.length > 0) {
      console.log(`   - Messages: ${JSON.stringify(tokenJson.messages)}`);
    }
    if (tokenJson.errors && tokenJson.errors.length > 0) {
      console.log(`   - Errors: ${JSON.stringify(tokenJson.errors)}`);
    }
  } catch (err) {
    console.log(`   - Request Error: ${err.message}`);
  }

  // 2. Workers Subdomain (Account Identifier Match Evidence)
  console.log('\n3️⃣ Cloudflare Workers Subdomain Check (GET /accounts/:id/workers/subdomain):');
  try {
    const subRes = await fetch(`https://api.cloudflare.com/client/v4/accounts/${ACCOUNT_ID}/workers/subdomain`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${API_TOKEN}`,
        'Content-Type': 'application/json'
      }
    });
    const subStatus = subRes.status;
    const subJson = await subRes.json().catch(() => ({}));
    console.log(`   - HTTP Status: ${subStatus}`);
    console.log(`   - Cloudflare Success: ${Boolean(subJson.success)}`);
    if (subJson.result?.subdomain) {
      console.log(`   - Subdomain Result: ${subJson.result.subdomain}`);
      console.log(`   - Account Match Evidence: Token is valid and successfully authorized for Account ID (${ACCOUNT_ID.slice(0, 4)}...${ACCOUNT_ID.slice(-4)}) on Workers scope.`);
    }
    if (subJson.errors && subJson.errors.length > 0) {
      console.log(`   - Errors: ${JSON.stringify(subJson.errors)}`);
    }
  } catch (err) {
    console.log(`   - Request Error: ${err.message}`);
  }

  // 3. Cloudflare D1 Database List Endpoint
  console.log('\n4️⃣ Cloudflare D1 Database List (GET /accounts/:id/d1/database):');
  try {
    const d1Res = await fetch(`https://api.cloudflare.com/client/v4/accounts/${ACCOUNT_ID}/d1/database`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${API_TOKEN}`,
        'Content-Type': 'application/json'
      }
    });
    const d1Status = d1Res.status;
    const d1Json = await d1Res.json().catch(() => ({}));
    console.log(`   - HTTP Status: ${d1Status}`);
    console.log(`   - Cloudflare Success: ${Boolean(d1Json.success)}`);
    if (d1Json.errors && d1Json.errors.length > 0) {
      console.log(`   - Errors: ${JSON.stringify(d1Json.errors)}`);
    }
    if (d1Json.messages && d1Json.messages.length > 0) {
      console.log(`   - Messages: ${JSON.stringify(d1Json.messages)}`);
    }
    if (d1Json.success && Array.isArray(d1Json.result)) {
      console.log(`   - Databases Count: ${d1Json.result.length}`);
      console.log(`   - Databases Found: ${JSON.stringify(d1Json.result.map(d => ({ name: d.name, uuid: d.uuid })))}`);
    }
  } catch (err) {
    console.log(`   - Request Error: ${err.message}`);
  }

  console.log('\n🔍 ========================================================');
}

runDiagnostic().catch(err => {
  console.error('Diagnostic error:', err.message);
});
