package com.studytracker.core.data.local.db.entity

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
    val finalScreenshotUrl: String?,
    val studentNote: String? = null
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
