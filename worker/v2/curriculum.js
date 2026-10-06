/**
 * StudyTracker V2 Curriculum & Measurement Engine
 * Core domain logic for courses, lessons, versioned items, puzzle ordering,
 * prerequisites, append-only idempotent attempts, and deterministic analytics.
 */

export class CurriculumEngine {
  constructor(storage) {
    this.storage = storage;
  }

  // --- COURSES ---
  async getCourses(familyCode, includeArchived = false) {
    return await this.storage.getCourses(familyCode, includeArchived);
  }

  async getCourse(familyCode, courseId) {
    return await this.storage.getCourseById(familyCode, courseId);
  }

  async createCourse(familyCode, { id, title, subject, gradeLevel = 9, description = '', visual = null, orderKey = null }) {
    if (!title || !subject) throw new Error('title and subject are required for course');
    const courseId = id || `course_${subject.toLowerCase().replace(/[^a-z0-9]/g, '_')}_${Date.now()}`;
    
    let effectiveOrderKey = orderKey;
    if (effectiveOrderKey === null || effectiveOrderKey === undefined) {
      const existing = await this.storage.getCourses(familyCode, true);
      const maxOrder = existing.reduce((max, c) => Math.max(max, c.orderKey || 0), 0);
      effectiveOrderKey = maxOrder + 1000.0;
    }

    const now = Date.now();
    const course = {
      id: courseId,
      familyCode,
      title: title.trim(),
      subject: subject.trim(),
      gradeLevel: Number(gradeLevel) || 9,
      description: description ? description.trim() : '',
      visual: visual && typeof visual === 'object' ? visual : null,
      orderKey: Number(effectiveOrderKey),
      isArchived: false,
      createdAt: now,
      updatedAt: now
    };

    return await this.storage.saveCourse(course);
  }

  async archiveCourse(familyCode, courseId) {
    const course = await this.storage.getCourseById(familyCode, courseId);
    if (!course) throw new Error(`Course not found: ${courseId}`);
    course.isArchived = true;
    course.updatedAt = Date.now();
    return await this.storage.saveCourse(course);
  }

  // --- LESSONS ---
  async getLessons(familyCode, courseId = null, includeArchived = false) {
    return await this.storage.getLessons(familyCode, courseId, includeArchived);
  }

  async getLesson(familyCode, lessonId) {
    return await this.storage.getLessonById(familyCode, lessonId);
  }

  async createLesson(familyCode, { id, courseId, title, orderKey = null }) {
    if (!courseId || !title) throw new Error('courseId and title are required for lesson');
    const course = await this.storage.getCourseById(familyCode, courseId);
    if (!course) throw new Error(`Course not found: ${courseId}`);

    const lessonId = id || `lesson_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`;
    let effectiveOrderKey = orderKey;
    if (effectiveOrderKey === null || effectiveOrderKey === undefined) {
      const existing = await this.storage.getLessons(familyCode, courseId, true);
      const maxOrder = existing.reduce((max, l) => Math.max(max, l.orderKey || 0), 0);
      effectiveOrderKey = maxOrder + 1000.0;
    }

    const now = Date.now();
    const lesson = {
      id: lessonId,
      courseId,
      familyCode,
      title: title.trim(),
      orderKey: Number(effectiveOrderKey),
      isArchived: false,
      createdAt: now,
      updatedAt: now
    };

    return await this.storage.saveLesson(lesson);
  }

  async archiveLesson(familyCode, lessonId) {
    const lesson = await this.storage.getLessonById(familyCode, lessonId);
    if (!lesson) throw new Error(`Lesson not found: ${lessonId}`);
    lesson.isArchived = true;
    lesson.updatedAt = Date.now();
    return await this.storage.saveLesson(lesson);
  }

  // --- LEARNING ITEMS & VERSIONS ---
  async getItems(familyCode, lessonId = null, includeArchived = false, statusFilter = null) {
    const items = await this.storage.getItems(familyCode, lessonId, includeArchived, statusFilter);
    // Enrich with current version details
    const enriched = await Promise.all(items.map(async item => {
      const versions = await this.storage.getVersions(item.id);
      const currentVersion = versions.find(v => v.id === item.currentVersionId) || versions[versions.length - 1] || null;
      const prereqs = await this.storage.getPrerequisites(familyCode, item.id);
      return {
        ...item,
        currentVersion,
        versionCount: versions.length,
        prerequisites: prereqs
      };
    }));
    return enriched;
  }

