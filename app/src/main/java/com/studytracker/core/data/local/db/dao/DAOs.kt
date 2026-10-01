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

    @Query("SELECT * FROM occurrences WHERE date = :date")
    suspend fun getOccurrencesByDate(date: String): List<OccurrenceEntity>

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

    @Query("UPDATE occurrences SET completedQuestionCount = MAX(completedQuestionCount, :count) WHERE occurrenceKey = :key")
    suspend fun updateCompletedQuestionCount(key: String, count: Int)

    @Query("UPDATE occurrences SET warning = :warning, warningText = :warningText, rejectCount = rejectCount + CASE WHEN :warning = 1 THEN 1 ELSE 0 END WHERE occurrenceKey = :key")
    suspend fun setWarning(key: String, warning: Boolean, warningText: String?)

    @Query("UPDATE occurrences SET approvedCount = approvedCount + 1 WHERE occurrenceKey = :key")
    suspend fun incrementApprovedCount(key: String)

    @Query("UPDATE occurrences SET status = 'PENDING', approvedCount = 0, warning = 0, warningText = NULL, rejectCount = 0, completedQuestionCount = 0, studentNote = NULL")
    suspend fun resetAllOccurrencesProgress()

    @Query("UPDATE occurrences SET status = 'PENDING', approvedCount = 0, warning = 0, warningText = NULL, rejectCount = 0, completedQuestionCount = 0, studentNote = NULL WHERE weekId = :weekId")
    suspend fun resetWeeklyOccurrencesProgress(weekId: String)

    @Query("DELETE FROM occurrences")
    suspend fun clearOccurrences()
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM active_plan ORDER BY rowid DESC LIMIT 1")
    fun getActivePlan(): Flow<PlanEntity?>

    @Query("SELECT * FROM active_plan ORDER BY rowid DESC LIMIT 1")
    suspend fun getActivePlanOnce(): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivePlan(plan: PlanEntity)

    @Query("DELETE FROM active_plan")
    suspend fun clearActivePlan()

    @Transaction
    suspend fun setActivePlan(plan: PlanEntity) {
        clearActivePlan()
        insertActivePlan(plan)
    }
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

    @Query("UPDATE sessions SET studentNote = :studentNote, updatedAt = CASE WHEN updatedAt > :updatedAt THEN updatedAt ELSE :updatedAt END WHERE sessionId = :sessionId")
    suspend fun updateStudentNote(sessionId: String, studentNote: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessionsOnce(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("UPDATE sessions SET status = 'INVALID', endTime = :endedAt, updatedAt = CASE WHEN updatedAt > :endedAt THEN updatedAt ELSE :endedAt END WHERE sessionId = :sessionId")
    suspend fun invalidateSession(sessionId: String, endedAt: Long)

    @Query("DELETE FROM sessions")
    suspend fun clearSessions()
}

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId ORDER BY capturedAt ASC")
    fun getScreenshotsForSession(sessionId: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId OR sessionId = :occurrenceKey OR occurrenceKey = :occurrenceKey ORDER BY capturedAt ASC")
    fun getScreenshotsForSessionAndOccurrence(sessionId: String, occurrenceKey: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId ORDER BY capturedAt ASC")
    suspend fun getScreenshotsForSessionOnce(sessionId: String): List<ScreenshotEntity>

    @Query("SELECT * FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId OR sessionId = :occurrenceKey OR occurrenceKey = :occurrenceKey ORDER BY capturedAt ASC")
    suspend fun getScreenshotsForSessionAndOccurrenceOnce(sessionId: String, occurrenceKey: String): List<ScreenshotEntity>

    @Query("SELECT * FROM screenshots ORDER BY capturedAt DESC")
    suspend fun getAllScreenshotsOnce(): List<ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: ScreenshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertScreenshots(screenshots: List<ScreenshotEntity>)

    @Query("SELECT COUNT(*) FROM screenshots WHERE sessionId = :sessionId OR occurrenceKey = :sessionId")
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

// --- V2 DAOS ---

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE familyCode = :familyCode AND isArchived = 0 ORDER BY orderKey ASC")
    fun getActiveCourses(familyCode: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE familyCode = :familyCode ORDER BY orderKey ASC")
    fun getAllCourses(familyCode: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Query("UPDATE courses SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveCourse(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM courses")
    suspend fun clearCourses()
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId AND isArchived = 0 ORDER BY orderKey ASC")
    fun getActiveLessonsForCourse(courseId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE familyCode = :familyCode AND isArchived = 0 ORDER BY orderKey ASC")
    fun getActiveLessonsForFamily(familyCode: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): LessonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveLesson(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()
}

@Dao
interface LearningItemDao {
    @Query("SELECT * FROM learning_items WHERE lessonId = :lessonId AND isArchived = 0 ORDER BY orderKey ASC")
    fun getActiveItemsForLesson(lessonId: String): Flow<List<LearningItemEntity>>

    @Query("SELECT * FROM learning_items WHERE familyCode = :familyCode AND isArchived = 0 ORDER BY orderKey ASC")
    fun getActiveItemsForFamily(familyCode: String): Flow<List<LearningItemEntity>>

    @Query("SELECT * FROM learning_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): LearningItemEntity?

    @Query("SELECT * FROM learning_items WHERE id = :id LIMIT 1")
    fun observeItemById(id: String): Flow<LearningItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: LearningItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<LearningItemEntity>)

    @Query("UPDATE learning_items SET orderKey = :newOrderKey, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOrderKey(id: String, newOrderKey: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE learning_items SET currentVersionId = :versionId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCurrentVersion(id: String, versionId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE learning_items SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveItem(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM learning_items")
    suspend fun clearItems()
}

@Dao
interface LearningItemVersionDao {
    @Query("SELECT * FROM learning_item_versions WHERE itemId = :itemId ORDER BY versionNumber ASC")
    fun getVersionsForItem(itemId: String): Flow<List<LearningItemVersionEntity>>

    @Query("SELECT * FROM learning_item_versions WHERE itemId = :itemId ORDER BY versionNumber ASC")
    suspend fun getVersionsForItemOnce(itemId: String): List<LearningItemVersionEntity>

    @Query("SELECT * FROM learning_item_versions WHERE id = :id LIMIT 1")
    suspend fun getVersionById(id: String): LearningItemVersionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: LearningItemVersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersions(versions: List<LearningItemVersionEntity>)

    @Query("DELETE FROM learning_item_versions")
    suspend fun clearVersions()
}

@Dao
interface ItemPrerequisiteDao {
    @Query("SELECT * FROM item_prerequisites WHERE itemId = :itemId")
    fun getPrerequisitesForItem(itemId: String): Flow<List<ItemPrerequisiteEntity>>

    @Query("SELECT * FROM item_prerequisites WHERE itemId = :itemId")
    suspend fun getPrerequisitesForItemOnce(itemId: String): List<ItemPrerequisiteEntity>

    @Query("SELECT * FROM item_prerequisites")
    suspend fun getAllPrerequisitesOnce(): List<ItemPrerequisiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrerequisite(prereq: ItemPrerequisiteEntity)

    @Query("DELETE FROM item_prerequisites WHERE itemId = :itemId AND requiredItemId = :requiredItemId")
    suspend fun deletePrerequisite(itemId: String, requiredItemId: String)

    @Query("DELETE FROM item_prerequisites")
    suspend fun clearPrerequisites()
}

@Dao
interface AttemptDao {
    @Query("SELECT * FROM attempts WHERE familyCode = :familyCode AND studentId = :studentId ORDER BY createdAt DESC")
    fun getAttemptsForStudent(familyCode: String, studentId: String): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE familyCode = :familyCode AND studentId = :studentId AND itemId = :itemId ORDER BY createdAt DESC")
    fun getAttemptsForItem(familyCode: String, studentId: String, itemId: String): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE familyCode = :familyCode AND studentId = :studentId AND clientAttemptId = :clientAttemptId LIMIT 1")
    suspend fun getAttemptByClientId(familyCode: String, studentId: String, clientAttemptId: String): AttemptEntity?

    @Query("SELECT * FROM attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: String): AttemptEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttempt(attempt: AttemptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttempt(attempt: AttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttempts(attempts: List<AttemptEntity>)

    @Query("SELECT * FROM attempts WHERE familyCode = :familyCode AND studentId = :studentId ORDER BY createdAt DESC")
    suspend fun getAttemptsForStudentOnce(familyCode: String, studentId: String): List<AttemptEntity>

    @Query("DELETE FROM attempts")
    suspend fun clearAttempts()
}

@Dao
interface QuizAnswerMetricDao {
    @Query("SELECT * FROM quiz_answers WHERE attemptId = :attemptId ORDER BY questionIndex ASC")
    fun getAnswersForAttempt(attemptId: String): Flow<List<QuizAnswerMetricEntity>>

    @Query("SELECT * FROM quiz_answers WHERE attemptId = :attemptId ORDER BY questionIndex ASC")
    suspend fun getAnswersForAttemptOnce(attemptId: String): List<QuizAnswerMetricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerMetrics(answers: List<QuizAnswerMetricEntity>)

    @Query("DELETE FROM quiz_answers")
    suspend fun clearAnswerMetrics()
}


