package com.studytracker.core.domain.engine

import androidx.compose.runtime.Immutable
import com.studytracker.core.domain.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class V2ItemState {
    COMPLETED,
    AVAILABLE,
    LOCKED_BY_PREREQUISITE,
    ARCHIVED
}

@Immutable
@Serializable
data class V2PrerequisiteStatus(
    val requiredItemId: String,
    val minScore: Double? = null,
    val isSatisfied: Boolean = false
)

@Immutable
@Serializable
data class V2ItemProgress(
    val item: LearningItem,
    val state: V2ItemState,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
    val bestScore: Double?,
    val latestAttempt: Attempt?,
    val prerequisites: List<V2PrerequisiteStatus> = emptyList()
)

@Immutable
@Serializable
data class V2LessonProgress(
    val lesson: Lesson,
    val totalItems: Int,
    val completedItems: Int,
    val completionPercentage: Int,
    val nextUnfinishedItem: LearningItem?,
    val itemsProgress: List<V2ItemProgress>
)

@Immutable
@Serializable
data class V2CourseProgress(
    val course: Course,
    val totalItems: Int,
    val completedItems: Int,
    val completionPercentage: Int,
    val averageScore: Double?,
    val lessonsProgress: List<V2LessonProgress>
)

// Content Payloads
@Immutable
@Serializable
data class VideoPayload(
    val url: String = "",
    val description: String? = null,
    val provider: String? = null
)

@Immutable
@Serializable
data class QuizQuestionPayload(
    val id: String = "",
    val prompt: String = "",
    val type: String = "MULTIPLE_CHOICE", // MULTIPLE_CHOICE or TRUE_FALSE
    val choices: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String? = null
)

@Immutable
@Serializable
data class QuizPayload(
    val questions: List<QuizQuestionPayload> = emptyList()
)

@Immutable
@Serializable
data class AnkiPayload(
    val deckName: String = "",
    val packageUri: String? = null,
    val webUrl: String? = null,
    val instructions: String? = null
)

object V2ProgressEngine {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseVideoPayload(rawJson: String?): VideoPayload {
        if (rawJson.isNullOrBlank()) return VideoPayload()
        return try {
            json.decodeFromString(VideoPayload.serializer(), rawJson)
        } catch (_: Exception) {
            VideoPayload(url = rawJson)
        }
    }

    fun parseQuizPayload(rawJson: String?): QuizPayload {
        if (rawJson.isNullOrBlank()) return QuizPayload()
        return try {
            json.decodeFromString(QuizPayload.serializer(), rawJson)
        } catch (_: Exception) {
            QuizPayload()
        }
    }

    fun parseAnkiPayload(rawJson: String?): AnkiPayload {
        if (rawJson.isNullOrBlank()) return AnkiPayload()
        return try {
            json.decodeFromString(AnkiPayload.serializer(), rawJson)
        } catch (_: Exception) {
            AnkiPayload(deckName = rawJson)
        }
    }

