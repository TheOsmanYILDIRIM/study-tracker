/**
 * StudyTracker V2 Doctor & Staging Diagnostic Tool
 * Inspects API v3 health, storage backend (KV vs D1), catalog stats,
 * attempt endpoint connectivity, and checks for seed drift without mutating state.
 */

const v2Api = require('./v2-api');
const { diffSeed, validateSeed, loadCatalogManifest } = require('./v2-seed');

async function runV2Doctor(familyCode, { seedFile } = {}) {
  const diagnostics = {
    timestamp: new Date().toISOString(),
    familyCode,
    overallHealthy: true,
    checks: {},
    catalogSummary: {},
    seedDrift: {},
    recommendations: []
  };

  // 1. Check /api/v3/health
  try {
    const healthRes = await v2Api.checkV2Health(familyCode);
    diagnostics.checks.healthEndpoint = {
      status: 'PASS',
      backend: healthRes.storageBackend || 'unknown',
      version: healthRes.version || '2.0.0',
      schemaVersion: healthRes.schemaVersion || 'v2'
    };
    diagnostics.storageBackend = healthRes.storageBackend || 'unknown';
  } catch (err) {
    diagnostics.overallHealthy = false;
    diagnostics.checks.healthEndpoint = {
      status: 'FAIL',
      error: err.message
    };
    diagnostics.recommendations.push('Cloudflare Worker v3 health endpoint unreachable. Ensure worker is running.');
  }

  // 2. Check Catalog Summary
  try {
    const catalogRes = await v2Api.fetchV2Catalog(familyCode, null);
    const curriculum = catalogRes.curriculum || [];
    
    let lessonCount = 0;
    let itemCount = 0;
    let activeItems = 0;
    let draftItems = 0;
    let archivedItems = 0;
    let videoCount = 0;
    let quizCount = 0;
    let ankiCount = 0;
    let reviewedCount = 0;

    for (const course of curriculum) {
      for (const lesson of course.lessons || []) {
        lessonCount++;
        for (const item of lesson.items || []) {
          itemCount++;
          const pubStatus = item.publishingStatus || (item.isArchived ? 'archived' : 'active');
          if (pubStatus === 'active') activeItems++;
          else if (pubStatus === 'draft') draftItems++;
          else if (pubStatus === 'archived') archivedItems++;

          if (item.itemType === 'VIDEO') videoCount++;
          else if (item.itemType === 'QUIZ') quizCount++;
          else if (item.itemType === 'ANKI') ankiCount++;

          const prov = item.currentVersion?.payload?.provenance || {};
          if (prov.reviewStatus === 'verified' || prov.reviewedOverride) {
            reviewedCount++;
          }
        }
      }
    }

    diagnostics.checks.catalog = {
      status: 'PASS',
      courseCount: curriculum.length,
      lessonCount,
      itemCount
    };

    diagnostics.catalogSummary = {
      courseCount: curriculum.length,
      lessonCount,
      itemCount,
      activeItems,
      draftItems,
      archivedItems,
      videoCount,
      quizCount,
      ankiCount,
      reviewedCount
    };
  } catch (err) {
    diagnostics.overallHealthy = false;
    diagnostics.checks.catalog = {
      status: 'FAIL',
      error: err.message
    };
    diagnostics.recommendations.push('Unable to fetch curriculum catalog from server.');
  }

  // 3. Check Attempts Endpoint
  try {
    const attemptsRes = await v2Api.fetchV2Attempts(familyCode, { limit: 1 });
    diagnostics.checks.attempts = {
      status: 'PASS',
      endpointReady: true,
      reportedAttempts: Array.isArray(attemptsRes.attempts) ? attemptsRes.attempts.length : 0
    };
  } catch (err) {
    diagnostics.overallHealthy = false;
    diagnostics.checks.attempts = {
      status: 'FAIL',
      error: err.message
    };
    diagnostics.recommendations.push('Attempts endpoint failed or returned non-200.');
  }

  // 4. Check Seed Drift (Dry-run diff without writes)
  try {
    const manifest = loadCatalogManifest(seedFile);
    const validation = validateSeed(manifest);
    const diff = await diffSeed(familyCode, manifest);

    const hasDrift = diff.summary.coursesToCreate > 0 ||
                     diff.summary.lessonsToCreate > 0 ||
                     diff.summary.itemsToCreate > 0 ||
                     diff.summary.itemsToUpdateContent > 0;

    diagnostics.checks.seedDrift = {
      status: hasDrift ? 'DRIFT_DETECTED' : 'IN_SYNC',
      manifestValid: validation.valid,
      summary: diff.summary
    };

    diagnostics.seedDrift = {
      inSync: !hasDrift,
      coursesToCreate: diff.summary.coursesToCreate,
      lessonsToCreate: diff.summary.lessonsToCreate,
      itemsToCreate: diff.summary.itemsToCreate,
      itemsToUpdate: diff.summary.itemsToUpdateContent,
      reviewedOverridesPreserved: diff.summary.reviewedOverridesPreserved || 0,
      extraRemoteItems: diff.summary.extraRemoteItems
    };

    if (hasDrift) {
      diagnostics.recommendations.push(
        `Seed drift detected (${diff.summary.itemsToCreate} to create, ${diff.summary.itemsToUpdateContent} to update). Run "studytracker-cli v2 seed apply" to synchronize without overwriting manual review overrides.`
      );
    }
  } catch (err) {
    diagnostics.checks.seedDrift = {
      status: 'ERROR',
      error: err.message
    };
    diagnostics.recommendations.push(`Seed drift check error: ${err.message}`);
  }

  return diagnostics;
}

module.exports = {
  runV2Doctor
};
