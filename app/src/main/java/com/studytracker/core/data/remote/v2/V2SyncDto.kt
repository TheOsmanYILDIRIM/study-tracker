package com.studytracker.core.data.remote.v2

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class V2CatalogResponseDto(
    val success: Boolean = false,
    val storageType: String? = null,
    val familyCode: String? = null,
    val curriculum: List<V2CourseDto> = emptyList(),
    val error: String? = null
)

@Serializable
data class V2CourseDto(
    val id: String,
    val familyCode: String = "",
    val title: String,
    val subject: String,
    val gradeLevel: Int = 9,
    val description: String? = null,
    val orderKey: Double = 1000.0,
    val isArchived: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lessons: List<V2LessonDto> = emptyList()
)

@Serializable
data class V2LessonDto(
    val id: String,
    val courseId: String = "",
    val familyCode: String = "",
    val title: String,
    val orderKey: Double = 1000.0,
    val isArchived: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val items: List<V2LearningItemDto> = emptyList()
)

@Serializable
data class V2LearningItemDto(
    val id: String,
    val lessonId: String = "",
    val familyCode: String = "",
    val itemType: String,
    val displayLabel: String,
    val stableKey: String = "",
    val orderKey: Double = 1000.0,
    val currentVersionId: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val currentVersion: V2LearningItemVersionDto? = null,
    val versionCount: Int = 1,
    val prerequisites: List<V2PrerequisiteDto> = emptyList()
)

@Serializable
data class V2LearningItemVersionDto(
    val id: String,
    val itemId: String = "",
    val versionNumber: Int = 1,
    val title: String = "",
    val contentUrl: String? = null,
    val payload: JsonElement? = null,
    val changelog: String? = null,
    val createdAt: Long = 0L
)

@Serializable
data class V2PrerequisiteDto(
    val id: String = "",
    val itemId: String = "",
    val requiredItemId: String = "",
    val minScore: Double? = null,
    val createdAt: Long = 0L
)

@Serializable
data class V2AttemptRequestDto(
    val clientAttemptId: String,
    val studentId: String = "student_default",
    val itemId: String,
    val versionId: String? = null,
    val status: String = "COMPLETED",
    val score: Double? = null,
    val durationSeconds: Int = 0,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val metadata: JsonElement? = null,
    val quizAnswers: List<V2QuizAnswerDto> = emptyList()
)

@Serializable
data class V2QuizAnswerDto(
    val id: String? = null,
    val questionId: String,
    val questionIndex: Int = 0,
    val selectedOption: String? = null,
    val isCorrect: Boolean = false,
    val durationSeconds: Int = 0
)

@Serializable
data class V2AttemptDto(
    val id: String,
    val clientAttemptId: String,
    val familyCode: String = "",
    val studentId: String = "student_default",
    val itemId: String,
    val versionId: String,
    val status: String = "COMPLETED",
    val score: Double? = null,
    val durationSeconds: Int = 0,
    val startedAt: Long = 0L,
    val completedAt: Long? = null,
    val metadata: JsonElement? = null,
    val createdAt: Long = 0L
)

@Serializable
data class V2AttemptResponseDto(
    val success: Boolean = false,
    val attempt: V2AttemptDto? = null,
    val quizAnswers: List<V2QuizAnswerDto> = emptyList(),
    val duplicate: Boolean = false,
    val error: String? = null
)

@Serializable
data class V2AttemptsListResponseDto(
    val success: Boolean = false,
    val attempts: List<V2AttemptDto> = emptyList(),
    val error: String? = null
)
