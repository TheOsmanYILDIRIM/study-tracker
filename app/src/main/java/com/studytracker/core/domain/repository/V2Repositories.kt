package com.studytracker.core.domain.repository

import com.studytracker.core.domain.model.*
import kotlinx.coroutines.flow.Flow

interface V2CurriculumRepository {
    fun getCourses(familyCode: String): Flow<List<Course>>
    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>>
    fun getLessonById(lessonId: String): Flow<Lesson?>
    fun getLearningItemsForLesson(lessonId: String): Flow<List<LearningItem>>
    fun getLearningItemsForFamily(familyCode: String): Flow<List<LearningItem>>
    fun getLearningItem(itemId: String): Flow<LearningItem?>
    fun getPrerequisites(): Flow<List<ItemPrerequisite>>
    suspend fun syncCatalog(familyCode: String, adminToken: String? = null): Result<Int>
}

interface V2AttemptRepository {
    fun getAttempts(familyCode: String, studentId: String): Flow<List<Attempt>>
    fun getAttemptsForItem(familyCode: String, studentId: String, itemId: String): Flow<List<Attempt>>
    suspend fun recordAttempt(attempt: Attempt, quizAnswers: List<QuizAnswerMetric> = emptyList()): Result<Attempt>
    suspend fun syncPendingAttempts(familyCode: String, studentId: String, adminToken: String? = null): Result<Int>
}
