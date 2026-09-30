/**
 * StudyTracker V2 Seed Catalog Tooling
 * Validates, diffs, and applies deterministic catalog manifests.
 * Idempotent, version-preserving, non-destructive.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const v2Api = require('./v2-api');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

function computeItemFingerprint(item) {
  const content = [
    item.stableKey || item.id,
    item.itemType,
    item.title || item.displayLabel,
    item.contentUrl || '',
    JSON.stringify(item.payload?.metadata || item.payload || {})
  ].join('|');
  return sha256(content);
}

function loadCatalogManifest(catalogPathOrObject) {
  if (typeof catalogPathOrObject === 'object' && catalogPathOrObject !== null) {
    return catalogPathOrObject;
  }
  const defaultPath = path.resolve(__dirname, '../../content/9-sinif-v2-catalog.json');
  const targetPath = catalogPathOrObject ? path.resolve(process.cwd(), catalogPathOrObject) : defaultPath;
  if (!fs.existsSync(targetPath)) {
    throw new Error(`Catalog manifest file not found: ${targetPath}`);
  }
  const raw = fs.readFileSync(targetPath, 'utf8');
  return JSON.parse(raw);
}

/**
 * 1. VALIDATE SEED MANIFEST
 */
function validateSeed(catalogInput) {
  const catalog = typeof catalogInput === 'string' || !catalogInput ? loadCatalogManifest(catalogInput) : catalogInput;
  const errors = [];
  const warnings = [];

  if (catalog.schemaVersion !== 'v2') {
    errors.push(`Invalid schemaVersion: expected "v2", got "${catalog.schemaVersion}"`);
  }

  if (!Array.isArray(catalog.courses) || catalog.courses.length === 0) {
    errors.push('Manifest must contain a non-empty "courses" array');
    return { valid: false, errors, warnings, stats: {} };
  }

  const courseIds = new Set();
  const lessonIds = new Set();
  const stableKeys = new Set();
  const itemIds = new Set();
  const validItemTypes = new Set(['VIDEO', 'QUIZ', 'ANKI']);

  let totalLessons = 0;
  let totalItems = 0;
  let videoCount = 0;
  let ankiCount = 0;
  let quizCount = 0;
  let verifiedCount = 0;
  let reviewCount = 0;

  const urlUsageMap = new Map(); // url -> array of items

  catalog.courses.forEach((course, cIdx) => {
    if (!course.id || !course.title || !course.subject) {
      errors.push(`Course at index ${cIdx} missing required fields (id, title, subject)`);
    }
    if (courseIds.has(course.id)) {
      errors.push(`Duplicate course id: ${course.id}`);
    }
    courseIds.add(course.id);

    if (!Array.isArray(course.lessons)) {
      errors.push(`Course ${course.id} must have a "lessons" array`);
      return;
    }

    course.lessons.forEach((lesson, lIdx) => {
      totalLessons++;
      if (!lesson.id || !lesson.title) {
        errors.push(`Lesson at index ${lIdx} in course ${course.id} missing required fields (id, title)`);
      }
      if (lessonIds.has(lesson.id)) {
        errors.push(`Duplicate lesson id: ${lesson.id}`);
      }
      lessonIds.add(lesson.id);

      if (!Array.isArray(lesson.items)) {
        errors.push(`Lesson ${lesson.id} must have an "items" array`);
        return;
      }

      lesson.items.forEach((item, iIdx) => {
        totalItems++;
        if (!item.id || !item.stableKey || !item.itemType || !item.displayLabel) {
          errors.push(`Item at index ${iIdx} in lesson ${lesson.id} missing required fields (id, stableKey, itemType, displayLabel)`);
        }
        if (itemIds.has(item.id)) {
          errors.push(`Duplicate item id: ${item.id}`);
        }
        itemIds.add(item.id);

        if (stableKeys.has(item.stableKey)) {
          errors.push(`Duplicate stableKey: ${item.stableKey}`);
        }
        stableKeys.add(item.stableKey);

        if (!validItemTypes.has(item.itemType)) {
          errors.push(`Item ${item.id} has invalid itemType: ${item.itemType}`);
        }

        if (item.itemType === 'VIDEO') videoCount++;
        else if (item.itemType === 'ANKI') ankiCount++;
        else if (item.itemType === 'QUIZ') quizCount++;

        const provenance = item.payload?.provenance || {};
        const reviewStatus = provenance.reviewStatus || 'unspecified';
        if (reviewStatus === 'verified') verifiedCount++;
        else reviewCount++;

        // Audit checks on URLs
        if (item.contentUrl) {
          const list = urlUsageMap.get(item.contentUrl) || [];
          list.push(item);
          urlUsageMap.set(item.contentUrl, list);

          if (item.contentUrl.includes('youtube.com/@')) {
            warnings.push({
              stableKey: item.stableKey,
              title: item.title,
              warning: 'Channel homepage URL used instead of direct video ID',
              reviewStatus
            });
          }
          if (item.contentUrl.includes('youtube.com/results?search_query=')) {
            warnings.push({
              stableKey: item.stableKey,
              title: item.title,
              warning: 'Search query URL used instead of direct video ID',
              reviewStatus
            });
          }
        }

        // Canonical History teacher check
        if (course.subject.toLowerCase().includes('tarih') || course.id.includes('tar')) {
          const teacher = item.payload?.teacher || '';
          if (item.contentUrl && !teacher.includes('Mehmet Celal') && !item.title.includes('Mehmet Celal')) {
            warnings.push({
              stableKey: item.stableKey,
              title: item.title,
              warning: 'History item source is not canonical teacher Mehmet Celal ÖZYILDIZ',
              reviewStatus: 'stale'
            });
          }
        }
      });
    });
  });

  // Check repeated video URLs across different topics
  for (const [url, items] of urlUsageMap.entries()) {
    if (items.length > 1 && !url.includes('youtube.com/@')) {
      const titles = items.map(i => i.title).join(' | ');
      warnings.push({
        url,
        count: items.length,
        warning: `Video URL reused across ${items.length} items: ${titles}`
      });
    }
  }

  const valid = errors.length === 0;
  return {
    valid,
    errors,
    warnings,
    stats: {
      courseCount: catalog.courses.length,
      lessonCount: totalLessons,
      itemCount: totalItems,
      videoCount,
      ankiCount,
      quizCount,
      verifiedCount,
      reviewCount,
      warningCount: warnings.length
    }
  };
}

