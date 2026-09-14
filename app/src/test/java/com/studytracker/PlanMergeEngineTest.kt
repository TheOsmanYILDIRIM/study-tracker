package com.studytracker

import com.studytracker.core.data.plan_engine.PlanMergeEngine
import com.studytracker.core.data.plan_engine.PlanValidator
import com.studytracker.core.data.plan_engine.ValidationResult
import com.studytracker.core.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class PlanMergeEngineTest {

    private val sampleWeek25Plan = Plan(
        schemaVersion = 1,
        planId = "plan_2026-W25_child_1",
        weekId = "2026-W25",
        weekStartDate = "2026-06-15",
        childId = "child_1",
        timezone = "Europe/Istanbul",
        updatedAt = "2026-06-17T12:00:00+03:00",
        tasks = listOf(
            TaskTemplate(
                taskId = "math_video",
                title = "Matematik videosu",
                kind = TaskKind.DAILY,
                plannedMinutes = 30
            ),
            TaskTemplate(
                taskId = "weekly_exam",
                title = "Deneme Sınavı",
                kind = TaskKind.WEEKLY,
                plannedMinutes = 120,
                targetMode = TargetMode.COUNT,
                targetCount = 2
            )
        ),
        dailyOccurrences = listOf(
            DailyOccurrenceJson(
                occurrenceKey = "math_video:2026-06-15",
                taskId = "math_video",
                date = "2026-06-15",
                title = "Matematik videosu - Revize Başlık",
                plannedMinutes = 40
            )
        ),
        weeklyOccurrences = listOf(
            WeeklyOccurrenceJson(
                occurrenceKey = "weekly_exam:2026-W25",
                taskId = "weekly_exam",
                weekId = "2026-W25",
                title = "Deneme Sınavı",
                targetCount = 2,
                plannedMinutes = 120
            )
        )
    )

    @Test
    fun `same-week revision preserves approved status and updates metadata`() {
        val existingOccurrences = mapOf(
            "math_video:2026-06-15" to Occurrence(
                occurrenceKey = "math_video:2026-06-15",
                taskId = "math_video",
                type = TaskKind.DAILY,
                date = "2026-06-15",
                title = "Matematik videosu",
                plannedMinutes = 30,
                status = OccurrenceStatus.APPROVED
            )
        )

        val (merged, result) = PlanMergeEngine.executeMerge(
            currentPlan = sampleWeek25Plan,
            newPlan = sampleWeek25Plan,
            existingOccurrences = existingOccurrences
        )

        assertTrue(result.isSameWeekRevision)
        assertEquals(1, result.preservedCount)
        val mathOccurrence = merged.find { it.occurrenceKey == "math_video:2026-06-15" }
        assertNotNull(mathOccurrence)
        assertEquals(OccurrenceStatus.APPROVED, mathOccurrence?.status)
        assertEquals("Matematik videosu - Revize Başlık", mathOccurrence?.title)
        assertEquals(40, mathOccurrence?.plannedMinutes)
    }

    @Test
    fun `new-week plan initializes all occurrences as pending`() {
        val newWeekPlan = sampleWeek25Plan.copy(
            planId = "plan_2026-W26_child_1",
            weekId = "2026-W26",
            weekStartDate = "2026-06-22",
            dailyOccurrences = listOf(
                DailyOccurrenceJson(
                    occurrenceKey = "math_video:2026-06-22",
                    taskId = "math_video",
                    date = "2026-06-22",
                    title = "Matematik videosu",
                    plannedMinutes = 30
                )
            ),
            weeklyOccurrences = listOf(
                WeeklyOccurrenceJson(
                    occurrenceKey = "weekly_exam:2026-W26",
                    taskId = "weekly_exam",
                    weekId = "2026-W26",
                    title = "Deneme Sınavı",
                    targetCount = 2,
                    plannedMinutes = 120
                )
            )
        )

        val existingOccurrences = mapOf(
            "math_video:2026-06-15" to Occurrence(
                occurrenceKey = "math_video:2026-06-15",
                taskId = "math_video",
                type = TaskKind.DAILY,
                date = "2026-06-15",
                title = "Matematik videosu",
                status = OccurrenceStatus.APPROVED
            )
        )

        val (merged, result) = PlanMergeEngine.executeMerge(
            currentPlan = sampleWeek25Plan, // previous week
            newPlan = newWeekPlan,           // new week
            existingOccurrences = existingOccurrences
        )

        assertFalse(result.isSameWeekRevision)
        assertEquals(0, result.preservedCount)
        assertEquals(2, result.occurrencesCreated)
        val newDaily = merged.find { it.occurrenceKey == "math_video:2026-06-22" }
        assertNotNull(newDaily)
        assertEquals(OccurrenceStatus.PENDING, newDaily?.status)
    }

    @Test
    fun `plan validator rejects malformed occurrenceKey`() {
        val badJson = """{
            "schemaVersion": 1,
            "planId": "plan_1",
            "weekId": "2026-W25",
            "weekStartDate": "2026-06-15",
            "childId": "c1",
            "updatedAt": "2026-06-15T00:00:00Z",
            "tasks": [{"taskId": "math", "title": "Math", "kind": "DAILY", "plannedMinutes": 30}],
            "dailyOccurrences": [{"occurrenceKey": "wrong_key", "taskId": "math", "date": "2026-06-15", "title": "Math"}],
            "weeklyOccurrences": []
        }"""

        val (_, validation) = PlanValidator.parseAndValidate(badJson)
        assertTrue(validation is ValidationResult.Invalid)
    }
}