  async getItem(familyCode, itemId) {
    const item = await this.storage.getItemById(familyCode, itemId);
    if (!item) return null;
    const versions = await this.storage.getVersions(itemId);
    const currentVersion = versions.find(v => v.id === item.currentVersionId) || versions[versions.length - 1] || null;
    const prereqs = await this.storage.getPrerequisites(familyCode, itemId);
    return {
      ...item,
      currentVersion,
      versions,
      prerequisites: prereqs
    };
  }

  /**
   * Create learning item with initial Version 1.
   * Supports insert-before / insert-after positioning.
   */
  async createItem(familyCode, {
    id,
    lessonId,
    itemType,
    displayLabel,
    stableKey,
    title,
    contentUrl = null,
    payload = null,
    orderKey = null,
    position = null,      // 'before' | 'after'
    targetItemId = null,
    publishingStatus = 'active'
  }) {
    if (!lessonId || !itemType || !displayLabel) {
      throw new Error('lessonId, itemType, and displayLabel are required for learning item');
    }
    const validTypes = ['VIDEO', 'QUIZ', 'ANKI'];
    if (!validTypes.includes(itemType.toUpperCase())) {
      throw new Error(`Invalid itemType: ${itemType}. Must be one of: ${validTypes.join(', ')}`);
    }

    const lesson = await this.storage.getLessonById(familyCode, lessonId);
    if (!lesson) throw new Error(`Lesson not found: ${lessonId}`);

    const existingItems = await this.storage.getItems(familyCode, lessonId, true, 'all');
    
    // Determine order_key using puzzle/modular positioning if specified
    let calculatedOrderKey = orderKey;
    if (position && targetItemId) {
      calculatedOrderKey = this.calculatePositionOrderKey(existingItems, position, targetItemId);
    } else if (calculatedOrderKey === null || calculatedOrderKey === undefined) {
      const maxOrder = existingItems.reduce((max, i) => Math.max(max, i.orderKey || 0), 0);
      calculatedOrderKey = maxOrder + 1000.0;
    }

    const itemId = id || `item_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`;
    const effectiveStableKey = stableKey || itemId;
    const version1Id = `ver_${itemId}_v1`;
    const now = Date.now();
    const effectivePubStatus = ['draft', 'active', 'archived'].includes(publishingStatus) ? publishingStatus : 'active';

    // Create Learning Item
    const item = {
      id: itemId,
      lessonId,
      familyCode,
      itemType: itemType.toUpperCase(),
      displayLabel: displayLabel.trim(),
      stableKey: effectiveStableKey.trim(),
      orderKey: Number(calculatedOrderKey),
      currentVersionId: version1Id,
      publishingStatus: effectivePubStatus,
      isArchived: effectivePubStatus === 'archived',
      createdAt: now,
      updatedAt: now
    };
    await this.storage.saveItem(item);

    // Create Version 1
    const v1 = {
      id: version1Id,
      itemId,
      versionNumber: 1,
      title: (title || displayLabel).trim(),
      contentUrl: contentUrl ? contentUrl.trim() : null,
      payload: payload || {},
      changelog: 'Initial version',
      createdAt: now
    };
    await this.storage.saveVersion(v1);

    // Check if rebalance is needed
    await this.checkAndRebalanceLessonItems(familyCode, lessonId);

    return {
      ...item,
      currentVersion: v1,
      versionCount: 1
    };
  }

  /**
   * Update item content -> Creates NEW Version (e.g. v2, v3) without altering item_id.
   * Prior attempts continue referencing their original version_id!
   */
  async updateItemContent(familyCode, itemId, { title, contentUrl, payload, changelog, publishingStatus }) {
    const item = await this.storage.getItemById(familyCode, itemId);
    if (!item) throw new Error(`Learning item not found: ${itemId}`);

    const versions = await this.storage.getVersions(itemId);
    const nextVersionNumber = (versions.length > 0 ? Math.max(...versions.map(v => v.versionNumber)) : 0) + 1;
    const newVersionId = `ver_${itemId}_v${nextVersionNumber}`;
    const now = Date.now();

    const currentVersion = versions.find(v => v.id === item.currentVersionId) || versions[versions.length - 1] || {};

    const newVersion = {
      id: newVersionId,
      itemId,
      versionNumber: nextVersionNumber,
      title: (title !== undefined && title !== null ? title : currentVersion.title || item.displayLabel).trim(),
      contentUrl: contentUrl !== undefined ? (contentUrl ? contentUrl.trim() : null) : currentVersion.contentUrl,
      payload: payload !== undefined ? payload : currentVersion.payload,
      changelog: changelog ? changelog.trim() : `Updated to version ${nextVersionNumber}`,
      createdAt: now
    };
    await this.storage.saveVersion(newVersion);

    // Update item pointer to latest version
    item.currentVersionId = newVersionId;
    if (publishingStatus && ['draft', 'active', 'archived'].includes(publishingStatus)) {
      item.publishingStatus = publishingStatus;
      item.isArchived = publishingStatus === 'archived';
    }
    item.updatedAt = now;
    await this.storage.saveItem(item);

    return {
      ...item,
      currentVersion: newVersion,
      versionCount: nextVersionNumber
    };
  }

