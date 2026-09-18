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

    @Query("DELETE FROM task_templates WHERE taskId = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("SELECT * FROM task_templates WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): TaskTemplateEntity?

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

    @Update
    suspend fun updateOccurrence(occurrence: OccurrenceEntity)

    @Query("DELETE FROM occurrences WHERE occurrenceKey = :key")
    suspend fun deleteOccurrence(key: String)

    @Query("DELETE FROM occurrences WHERE taskId = :taskId")
    suspend fun deleteOccurrencesByTaskId(taskId: String)

    @Query("UPDATE occurrences SET status = :status WHERE occurrenceKey = :key")
    suspend fun updateStatus(key: String, status: OccurrenceStatus)

    @Query("UPDATE occurrences SET studentNote = :studentNote WHERE occurrenceKey = :key")
    suspend fun updateStudentNote(key: String, studentNote: String?)

    @Query("UPDATE occurrences SET warning = :warning, warningText = :warningText, rejectCount = rejectCount + 1 WHERE occurrenceKey = :key")
    suspend fun setWarning(key: String, warning: Boolean, warningText: String?)

    @Query("UPDATE occurrences SET approvedCount = approvedCount + 1 WHERE occurrenceKey = :key")
    suspend fun incrementApprovedCount(key: String)

    @Query("UPDATE occurrences SET status = 'PENDING', approvedCount = 0, warning = 0, warningText = NULL, rejectCount = 0, studentNote = NULL")
    suspend fun resetAllOccurrencesProgress()

    @Query("UPDATE occurrences SET status = 'PENDING', approvedCount = 0, warning = 0, warningText = NULL, rejectCount = 0, studentNote = NULL WHERE weekId = :weekId")
    suspend fun resetWeeklyOccurrencesProgress(weekId: String)

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

    @Query("UPDATE sessions SET studentNote = :studentNote WHERE sessionId = :sessionId")
    suspend fun updateStudentNote(sessionId: String, studentNote: String?)

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessionsOnce(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSessions()
}

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId ORDER BY capturedAt ASC")
    fun getScreenshotsForSession(sessionId: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId ORDER BY capturedAt ASC")
    suspend fun getScreenshotsForSessionOnce(sessionId: String): List<ScreenshotEntity>

    @Query("SELECT * FROM screenshots ORDER BY capturedAt DESC")
    suspend fun getAllScreenshotsOnce(): List<ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScreenshots(screenshots: List<ScreenshotEntity>)

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

    @Query("DELETE FROM reviews")
    suspend fun clearReviews()
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY rowid DESC")
    fun getAllQuizzes(): kotlinx.coroutines.flow.Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes")
    suspend fun getAllQuizzesOnce(): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId LIMIT 1")
    suspend fun getQuizById(quizId: String): QuizEntity?

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId LIMIT 1")
    fun observeQuizById(quizId: String): kotlinx.coroutines.flow.Flow<QuizEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuizzes(quizzes: List<QuizEntity>)

    @Query("UPDATE quizzes SET completed = :completed, submittedAt = :submittedAt, studentAnswersJson = :studentAnswersJson, studentDurationSeconds = :studentDurationSeconds, correctCount = :correctCount, wrongCount = :wrongCount, emptyCount = :emptyCount, studentNote = :studentNote WHERE quizId = :quizId")
    suspend fun submitQuiz(
        quizId: String,
        completed: Boolean,
        submittedAt: Long,
        studentAnswersJson: String,
        studentDurationSeconds: Int,
        correctCount: Int,
        wrongCount: Int,
        emptyCount: Int,
        studentNote: String?
    )

    @Query("UPDATE quizzes SET completed = 0, submittedAt = NULL, studentAnswersJson = '{}', studentDurationSeconds = 0, correctCount = 0, wrongCount = 0, emptyCount = 0, studentNote = NULL")
    suspend fun resetAllQuizzesProgress()

    @Query("DELETE FROM quizzes WHERE quizId = :quizId")
    suspend fun deleteQuiz(quizId: String)

    @Query("DELETE FROM quizzes")
    suspend fun clearQuizzes()
}

