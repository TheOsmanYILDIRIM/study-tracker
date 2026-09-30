/**
 * StudyTracker V2 Storage Abstraction
 * Supports D1 SQL storage when env.DB exists, with automatic fallback to KV / in-memory store.
 * Zero external runtime dependencies.
 */

export function createV2Storage(env, inMemoryStore) {
  const hasD1 = Boolean(env && env.DB && typeof env.DB.prepare === 'function');

  if (hasD1) {
    return createD1Storage(env.DB);
  } else {
    return createKVFallbackStorage(env, inMemoryStore);
  }
}

function createD1Storage(db) {
  return {
    type: 'd1',

    // Courses
    async getCourses(familyCode, includeArchived = false) {
      let sql = 'SELECT * FROM courses WHERE family_code = ?';
      if (!includeArchived) sql += ' AND is_archived = 0';
      sql += ' ORDER BY order_key ASC';
      const result = await db.prepare(sql).bind(familyCode).all();
      return (result.results || []).map(mapDbCourse);
    },

    async getCourseById(familyCode, courseId) {
      const row = await db.prepare('SELECT * FROM courses WHERE id = ? AND family_code = ?').bind(courseId, familyCode).first();
      return row ? mapDbCourse(row) : null;
    },

    async saveCourse(course) {
      const sql = `INSERT INTO courses (id, family_code, title, subject, grade_level, description, order_key, is_archived, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
          title = excluded.title,
          subject = excluded.subject,
          grade_level = excluded.grade_level,
          description = excluded.description,
          order_key = excluded.order_key,
          is_archived = excluded.is_archived,
          updated_at = excluded.updated_at`;
      await db.prepare(sql).bind(
        course.id,
        course.familyCode,
        course.title,
        course.subject,
        course.gradeLevel || 9,
        course.description || '',
        course.orderKey || 1000.0,
        course.isArchived ? 1 : 0,
        course.createdAt,
        course.updatedAt
      ).run();
      return course;
    },

    // Lessons
    async getLessons(familyCode, courseId = null, includeArchived = false) {
      let sql = 'SELECT * FROM lessons WHERE family_code = ?';
      const params = [familyCode];
      if (courseId) {
        sql += ' AND course_id = ?';
        params.push(courseId);
      }
      if (!includeArchived) sql += ' AND is_archived = 0';
      sql += ' ORDER BY order_key ASC';
      const result = await db.prepare(sql).bind(...params).all();
      return (result.results || []).map(mapDbLesson);
    },

    async getLessonById(familyCode, lessonId) {
      const row = await db.prepare('SELECT * FROM lessons WHERE id = ? AND family_code = ?').bind(lessonId, familyCode).first();
      return row ? mapDbLesson(row) : null;
    },

    async saveLesson(lesson) {
      const sql = `INSERT INTO lessons (id, course_id, family_code, title, order_key, is_archived, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
          title = excluded.title,
          order_key = excluded.order_key,
          is_archived = excluded.is_archived,
          updated_at = excluded.updated_at`;
      await db.prepare(sql).bind(
        lesson.id,
        lesson.courseId,
        lesson.familyCode,
        lesson.title,
        lesson.orderKey || 1000.0,
        lesson.isArchived ? 1 : 0,
        lesson.createdAt,
        lesson.updatedAt
      ).run();
      return lesson;
    },

    // Learning Items
    async getItems(familyCode, lessonId = null, includeArchived = false) {
      let sql = 'SELECT * FROM learning_items WHERE family_code = ?';
      const params = [familyCode];
      if (lessonId) {
        sql += ' AND lesson_id = ?';
        params.push(lessonId);
      }
      if (!includeArchived) sql += ' AND is_archived = 0';
      sql += ' ORDER BY order_key ASC';
      const result = await db.prepare(sql).bind(...params).all();
      return (result.results || []).map(mapDbItem);
    },

    async getItemById(familyCode, itemId) {
      const row = await db.prepare('SELECT * FROM learning_items WHERE id = ? AND family_code = ?').bind(itemId, familyCode).first();
      return row ? mapDbItem(row) : null;
    },

    async saveItem(item) {
      const sql = `INSERT INTO learning_items (id, lesson_id, family_code, item_type, display_label, stable_key, order_key, current_version_id, is_archived, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(id) DO UPDATE SET
          item_type = excluded.item_type,
          display_label = excluded.display_label,
          order_key = excluded.order_key,
          current_version_id = excluded.current_version_id,
          is_archived = excluded.is_archived,
          updated_at = excluded.updated_at`;
      await db.prepare(sql).bind(
        item.id,
        item.lessonId,
        item.familyCode,
        item.itemType,
        item.displayLabel,
        item.stableKey,
        item.orderKey || 1000.0,
        item.currentVersionId,
        item.isArchived ? 1 : 0,
        item.createdAt,
        item.updatedAt
      ).run();
      return item;
    },

    async updateItemOrderKey(familyCode, itemId, newOrderKey) {
      await db.prepare('UPDATE learning_items SET order_key = ?, updated_at = ? WHERE id = ? AND family_code = ?')
        .bind(newOrderKey, Date.now(), itemId, familyCode).run();
    },

    // Learning Item Versions
    async getVersions(itemId) {
      const result = await db.prepare('SELECT * FROM learning_item_versions WHERE item_id = ? ORDER BY version_number ASC').bind(itemId).all();
      return (result.results || []).map(mapDbVersion);
    },

    async getVersionById(versionId) {
      const row = await db.prepare('SELECT * FROM learning_item_versions WHERE id = ?').bind(versionId).first();
      return row ? mapDbVersion(row) : null;
    },

    async saveVersion(version) {
      const sql = `INSERT INTO learning_item_versions (id, item_id, version_number, title, content_url, payload_json, changelog, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)`;
      await db.prepare(sql).bind(
        version.id,
        version.itemId,
        version.versionNumber,
        version.title,
        version.contentUrl || null,
        typeof version.payload === 'string' ? version.payload : JSON.stringify(version.payload || {}),
        version.changelog || '',
        version.createdAt
      ).run();
      return version;
    },

    // Prerequisites
    async getPrerequisites(familyCode, itemId = null) {
      let sql = `SELECT p.* FROM item_prerequisites p
        JOIN learning_items i ON p.item_id = i.id
        WHERE i.family_code = ?`;
      const params = [familyCode];
      if (itemId) {
        sql += ' AND p.item_id = ?';
        params.push(itemId);
      }
      const result = await db.prepare(sql).bind(...params).all();
      return (result.results || []).map(mapDbPrereq);
    },

    async savePrerequisite(prereq) {
      const sql = `INSERT INTO item_prerequisites (id, item_id, required_item_id, min_score, created_at)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT(item_id, required_item_id) DO UPDATE SET min_score = excluded.min_score`;
      await db.prepare(sql).bind(
        prereq.id,
        prereq.itemId,
        prereq.requiredItemId,
        prereq.minScore !== undefined ? prereq.minScore : null,
        prereq.createdAt
      ).run();
      return prereq;
    },

    async deletePrerequisite(itemId, requiredItemId) {
      await db.prepare('DELETE FROM item_prerequisites WHERE item_id = ? AND required_item_id = ?')
        .bind(itemId, requiredItemId).run();
    },

    // Attempts
    async getAttemptByClientId(familyCode, studentId, clientAttemptId) {
      const row = await db.prepare('SELECT * FROM attempts WHERE family_code = ? AND student_id = ? AND client_attempt_id = ?')
        .bind(familyCode, studentId, clientAttemptId).first();
      return row ? mapDbAttempt(row) : null;
    },

    async getAttempts(familyCode, filter = {}) {
      let sql = 'SELECT * FROM attempts WHERE family_code = ?';
      const params = [familyCode];
      if (filter.studentId) {
        sql += ' AND student_id = ?';
        params.push(filter.studentId);
      }
      if (filter.itemId) {
        sql += ' AND item_id = ?';
        params.push(filter.itemId);
      }
      sql += ' ORDER BY created_at DESC';
      if (filter.limit && Number.isInteger(filter.limit)) {
        sql += ` LIMIT ${filter.limit}`;
      }
      const result = await db.prepare(sql).bind(...params).all();
      return (result.results || []).map(mapDbAttempt);
    },

    async saveAttempt(attempt) {
      const sql = `INSERT INTO attempts (id, client_attempt_id, family_code, student_id, item_id, version_id, status, score, duration_seconds, started_at, completed_at, metadata_json, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`;
      await db.prepare(sql).bind(
        attempt.id,
        attempt.clientAttemptId,
        attempt.familyCode,
        attempt.studentId,
        attempt.itemId,
        attempt.versionId,
        attempt.status,
        attempt.score !== undefined ? attempt.score : null,
        attempt.durationSeconds || 0,
        attempt.startedAt,
        attempt.completedAt || null,
        typeof attempt.metadata === 'string' ? attempt.metadata : JSON.stringify(attempt.metadata || {}),
        attempt.createdAt
      ).run();
      return attempt;
    },

    // Quiz Answers
    async saveQuizAnswers(answers) {
      if (!answers || answers.length === 0) return;
      for (const a of answers) {
        await db.prepare(`INSERT INTO quiz_answers (id, attempt_id, question_id, question_index, selected_option, is_correct, duration_seconds, created_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?)`).bind(
            a.id,
            a.attemptId,
            a.questionId,
            a.questionIndex,
            a.selectedOption || null,
            a.isCorrect ? 1 : 0,
            a.durationSeconds || 0,
            a.createdAt
          ).run();
      }
    },

    async getQuizAnswers(attemptId) {
      const result = await db.prepare('SELECT * FROM quiz_answers WHERE attempt_id = ? ORDER BY question_index ASC').bind(attemptId).all();
      return (result.results || []).map(mapDbQuizAnswer);
    }
  };
}

