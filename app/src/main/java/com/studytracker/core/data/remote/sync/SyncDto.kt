package com.studytracker.core.data.remote.sync

import com.studytracker.core.domain.model.Quiz
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteFamilyDto(
    @SerialName("id") val id: String,
    @SerialName("pair_code") val pairCode: String,
    @SerialName("family_name") val familyName: String = "Çalışma Ailesi",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class RemoteOccurrenceSyncDto(
    @SerialName("id") val id: String,
    @SerialName("family_code") val familyCode: String,
    @SerialName("date") val date: String,
    @SerialName("plan_id") val planId: String,
    @SerialName("subject") val subject: String,
    @SerialName("topic") val topic: String,
    @SerialName("target_duration_min") val targetDurationMin: Int,
    @SerialName("target_question_count") val targetQuestionCount: Int,
    @SerialName("completed_duration_min") val completedDurationMin: Int,
    @SerialName("completed_question_count") val completedQuestionCount: Int,
    @SerialName("status") val status: String,
    @SerialName("parent_note") val parentNote: String,
    @SerialName("week_id") val weekId: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("student_note") val studentNote: String? = null,
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class RemoteSessionSyncDto(
    @SerialName("id") val id: String,
    @SerialName("family_code") val familyCode: String,
    @SerialName("occurrence_id") val occurrenceId: String,
    @SerialName("start_time") val startTime: Long,
    @SerialName("end_time") val endTime: Long?,
    @SerialName("duration_min") val durationMin: Int,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("notes") val notes: String = "",
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class RemoteScreenshotSyncDto(
    @SerialName("id") val id: String,
    @SerialName("family_code") val familyCode: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("ai_analysis_json") val aiAnalysisJson: String? = null
)

@Serializable
data class RemoteReviewSyncDto(
    @SerialName("id") val id: String,
    @SerialName("family_code") val familyCode: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("is_approved") val isApproved: Boolean,
    @SerialName("rejection_reason") val rejectionReason: String?,
    @SerialName("parent_rating") val parentRating: Int?,
    @SerialName("feedback_note") val feedbackNote: String?,
    @SerialName("reviewed_at") val reviewedAt: Long = System.currentTimeMillis()
)

@Serializable
data class LocalPlanSyncDto(
    val planId: String,
    val weekId: String,
    val weekStartDate: String,
    val childId: String,
    val timezone: String,
    val updatedAt: String,
    val rawJson: String
)

@Serializable
data class LocalTaskTemplateSyncDto(
    val taskId: String,
    val title: String,
    val kind: String,
    val contentType: String,
    val youtubeUrl: String?,
    val plannedMinutes: Int,
    val targetMode: String? = null,
    val targetCount: Int? = null,
    val targetMinutes: Int? = null,
    val reviewRequired: Boolean = true,
    val active: Boolean = true
)

@Serializable
data class SharedFamilySyncPayload(
    val familyCode: String,
    val plan: LocalPlanSyncDto? = null,
    val tasks: List<LocalTaskTemplateSyncDto> = emptyList(),
    val occurrences: List<RemoteOccurrenceSyncDto> = emptyList(),
    val sessions: List<RemoteSessionSyncDto> = emptyList(),
    val screenshots: List<RemoteScreenshotSyncDto> = emptyList(),
    val reviews: List<RemoteReviewSyncDto> = emptyList(),
    val quizzes: List<Quiz> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SyncResponseStatus(
    val success: Boolean,
    val message: String? = null,
    val syncedCount: Int = 0
)
