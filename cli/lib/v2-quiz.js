/**
 * StudyTracker V2 Deterministic Quiz Schema Validator & Authoring Workflow
 * Supported types: MULTIPLE_CHOICE and TRUE_FALSE only.
 * No AI generation. Pure deterministic rules.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const v2Api = require('./v2-api');

function sha256(text) {
  return crypto.createHash('sha256').update(text).digest('hex').slice(0, 16);
}

function normalizeTrueFalseAnswer(val) {
  if (typeof val === 'boolean') return val ? 'TRUE' : 'FALSE';
  if (typeof val === 'string') {
    const s = val.trim().toUpperCase();
    if (s === 'TRUE' || s === 'DOĞRU' || s === 'D' || s === 'T' || s === '1') return 'TRUE';
    if (s === 'FALSE' || s === 'YANLIŞ' || s === 'Y' || s === 'F' || s === '0') return 'FALSE';
  }
  return null;
}

function computeQuizFingerprint(normalizedQuiz) {
  const stableQuestions = (normalizedQuiz.questions || []).map(q => ({
    id: q.id,
    type: q.type,
    prompt: q.prompt,
    choices: Array.isArray(q.choices) ? [...q.choices] : null,
    correctAnswer: q.correctAnswer,
    explanation: q.explanation || null
  }));

  const stableObj = {
    quizTitle: normalizedQuiz.quizTitle || normalizedQuiz.title || '',
    questions: stableQuestions
  };

  return sha256(JSON.stringify(stableObj));
}

function loadQuizFile(filePathOrObject) {
  if (typeof filePathOrObject === 'object' && filePathOrObject !== null) {
    return filePathOrObject;
  }
  const targetPath = path.resolve(process.cwd(), filePathOrObject);
  if (!fs.existsSync(targetPath)) {
    throw new Error(`Quiz file not found: ${targetPath}`);
  }
  const raw = fs.readFileSync(targetPath, 'utf8');
  return JSON.parse(raw);
}

/**
 * Validate quiz JSON against V2 deterministic schema
 */
