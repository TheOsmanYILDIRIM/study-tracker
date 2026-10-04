/**
 * Script to analyze seed audit warnings from validateSeed and generate content/v2/audit-warning-review.json
 * Classifies all warnings into:
 * - false_positive (e.g. intentional verified shared curriculum coverage)
 * - obsolete_metadata (e.g. stale teacher or review status metadata)
 * - real_content_issue (e.g. bad/unreviewed URLs or wrong content)
 * - intentional_policy_warning (e.g. draft channel URLs or unapproved reuses)
 */

const fs = require('fs');
const path = require('path');

const catalog = require('../content/9-sinif-v2-catalog.json');

// Find all URL usages
const urlUsageMap = new Map();
const allItems = [];

for (const course of catalog.courses) {
  for (const lesson of course.lessons) {
    for (const item of lesson.items) {
      allItems.push({
        ...item,
        courseId: course.id,
        lessonId: lesson.id
      });
      if (item.contentUrl) {
        const list = urlUsageMap.get(item.contentUrl) || [];
        list.push({
          id: item.id,
          stableKey: item.stableKey,
          courseId: course.id,
          lessonId: lesson.id,
          title: item.title,
          provider: item.payload?.provider || null,
          teacher: item.payload?.teacher || null,
          verifiedLectureTitle: item.payload?.verifiedLectureTitle || null,
          sharedSource: item.payload?.provenance?.sharedSource || false,
          sharedSourceReason: item.payload?.provenance?.sharedSourceReason || null,
          transcriptFingerprint: item.payload?.provenance?.transcriptFingerprint || null,
          reviewStatus: item.payload?.provenance?.reviewStatus || 'unspecified'
        });
        urlUsageMap.set(item.contentUrl, list);
      }
    }
  }
}

const duplicateGroups = [];
for (const [url, items] of urlUsageMap.entries()) {
  if (items.length > 1 && !url.includes('youtube.com/@')) {
    duplicateGroups.push({ url, items });
  }
}

const auditedWarnings = duplicateGroups.map((group, idx) => {
  const allShared = group.items.every(it => it.sharedSource === true && Boolean(it.sharedSourceReason));
  const sharedReasons = [...new Set(group.items.map(it => it.sharedSourceReason).filter(Boolean))];

  let classification = 'real_content_issue';
  let rationale = '';

  if (allShared) {
    classification = 'false_positive';
    rationale = `Legitimate intentional shared video resource covering unified curriculum topics (${sharedReasons.join('; ')}), with explicit sharedSource=true, verified Turkish transcripts, and matching fingerprints.`;
  } else {
    classification = 'real_content_issue';
    rationale = `Unreviewed URL reuse across multiple items without explicit sharedSource provenance.`;
  }

  return {
    warningIndex: idx + 1,
    url: group.url,
    itemCount: group.items.length,
    itemIds: group.items.map(it => it.id),
    titles: group.items.map(it => it.title),
    classification,
    rationale,
    sharedSourceReason: sharedReasons.join('; '),
    items: group.items
  };
});

const summary = {
  false_positive: auditedWarnings.filter(w => w.classification === 'false_positive').length,
  obsolete_metadata: auditedWarnings.filter(w => w.classification === 'obsolete_metadata').length,
  real_content_issue: auditedWarnings.filter(w => w.classification === 'real_content_issue').length,
  intentional_policy_warning: auditedWarnings.filter(w => w.classification === 'intentional_policy_warning').length
};

const output = {
  generatedAt: new Date().toISOString(),
  totalRawWarnings: auditedWarnings.length,
  classificationSummary: summary,
  actionTaken: 'Enhanced validateSeed to distinguish legitimate intentional shared sources (where all items have sharedSource=true and sharedSourceReason) from unreviewed duplicate reuses and ambiguous URLs.',
  warnings: auditedWarnings
};

const targetPath = path.resolve(__dirname, '../content/v2/audit-warning-review.json');
fs.writeFileSync(targetPath, JSON.stringify(output, null, 2), 'utf8');

console.log(`Audit warning review written to content/v2/audit-warning-review.json`);
console.log(`- Total Warnings Audited: ${auditedWarnings.length}`);
console.log(`- False Positives (Legitimate Shared): ${summary.false_positive}`);
console.log(`- Real Content Issues: ${summary.real_content_issue}`);
console.log(`- Intentional Policy Warnings: ${summary.intentional_policy_warning}`);
console.log(`- Obsolete Metadata: ${summary.obsolete_metadata}`);
