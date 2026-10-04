#!/usr/bin/env node
/**
 * scripts/generate-video-audit.cjs
 * Generates content/v2/video-quality-audit.json
 */
const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
const ITEMS_DIR = path.join(ROOT, "content", "v2", "items");
const AUDIT_OUT = path.join(ROOT, "content", "v2", "video-quality-audit.json");
const MANIFEST_PATH = path.join(ROOT, "content", "v2", "transcripts", "manifest.json");

function generateAudit() {
  const files = fs.readdirSync(ITEMS_DIR).filter(f => f.endsWith(".json"));
  
  let totalVideos = 0;
  let activeCount = 0;
  let draftCount = 0;
  let noUrlCount = 0;
  
  const perCourse = {};
  const providerDistribution = {};
  const statusDistribution = {};
  const urlMap = {};
  const missingProvenance = [];
  const changedUrlVsTranscriptMismatches = [];
  let hasTranscriptPathCount = 0;
  let hasTranscriptFingerprintCount = 0;

  for (const f of files) {
    const filePath = path.join(ITEMS_DIR, f);
    const item = JSON.parse(fs.readFileSync(filePath, "utf8"));
    if (item.itemType !== "VIDEO") continue;

    totalVideos++;
    const courseId = item.courseId || "unknown";
    if (!perCourse[courseId]) {
      perCourse[courseId] = { total: 0, active: 0, draft: 0, noUrl: 0, itemIds: [] };
    }
    perCourse[courseId].total++;
    perCourse[courseId].itemIds.push(item.id);

    const status = item.publishingStatus || "draft";
    statusDistribution[status] = (statusDistribution[status] || 0) + 1;
    if (status === "active") perCourse[courseId].active++;
    else if (status === "draft") perCourse[courseId].draft++;

    const url = (item.contentUrl || "").trim();
    if (!url) {
      noUrlCount++;
      perCourse[courseId].noUrl++;
    } else {
      if (!urlMap[url]) {
        urlMap[url] = [];
      }
      urlMap[url].push({
        id: item.id,
        courseId: item.courseId,
        lessonId: item.lessonId,
        title: item.title,
        status: item.publishingStatus
      });
    }

    const provider = item.payload?.provider || "unassigned";
    providerDistribution[provider] = (providerDistribution[provider] || 0) + 1;

    const prov = item.payload?.provenance || {};
    if (!prov.schemaVersion || !prov.reviewStatus || !prov.fingerprint) {
      missingProvenance.push({
        id: item.id,
        missingFields: [
          !prov.schemaVersion && "schemaVersion",
          !prov.reviewStatus && "reviewStatus",
          !prov.fingerprint && "fingerprint"
        ].filter(Boolean)
      });
    }

    if (prov.transcriptPath) hasTranscriptPathCount++;
    if (prov.transcriptFingerprint) hasTranscriptFingerprintCount++;

    if (url && prov.sourceVideoUrl && prov.sourceVideoUrl !== url) {
      changedUrlVsTranscriptMismatches.push({
        id: item.id,
        contentUrl: url,
        sourceVideoUrl: prov.sourceVideoUrl,
        transcriptPath: prov.transcriptPath,
        transcriptFingerprint: prov.transcriptFingerprint
      });
    }
  }

  const duplicateUrlGroups = [];
  for (const [url, items] of Object.entries(urlMap)) {
    if (items.length > 1) {
      const courses = Array.from(new Set(items.map(i => i.courseId)));
      duplicateUrlGroups.push({
        url,
        count: items.length,
        courses,
        items
      });
    }
  }
  duplicateUrlGroups.sort((a, b) => b.count - a.count);

  let manifestData = null;
  if (fs.existsSync(MANIFEST_PATH)) {
    try {
      manifestData = JSON.parse(fs.readFileSync(MANIFEST_PATH, "utf8"));
    } catch {}
  }

  const audit = {
    generatedAt: new Date().toISOString(),
    totalVideos,
    uniqueUrls: Object.keys(urlMap).length,
    noUrlCount,
    statusDistribution,
    providerDistribution,
    perCourse,
    transcriptCoverage: {
      hasTranscriptPathCount,
      hasTranscriptFingerprintCount,
      missingTranscriptCount: totalVideos - hasTranscriptFingerprintCount,
      manifestSummary: manifestData ? {
        fetched: manifestData.fetched,
        reused: manifestData.reused,
        missing: manifestData.missing,
        failed: manifestData.failed,
        youtubeVideos: manifestData.youtubeVideos
      } : null
    },
    missingProvenanceCount: missingProvenance.length,
    missingProvenance,
    changedUrlVsTranscriptMismatchesCount: changedUrlVsTranscriptMismatches.length,
    changedUrlVsTranscriptMismatches,
    duplicateUrlGroupsCount: duplicateUrlGroups.length,
    duplicateUrlGroups
  };

  fs.writeFileSync(AUDIT_OUT, JSON.stringify(audit, null, 2) + "\n");
  console.log(`Audit written to ${path.relative(ROOT, AUDIT_OUT)}`);
  console.log(`- Total Videos: ${totalVideos}`);
  console.log(`- Unique URLs: ${audit.uniqueUrls}`);
  console.log(`- Duplicate URL Groups: ${duplicateUrlGroups.length}`);
  console.log(`- Changed URL vs Transcript Mismatches: ${changedUrlVsTranscriptMismatches.length}`);
  console.log(`- Has Transcript Fingerprint: ${hasTranscriptFingerprintCount}/${totalVideos}`);
  return audit;
}

if (require.main === module) {
  generateAudit();
}

module.exports = { generateAudit };
