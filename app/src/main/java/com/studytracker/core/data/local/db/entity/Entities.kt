package com.studytracker.core.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.studytracker.core.domain.model.*

@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey val taskId: String,
    val title: String,
    val kind: TaskKind,
    val contentType: ContentType,
    val youtubeUrl: String?,
    val plannedMinutes: Int,
    val targetMode: TargetMode?,
    val targetCount: Int?,
    val targetMinutes: Int?,
    val reviewRequired: Boolean,
    val active: Boolean
)

@Entity(
    tableName = "occurrences",
    indices = [
        Index(value = ["date"]),
        Index(value = ["weekId"]),
        Index(value = ["status"]),
        Index(value = ["type"])
    ]
)
data class OccurrenceEntity(
    @PrimaryKey val occurrenceKey: String,
    val taskId: String,
    val type: TaskKind,
    val date: String?,
    val weekId: String?,
    val title: String,
    val plannedMinutes: Int,
    val youtubeUrl: String?,
    val reviewRequired: Boolean,
    val status: OccurrenceStatus,
    val warning: Boolean,
    val warningText: String?,
    val rejectCount: Int,
    val approvedCount: Int,
    val targetCount: Int?,
    val targetMinutes: Int?,
    @ColumnInfo(defaultValue = "0") val completedQuestionCount: Int = 0,
    val studentNote: String? = null
)

@Entity(tableName = "active_plan")
data class PlanEntity(
    @PrimaryKey val planId: String,
    val weekId: String,
    val weekStartDate: String,
    val childId: String,
    val timezone: String,
    val updatedAt: String,
    val rawJson: String
)

@Entity(
    tableName = "sessions",
    indices = [
        Index(value = ["status"]),
        Index(value = ["occurrenceKey"])
    ]
)
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val occurrenceKey: String,
    val childId: String,
    val startTime: Long,
    val endTime: Long?,
    val status: SessionStatus,
    val screenshotCount: Int,
    @ColumnInfo(defaultValue = "0") val activeDurationSeconds: Long = 0L,
    @ColumnInfo(defaultValue = "0") val reportedQuestionCount: Int = 0,
    val finalScreenshotUrl: String?,
    val studentNote: String? = null,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = startTime
)

@Entity(
    tableName = "screenshots",
    indices = [
        Index(value = ["sessionId"])
    ]
)
data class ScreenshotEntity(
    @PrimaryKey val screenshotId: String,
    val sessionId: String,
    val occurrenceKey: String,
    val capturedAt: Long,
    val url: String,
    val sizeKb: Int,
    val uploadStatus: UploadStatus
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val sessionId: String,
    val occurrenceKey: String,
    val reviewStatus: ReviewStatus,
    val reviewNote: String?,
    val reviewedAt: Long
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val quizId: String,
    val title: String,
    val description: String? = null,
    val date: String? = null,
    val weekId: String? = null,
    val durationMinutes: Int = 15,
    val targetOccurrenceKey: String? = null,
    val questionsJson: String = "[]",
    val completed: Boolean = false,
    val submittedAt: Long? = null,
    val studentAnswersJson: String = "{}",
    val studentDurationSeconds: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val emptyCount: Int = 0,
    val studentNote: String? = null
)

// --- V2 ENTITIES ---

@Entity(
    tableName = "courses",
    indices = [
        Index(value = ["familyCode", "isArchived", "orderKey"])
    ]
)
data class CourseEntity(
    @PrimaryKey val id: String,
    val familyCode: String,
    val title: String,
    val subject: String,
    @ColumnInfo(defaultValue = "9") val gradeLevel: Int = 9,
    val description: String? = null,
    @ColumnInfo(defaultValue = "1000.0") val orderKey: Double = 1000.0,
    @ColumnInfo(defaultValue = "0") val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "lessons",
    indices = [
        Index(value = ["courseId", "isArchived", "orderKey"]),
        Index(value = ["familyCode"])
    ]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val familyCode: String,
    val title: String,
    @ColumnInfo(defaultValue = "1000.0") val orderKey: Double = 1000.0,
    @ColumnInfo(defaultValue = "0") val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "learning_items",
    indices = [
        Index(value = ["lessonId", "isArchived", "orderKey"]),
        Index(value = ["familyCode", "stableKey"])
    ]
)
data class LearningItemEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val familyCode: String,
    val itemType: ItemType,
    val displayLabel: String,
    val stableKey: String,
    @ColumnInfo(defaultValue = "1000.0") val orderKey: Double = 1000.0,
    val currentVersionId: String,
    @ColumnInfo(defaultValue = "0") val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "learning_item_versions",
    indices = [
        Index(value = ["itemId", "versionNumber"], unique = true)
    ]
)
data class LearningItemVersionEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val versionNumber: Int,
    val title: String,
    val contentUrl: String? = null,
    val payloadJson: String? = null,
    val changelog: String? = null,
    val createdAt: Long
)

@Entity(
    tableName = "item_prerequisites",
    indices = [
        Index(value = ["itemId", "requiredItemId"], unique = true),
        Index(value = ["itemId"])
    ]
)
data class ItemPrerequisiteEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val requiredItemId: String,
    val minScore: Double? = null,
    val createdAt: Long
)

@Entity(
    tableName = "attempts",
    indices = [
        Index(value = ["familyCode", "studentId", "clientAttemptId"], unique = true),
        Index(value = ["familyCode", "studentId", "itemId", "createdAt"])
    ]
)
data class AttemptEntity(
    @PrimaryKey val id: String,
    val clientAttemptId: String,
    val familyCode: String,
    val studentId: String,
    val itemId: String,
    val versionId: String,
    val status: AttemptStatus,
    val score: Double? = null,
    @ColumnInfo(defaultValue = "0") val durationSeconds: Int = 0,
    val startedAt: Long,
    val completedAt: Long? = null,
    val metadataJson: String? = null,
    val createdAt: Long
)

@Entity(
    tableName = "quiz_answers",
    indices = [
        Index(value = ["attemptId"])
    ]
)
data class QuizAnswerMetricEntity(
    @PrimaryKey val id: String,
    val attemptId: String,
    val questionId: String,
    val questionIndex: Int,
    val selectedOption: String? = null,
    @ColumnInfo(defaultValue = "0") val isCorrect: Boolean = false,
    @ColumnInfo(defaultValue = "0") val durationSeconds: Int = 0,
    val createdAt: Long
)



