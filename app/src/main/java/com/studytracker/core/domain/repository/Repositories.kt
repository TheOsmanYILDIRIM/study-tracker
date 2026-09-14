package com.studytracker.core.domain.repository

import com.studytracker.core.domain.model.*
import kotlinx.coroutines.flow.Flow

data class PlanImportResult(
    val success: Boolean,
    val isSameWeekRevision: Boolean,
    val tasksCount: Int,
    val occurrencesUpdated: Int,
    val occurrencesCreated: Int,
    val preservedCount: Int,
    val errorMessage: String? = null
)

interface PlanRepository {
    fun getActivePlan(): Flow<Plan?>
    suspend fun getActivePlanOnce(): Plan?
    suspend fun importPlanJson(planJson: String): Result<PlanImportResult>
    suspend fun exportCurrentPlanJson(): String
    suspend fun clearAllPlanData()
}

interface OccurrenceRepository {
    fun getAllOccurrences(): Flow<List<Occurrence>>
    fun getDailyOccurrencesForDate(date: String): Flow<List<Occurrence>>
    fun getWeeklyOccurrences(weekId: String): Flow<List<Occurrence>>
    fun getOccurrenceByKey(key: String): Flow<Occurrence?>
    suspend fun getOccurrenceByKeyOnce(key: String): Occurrence?
    suspend fun upsertOccurrences(occurrences: List<Occurrence>)
    suspend fun updateStatus(occurrenceKey: String, status: OccurrenceStatus)
    suspend fun setWarning(occurrenceKey: String, warning: Boolean, note: String?)
    suspend fun incrementApprovedCount(occurrenceKey: String): Occurrence
}

interface SessionRepository {
    fun getActiveSession(): Flow<Session?>
    suspend fun getActiveSessionOnce(): Session?
    fun getWaitingReviewSessions(): Flow<List<Session>>
    fun getSessionsForOccurrence(occurrenceKey: String): Flow<List<Session>>
    suspend fun getSessionById(sessionId: String): Session?
    suspend fun startSession(occurrenceKey: String, childId: String): Session
    suspend fun finishSession(sessionId: String, finalScreenshotUrl: String?): Session
    suspend fun submitReview(review: Review)
    suspend fun clearAllSessions()
}

interface ScreenshotRepository {
    fun getScreenshotsForSession(sessionId: String): Flow<List<Screenshot>>
    suspend fun saveScreenshot(screenshot: Screenshot)
    suspend fun getScreenshotsCount(sessionId: String): Int
}

interface CaptureDriver {
    suspend fun start(sessionId: String, occurrenceKey: String)
    suspend fun captureNow(): Screenshot
    suspend fun stop(): Screenshot?
}