function createKVFallbackStorage(env, inMemoryStore) {
  const kv = env?.STUDY_SYNC_KV;

  async function readKey(key) {
    if (kv) {
      const raw = await kv.get(key);
      return raw ? JSON.parse(raw) : null;
    }
    return inMemoryStore?.get(key) || null;
  }

  async function writeKey(key, data) {
    if (kv) {
      await kv.put(key, JSON.stringify(data));
    } else if (inMemoryStore) {
      inMemoryStore.set(key, data);
    }
  }

  return {
    type: 'kv_fallback',

    // Courses
    async getCourses(familyCode, includeArchived = false) {
      const list = (await readKey(`v2:${familyCode}:courses`)) || [];
      return list
        .filter(c => includeArchived || !c.isArchived)
        .sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    },

    async getCourseById(familyCode, courseId) {
      const list = (await readKey(`v2:${familyCode}:courses`)) || [];
      return list.find(c => c.id === courseId) || null;
    },

    async saveCourse(course) {
      const key = `v2:${course.familyCode}:courses`;
      const list = (await readKey(key)) || [];
      const idx = list.findIndex(c => c.id === course.id);
      if (idx >= 0) {
        list[idx] = { ...list[idx], ...course, updatedAt: Date.now() };
      } else {
        list.push(course);
      }
      await writeKey(key, list);
      return course;
    },

    // Lessons
    async getLessons(familyCode, courseId = null, includeArchived = false) {
      const list = (await readKey(`v2:${familyCode}:lessons`)) || [];
      return list
        .filter(l => (!courseId || l.courseId === courseId) && (includeArchived || !l.isArchived))
        .sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    },

    async getLessonById(familyCode, lessonId) {
      const list = (await readKey(`v2:${familyCode}:lessons`)) || [];
      return list.find(l => l.id === lessonId) || null;
    },

    async saveLesson(lesson) {
      const key = `v2:${lesson.familyCode}:lessons`;
      const list = (await readKey(key)) || [];
      const idx = list.findIndex(l => l.id === lesson.id);
      if (idx >= 0) {
        list[idx] = { ...list[idx], ...lesson, updatedAt: Date.now() };
      } else {
        list.push(lesson);
      }
      await writeKey(key, list);
      return lesson;
    },

    // Learning Items
    async getItems(familyCode, lessonId = null, includeArchived = false) {
      const list = (await readKey(`v2:${familyCode}:items`)) || [];
      return list
        .filter(i => (!lessonId || i.lessonId === lessonId) && (includeArchived || !i.isArchived))
        .sort((a, b) => (a.orderKey || 0) - (b.orderKey || 0));
    },

    async getItemById(familyCode, itemId) {
      const list = (await readKey(`v2:${familyCode}:items`)) || [];
      return list.find(i => i.id === itemId) || null;
    },

    async saveItem(item) {
      const key = `v2:${item.familyCode}:items`;
      const list = (await readKey(key)) || [];
      const idx = list.findIndex(i => i.id === item.id);
      if (idx >= 0) {
        list[idx] = { ...list[idx], ...item, updatedAt: Date.now() };
      } else {
        list.push(item);
      }
      await writeKey(key, list);
      return item;
    },

    async updateItemOrderKey(familyCode, itemId, newOrderKey) {
      const key = `v2:${familyCode}:items`;
      const list = (await readKey(key)) || [];
      const item = list.find(i => i.id === itemId);
      if (item) {
        item.orderKey = newOrderKey;
        item.updatedAt = Date.now();
        await writeKey(key, list);
      }
    },

    // Versions
    async getVersions(itemId) {
      const list = (await readKey(`v2:versions:${itemId}`)) || [];
      return list.sort((a, b) => a.versionNumber - b.versionNumber);
    },

    async getVersionById(versionId) {
      const itemId = versionId.split('_v')[0].replace('ver_', '');
      const list = (await readKey(`v2:versions:${itemId}`)) || [];
      return list.find(v => v.id === versionId) || null;
    },

    async saveVersion(version) {
      const key = `v2:versions:${version.itemId}`;
      const list = (await readKey(key)) || [];
      list.push(version);
      await writeKey(key, list);
      return version;
    },

    // Prerequisites
    async getPrerequisites(familyCode, itemId = null) {
      const list = (await readKey(`v2:${familyCode}:prereqs`)) || [];
      if (itemId) {
        return list.filter(p => p.itemId === itemId);
      }
      return list;
    },

    async savePrerequisite(prereq) {
      const key = `v2:prereqs`;
      const itemKey = `v2:${prereq.familyCode}:prereqs`;
      const list = (await readKey(itemKey)) || [];
      const idx = list.findIndex(p => p.itemId === prereq.itemId && p.requiredItemId === prereq.requiredItemId);
      if (idx >= 0) {
        list[idx] = prereq;
      } else {
        list.push(prereq);
      }
      await writeKey(itemKey, list);
      return prereq;
    },

    async deletePrerequisite(familyCode, itemId, requiredItemId) {
      const itemKey = `v2:${familyCode}:prereqs`;
      let list = (await readKey(itemKey)) || [];
      list = list.filter(p => !(p.itemId === itemId && p.requiredItemId === requiredItemId));
      await writeKey(itemKey, list);
    },

    // Attempts
    async getAttemptByClientId(familyCode, studentId, clientAttemptId) {
      const list = (await readKey(`v2:${familyCode}:attempts`)) || [];
      return list.find(a => a.studentId === studentId && a.clientAttemptId === clientAttemptId) || null;
    },

    async getAttempts(familyCode, filter = {}) {
      let list = (await readKey(`v2:${familyCode}:attempts`)) || [];
      if (filter.studentId) {
        list = list.filter(a => a.studentId === filter.studentId);
      }
      if (filter.itemId) {
        list = list.filter(a => a.itemId === filter.itemId);
      }
      list.sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
      if (filter.limit && Number.isInteger(filter.limit)) {
        list = list.slice(0, filter.limit);
      }
      return list;
    },

    async saveAttempt(attempt) {
      const key = `v2:${attempt.familyCode}:attempts`;
      const list = (await readKey(key)) || [];
      list.unshift(attempt);
      await writeKey(key, list);
      return attempt;
    },

    // Quiz Answers
    async saveQuizAnswers(answers) {
      if (!answers || answers.length === 0) return;
      for (const a of answers) {
        const key = `v2:quiz_answers:${a.attemptId}`;
        const list = (await readKey(key)) || [];
        list.push(a);
        await writeKey(key, list);
      }
    },

    async getQuizAnswers(attemptId) {
      const key = `v2:quiz_answers:${attemptId}`;
      return (await readKey(key)) || [];
    }
  };
}

