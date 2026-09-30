package com.studytracker.core.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class TaskKind {
    DAILY,
    WEEKLY
}

@Serializable
enum class ContentType {
    VIDEO,
    READING,
    APP,
    EXAM,
    OTHER
}

@Serializable
enum class TargetMode {
    COUNT,
    MINUTES
}

@Serializable
enum class OccurrenceStatus {
    PENDING,
    ACTIVE,
    WAITING_REVIEW,
    APPROVED,
    REJECTED,
    ARCHIVED
}

@Serializable
enum class SessionStatus {
    ACTIVE,
    WAITING_REVIEW,
    APPROVED,
    REJECTED,
    INVALID
}

@Serializable
enum class UploadStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    FAILED
}

@Serializable
enum class ReviewStatus {
    APPROVED,
    REJECTED
}

@Immutable
@Serializable
data class TaskTemplate(
    val taskId: String,
    val title: String,
    val kind: TaskKind,
    val contentType: ContentType = ContentType.OTHER,
    val youtubeUrl: String? = null,
    val plannedMinutes: Int = 30,
    val targetMode: TargetMode? = null,
    val targetCount: Int? = null,
    val targetMinutes: Int? = null,
    val reviewRequired: Boolean = true,
    val active: Boolean = true
)

@Immutable
@Serializable
data class Occurrence(
    val occurrenceKey: String,
    val taskId: String,
    val type: TaskKind,
    val date: String? = null,           // YYYY-MM-DD (daily için)
    val weekId: String? = null,         // YYYY-Www (weekly için)
    val title: String,
    val plannedMinutes: Int = 30,
    val youtubeUrl: String? = null,
    val reviewRequired: Boolean = true,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val warning: Boolean = false,
    val warningText: String? = null,
    val rejectCount: Int = 0,
    val approvedCount: Int = 0,         // weekly görev tamamlanma sayacı
    val targetCount: Int? = null,
    val targetMinutes: Int? = null,
    val completedQuestionCount: Int = 0,
    val studentNote: String? = null
)

@Immutable
@Serializable
data class DailyOccurrenceJson(
    val occurrenceKey: String,
    val taskId: String,
    val date: String,
    val title: String,
    val plannedMinutes: Int = 30,
    val youtubeUrl: String? = null,
    val reviewRequired: Boolean = true
)

@Immutable
@Serializable
data class WeeklyOccurrenceJson(
    val occurrenceKey: String,
    val taskId: String,
    val weekId: String,
    val title: String,
    val targetMode: TargetMode = TargetMode.COUNT,
    val targetCount: Int? = 1,
    val targetMinutes: Int? = null,
    val plannedMinutes: Int = 60,
    val reviewRequired: Boolean = true
)

@Immutable
@Serializable
data class Plan(
    val schemaVersion: Int = 1,
    val planId: String,
    val weekId: String,                 // YYYY-Www
    val weekStartDate: String,          // YYYY-MM-DD
    val childId: String,
    val timezone: String = "Europe/Istanbul",
    val updatedAt: String,
    val tasks: List<TaskTemplate> = emptyList(),
    val dailyOccurrences: List<DailyOccurrenceJson> = emptyList(),
    val weeklyOccurrences: List<WeeklyOccurrenceJson> = emptyList()
)

@Immutable
@Serializable
data class Session(
    val sessionId: String,
    val occurrenceKey: String,
    val childId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val screenshotCount: Int = 0,
    val activeDurationSeconds: Long = 0L,
    val reportedQuestionCount: Int = 0,
    val finalScreenshotUrl: String? = null,
    val studentNote: String? = null,
    val updatedAt: Long = startTime
)

@Immutable
@Serializable
data class Screenshot(
    val screenshotId: String,
    val sessionId: String,
    val occurrenceKey: String,
    val capturedAt: Long,
    val url: String,
    val sizeKb: Int = 0,
    val uploadStatus: UploadStatus = UploadStatus.PENDING
)

@Immutable
@Serializable
data class Review(
    val sessionId: String,
    val occurrenceKey: String,
    val reviewStatus: ReviewStatus,
    val reviewNote: String? = null,
    val reviewedAt: Long
)

@Immutable
@Serializable
data class QuizOption(
    val key: String,                      // "A", "B", "C", "D", "E"
    val text: String                      // Option text with LaTeX support
)

@Immutable
@Serializable
data class QuizQuestion(
    val questionId: String,
    val questionNumber: Int,
    val text: String,                     // Question text with LaTeX support
    val options: List<QuizOption> = emptyList(),
    val correctOption: String = "",       // "A", "B", "C", "D", "E" (Hidden from student during quiz)
    val solutionExplanation: String? = null
)

@Immutable
@Serializable
data class Quiz(
    val quizId: String,
    val title: String,
    val description: String? = null,
    val date: String? = null,             // YYYY-MM-DD
    val weekId: String? = null,           // YYYY-Www
    val durationMinutes: Int = 15,
    val targetOccurrenceKey: String? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val completed: Boolean = false,
    val submittedAt: Long? = null,
    val studentAnswers: Map<String, String> = emptyMap(), // questionId -> selectedOption ("A", "B", ...)
    val studentDurationSeconds: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val emptyCount: Int = 0,
    val studentNote: String? = null
)

// --- V2 CURRICULUM & MEASUREMENT DOMAIN MODELS ---

@Serializable
enum class ItemType {
    VIDEO,
    QUIZ,
    ANKI
}

@Serializable
enum class AttemptStatus {
    STARTED,
    COMPLETED,
    ABANDONED
}

@Immutable
@Serializable
data class Course(
    val id: String,
    val familyCode: String,
    val title: String,
    val subject: String,
    val gradeLevel: Int = 9,
    val description: String = "",
    val orderKey: Double = 1000.0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class Lesson(
    val id: String,
    val courseId: String,
    val familyCode: String,
    val title: String,
    val orderKey: Double = 1000.0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class LearningItem(
    val id: String,
    val lessonId: String,
    val familyCode: String,
    val itemType: ItemType,
    val displayLabel: String,
    val stableKey: String,
    val orderKey: Double = 1000.0,
    val currentVersionId: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val currentVersion: LearningItemVersion? = null,
    val versionCount: Int = 1,
    val prerequisites: List<ItemPrerequisite> = emptyList()
)

@Immutable
@Serializable
data class LearningItemVersion(
    val id: String,
    val itemId: String,
    val versionNumber: Int,
    val title: String,
    val contentUrl: String? = null,
    val payloadJson: String? = null,
    val changelog: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class ItemPrerequisite(
    val id: String,
    val itemId: String,
    val requiredItemId: String,
    val minScore: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class Attempt(
    val id: String,
    val clientAttemptId: String,
    val familyCode: String,
    val studentId: String,
    val itemId: String,
    val versionId: String,
    val status: AttemptStatus = AttemptStatus.COMPLETED,
    val score: Double? = null,
    val durationSeconds: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val metadataJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class QuizAnswerMetric(
    val id: String,
    val attemptId: String,
    val questionId: String,
    val questionIndex: Int,
    val selectedOption: String? = null,
    val isCorrect: Boolean = false,
    val durationSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Immutable
@Serializable
data class CurriculumProgressSummary(
    val familyCode: String,
    val studentId: String,
    val totalItems: Int,
    val completedItems: Int,
    val completionPercentage: Int,
    val averageScore: Double?
)