/**
 * 2. DIFF SEED MANIFEST AGAINST REMOTE CATALOG
 */
async function diffSeed(familyCode, catalogInput) {
  const catalog = typeof catalogInput === 'string' || !catalogInput ? loadCatalogManifest(catalogInput) : catalogInput;
  const remoteRes = await v2Api.fetchV2Catalog(familyCode);
  const remoteCourses = remoteRes.curriculum || [];

  const remoteCourseMap = new Map(remoteCourses.map(c => [c.id, c]));
  const remoteLessonMap = new Map();
  const remoteItemByStableKey = new Map();
  const remoteItemById = new Map();

  remoteCourses.forEach(c => {
    (c.lessons || []).forEach(l => {
      remoteLessonMap.set(l.id, l);
      (l.items || []).forEach(i => {
        if (i.stableKey) remoteItemByStableKey.set(i.stableKey, i);
        remoteItemById.set(i.id, i);
      });
    });
  });

  const coursesToCreate = [];
  const lessonsToCreate = [];
  const itemsToCreate = [];
  const itemsToUpdateContent = [];
  const itemsIdentical = [];

  const seedItemIds = new Set();
  const seedStableKeys = new Set();

  for (const course of catalog.courses) {
    if (!remoteCourseMap.has(course.id)) {
      coursesToCreate.push(course);
    }

    for (const lesson of course.lessons) {
      if (!remoteLessonMap.has(lesson.id)) {
        lessonsToCreate.push(lesson);
      }

      for (const item of lesson.items) {
        seedItemIds.add(item.id);
        seedStableKeys.add(item.stableKey);

        const existing = remoteItemByStableKey.get(item.stableKey) || remoteItemById.get(item.id);
        if (!existing) {
          itemsToCreate.push({ ...item, lessonId: lesson.id, courseId: course.id });
        } else {
          // Compare content
          const curVer = existing.currentVersion || {};
          const currentTitle = curVer.title || existing.displayLabel;
          const currentUrl = curVer.contentUrl || null;
          const seedTitle = item.title || item.displayLabel;
          const seedUrl = item.contentUrl || null;

          const titleChanged = currentTitle.trim() !== seedTitle.trim();
          const urlChanged = (currentUrl || '').trim() !== (seedUrl || '').trim();

          // Also check payload fingerprint
          const seedFp = item.payload?.provenance?.fingerprint || computeItemFingerprint(item);
          const currentPayloadFp = curVer.payload?.provenance?.fingerprint || null;
          const fpChanged = currentPayloadFp && currentPayloadFp !== seedFp;

          if (titleChanged || urlChanged || fpChanged) {
            itemsToUpdateContent.push({
              itemId: existing.id,
              stableKey: item.stableKey,
              currentVersionId: existing.currentVersionId,
              previousTitle: currentTitle,
              newTitle: seedTitle,
              previousUrl: currentUrl,
              newUrl: seedUrl,
              payload: item.payload,
              changelog: `Seed update: ${titleChanged ? 'title ' : ''}${urlChanged ? 'url ' : ''}`.trim()
            });
          } else {
            itemsIdentical.push({
              itemId: existing.id,
              stableKey: item.stableKey,
              displayLabel: item.displayLabel
            });
          }
        }
      }
    }
  }

  // Find extra remote items (which are preserved and never deleted)
  const extraRemoteItems = [];
  remoteCourses.forEach(c => {
    (c.lessons || []).forEach(l => {
      (l.items || []).forEach(i => {
        if (!seedItemIds.has(i.id) && !seedStableKeys.has(i.stableKey)) {
          extraRemoteItems.push({
            id: i.id,
            stableKey: i.stableKey,
            displayLabel: i.displayLabel,
            courseId: c.id,
            lessonId: l.id
          });
        }
      });
    });
  });

  return {
    familyCode,
    summary: {
      coursesToCreate: coursesToCreate.length,
      lessonsToCreate: lessonsToCreate.length,
      itemsToCreate: itemsToCreate.length,
      itemsToUpdateContent: itemsToUpdateContent.length,
      itemsIdentical: itemsIdentical.length,
      extraRemoteItems: extraRemoteItems.length
    },
    coursesToCreate,
    lessonsToCreate,
    itemsToCreate,
    itemsToUpdateContent,
    itemsIdentical,
    extraRemoteItems
  };
}

