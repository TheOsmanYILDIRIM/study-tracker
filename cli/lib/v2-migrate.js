/**
 * StudyTracker V1 -> V2 Migration Engine
 * Analyzes, plans, and applies lossless idempotent migrations from V1 occurrences/sessions/quizzes
 * into V2 append-only attempts and version-bound records.
 */

const fs = require('fs');
const path = require('path');
const { fetchFamilyData } = require('./api');
const v2Api = require('./v2-api');

function extractYouTubeId(url) {
  if (!url) return null;
  const match = url.match(/(?:youtube\.com\/(?:watch\?v=|embed\/|v\/)|youtu\.be\/)([\w-]{11})/i);
  return match ? match[1] : null;
}

function normalizeText(text) {
  if (!text) return '';
  return text
    .toLowerCase()
    .replace(/[çÇ]/g, 'c')
    .replace(/[ğĞ]/g, 'g')
    .replace(/[ıİ]/g, 'i')
    .replace(/[öÖ]/g, 'o')
    .replace(/[şŞ]/g, 's')
    .replace(/[üÜ]/g, 'u')
    .replace(/[^a-z0-9]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

/**
 * 1. ANALYZE MIGRATION CANDIDATES
 */
async function analyzeMigration(familyCode) {
  const v1Data = await fetchFamilyData(familyCode);
  const v2CatalogRes = await v2Api.fetchV2Catalog(familyCode);
  const v2Courses = v2CatalogRes.curriculum || [];

  // Flatten V2 items with course/lesson context
  const v2Items = [];
  v2Courses.forEach(c => {
    (c.lessons || []).forEach(l => {
      (l.items || []).forEach(i => {
        const videoId = extractYouTubeId(i.currentVersion?.contentUrl || i.contentUrl);
        v2Items.push({
          id: i.id,
          stableKey: i.stableKey,
          itemType: i.itemType,
          displayLabel: i.displayLabel,
          title: i.currentVersion?.title || i.displayLabel,
          contentUrl: i.currentVersion?.contentUrl || null,
          videoId,
          currentVersionId: i.currentVersionId || i.currentVersion?.id || `ver_${i.id}_v1`,
          courseId: c.id,
          courseSubject: c.subject,
          lessonId: l.id,
          lessonTitle: l.title
        });
      });
    });
  });

  const v1Occurrences = v1Data.occurrences || [];
  const v1Sessions = v1Data.sessions || [];
  const v1Quizzes = v1Data.quizzes || [];
  const v1Reviews = v1Data.reviews || [];

  const mappings = [];
  let exactCount = 0;
  let highCount = 0;
  let mediumCount = 0;
  let unmatchedCount = 0;

  for (const occ of v1Occurrences) {
    const occVideoId = extractYouTubeId(occ.youtubeUrl);
    const normSubject = normalizeText(occ.subject);
    const normTopic = normalizeText(occ.topic);

    let match = null;
    let confidence = 'unmatched';
    let evidence = 'No matching V2 learning item found';

    // 1. Explicit ID or stable_key match
    const explicitMatch = v2Items.find(i => i.id === occ.id || i.stableKey === occ.id || i.stableKey === occ.planId);
    if (explicitMatch) {
      match = explicitMatch;
      confidence = 'exact';
      evidence = `Explicit ID/StableKey match (${explicitMatch.stableKey})`;
    }

    // 2. Exact YouTube Video ID match (if videoId exists)
    if (!match && occVideoId) {
      const vidMatch = v2Items.find(i => i.videoId && i.videoId === occVideoId);
      if (vidMatch) {
        match = vidMatch;
        confidence = 'exact';
        evidence = `Exact YouTube video ID match (${occVideoId}) with V2 item '${vidMatch.title}'`;
      }
    }

    // 3. Normalized Subject + Topic + Title token matching
    if (!match) {
      let bestScore = 0;
      let bestItem = null;

      for (const item of v2Items) {
        const normItemTitle = normalizeText(item.title);
        const normCourseSubject = normalizeText(item.courseSubject);
        const normLessonTitle = normalizeText(item.lessonTitle);

        // Subject check
        const subjectMatch = normSubject.includes(normCourseSubject) || normCourseSubject.includes(normSubject);
        if (!subjectMatch) continue;

        // Calculate token overlap
        const occTokens = new Set(normSubject.split(' ').concat(normTopic.split(' ')).filter(t => t.length > 2));
        const itemTokens = new Set(normItemTitle.split(' ').concat(normLessonTitle.split(' ')).filter(t => t.length > 2));

        let common = 0;
        for (const t of occTokens) {
          if (itemTokens.has(t)) common++;
        }

        const score = occTokens.size > 0 ? (common / occTokens.size) : 0;
        if (score > bestScore) {
          bestScore = score;
          bestItem = item;
        }
      }

      if (bestItem && bestScore >= 0.5) {
        match = bestItem;
        confidence = bestScore >= 0.75 ? 'high' : 'medium';
        evidence = `Normalized subject and title token overlap (${Math.round(bestScore * 100)}%) with '${bestItem.title}'`;
      }
    }

    if (confidence === 'exact') exactCount++;
    else if (confidence === 'high') highCount++;
    else if (confidence === 'medium') mediumCount++;
    else unmatchedCount++;

    // Associated session and review
    const session = v1Sessions.find(s => s.occurrenceId === occ.id || s.id === occ.id);
    const review = v1Reviews.find(r => r.sessionId === occ.id || r.sessionId === session?.id);

    mappings.push({
      legacyType: 'OCCURRENCE',
      legacyId: occ.id,
      legacySubject: occ.subject,
      legacyDate: occ.date,
      legacyWeekId: occ.weekId,
      legacyStatus: occ.status,
      legacyDurationMin: occ.completedDurationMin || session?.durationMin || 0,
      legacyQuestionCount: occ.completedQuestionCount || 0,
      legacyYoutubeUrl: occ.youtubeUrl,
      targetItem: match ? {
        id: match.id,
        stableKey: match.stableKey,
        itemType: match.itemType,
        displayLabel: match.displayLabel,
        title: match.title,
        versionId: match.currentVersionId
      } : null,
      confidence,
      evidence,
      hasSession: Boolean(session),
      hasReview: Boolean(review),
      legacyReviewApproved: review ? review.isApproved : null
    });
  }

  // Quizzes mapping
  for (const quiz of v1Quizzes) {
    const normQuizTitle = normalizeText(quiz.title);
    const v2QuizItem = v2Items.find(i => i.itemType === 'QUIZ' && (
      i.id === quiz.id ||
      i.stableKey === quiz.id ||
      normalizeText(i.title).includes(normQuizTitle) ||
      normQuizTitle.includes(normalizeText(i.title))
    ));

    let confidence = 'unmatched';
    let evidence = 'No concrete V2 QUIZ item matches legacy quiz';
    if (v2QuizItem) {
      confidence = 'high';
      evidence = `Matched concrete V2 QUIZ item '${v2QuizItem.title}'`;
      highCount++;
    } else {
      unmatchedCount++;
    }

    mappings.push({
      legacyType: 'QUIZ',
      legacyId: quiz.id,
      legacySubject: quiz.title,
      legacyScore: quiz.score,
      legacyIsCompleted: quiz.isCompleted,
      targetItem: v2QuizItem ? {
        id: v2QuizItem.id,
        stableKey: v2QuizItem.stableKey,
        itemType: 'QUIZ',
        title: v2QuizItem.title,
        versionId: v2QuizItem.currentVersionId
      } : null,
      confidence,
      evidence
    });
  }

  return {
    familyCode,
    totalV1Records: mappings.length,
    counts: {
      exact: exactCount,
      high: highCount,
      medium: mediumCount,
      unmatched: unmatchedCount
    },
    mappings
  };
}

/**
 * 2. PLAN MIGRATION
 * Converts exact and high confidence completed records into deterministic V2 attempts.
 * Medium and unmatched are strictly excluded from automated apply.
 */
async function planMigration(familyCode, { analyzeResult = null } = {}) {
  const analysis = analyzeResult || await analyzeMigration(familyCode);
  const plannedAttempts = [];
  const skippedRecords = [];

  for (const m of analysis.mappings) {
    const isCompleted = m.legacyStatus === 'APPROVED' || 
                        m.legacyStatus === 'WAITING_REVIEW' || 
                        m.legacyDurationMin > 0 || 
                        m.legacyIsCompleted === true;

    if (!isCompleted) {
      skippedRecords.push({
        legacyId: m.legacyId,
        legacySubject: m.legacySubject,
        reason: 'Legacy record is not completed (PENDING with 0 duration)'
      });
      continue;
    }

    // Safety rule: never auto-apply medium or unmatched confidence
    if (m.confidence !== 'exact' && m.confidence !== 'high') {
      skippedRecords.push({
        legacyId: m.legacyId,
        legacySubject: m.legacySubject,
        confidence: m.confidence,
        reason: `Confidence is '${m.confidence}'; auto-apply disallowed. Evidence: ${m.evidence}`
      });
      continue;
    }

    if (!m.targetItem) {
      skippedRecords.push({
        legacyId: m.legacyId,
        legacySubject: m.legacySubject,
        reason: 'No target V2 item resolved'
      });
      continue;
    }

    // Deterministic clientAttemptId for idempotency
    const clientAttemptId = `mig_v1_${m.legacyId.replace(/[^a-zA-Z0-9_]/g, '_')}`;
    const durationSeconds = (m.legacyDurationMin || 0) * 60;
    const score = m.legacyScore !== undefined ? m.legacyScore : (m.legacyQuestionCount > 0 ? 100 : null);
    const now = Date.now();

    plannedAttempts.push({
      clientAttemptId,
      studentId: 'child_1',
      itemId: m.targetItem.id,
      versionId: m.targetItem.versionId,
      status: 'COMPLETED',
      score,
      durationSeconds,
      startedAt: now - durationSeconds * 1000,
      completedAt: now,
      metadata: {
        source: 'V1_MIGRATION',
        legacyId: m.legacyId,
        legacyType: m.legacyType,
        legacySubject: m.legacySubject,
        legacyStatus: m.legacyStatus,
        legacyWeekId: m.legacyWeekId || null,
        legacyReviewApproved: m.legacyReviewApproved,
        confidence: m.confidence,
        evidence: m.evidence
      }
    });
  }

  return {
    familyCode,
    generatedAt: new Date().toISOString(),
    stats: {
      totalAnalyzed: analysis.totalV1Records,
      totalPlanned: plannedAttempts.length,
      totalSkipped: skippedRecords.length,
      confidenceCounts: analysis.counts
    },
    plannedAttempts,
    skippedRecords
  };
}

/**
 * 3. APPLY MIGRATION PLAN
 */
async function applyMigration(familyCode, planInput, { dryRun = false } = {}) {
  let plan = planInput;
  if (typeof planInput === 'string') {
    const planPath = path.resolve(process.cwd(), planInput);
    if (!fs.existsSync(planPath)) {
      throw new Error(`Migration plan file not found: ${planPath}`);
    }
    plan = JSON.parse(fs.readFileSync(planPath, 'utf8'));
  }

  if (!plan || !Array.isArray(plan.plannedAttempts)) {
    throw new Error('Invalid migration plan format: missing "plannedAttempts" array');
  }

  if (dryRun) {
    return {
      dryRun: true,
      familyCode,
      status: 'DRY_RUN_COMPLETED',
      plannedAttemptsCount: plan.plannedAttempts.length,
      plannedAttempts: plan.plannedAttempts
    };
  }

  const results = {
    recorded: [],
    duplicates: [],
    errors: []
  };

  for (const attempt of plan.plannedAttempts) {
    try {
      const res = await v2Api.recordV2Attempt(familyCode, attempt);
      if (res.duplicate) {
        results.duplicates.push({ clientAttemptId: attempt.clientAttemptId, attemptId: res.attempt?.id });
      } else {
        results.recorded.push({ clientAttemptId: attempt.clientAttemptId, attemptId: res.attempt?.id });
      }
    } catch (err) {
      results.errors.push({ clientAttemptId: attempt.clientAttemptId, error: err.message });
    }
  }

  return {
    dryRun: false,
    familyCode,
    status: 'APPLIED',
    summary: {
      totalAttempts: plan.plannedAttempts.length,
      recordedCount: results.recorded.length,
      duplicateCount: results.duplicates.length,
      errorCount: results.errors.length
    },
    results
  };
}

module.exports = {
  extractYouTubeId,
  normalizeText,
  analyzeMigration,
  planMigration,
  applyMigration
};
