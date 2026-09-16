package com.studytracker.core.data.local.repository

import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.data.plan_engine.PlanMergeEngine
import com.studytracker.core.data.plan_engine.PlanValidator
import com.studytracker.core.data.plan_engine.ValidationResult
import com.studytracker.core.domain.model.*
import com.studytracker.core.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class LocalPlanRepositoryImpl(
    private val db: AppDatabase
) : PlanRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    override fun getActivePlan(): Flow<Plan?> {
        return db.planDao().getActivePlan()
            .map { entity ->
                entity?.let {
                    try {
                        json.decodeFromString(Plan.serializer(), it.rawJson)
                    } catch (e: Exception) {
                        val (p, _) = PlanValidator.parseAndValidate(it.rawJson)
                        p
                    }
                }
            }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override suspend fun getActivePlanOnce(): Plan? {
        val entity = db.planDao().getActivePlanOnce() ?: return null
        return try {
            json.decodeFromString(Plan.serializer(), entity.rawJson)
        } catch (e: Exception) {
            val (p, _) = PlanValidator.parseAndValidate(entity.rawJson)
            p
        }
    }

    override suspend fun importPlanJson(planJson: String): Result<PlanImportResult> {
        val (newPlan, validation) = PlanValidator.parseAndValidate(planJson)
        if (validation is ValidationResult.Invalid || newPlan == null) {
            return Result.failure(Exception((validation as? ValidationResult.Invalid)?.reason ?: "Bilinmeyen doğrulama hatası"))
        }

        val currentPlan = getActivePlanOnce()
        val existingEntities = db.occurrenceDao().getAllOccurrencesOnce()
        val existingMap = existingEntities.associate { it.occurrenceKey to it.toDomain() }

        val (mergedOccurrences, importResult) = PlanMergeEngine.executeMerge(
            currentPlan = currentPlan,
            newPlan = newPlan,
            existingOccurrences = existingMap
        )

        // DB Transactions
        val taskEntities = newPlan.tasks.map { it.toEntity() }
        val occurrenceEntities = mergedOccurrences.map { it.toEntity() }
        val canonicalJson = try {
            json.encodeToString(newPlan)
        } catch (_: Exception) {
            planJson
        }

        val planEntity = PlanEntity(
            planId = newPlan.planId,
            weekId = newPlan.weekId,
            weekStartDate = newPlan.weekStartDate,
            childId = newPlan.childId,
            timezone = newPlan.timezone,
            updatedAt = newPlan.updatedAt,
            rawJson = canonicalJson
        )

        db.taskTemplateDao().upsertTasks(taskEntities)
        db.occurrenceDao().upsertOccurrences(occurrenceEntities)
        db.planDao().setActivePlan(planEntity)

        return Result.success(importResult)
    }

    override suspend fun exportCurrentPlanJson(): String {
        val plan = getActivePlanOnce() ?: return "{}"
        return json.encodeToString(plan)
    }

    override suspend fun clearAllPlanData() {
        db.taskTemplateDao().clearTasks()
        db.occurrenceDao().clearOccurrences()
        db.planDao().clearActivePlan()
    }
}

