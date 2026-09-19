package com.studytracker.core.data.remote.sync

import com.studytracker.core.domain.model.Quiz
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteFamilyDto(
    val id: String = "",
    val pairCode: String = "",
    val familyName: String = "Çalışma Ailesi",
    val createdAt: String? = null
)

@Serializable
data class RemoteOccurrenceSyncDto(
    val id: String = "",
    val familyCode: String = "",
    val date: String = "",
    val planId: String = "",
    val subject: String = "",
    val topic: String = "",
    val targetDurationMin: Int = 30,
    val targetQuestionCount: Int = 0,
    val completedDurationMin: Int = 0,
    val completedQuestionCount: Int = 0,
    val status: String = "PENDING",
    val parentNote: String = "",
    val weekId: String = "",
    val orderIndex: Int = 0,
    val studentNote: String? = null,
    val youtubeUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class RemoteSessionSyncDto(
    val id: String = "",
    val familyCode: String = "",
    val occurrenceId: String = "",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val durationMin: Int = 0,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class RemoteScreenshotSyncDto(
    val id: String = "",
    val familyCode: String = "",
    val sessionId: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val aiAnalysisJson: String? = null
)

@Serializable
data class RemoteReviewSyncDto(
    val id: String = "",
    val familyCode: String = "",
    val sessionId: String = "",
    val isApproved: Boolean = true,
    val rejectionReason: String? = null,
    val parentRating: Int? = 5,
    val feedbackNote: String? = null,
    val reviewedAt: Long = System.currentTimeMillis()
)

@Serializable
data class LocalPlanSyncDto(
    val planId: String = "",
    val weekId: String = "",
    val weekStartDate: String = "",
    val childId: String = "",
    val timezone: String = "Europe/Istanbul",
    val updatedAt: String = "",
    val rawJson: String = "{}"
)

@Serializable
data class LocalTaskTemplateSyncDto(
    val taskId: String = "",
    val title: String = "",
    val kind: String = "DAILY",
    val contentType: String = "OTHER",
    val youtubeUrl: String? = null,
    val plannedMinutes: Int = 30,
    val targetMode: String? = null,
    val targetCount: Int? = null,
    val targetMinutes: Int? = null,
    val reviewRequired: Boolean = true,
    val active: Boolean = true
)

@Serializable
data class RemoteMessageSyncDto(
    val id: String = "",
    val familyCode: String = "",
    val senderRole: String = "PARENT", // "PARENT" or "SYSTEM"
    val title: String = "",
    val message: String = "",
    val type: String = "REMINDER", // "REMINDER", "PRAISE", "URGENT", "CUSTOM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetDate: String? = null,
    val targetOccurrenceId: String? = null
)

@Serializable
data class SharedFamilySyncPayload(
    val familyCode: String = "",
    val senderRole: String = "PARENT",
    val action: String = "SYNC",
    val deleteTaskId: String? = null,
    val plan: LocalPlanSyncDto? = null,
    val tasks: List<LocalTaskTemplateSyncDto> = emptyList(),
    val occurrences: List<RemoteOccurrenceSyncDto> = emptyList(),
    val sessions: List<RemoteSessionSyncDto> = emptyList(),
    val screenshots: List<RemoteScreenshotSyncDto> = emptyList(),
    val reviews: List<RemoteReviewSyncDto> = emptyList(),
    val quizzes: List<Quiz> = emptyList(),
    val messages: List<RemoteMessageSyncDto> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SyncResponseStatus(
    val success: Boolean = false,
    val message: String? = null,
    val syncedCount: Int = 0
)
