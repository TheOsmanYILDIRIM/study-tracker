package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.*
import com.studytracker.core.domain.repository.PlanImportResult

object PlanMergeEngine {

    fun executeMerge(
        currentPlan: Plan?,
        newPlan: Plan,
        existingOccurrences: Map<String, Occurrence>
    ): Pair<List<Occurrence>, PlanImportResult> {
        val isSameWeekRevision = currentPlan != null && currentPlan.weekId == newPlan.weekId

        val mergedOccurrences = mutableListOf<Occurrence>()
        var preservedCount = 0
        var updatedCount = 0
        var createdCount = 0

        // 1. Process Daily Occurrences
        for (dailyJson in newPlan.dailyOccurrences) {
            val key = dailyJson.occurrenceKey
            val existing = existingOccurrences[key]

            if (existing != null && isSameWeekRevision) {
                // Same-Week Revision: Preserve completion status
                val isCompletedOrInProgress = existing.status == OccurrenceStatus.APPROVED ||
                        existing.status == OccurrenceStatus.WAITING_REVIEW ||
                        existing.status == OccurrenceStatus.ACTIVE

                if (isCompletedOrInProgress) {
                    preservedCount++
                } else {
                    updatedCount++
                }

                mergedOccurrences.add(
                    existing.copy(
                        title = dailyJson.title,
                        plannedMinutes = dailyJson.plannedMinutes,
                        youtubeUrl = dailyJson.youtubeUrl,
                        reviewRequired = dailyJson.reviewRequired
                        // status, warning, rejectCount, approvedCount are PRESERVED
                    )
                )
            } else {
                // New occurrence or New Week
                createdCount++
                mergedOccurrences.add(
                    Occurrence(
                        occurrenceKey = dailyJson.occurrenceKey,
                        taskId = dailyJson.taskId,
                        type = TaskKind.DAILY,
                        date = dailyJson.date,
                        weekId = newPlan.weekId,
                        title = dailyJson.title,
                        plannedMinutes = dailyJson.plannedMinutes,
                        youtubeUrl = dailyJson.youtubeUrl,
                        reviewRequired = dailyJson.reviewRequired,
                        status = OccurrenceStatus.PENDING,
                        warning = false,
                        warningText = null,
                        rejectCount = 0,
                        approvedCount = 0
                    )
                )
            }
        }

        // 2. Process Weekly Occurrences
        for (weeklyJson in newPlan.weeklyOccurrences) {
            val key = weeklyJson.occurrenceKey
            val existing = existingOccurrences[key]

            if (existing != null && isSameWeekRevision) {
                val newTargetCount = weeklyJson.targetCount ?: 1
                val currentApprovedCount = existing.approvedCount

                // Check if target is satisfied
                val newStatus = when {
                    currentApprovedCount >= newTargetCount -> OccurrenceStatus.APPROVED
                    existing.status == OccurrenceStatus.WAITING_REVIEW -> OccurrenceStatus.WAITING_REVIEW
                    existing.status == OccurrenceStatus.ACTIVE -> OccurrenceStatus.ACTIVE
                    else -> OccurrenceStatus.PENDING
                }

                if (newStatus == OccurrenceStatus.APPROVED || existing.status == OccurrenceStatus.APPROVED) {
                    preservedCount++
                } else {
                    updatedCount++
                }

                mergedOccurrences.add(
                    existing.copy(
                        title = weeklyJson.title,
                        plannedMinutes = weeklyJson.plannedMinutes,
                        targetCount = weeklyJson.targetCount,
                        targetMinutes = weeklyJson.targetMinutes,
                        reviewRequired = weeklyJson.reviewRequired,
                        status = newStatus
                    )
                )
            } else {
                // New weekly occurrence or New Week
                createdCount++
                mergedOccurrences.add(
                    Occurrence(
                        occurrenceKey = weeklyJson.occurrenceKey,
                        taskId = weeklyJson.taskId,
                        type = TaskKind.WEEKLY,
                        date = null,
                        weekId = weeklyJson.weekId,
                        title = weeklyJson.title,
                        plannedMinutes = weeklyJson.plannedMinutes,
                        reviewRequired = weeklyJson.reviewRequired,
                        status = OccurrenceStatus.PENDING,
                        warning = false,
                        warningText = null,
                        rejectCount = 0,
                        approvedCount = 0,
                        targetCount = weeklyJson.targetCount,
                        targetMinutes = weeklyJson.targetMinutes
                    )
                )
            }
        }

        val result = PlanImportResult(
            success = true,
            isSameWeekRevision = isSameWeekRevision,
            tasksCount = newPlan.tasks.size,
            occurrencesUpdated = updatedCount,
            occurrencesCreated = createdCount,
            preservedCount = preservedCount,
            errorMessage = null
        )

        return Pair(mergedOccurrences, result)
    }
}