class LocalOccurrenceRepositoryImpl(
    private val db: AppDatabase
) : OccurrenceRepository {

    override fun getAllOccurrences(): Flow<List<Occurrence>> {
        return db.occurrenceDao().getAllOccurrences()
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override fun getDailyOccurrencesForDate(date: String): Flow<List<Occurrence>> {
        return db.occurrenceDao().getDailyOccurrencesForDate(date)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override fun getWeeklyOccurrences(weekId: String): Flow<List<Occurrence>> {
        return db.occurrenceDao().getWeeklyOccurrences(weekId)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override fun getOccurrenceByKey(key: String): Flow<Occurrence?> {
        return db.occurrenceDao().getOccurrenceByKey(key)
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override suspend fun getOccurrenceByKeyOnce(key: String): Occurrence? {
        return db.occurrenceDao().getOccurrenceByKeyOnce(key)?.toDomain()
    }

    override suspend fun upsertOccurrences(occurrences: List<Occurrence>) {
        db.occurrenceDao().upsertOccurrences(occurrences.map { it.toEntity() })
    }

    override suspend fun updateStatus(occurrenceKey: String, status: OccurrenceStatus) {
        db.occurrenceDao().updateStatus(occurrenceKey, status)
    }

    override suspend fun setWarning(occurrenceKey: String, warning: Boolean, note: String?) {
        db.occurrenceDao().setWarning(occurrenceKey, warning, note)
    }

    override suspend fun incrementApprovedCount(occurrenceKey: String): Occurrence {
        db.occurrenceDao().incrementApprovedCount(occurrenceKey)
        val entity = db.occurrenceDao().getOccurrenceByKeyOnce(occurrenceKey)
            ?: throw IllegalStateException("Occurrence not found: $occurrenceKey")
        val domain = entity.toDomain()
        val target = domain.targetCount ?: 1
        if (domain.approvedCount >= target) {
            db.occurrenceDao().updateStatus(occurrenceKey, OccurrenceStatus.APPROVED)
        }
        return db.occurrenceDao().getOccurrenceByKeyOnce(occurrenceKey)!!.toDomain()
    }
}

class LocalSessionRepositoryImpl(
    private val db: AppDatabase
) : SessionRepository {

    override fun getActiveSession(): Flow<Session?> {
        return db.sessionDao().getActiveSession()
            .map { it?.toDomain() }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override suspend fun getActiveSessionOnce(): Session? {
        return db.sessionDao().getActiveSessionOnce()?.toDomain()
    }

    override fun getWaitingReviewSessions(): Flow<List<Session>> {
        return db.sessionDao().getWaitingReviewSessions()
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override fun getSessionsForOccurrence(occurrenceKey: String): Flow<List<Session>> {
        return db.sessionDao().getSessionsForOccurrence(occurrenceKey)
            .map { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()
            .flowOn(kotlinx.coroutines.Dispatchers.IO)
    }

    override suspend fun getSessionById(sessionId: String): Session? {
        return db.sessionDao().getSessionById(sessionId)?.toDomain()
    }

    override suspend fun startSession(occurrenceKey: String, childId: String): Session {
        val session = Session(
            sessionId = "sess_" + UUID.randomUUID().toString().take(8),
            occurrenceKey = occurrenceKey,
            childId = childId,
            startTime = System.currentTimeMillis(),
            status = SessionStatus.ACTIVE
        )
        db.sessionDao().upsertSession(session.toEntity())
        db.occurrenceDao().updateStatus(occurrenceKey, OccurrenceStatus.ACTIVE)
        return session
    }

    override suspend fun finishSession(sessionId: String, finalScreenshotUrl: String?): Session {
        val existing = db.sessionDao().getSessionById(sessionId)
            ?: throw IllegalStateException("Session not found: $sessionId")

        val count = db.screenshotDao().getScreenshotsCount(sessionId)
        val updated = existing.copy(
            endTime = System.currentTimeMillis(),
            status = SessionStatus.WAITING_REVIEW,
            screenshotCount = count,
            finalScreenshotUrl = finalScreenshotUrl
        )
        db.sessionDao().upsertSession(updated)
        db.occurrenceDao().updateStatus(existing.occurrenceKey, OccurrenceStatus.WAITING_REVIEW)
        return updated.toDomain()
    }

    override suspend fun submitReview(review: Review) {
        db.reviewDao().insertReview(review.toEntity())

        val sessionStatus = if (review.reviewStatus == ReviewStatus.APPROVED) {
            SessionStatus.APPROVED
        } else {
            SessionStatus.REJECTED
        }

        val session = db.sessionDao().getSessionById(review.sessionId)
        if (session != null) {
            db.sessionDao().upsertSession(session.copy(status = sessionStatus))
        }

        val targetOccKey = session?.occurrenceKey ?: review.occurrenceKey
        val occurrence = db.occurrenceDao().getOccurrenceByKeyOnce(targetOccKey)
        if (occurrence != null) {
            if (review.reviewStatus == ReviewStatus.APPROVED) {
                if (occurrence.type == TaskKind.WEEKLY) {
                    db.occurrenceDao().incrementApprovedCount(occurrence.occurrenceKey)
                    val updated = db.occurrenceDao().getOccurrenceByKeyOnce(occurrence.occurrenceKey)!!
                    val target = updated.targetCount ?: 1
                    val newStatus = if (updated.approvedCount >= target) OccurrenceStatus.APPROVED else OccurrenceStatus.PENDING
                    db.occurrenceDao().updateStatus(occurrence.occurrenceKey, newStatus)
                } else {
                    db.occurrenceDao().updateStatus(occurrence.occurrenceKey, OccurrenceStatus.APPROVED)
                }
                db.occurrenceDao().setWarning(occurrence.occurrenceKey, false, null)
            } else {
                db.occurrenceDao().updateStatus(occurrence.occurrenceKey, OccurrenceStatus.PENDING)
                db.occurrenceDao().setWarning(
                    occurrence.occurrenceKey,
                    true,
                    review.reviewNote ?: "Bu görev onaylanmadı. Lütfen eksikleri tamamlayıp tekrar yap."
                )
            }
        }
    }

    override suspend fun clearAllSessions() {
        db.sessionDao().clearSessions()
        db.screenshotDao().clearScreenshots()
    }
}

// Extension mappers
fun TaskTemplate.toEntity() = TaskTemplateEntity(
    taskId = taskId, title = title, kind = kind, contentType = contentType,
    youtubeUrl = youtubeUrl, plannedMinutes = plannedMinutes, targetMode = targetMode,
    targetCount = targetCount, targetMinutes = targetMinutes, reviewRequired = reviewRequired, active = active
)

fun TaskTemplateEntity.toDomain() = TaskTemplate(
    taskId = taskId, title = title, kind = kind, contentType = contentType,
    youtubeUrl = youtubeUrl, plannedMinutes = plannedMinutes, targetMode = targetMode,
    targetCount = targetCount, targetMinutes = targetMinutes, reviewRequired = reviewRequired, active = active
)

fun Occurrence.toEntity() = OccurrenceEntity(
    occurrenceKey = occurrenceKey, taskId = taskId, type = type, date = date, weekId = weekId,
    title = title, plannedMinutes = plannedMinutes, youtubeUrl = youtubeUrl, reviewRequired = reviewRequired,
    status = status, warning = warning, warningText = warningText, rejectCount = rejectCount,
    approvedCount = approvedCount, targetCount = targetCount, targetMinutes = targetMinutes
)

fun OccurrenceEntity.toDomain() = Occurrence(
    occurrenceKey = occurrenceKey, taskId = taskId, type = type, date = date, weekId = weekId,
    title = title, plannedMinutes = plannedMinutes, youtubeUrl = youtubeUrl, reviewRequired = reviewRequired,
    status = status, warning = warning, warningText = warningText, rejectCount = rejectCount,
    approvedCount = approvedCount, targetCount = targetCount, targetMinutes = targetMinutes
)

fun Session.toEntity() = SessionEntity(
    sessionId = sessionId, occurrenceKey = occurrenceKey, childId = childId,
    startTime = startTime, endTime = endTime, status = status,
    screenshotCount = screenshotCount, finalScreenshotUrl = finalScreenshotUrl
)

fun SessionEntity.toDomain() = Session(
    sessionId = sessionId, occurrenceKey = occurrenceKey, childId = childId,
    startTime = startTime, endTime = endTime, status = status,
    screenshotCount = screenshotCount, finalScreenshotUrl = finalScreenshotUrl
)

fun Screenshot.toEntity() = ScreenshotEntity(
    screenshotId = screenshotId, sessionId = sessionId, occurrenceKey = occurrenceKey,
    capturedAt = capturedAt, url = url, sizeKb = sizeKb, uploadStatus = uploadStatus
)

fun ScreenshotEntity.toDomain() = Screenshot(
    screenshotId = screenshotId, sessionId = sessionId, occurrenceKey = occurrenceKey,
    capturedAt = capturedAt, url = url, sizeKb = sizeKb, uploadStatus = uploadStatus
)

fun Review.toEntity() = ReviewEntity(
    sessionId = sessionId, occurrenceKey = occurrenceKey, reviewStatus = reviewStatus,
    reviewNote = reviewNote, reviewedAt = reviewedAt
)