  /**
   * Update item publishing status without changing item ID or version
   */
  async updateItemStatus(familyCode, itemId, publishingStatus) {
    const valid = ['draft', 'active', 'archived'];
    if (!valid.includes(publishingStatus)) {
      throw new Error(`Invalid publishing status: ${publishingStatus}. Must be one of: ${valid.join(', ')}`);
    }
    const item = await this.storage.getItemById(familyCode, itemId);
    if (!item) throw new Error(`Item not found: ${itemId}`);

    item.publishingStatus = publishingStatus;
    item.isArchived = publishingStatus === 'archived';
    item.updatedAt = Date.now();
    await this.storage.saveItem(item);
    return item;
  }

  /**
   * Reorder learning item (insert-before or insert-after target item).
   * Mutates ONLY order_key. IDs and attempt history remain untouched.
   */
  async reorderItem(familyCode, itemId, { position, targetItemId, orderKey = null }) {
    const item = await this.storage.getItemById(familyCode, itemId);
    if (!item) throw new Error(`Item not found: ${itemId}`);

    const existingItems = await this.storage.getItems(familyCode, item.lessonId, true, 'all');
    let newOrderKey = orderKey;

    if (position && targetItemId) {
      // Exclude item itself when calculating target position
      const otherItems = existingItems.filter(i => i.id !== itemId);
      newOrderKey = this.calculatePositionOrderKey(otherItems, position, targetItemId);
    } else if (newOrderKey === null || newOrderKey === undefined) {
      throw new Error('Either (position and targetItemId) or explicit orderKey must be provided');
    }

    item.orderKey = Number(newOrderKey);
    item.updatedAt = Date.now();
    await this.storage.saveItem(item);

    await this.checkAndRebalanceLessonItems(familyCode, item.lessonId);

    return item;
  }

  async archiveItem(familyCode, itemId) {
    const item = await this.storage.getItemById(familyCode, itemId);
    if (!item) throw new Error(`Item not found: ${itemId}`);
    item.isArchived = true;
    item.publishingStatus = 'archived';
    item.updatedAt = Date.now();
    return await this.storage.saveItem(item);
  }

  // --- PUZZLE ORDERING UTILITIES ---
  calculatePositionOrderKey(items, position, targetItemId) {
    const sorted = [...items].sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    const targetIdx = sorted.findIndex(i => i.id === targetItemId);
    if (targetIdx === -1) {
      throw new Error(`Target item for positioning not found: ${targetItemId}`);
    }

    const target = sorted[targetIdx];

    if (position === 'before') {
      if (targetIdx === 0) {
        // First item: place midway before it or step down
        return target.orderKey <= 1.0 ? target.orderKey / 2.0 : target.orderKey - 1000.0 <= 0 ? target.orderKey / 2.0 : target.orderKey - 1000.0;
      } else {
        const prev = sorted[targetIdx - 1];
        return (prev.orderKey + target.orderKey) / 2.0;
      }
    } else if (position === 'after') {
      if (targetIdx === sorted.length - 1) {
        // Last item
        return target.orderKey + 1000.0;
      } else {
        const next = sorted[targetIdx + 1];
        return (target.orderKey + next.orderKey) / 2.0;
      }
    } else {
      throw new Error(`Invalid position: ${position}. Must be 'before' or 'after'`);
    }
  }