// Helpers to map DB row names to camelCase objects
function mapDbCourse(row) {
  return {
    id: row.id,
    familyCode: row.family_code,
    title: row.title,
    subject: row.subject,
    gradeLevel: row.grade_level,
    description: row.description || '',
    orderKey: row.order_key,
    isArchived: Boolean(row.is_archived),
    createdAt: row.created_at,
    updatedAt: row.updated_at
  };
}

function mapDbLesson(row) {
  return {
    id: row.id,
    courseId: row.course_id,
    familyCode: row.family_code,
    title: row.title,
    orderKey: row.order_key,
    isArchived: Boolean(row.is_archived),
    createdAt: row.created_at,
    updatedAt: row.updated_at
  };
}

function mapDbItem(row) {
  return {
    id: row.id,
    lessonId: row.lesson_id,
    familyCode: row.family_code,
    itemType: row.item_type,
    displayLabel: row.display_label,
    stableKey: row.stable_key,
    orderKey: row.order_key,
    currentVersionId: row.current_version_id,
    isArchived: Boolean(row.is_archived),
    createdAt: row.created_at,
    updatedAt: row.updated_at
  };
}

function mapDbVersion(row) {
  let payload = null;
  try {
    payload = row.payload_json ? JSON.parse(row.payload_json) : null;
  } catch (_) {
    payload = row.payload_json;
  }
  return {
    id: row.id,
    itemId: row.item_id,
    versionNumber: row.version_number,
    title: row.title,
    contentUrl: row.content_url || null,
    payload,
    changelog: row.changelog || '',
    createdAt: row.created_at
  };
}

