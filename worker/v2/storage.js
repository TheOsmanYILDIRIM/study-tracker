/**
 * StudyTracker V2 KV storage.
 *
 * Canonical V2 rule:
 * - Cloudflare KV is the active backend.
 * - attempts / measurement history are append-only.
 * - catalog and projections may be updated.
 * - no D1 runtime fallback.
 */

export function createV2Storage(env, inMemoryStore) {
  return createKVStorage(env, inMemoryStore);
}

function createKVStorage(env, inMemoryStore) {
  const kv = env?.STUDY_SYNC_KV;

  async function readKey(key) {
    if (kv) {
      const raw = await kv.get(key);
      return raw ? JSON.parse(raw) : null;
    }
    return inMemoryStore?.get(key) ?? null;
  }

  async function writeKey(key, data) {
    if (kv) {
      await kv.put(key, JSON.stringify(data));
      return;
    }
    if (!inMemoryStore) {
      throw new Error('STUDY_SYNC_KV is not bound and no in-memory test store is available');
    }
    inMemoryStore.set(key, data);
  }

  async function readList(key) {
    const value = await readKey(key);
    if (value === null) return [];
    if (!Array.isArray(value)) {
      throw new TypeError(`Corrupt KV list at ${key}: expected an array`);
    }
    return value;
  }

  async function upsertListEntry(key, entity, idField = 'id') {
    const list = await readList(key);
    const idx = list.findIndex(x => x?.[idField] === entity?.[idField]);
    if (idx >= 0) list[idx] = entity;
    else list.push(entity);
    await writeKey(key, list);
    return entity;
  }

  const courseKey = familyCode => `v2:${familyCode}:catalog:courses`;
  const lessonKey = familyCode => `v2:${familyCode}:catalog:lessons`;
  const itemKey = familyCode => `v2:${familyCode}:catalog:items`;
  const prereqKey = familyCode => `v2:${familyCode}:catalog:prereqs`;
  const versionsKey = itemId => `v2:catalog:versions:${itemId}`;

  const attemptKey = (familyCode, attemptId) => `v2:${familyCode}:measurement:attempt:${attemptId}`;
  const attemptIndexKey = familyCode => `v2:${familyCode}:measurement:attempt-index`;
  const clientAttemptKey = (familyCode, studentId, clientAttemptId) =>
    `v2:${familyCode}:measurement:client-attempt:${studentId}:${clientAttemptId}`;
  const quizAnswersKey = attemptId => `v2:measurement:quiz-answers:${attemptId}`;

  return {
    type: 'kv',

    async getCourses(familyCode, includeArchived = false) {
      return (await readList(courseKey(familyCode)))
        .filter(c => includeArchived || !c.isArchived)
        .sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    },

    async getCourseById(familyCode, courseId) {
      return (await readList(courseKey(familyCode))).find(c => c.id === courseId) || null;
    },

    async saveCourse(course) {
      const normalized = { ...course, updatedAt: course.updatedAt || Date.now() };
      return upsertListEntry(courseKey(course.familyCode), normalized);
    },

    async getLessons(familyCode, courseId = null, includeArchived = false) {
      return (await readList(lessonKey(familyCode)))
        .filter(l => (!courseId || l.courseId === courseId) && (includeArchived || !l.isArchived))
        .sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    },

    async getLessonById(familyCode, lessonId) {
      return (await readList(lessonKey(familyCode))).find(l => l.id === lessonId) || null;
    },

    async saveLesson(lesson) {
      const normalized = { ...lesson, updatedAt: lesson.updatedAt || Date.now() };
      return upsertListEntry(lessonKey(lesson.familyCode), normalized);
    },

    async getItems(familyCode, lessonId = null, includeArchived = false, statusFilter = null) {
      return (await readList(itemKey(familyCode)))
        .filter(item => {
          if (lessonId && item.lessonId !== lessonId) return false;
          const publishingStatus = item.publishingStatus || (item.isArchived ? 'archived' : 'active');
          if (statusFilter && statusFilter !== 'all') return publishingStatus === statusFilter;
          if (!includeArchived) return !item.isArchived && publishingStatus === 'active';
          return true;
        })
        .sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    },

    async getItemById(familyCode, itemId) {
      return (await readList(itemKey(familyCode))).find(i => i.id === itemId) || null;
    },

    async saveItem(item) {
      const publishingStatus = item.publishingStatus || (item.isArchived ? 'archived' : 'active');
      const normalized = {
        ...item,
        publishingStatus,
        isArchived: Boolean(item.isArchived || publishingStatus === 'archived'),
        updatedAt: Date.now()
      };
      return upsertListEntry(itemKey(item.familyCode), normalized);
    },

    async updateItemOrderKey(familyCode, itemId, newOrderKey) {
      const key = itemKey(familyCode);
      const list = await readList(key);
      const idx = list.findIndex(i => i.id === itemId);
      if (idx < 0) return;
      list[idx] = { ...list[idx], orderKey: newOrderKey, updatedAt: Date.now() };
      await writeKey(key, list);
    },

    async getVersions(itemId) {
      return (await readList(versionsKey(itemId))).sort((a, b) => a.versionNumber - b.versionNumber);
    },

    async getVersionById(versionId) {
      // Version IDs are generated as ver_<itemId>_v<N> by the V2 engine.
      const match = /^ver_(.+)_v\d+$/.exec(versionId);
      if (!match) return null;
      return (await readList(versionsKey(match[1]))).find(v => v.id === versionId) || null;
    },

    async saveVersion(version) {
      const key = versionsKey(version.itemId);
      const list = await readList(key);
      const existing = list.find(v => v.id === version.id);
      if (existing) return existing; // immutable version snapshot
      list.push(version);
      await writeKey(key, list);
      return version;
    },

    async getPrerequisites(familyCode, itemId = null) {
      const list = await readList(prereqKey(familyCode));
      return itemId ? list.filter(p => p.itemId === itemId) : list;
    },

    async savePrerequisite(prereq) {
      const key = prereqKey(prereq.familyCode);
      const list = await readList(key);
      const idx = list.findIndex(
        p => p.itemId === prereq.itemId && p.requiredItemId === prereq.requiredItemId
      );
      if (idx >= 0) list[idx] = prereq;
      else list.push(prereq);
      await writeKey(key, list);
      return prereq;
    },

    async deletePrerequisite(familyCode, itemId, requiredItemId) {
      const key = prereqKey(familyCode);
      const list = await readList(key);
      await writeKey(
        key,
        list.filter(p => !(p.itemId === itemId && p.requiredItemId === requiredItemId))
      );
    },

    async getAttemptByClientId(familyCode, studentId, clientAttemptId) {
      const ref = await readKey(clientAttemptKey(familyCode, studentId, clientAttemptId));
      if (!ref?.attemptId) return null;
      return await readKey(attemptKey(familyCode, ref.attemptId));
    },

    async getAttempts(familyCode, filter = {}) {
      const index = await readList(attemptIndexKey(familyCode));
      const limit = Number.isInteger(filter.limit) && filter.limit > 0 ? filter.limit : 50;
      const out = [];

      for (const row of index) {
        if (out.length >= limit) break;
        if (filter.studentId && row.studentId !== filter.studentId) continue;
        if (filter.itemId && row.itemId !== filter.itemId) continue;
        const attempt = await readKey(attemptKey(familyCode, row.attemptId));
        if (attempt) out.push(attempt);
      }
      return out;
    },

    async saveAttempt(attempt) {
      const existing = await this.getAttemptByClientId(
        attempt.familyCode,
        attempt.studentId,
        attempt.clientAttemptId
      );
      if (existing) return existing;

      // Write the immutable measurement record first.
      await writeKey(attemptKey(attempt.familyCode, attempt.id), attempt);

      // Then write the idempotency pointer and derived query index.
      await writeKey(
        clientAttemptKey(attempt.familyCode, attempt.studentId, attempt.clientAttemptId),
        { attemptId: attempt.id, createdAt: attempt.createdAt }
      );

      const indexKey = attemptIndexKey(attempt.familyCode);
      const index = await readList(indexKey);
      if (!index.some(row => row.attemptId === attempt.id)) {
        index.unshift({
          attemptId: attempt.id,
          studentId: attempt.studentId,
          itemId: attempt.itemId,
          versionId: attempt.versionId,
          status: attempt.status,
          score: attempt.score ?? null,
          createdAt: attempt.createdAt
        });
        await writeKey(indexKey, index);
      }

      return attempt;
    },

    async saveQuizAnswers(answers) {
      if (!Array.isArray(answers) || answers.length === 0) return;
      const attemptId = answers[0].attemptId;
      if (!attemptId) throw new Error('quiz answer attemptId is required');

      const existing = await readKey(quizAnswersKey(attemptId));
      if (existing) return; // append-only attempt detail: never rewrite answers
      await writeKey(quizAnswersKey(attemptId), answers);
    },

    async getQuizAnswers(attemptId) {
      return (await readKey(quizAnswersKey(attemptId))) || [];
    }
  };
}