  async checkAndRebalanceLessonItems(familyCode, lessonId) {
    const items = await this.storage.getItems(familyCode, lessonId, true);
    if (items.length < 2) return;

    // Check if difference between adjacent items is too small (< 1e-4)
    let needsRebalance = false;
    for (let i = 0; i < items.length - 1; i++) {
      if (Math.abs(items[i + 1].orderKey - items[i].orderKey) < 0.0001) {
        needsRebalance = true;
        break;
      }
    }

    if (needsRebalance) {
      // Rebalance: assign 1000, 2000, 3000...
      for (let i = 0; i < items.length; i++) {
        const newKey = (i + 1) * 1000.0;
        await this.storage.updateItemOrderKey(familyCode, items[i].id, newKey);
      }
    }
  }

  // --- PREREQUISITES ---
  async addPrerequisite(familyCode, { itemId, requiredItemId, minScore = null }) {
    if (!itemId || !requiredItemId) throw new Error('itemId and requiredItemId are required');
    if (itemId === requiredItemId) throw new Error('An item cannot depend on itself');

    const item = await this.storage.getItemById(familyCode, itemId);
    const requiredItem = await this.storage.getItemById(familyCode, requiredItemId);
    if (!item) throw new Error(`Item not found: ${itemId}`);
    if (!requiredItem) throw new Error(`Required item not found: ${requiredItemId}`);

    // Circular dependency check
    const existingPrereqs = await this.storage.getPrerequisites(familyCode);
    const adj = {};
    existingPrereqs.forEach(p => {
      if (!adj[p.itemId]) adj[p.itemId] = [];
      adj[p.itemId].push(p.requiredItemId);
    });
    if (!adj[itemId]) adj[itemId] = [];
    adj[itemId].push(requiredItemId);

    if (this.detectCycle(adj, itemId)) {
      throw new Error(`Circular prerequisite detected: ${itemId} -> ${requiredItemId}`);
    }

    const prereq = {
      id: `prereq_${itemId}_${requiredItemId}`,
      itemId,
      requiredItemId,
      minScore: minScore !== null && minScore !== undefined ? Number(minScore) : null,
      familyCode,
      createdAt: Date.now()
    };

    return await this.storage.savePrerequisite(prereq);
  }

  async removePrerequisite(familyCode, itemId, requiredItemId) {
    await this.storage.deletePrerequisite(familyCode, itemId, requiredItemId);
    return { success: true, removed: { itemId, requiredItemId } };
  }

  detectCycle(adj, startNode) {
    const visited = new Set();
    const recursionStack = new Set();

    function dfs(node) {
      visited.add(node);
      recursionStack.add(node);

      const neighbors = adj[node] || [];
      for (const neighbor of neighbors) {
        if (!visited.has(neighbor)) {
          if (dfs(neighbor)) return true;
        } else if (recursionStack.has(neighbor)) {
          return true;
        }
      }

      recursionStack.delete(node);
      return false;
    }

    return dfs(startNode);
  }