function mapDbPrereq(row) {
  return {
    id: row.id,
    itemId: row.item_id,
    requiredItemId: row.required_item_id,
    minScore: row.min_score !== null && row.min_score !== undefined ? Number(row.min_score) : null,
    createdAt: row.created_at
  };
}

function mapDbAttempt(row) {
  let metadata = null;
  try {
    metadata = row.metadata_json ? JSON.parse(row.metadata_json) : null;
  } catch (_) {
    metadata = row.metadata_json;
  }
  return {
    id: row.id,
    clientAttemptId: row.client_attempt_id,
    familyCode: row.family_code,
    studentId: row.student_id,
    itemId: row.item_id,
    versionId: row.version_id,
    status: row.status,
    score: row.score !== null && row.score !== undefined ? Number(row.score) : null,
    durationSeconds: row.duration_seconds || 0,
    startedAt: row.started_at,
    completedAt: row.completed_at || null,
    metadata,
    createdAt: row.created_at
  };
}

function mapDbQuizAnswer(row) {
  return {
    id: row.id,
    attemptId: row.attempt_id,
    questionId: row.question_id,
    questionIndex: row.question_index,
    selectedOption: row.selected_option || null,
    isCorrect: Boolean(row.is_correct),
    durationSeconds: row.duration_seconds || 0,
    createdAt: row.created_at
  };
}
