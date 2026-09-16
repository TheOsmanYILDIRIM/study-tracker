package com.studytracker.core.data.local.db.dao

import androidx.room.*
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTemplateDao {
    @Query("SELECT * FROM task_templates")
    fun getAllTasks(): Flow<List<TaskTemplateEntity>>

    @Query("SELECT * FROM task_templates")
    suspend fun getAllTasksOnce(): List<TaskTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<TaskTemplateEntity>)

    @Query("DELETE FROM task_templates")
    suspend fun clearTasks()
}

@Dao
interface OccurrenceDao {
    @Query("SELECT * FROM occurrences")
    fun getAllOccurrences(): Flow<List<OccurrenceEntity>>

    @Query("SELECT * FROM occurrences WHERE date = :date")
    fun getDailyOccurrencesForDate(date: String): Flow<List<OccurrenceEntity>>

    @Query("SELECT * FROM occurrences WHERE weekId = :weekId")
    fun getWeeklyOccurrences(weekId: String): Flow<List<OccurrenceEntity>>

    @Query("SELECT * FROM occurrences WHERE occurrenceKey = :key LIMIT 1")
    fun getOccurrenceByKey(key: String): Flow<OccurrenceEntity?>

    @Query("SELECT * FROM occurrences WHERE occurrenceKey = :key LIMIT 1")
    suspend fun getOccurrenceByKeyOnce(key: String): OccurrenceEntity?

    @Query("SELECT * FROM occurrences")
    suspend fun getAllOccurrencesOnce(): List<OccurrenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOccurrences(occurrences: List<OccurrenceEntity>)

    @Query("UPDATE occurrences SET status = :status WHERE occurrenceKey = :key")
    suspend fun updateStatus(key: String, status: OccurrenceStatus)

    @Query("UPDATE occurrences SET warning = :warning, warningText = :warningText, rejectCount = rejectCount + 1 WHERE occurrenceKey = :key")
    suspend fun setWarning(key: String, warning: Boolean, warningText: String?)

    @Query("UPDATE occurrences SET approvedCount = approvedCount + 1 WHERE occurrenceKey = :key")
    suspend fun incrementApprovedCount(key: String)

    @Query("DELETE FROM occurrences")
    suspend fun clearOccurrences()
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM active_plan LIMIT 1")
    fun getActivePlan(): Flow<PlanEntity?>

    @Query("SELECT * FROM active_plan LIMIT 1")
    suspend fun getActivePlanOnce(): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActivePlan(plan: PlanEntity)

    @Query("DELETE FROM active_plan")
    suspend fun clearActivePlan()
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE status = :status LIMIT 1")
    fun getActiveSession(status: SessionStatus = SessionStatus.ACTIVE): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE status = :status LIMIT 1")
    suspend fun getActiveSessionOnce(status: SessionStatus = SessionStatus.ACTIVE): SessionEntity?

    @Query("SELECT * FROM sessions WHERE status = 'WAITING_REVIEW' ORDER BY endTime DESC")
    fun getWaitingReviewSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE occurrenceKey = :key ORDER BY startTime DESC")
    fun getSessionsForOccurrence(key: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessionsOnce(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSessions()
}

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId ORDER BY capturedAt ASC")
    fun getScreenshotsForSession(sessionId: String): Flow<List<ScreenshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity)

    @Query("SELECT COUNT(*) FROM screenshots WHERE sessionId = :sessionId")
    suspend fun getScreenshotsCount(sessionId: String): Int

    @Query("DELETE FROM screenshots")
    suspend fun clearScreenshots()
}

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getReviewForSession(sessionId: String): ReviewEntity?

    @Query("SELECT * FROM reviews")
    suspend fun getAllReviewsOnce(): List<ReviewEntity>
}