  // --- ATTEMPTS & MEASUREMENT ---
  /**
   * Append-only, idempotent attempt recording.
   * If clientAttemptId already exists for this student+family, returns the existing attempt (idempotent 200).
   */
  async recordAttempt(familyCode, {
    clientAttemptId,
    studentId = 'student_default',
    itemId,
    versionId = null,
    status = 'COMPLETED',
    score = null,
    durationSeconds = 0,
    startedAt = null,
    completedAt = null,
    metadata = {},
    quizAnswers = [],
    answers = []
  }) {
    if (!clientAttemptId || !itemId) {
      throw new Error('clientAttemptId and itemId are required for recording attempt');
    }

    const effectiveAnswers = Array.isArray(quizAnswers) && quizAnswers.length > 0 ? quizAnswers : (Array.isArray(answers) ? answers : []);

    // Idempotency check
    const existing = await this.storage.getAttemptByClientId(familyCode, studentId, clientAttemptId);
    if (existing) {
      const dbAnswers = await this.storage.getQuizAnswers(existing.id);
      return { attempt: existing, quizAnswers: dbAnswers, duplicate: true };
    }

    const item = await this.storage.getItemById(familyCode, itemId);
    if (!item) throw new Error(`Learning item not found: ${itemId}`);

    // If versionId not supplied, bind to current_version_id
    const effectiveVersionId = versionId || item.currentVersionId;
    const now = Date.now();
    const attemptId = `att_${now}_${Math.random().toString(36).slice(2, 6)}`;

    const normalizedStatus = ['STARTED', 'COMPLETED', 'ABANDONED'].includes((status || '').toUpperCase())
      ? status.toUpperCase()
      : 'COMPLETED';
    const normalizedMetadata = {
      ...(metadata || {}),
      measurementKind: metadata?.measurementKind || item.itemType,
      selfCompleted: item.itemType === 'VIDEO'
        ? (metadata?.selfCompleted ?? normalizedStatus === 'COMPLETED')
        : metadata?.selfCompleted
    };

    const attempt = {
      id: attemptId,
      clientAttemptId,
      familyCode,
      studentId,
      itemId,
      versionId: effectiveVersionId,
      status: normalizedStatus,
      score: score !== null && score !== undefined ? Number(score) : null,
      durationSeconds: Number(durationSeconds) || 0,
      startedAt: startedAt ? Number(startedAt) : now,
      completedAt: completedAt ? Number(completedAt) : (normalizedStatus === 'COMPLETED' ? now : null),
      metadata: normalizedMetadata,
      createdAt: now
    };

    await this.storage.saveAttempt(attempt);

    // Save quiz answers if provided
    let savedAnswers = [];
    if (Array.isArray(effectiveAnswers) && effectiveAnswers.length > 0) {
      savedAnswers = effectiveAnswers.map((a, idx) => {
        if (!a || typeof a !== 'object') {
          throw new Error(`Invalid quizAnswer metric entry at index ${idx}`);
        }
        const qIdx = a.questionIndex !== undefined && a.questionIndex !== null ? parseInt(a.questionIndex, 10) : idx;
        if (isNaN(qIdx) || qIdx < 0) {
          throw new Error(`Invalid questionIndex at index ${idx}`);
        }
        return {
          id: a.id || `ans_${attemptId}_${idx}`,
          attemptId,
          questionId: String(a.questionId || `q_${idx}`).trim(),
          questionIndex: qIdx,
          selectedOption: a.selectedOption !== undefined && a.selectedOption !== null ? String(a.selectedOption).trim() : null,
          isCorrect: Boolean(a.isCorrect),
          durationSeconds: Math.max(0, parseInt(a.durationSeconds, 10) || 0),
          createdAt: now
        };
      });
      await this.storage.saveQuizAnswers(savedAnswers);
    }

    return { attempt, quizAnswers: savedAnswers, duplicate: false };
  }

  async getAttempts(familyCode, filter = {}) {
    return await this.storage.getAttempts(familyCode, filter);
  }