function validateQuizSchema(quizInput) {
  const quiz = typeof quizInput === 'string' || (typeof quizInput === 'object' && quizInput !== null)
    ? (typeof quizInput === 'string' ? loadQuizFile(quizInput) : quizInput)
    : null;

  const errors = [];

  if (!quiz || typeof quiz !== 'object') {
    return { valid: false, errors: ['Quiz payload must be a JSON object'], normalizedQuiz: null, fingerprint: null };
  }

  const title = (quiz.quizTitle || quiz.title || '').trim();
  if (!title) {
    errors.push('Quiz must have a non-empty "quizTitle" or "title"');
  }

  if (!Array.isArray(quiz.questions) || quiz.questions.length === 0) {
    errors.push('Quiz must contain a non-empty "questions" array (minimum 1 question required)');
    return { valid: false, errors, normalizedQuiz: null, fingerprint: null };
  }

  const seenIds = new Set();
  const normalizedQuestions = [];

  quiz.questions.forEach((q, idx) => {
    const qNum = idx + 1;
    if (!q || typeof q !== 'object') {
      errors.push(`Question #${qNum} must be an object`);
      return;
    }

    const qId = (q.id || '').trim();
    if (!qId) {
      errors.push(`Question #${qNum} is missing required "id"`);
    } else if (seenIds.has(qId)) {
      errors.push(`Duplicate question id: "${qId}" at question #${qNum}`);
    } else {
      seenIds.add(qId);
    }

    const prompt = (q.prompt || '').trim();
    if (!prompt) {
      errors.push(`Question #${qNum} ("${qId || 'unnamed'}") must have a non-empty "prompt"`);
    }

    const type = (q.type || '').toUpperCase().trim();
    if (!['MULTIPLE_CHOICE', 'TRUE_FALSE'].includes(type)) {
      errors.push(`Question #${qNum} ("${qId}") has invalid type: "${q.type}". Supported types are MULTIPLE_CHOICE and TRUE_FALSE only.`);
      return;
    }

    let normalizedChoices = null;
    let normalizedCorrect = null;

    if (type === 'MULTIPLE_CHOICE') {
      if (!Array.isArray(q.choices) || q.choices.length < 2) {
        errors.push(`Question #${qNum} ("${qId}") must have a "choices" array with at least 2 choices`);
      } else {
        normalizedChoices = q.choices.map(c => (typeof c === 'string' ? c.trim() : String(c).trim()));
        if (normalizedChoices.some(c => !c)) {
          errors.push(`Question #${qNum} ("${qId}") choices cannot contain empty strings`);
        }

        const rawCorrect = (q.correctAnswer !== undefined && q.correctAnswer !== null ? String(q.correctAnswer) : '').trim();
        if (!rawCorrect) {
          errors.push(`Question #${qNum} ("${qId}") must specify a non-empty "correctAnswer"`);
        } else if (!normalizedChoices.includes(rawCorrect)) {
          errors.push(`Question #${qNum} ("${qId}") correctAnswer "${rawCorrect}" is not found in choices: [${normalizedChoices.join(', ')}]`);
        } else {
          normalizedCorrect = rawCorrect;
        }
      }
    } else if (type === 'TRUE_FALSE') {
      const canonical = normalizeTrueFalseAnswer(q.correctAnswer);
      if (canonical === null) {
        errors.push(`Question #${qNum} ("${qId}") TRUE_FALSE question must have boolean or canonical correctAnswer (e.g. true, false, "TRUE", "FALSE", "Doğru", "Yanlış")`);
      } else {
        normalizedCorrect = canonical;
        normalizedChoices = ['TRUE', 'FALSE'];
      }
    }

    const explanation = q.explanation ? String(q.explanation).trim() : null;

    normalizedQuestions.push({
      id: qId,
      questionIndex: idx,
      type,
      prompt,
      choices: normalizedChoices,
      correctAnswer: normalizedCorrect,
      explanation
    });
  });

  const valid = errors.length === 0;
  const normalizedQuiz = valid ? {
    quizTitle: title,
    questions: normalizedQuestions,
    questionCount: normalizedQuestions.length,
    schemaVersion: 'v2-quiz'
  } : null;

  const fingerprint = normalizedQuiz ? computeQuizFingerprint(normalizedQuiz) : null;

  return {
    valid,
    errors,
    normalizedQuiz,
    fingerprint,
    stats: {
      questionCount: normalizedQuestions.length,
      multipleChoiceCount: normalizedQuestions.filter(q => q.type === 'MULTIPLE_CHOICE').length,
      trueFalseCount: normalizedQuestions.filter(q => q.type === 'TRUE_FALSE').length
    }
  };
}

/**
 * Attach validated quiz to existing QUIZ learning item (creates new version)
 */
