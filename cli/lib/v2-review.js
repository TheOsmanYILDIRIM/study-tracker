/**
 * StudyTracker V2 Content Review Workflow
 * Manages item review states, approvals, rejections, content replacements,
 * immutable provenance and audit history.
 */

const fs = require('fs');
const path = require('path');
const v2Api = require('./v2-api');

/**
 * List items needing review, drafts, or verified items
 */
async function listReviewItems(familyCode, { status, courseId } = {}) {
  const catalogRes = await v2Api.fetchV2Catalog(familyCode, courseId);
  const curriculum = catalogRes.curriculum || [];

  const items = [];
  for (const course of curriculum) {
    for (const lesson of course.lessons || []) {
      for (const item of lesson.items || []) {
        const curVer = item.currentVersion || {};
        const payload = curVer.payload || {};
        const provenance = payload.provenance || {};
        const reviewStatus = provenance.reviewStatus || 'unspecified';
        const pubStatus = item.publishingStatus || (item.isArchived ? 'archived' : 'active');

        // Status matching
        let matches = true;
        if (status) {
          const s = status.toLowerCase();
          if (s === 'needs_review') matches = reviewStatus === 'needs_review' || reviewStatus === 'unspecified';
          else if (s === 'draft') matches = pubStatus === 'draft';
          else if (s === 'verified') matches = reviewStatus === 'verified';
          else if (s === 'active') matches = pubStatus === 'active';
          else if (s === 'archived') matches = pubStatus === 'archived';
          else if (s === 'rejected') matches = reviewStatus === 'rejected';
        }

        if (matches) {
          // Check audit flags
          const auditWarnings = [];
          if (curVer.contentUrl?.includes('youtube.com/@')) {
            auditWarnings.push('Channel homepage URL used instead of direct video ID');
          }
          if (course.subject.toLowerCase().includes('tarih') || course.id.includes('tar')) {
            const teacher = payload.teacher || '';
            if (curVer.contentUrl && !teacher.includes('Mehmet Celal') && !(curVer.title || '').includes('Mehmet Celal')) {
              auditWarnings.push('History video teacher is not canonical Mehmet Celal ÖZYILDIZ');
            }
          }

          items.push({
            id: item.id,
            stableKey: item.stableKey,
            courseId: course.id,
            courseTitle: course.title,
            lessonId: lesson.id,
            lessonTitle: lesson.title,
            displayLabel: item.displayLabel,
            itemType: item.itemType,
            title: curVer.title || item.displayLabel,
            contentUrl: curVer.contentUrl || null,
            publishingStatus: pubStatus,
            reviewStatus,
            reviewedOverride: Boolean(provenance.reviewedOverride),
            versionCount: item.versionCount || 1,
            teacher: payload.teacher || null,
            channel: payload.channel || null,
            auditWarnings,
            lastReviewedAt: provenance.reviewedAt || null
          });
        }
      }
    }
  }

  return {
    familyCode,
    filter: { status: status || 'all', courseId: courseId || null },
    total: items.length,
    items
  };
}

/**
 * Show detailed review info for a specific item
 */
async function showReviewItem(familyCode, stableKeyOrId) {
  const catalogRes = await v2Api.fetchV2Catalog(familyCode);
  const curriculum = catalogRes.curriculum || [];

  let targetSummary = null;
  let targetCourse = null;
  let targetLesson = null;

  for (const course of curriculum) {
    for (const lesson of course.lessons || []) {
      for (const item of lesson.items || []) {
        if (item.stableKey === stableKeyOrId || item.id === stableKeyOrId) {
          targetSummary = item;
          targetCourse = course;
          targetLesson = lesson;
          break;
        }
      }
      if (targetSummary) break;
    }
    if (targetSummary) break;
  }

  if (!targetSummary) {
    throw new Error(`Review item not found: "${stableKeyOrId}"`);
  }

  const itemDetailRes = await v2Api.getV2Item(familyCode, targetSummary.id);
  const item = itemDetailRes.item;
  const curVer = item.currentVersion || {};
  const payload = curVer.payload || {};
  const provenance = payload.provenance || {};

  return {
    item: {
      id: item.id,
      stableKey: item.stableKey,
      displayLabel: item.displayLabel,
      itemType: item.itemType,
      orderKey: item.orderKey,
      publishingStatus: item.publishingStatus || (item.isArchived ? 'archived' : 'active'),
      isArchived: Boolean(item.isArchived)
    },
    course: { id: targetCourse.id, title: targetCourse.title, subject: targetCourse.subject },
    lesson: { id: targetLesson.id, title: targetLesson.title },
    currentVersion: {
      id: curVer.id,
      versionNumber: curVer.versionNumber,
      title: curVer.title,
      contentUrl: curVer.contentUrl,
      changelog: curVer.changelog,
      createdAt: curVer.createdAt,
      payload
    },
    provenance: {
      reviewStatus: provenance.reviewStatus || 'unspecified',
      reviewedOverride: Boolean(provenance.reviewedOverride),
      sourceRef: provenance.sourceRef || null,
      fingerprint: provenance.fingerprint || null,
      reviewedBy: provenance.reviewedBy || null,
      reviewedAt: provenance.reviewedAt || null,
      reviewNotes: provenance.reviewNotes || null,
      rejectReason: provenance.rejectReason || null
    },
    reviewHistory: Array.isArray(provenance.reviewHistory) ? provenance.reviewHistory : [],
    versions: item.versions || []
  };
}

