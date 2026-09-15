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
    val targetMinutes: Int? = null
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
    val finalScreenshotUrl: String? = null
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