async function attachQuizToItem(familyCode, stableKeyOrId, quizFilePathOrObject, { note } = {}) {
  const validation = validateQuizSchema(quizFilePathOrObject);
  if (!validation.valid) {
    throw new Error(`Quiz validation failed:\n  - ${validation.errors.join('\n  - ')}`);
  }

  const catalogRes = await v2Api.fetchV2Catalog(familyCode, null);
  const curriculum = catalogRes.curriculum || [];
  
  let targetItem = null;
  for (const course of curriculum) {
    for (const lesson of course.lessons || []) {
      for (const item of lesson.items || []) {
        if (item.stableKey === stableKeyOrId || item.id === stableKeyOrId) {
          targetItem = item;
          break;
        }
      }
      if (targetItem) break;
    }
    if (targetItem) break;
  }

  if (!targetItem) {
    throw new Error(`Learning item not found for key/id: ${stableKeyOrId}`);
  }

  const currentVer = targetItem.currentVersion || {};
  const currentPayload = currentVer.payload || {};
  const currentProv = currentPayload.provenance || {};
  const reviewHistory = Array.isArray(currentProv.reviewHistory) ? [...currentProv.reviewHistory] : [];
  
  const timestamp = new Date().toISOString();
  reviewHistory.push({
    action: 'ATTACH_QUIZ',
    reviewer: 'cli_author',
    note: note || `Attached quiz with ${validation.stats.questionCount} questions`,
    fingerprint: validation.fingerprint,
    timestamp
  });

  const updatedPayload = {
    ...currentPayload,
    quiz: validation.normalizedQuiz,
    provenance: {
      ...currentProv,
      fingerprint: validation.fingerprint,
      reviewStatus: 'verified',
      reviewedOverride: true,
      reviewedAt: timestamp,
      reviewHistory
    }
  };

  const updateRes = await v2Api.updateV2ItemContent(familyCode, targetItem.id, {
    title: targetItem.currentVersion?.title || validation.normalizedQuiz.quizTitle,
    payload: updatedPayload,
    changelog: note || `Updated quiz payload (fp: ${validation.fingerprint})`,
    publishingStatus: 'active'
  });

  return {
    success: true,
    itemId: targetItem.id,
    stableKey: targetItem.stableKey,
    versionNumber: updateRes.item?.currentVersion?.versionNumber || 2,
    fingerprint: validation.fingerprint,
    item: updateRes.item
  };
}

/**
 * Create new QUIZ learning item with puzzle positioning (e.g. Quiz 17.2 between Quiz 17 and Video 18)
 */
async function createQuizItem(familyCode, {
  lessonIdOrKey,
  displayLabel,
  stableKey,
  quizFilePathOrObject,
  title,
  beforeTarget,
  afterTarget
}) {
  const validation = validateQuizSchema(quizFilePathOrObject);
  if (!validation.valid) {
    throw new Error(`Quiz validation failed:\n  - ${validation.errors.join('\n  - ')}`);
  }

  // Find lesson and target item if positioning specified
  const catalogRes = await v2Api.fetchV2Catalog(familyCode, null);
  const curriculum = catalogRes.curriculum || [];

  let resolvedLessonId = lessonIdOrKey;
  let targetItemId = null;
  let position = null;

  for (const course of curriculum) {
    for (const lesson of course.lessons || []) {
      if (lesson.id === lessonIdOrKey || lesson.stableKey === lessonIdOrKey) {
        resolvedLessonId = lesson.id;
      }
      for (const item of lesson.items || []) {
        if (beforeTarget && (item.id === beforeTarget || item.stableKey === beforeTarget)) {
          targetItemId = item.id;
          position = 'before';
          resolvedLessonId = lesson.id;
        }
        if (afterTarget && (item.id === afterTarget || item.stableKey === afterTarget)) {
          targetItemId = item.id;
          position = 'after';
          resolvedLessonId = lesson.id;
        }
      }
    }
  }

  if (!resolvedLessonId) {
    throw new Error(`Lesson not found: ${lessonIdOrKey}`);
  }

  const effectiveTitle = title || validation.normalizedQuiz.quizTitle || displayLabel;
  const now = new Date().toISOString();

  const payload = {
    quiz: validation.normalizedQuiz,
    provenance: {
      sourceRef: 'cli_authoring',
      fingerprint: validation.fingerprint,
      reviewStatus: 'verified',
      reviewedOverride: true,
      importedAt: now,
      schemaVersion: 'v2'
    }
  };

  const itemData = {
    lessonId: resolvedLessonId,
    itemType: 'QUIZ',
    displayLabel,
    stableKey: stableKey || `quiz_${Date.now()}`,
    title: effectiveTitle,
    payload,
    position,
    targetItemId,
    publishingStatus: 'active'
  };

  const createRes = await v2Api.createV2Item(familyCode, itemData);

  return {
    success: true,
    item: createRes.item,
    fingerprint: validation.fingerprint
  };
}

module.exports = {
  validateQuizSchema,
  computeQuizFingerprint,
  attachQuizToItem,
  createQuizItem,
  loadQuizFile
};