  // --- DETERMINISTIC PROGRESS & ANALYTICS ---
  async getProgressAnalytics(familyCode, studentId = 'student_default', courseId = null) {
    const courses = await this.storage.getCourses(familyCode, false);
    const targetCourses = courseId ? courses.filter(c => c.id === courseId) : courses;
    const allAttempts = await this.storage.getAttempts(familyCode, { studentId, limit: 5000 });
    const allPrereqs = await this.storage.getPrerequisites(familyCode);

    const attemptsByItem = {};
    allAttempts.forEach(att => {
      if (!attemptsByItem[att.itemId]) attemptsByItem[att.itemId] = [];
      attemptsByItem[att.itemId].push(att);
    });

    const coursesAnalytics = await Promise.all(targetCourses.map(async course => {
      const lessons = await this.storage.getLessons(familyCode, course.id, false);
      let courseTotalItems = 0;
      let courseCompletedItems = 0;
      const courseLearningScores = [];
      let courseDelayedRecallAttempts = 0;
      let courseAnkiReviews = 0;

      const lessonsAnalytics = await Promise.all(lessons.map(async lesson => {
        const items = await this.storage.getItems(familyCode, lesson.id, false);
        let lessonCompletedCount = 0;

        const itemsAnalytics = items.map(item => {
          const itemAttempts = attemptsByItem[item.id] || [];
          const completedAttempts = itemAttempts.filter(a => a.status === 'COMPLETED');
          const isCompleted = completedAttempts.length > 0;
          if (isCompleted) lessonCompletedCount++;

          const scoredAttempts = completedAttempts.filter(a => a.score !== null && a.score !== undefined);
          const bestScore = scoredAttempts.length
            ? Math.max(...scoredAttempts.map(a => Number(a.score)))
            : null;
          const latestAttempt = itemAttempts[0] || null;

          const delayedRecallAttempts = completedAttempts.filter(a =>
            a.metadata?.measurementKind === 'DELAYED_RECALL' ||
            Number(a.metadata?.recallDelayHours || 0) > 0
          ).length;

          const anki = completedAttempts.reduce((acc, a) => {
            const m = a.metadata || {};
            acc.reviewedCount += Number(m.reviewedCount || 0);
            acc.again += Number(m.again || 0);
            acc.hard += Number(m.hard || 0);
            acc.good += Number(m.good || 0);
            acc.easy += Number(m.easy || 0);
            return acc;
          }, { reviewedCount: 0, again: 0, hard: 0, good: 0, easy: 0 });

          const selfCompleted = item.itemType === 'VIDEO' &&
            completedAttempts.some(a => a.metadata?.selfCompleted !== false);

          if (item.itemType === 'QUIZ') {
            scoredAttempts.forEach(a => courseLearningScores.push(Number(a.score)));
            courseDelayedRecallAttempts += delayedRecallAttempts;
          }
          if (item.itemType === 'ANKI') courseAnkiReviews += anki.reviewedCount;

          const itemPrereqs = allPrereqs.filter(p => p.itemId === item.id);
          const prereqsStatus = itemPrereqs.map(p => {
            const reqAttempts = (attemptsByItem[p.requiredItemId] || []).filter(a => a.status === 'COMPLETED');
            const reqScores = reqAttempts
              .filter(a => a.score !== null && a.score !== undefined)
              .map(a => Number(a.score));
            const reqBestScore = reqScores.length ? Math.max(...reqScores) : null;
            const scoreSatisfied = p.minScore === null || (reqBestScore !== null && reqBestScore >= p.minScore);
            return {
              requiredItemId: p.requiredItemId,
              minScore: p.minScore,
              satisfied: reqAttempts.length > 0 && scoreSatisfied
            };
          });

          return {
            id: item.id,
            displayLabel: item.displayLabel,
            stableKey: item.stableKey,
            itemType: itemTypeBadge(item.itemType),
            rawItemType: item.itemType,
            orderKey: item.orderKey,
            isCompleted,
            selfCompleted,
            isUnlocked: prereqsStatus.every(p => p.satisfied),
            attemptCount: itemAttempts.length,
            completedAttemptCount: completedAttempts.length,
            bestScore,
            delayedRecallAttempts,
            anki,
            latestAttemptAt: latestAttempt ? latestAttempt.createdAt : null,
            prerequisites: prereqsStatus
          };
        });

        courseTotalItems += items.length;
        courseCompletedItems += lessonCompletedCount;

        return {
          id: lesson.id,
          title: lesson.title,
          orderKey: lesson.orderKey,
          totalItems: items.length,
          completedItems: lessonCompletedCount,
          completionPercentage: items.length > 0 ? Math.round((lessonCompletedCount / items.length) * 100) : 0,
          items: itemsAnalytics
        };
      }));

      const averageLearningScore = courseLearningScores.length
        ? Math.round((courseLearningScores.reduce((a, b) => a + b, 0) / courseLearningScores.length) * 10) / 10
        : null;

      return {
        id: course.id,
        title: course.title,
        subject: course.subject,
        gradeLevel: course.gradeLevel,
        totalItems: courseTotalItems,
        completedItems: courseCompletedItems,
        completionPercentage: courseTotalItems > 0 ? Math.round((courseCompletedItems / courseTotalItems) * 100) : 0,
        measurement: {
          averageLearningScore,
          scoredQuizAttempts: courseLearningScores.length,
          delayedRecallAttempts: courseDelayedRecallAttempts,
          ankiReviews: courseAnkiReviews
        },
        lessons: lessonsAnalytics
      };
    }));

    return {
      familyCode,
      studentId,
      generatedAt: Date.now(),
      model: 'measurement-not-control',
      courses: coursesAnalytics
    };
  }

  // --- BOUNDED AI CONTEXT EXPORT ---
  async exportContext(familyCode, studentId = 'student_default', courseId = null) {
    const analytics = await this.getProgressAnalytics(familyCode, studentId, courseId);
    return {
      schemaVersion: '2.0-measurement-curriculum',
      exportedAt: new Date().toISOString(),
      studentId,
      familyCode,
      curriculumTopology: analytics.courses,
      coexistenceNote: 'V2 is measurement-first: VIDEO completion is self-reported; learning evidence comes from QUIZ, delayed recall and ANKI metrics. V1 control/review semantics are not canonical V2 progress.'
    };
  }
}

function itemTypeBadge(type) {
  switch (type) {
    case 'VIDEO': return 'VIDEO';
    case 'QUIZ': return 'QUIZ';
    case 'ANKI': return 'ANKI';
    default: return type;
  }
}