/**
 * Approve an item
 */
async function approveReviewItem(familyCode, stableKeyOrId, { note, reviewer = 'cli_reviewer' } = {}) {
  const showRes = await showReviewItem(familyCode, stableKeyOrId);
  const itemId = showRes.item.id;

  const res = await v2Api.reviewV2Item(familyCode, itemId, {
    action: 'APPROVE',
    reviewer,
    note: note || 'Approved via StudyTracker CLI review'
  });

  return {
    success: true,
    action: 'APPROVE',
    itemId: res.item.id,
    stableKey: res.item.stableKey,
    publishingStatus: res.item.publishingStatus,
    versionNumber: res.item.currentVersion?.versionNumber,
    message: `Item ${res.item.stableKey} (${res.item.id}) approved and published as active.`
  };
}

/**
 * Reject an item
 */
async function rejectReviewItem(familyCode, stableKeyOrId, { reason, reviewer = 'cli_reviewer' } = {}) {
  if (!reason || !reason.trim()) {
    throw new Error('A non-empty --reason is required when rejecting content');
  }

  const showRes = await showReviewItem(familyCode, stableKeyOrId);
  const itemId = showRes.item.id;

  const res = await v2Api.reviewV2Item(familyCode, itemId, {
    action: 'REJECT',
    reviewer,
    reason: reason.trim()
  });

  return {
    success: true,
    action: 'REJECT',
    itemId: res.item.id,
    stableKey: res.item.stableKey,
    publishingStatus: res.item.publishingStatus,
    versionNumber: res.item.currentVersion?.versionNumber,
    message: `Item ${res.item.stableKey} (${res.item.id}) rejected and marked as draft (history preserved).`
  };
}

/**
 * Replace content for an item (creates new version, retains immutable IDs)
 */
async function replaceContentReviewItem(familyCode, stableKeyOrId, { filePath, contentData, note, reviewer = 'cli_reviewer' } = {}) {
  let content = contentData;
  if (!content && filePath) {
    const fullPath = path.resolve(process.cwd(), filePath);
    if (!fs.existsSync(fullPath)) {
      throw new Error(`Content file not found: ${fullPath}`);
    }
    content = JSON.parse(fs.readFileSync(fullPath, 'utf8'));
  }

  if (!content || typeof content !== 'object') {
    throw new Error('Valid content JSON object or --file is required');
  }

  const showRes = await showReviewItem(familyCode, stableKeyOrId);
  const itemId = showRes.item.id;

  const res = await v2Api.reviewV2Item(familyCode, itemId, {
    action: 'REPLACE_CONTENT',
    reviewer,
    note: note || `Replaced content from ${filePath || 'JSON payload'}`,
    content
  });

  return {
    success: true,
    action: 'REPLACE_CONTENT',
    itemId: res.item.id,
    stableKey: res.item.stableKey,
    publishingStatus: res.item.publishingStatus,
    versionNumber: res.item.currentVersion?.versionNumber,
    title: res.item.currentVersion?.title,
    contentUrl: res.item.currentVersion?.contentUrl,
    message: `Item ${res.item.stableKey} content replaced with new Version ${res.item.currentVersion?.versionNumber} (immutable ID preserved).`
  };
}

module.exports = {
  listReviewItems,
  showReviewItem,
  approveReviewItem,
  rejectReviewItem,
  replaceContentReviewItem
};