    /**
     * Deterministically calculates item progress based strictly on student attempts and prerequisites.
     * Zero parent approval dependency.
     */
    fun evaluateItemProgress(
        item: LearningItem,
        itemPrerequisites: List<ItemPrerequisite>,
        attemptsByItemId: Map<String, List<Attempt>>
    ): V2ItemProgress {
        if (item.isArchived) {
            return V2ItemProgress(
                item = item,
                state = V2ItemState.ARCHIVED,
                isCompleted = false,
                isUnlocked = false,
                bestScore = null,
                latestAttempt = null
            )
        }

        val itemAttempts = attemptsByItemId[item.id] ?: emptyList()
        val completedAttempts = itemAttempts.filter { it.status == AttemptStatus.COMPLETED }
        val isCompleted = completedAttempts.isNotEmpty()
        val bestScore = completedAttempts.mapNotNull { it.score }.maxOrNull()
        val latestAttempt = itemAttempts.maxByOrNull { it.createdAt }

        val prereqStatuses = itemPrerequisites.map { prereq ->
            val reqAttempts = (attemptsByItemId[prereq.requiredItemId] ?: emptyList()).filter { it.status == AttemptStatus.COMPLETED }
            val reqCompleted = reqAttempts.isNotEmpty()
            val reqBestScore = reqAttempts.mapNotNull { it.score }.maxOrNull()
            val minScore = prereq.minScore
            val scoreSatisfied = minScore == null || (reqBestScore != null && reqBestScore >= minScore)
            V2PrerequisiteStatus(
                requiredItemId = prereq.requiredItemId,
                minScore = minScore,
                isSatisfied = reqCompleted && scoreSatisfied
            )
        }

        val isUnlocked = prereqStatuses.all { it.isSatisfied }
        val state = when {
            isCompleted -> V2ItemState.COMPLETED
            !isUnlocked -> V2ItemState.LOCKED_BY_PREREQUISITE
            else -> V2ItemState.AVAILABLE
        }

        return V2ItemProgress(
            item = item,
            state = state,
            isCompleted = isCompleted,
            isUnlocked = isUnlocked,
            bestScore = bestScore,
            latestAttempt = latestAttempt,
            prerequisites = prereqStatuses
        )
    }

    /**
     * Calculates lesson progress and next unfinished available item.
     */
    fun evaluateLessonProgress(
        lesson: Lesson,
        items: List<LearningItem>,
        prerequisites: List<ItemPrerequisite>,
        attempts: List<Attempt>
    ): V2LessonProgress {
        val attemptsByItemId = attempts.groupBy { it.itemId }
        val prereqsByItemId = prerequisites.groupBy { it.itemId }

        val activeItems = items.filter { !it.isArchived }.sortedBy { it.orderKey }
        val itemsProgress = activeItems.map { item ->
            evaluateItemProgress(
                item = item,
                itemPrerequisites = prereqsByItemId[item.id] ?: emptyList(),
                attemptsByItemId = attemptsByItemId
            )
        }

        val totalItems = itemsProgress.size
        val completedItems = itemsProgress.count { it.isCompleted }
        val completionPercentage = if (totalItems > 0) Math.round((completedItems.toDouble() / totalItems.toDouble()) * 100.0).toInt() else 0

        // Find next unfinished item in order: first uncompleted available item
        val nextUnfinished = itemsProgress.firstOrNull { !it.isCompleted && it.isUnlocked }?.item

        return V2LessonProgress(
            lesson = lesson,
            totalItems = totalItems,
            completedItems = completedItems,
            completionPercentage = completionPercentage,
            nextUnfinishedItem = nextUnfinished,
            itemsProgress = itemsProgress
        )
    }

    /**
     * Calculates course progress across all lessons.
     */
    fun evaluateCourseProgress(
        course: Course,
        lessons: List<Lesson>,
        itemsByLessonId: Map<String, List<LearningItem>>,
        prerequisites: List<ItemPrerequisite>,
        attempts: List<Attempt>
    ): V2CourseProgress {
        val sortedLessons = lessons.filter { !it.isArchived }.sortedBy { it.orderKey }
        val lessonsProgress = sortedLessons.map { lesson ->
            val items = itemsByLessonId[lesson.id] ?: emptyList()
            evaluateLessonProgress(lesson, items, prerequisites, attempts)
        }

        val totalItems = lessonsProgress.sumOf { it.totalItems }
        val completedItems = lessonsProgress.sumOf { it.completedItems }
        val percentage = if (totalItems > 0) Math.round((completedItems.toDouble() / totalItems.toDouble()) * 100.0).toInt() else 0

        val scores = lessonsProgress.flatMap { lp ->
            lp.itemsProgress.mapNotNull { it.bestScore }
        }
        val averageScore = if (scores.isNotEmpty()) {
            Math.round((scores.average()) * 10.0) / 10.0
        } else null

        return V2CourseProgress(
            course = course,
            totalItems = totalItems,
            completedItems = completedItems,
            completionPercentage = percentage,
            averageScore = averageScore,
            lessonsProgress = lessonsProgress
        )
    }
}