/**
 * 3. APPLY SEED MANIFEST
 */
async function applySeed(familyCode, catalogInput, { dryRun = false } = {}) {
  const catalog = typeof catalogInput === 'string' || !catalogInput ? loadCatalogManifest(catalogInput) : catalogInput;
  const validation = validateSeed(catalog);
  if (!validation.valid) {
    throw new Error(`Catalog validation failed with ${validation.errors.length} errors: ${validation.errors.join('; ')}`);
  }

  const diff = await diffSeed(familyCode, catalog);

  if (dryRun) {
    return {
      dryRun: true,
      familyCode,
      diff,
      status: 'DRY_RUN_COMPLETED',
      message: 'No mutations performed in dry-run mode.'
    };
  }

  const appliedActions = {
    coursesCreated: [],
    lessonsCreated: [],
    itemsCreated: [],
    itemsUpdated: []
  };

  // 1. Create missing courses
  for (const course of diff.coursesToCreate) {
    const res = await v2Api.createV2Course(familyCode, {
      id: course.id,
      title: course.title,
      subject: course.subject,
      gradeLevel: course.gradeLevel || 9,
      description: course.description || '',
      orderKey: course.orderKey
    });
    appliedActions.coursesCreated.push(res.course.id);
  }

  // 2. Create missing lessons
  for (const lesson of diff.lessonsToCreate) {
    const res = await v2Api.createV2Lesson(familyCode, {
      id: lesson.id,
      courseId: lesson.courseId,
      title: lesson.title,
      orderKey: lesson.orderKey
    });
    appliedActions.lessonsCreated.push(res.lesson.id);
  }

  // 3. Create missing items
  for (const item of diff.itemsToCreate) {
    const res = await v2Api.createV2Item(familyCode, {
      id: item.id,
      lessonId: item.lessonId,
      itemType: item.itemType,
      displayLabel: item.displayLabel,
      stableKey: item.stableKey,
      title: item.title || item.displayLabel,
      contentUrl: item.contentUrl || null,
      payload: item.payload || {},
      orderKey: item.orderKey
    });
    appliedActions.itemsCreated.push(res.item.id);
  }

  // 4. Update changed content -> Creates NEW version without changing item ID
  for (const upd of diff.itemsToUpdateContent) {
    const res = await v2Api.updateV2ItemContent(familyCode, upd.itemId, {
      title: upd.newTitle,
      contentUrl: upd.newUrl,
      payload: upd.payload,
      changelog: upd.changelog || 'Seed catalog content update'
    });
    appliedActions.itemsUpdated.push({
      itemId: res.item.id,
      newVersionId: res.item.currentVersionId
    });
  }

  return {
    dryRun: false,
    familyCode,
    status: 'APPLIED',
    summary: {
      coursesCreated: appliedActions.coursesCreated.length,
      lessonsCreated: appliedActions.lessonsCreated.length,
      itemsCreated: appliedActions.itemsCreated.length,
      itemsUpdated: appliedActions.itemsUpdated.length,
      itemsIdentical: diff.itemsIdentical.length,
      extraRemotePreserved: diff.extraRemoteItems.length
    },
    appliedActions
  };
}

module.exports = {
  validateSeed,
  diffSeed,
  applySeed,
  computeItemFingerprint,
  loadCatalogManifest
};
