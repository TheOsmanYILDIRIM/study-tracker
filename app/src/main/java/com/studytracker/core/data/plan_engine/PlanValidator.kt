package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.DailyOccurrenceJson
import com.studytracker.core.domain.model.Plan
import com.studytracker.core.domain.model.WeeklyOccurrenceJson
import kotlinx.serialization.json.Json

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

object PlanValidator {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val weekIdRegex = Regex("""^\d{4}-W(0[1-9]|[1-4][0-9]|5[0-3])$""")
    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val taskIdRegex = Regex("""^[a-z0-9_]+$""")

    fun parseAndValidate(rawInput: String): Pair<Plan?, ValidationResult> {
        val trimmed = rawInput.trim()
        var plan: Plan = if (trimmed.startsWith("{")) {
            try {
                jsonParser.decodeFromString(Plan.serializer(), trimmed)
            } catch (e: Exception) {
                // If JSON fails, attempt SimplePlanParser as fallback
                val (simplePlan, simpleResult) = SimplePlanParser.parse(trimmed)
                if (simplePlan != null && simpleResult is ValidationResult.Valid) {
                    simplePlan
                } else {
                    return Pair(null, ValidationResult.Invalid("Geçersiz JSON / Metin formatı: ${e.localizedMessage}"))
                }
            }
        } else {
            val (simplePlan, simpleResult) = SimplePlanParser.parse(trimmed)
            if (simplePlan == null || simpleResult is ValidationResult.Invalid) {
                return Pair(null, simpleResult)
            }
            simplePlan
        }

        if (plan.planId.isBlank()) {
            plan = plan.copy(planId = "plan_${plan.weekId}_${plan.childId}_${System.currentTimeMillis()}")
        }

        if (!weekIdRegex.matches(plan.weekId)) {
            return Pair(plan, ValidationResult.Invalid("Hatalı weekId formatı: '${plan.weekId}'. Örnek: '2026-W25'"))
        }

        if (!dateRegex.matches(plan.weekStartDate)) {
            return Pair(plan, ValidationResult.Invalid("Hatalı weekStartDate formatı: '${plan.weekStartDate}'. Örnek: '2026-06-15'"))
        }

        // Auto-sanitize tasks: deduplicate by taskId
        val cleanTasks = plan.tasks
            .filter { it.taskId.isNotBlank() }
            .map { it.copy(taskId = SimplePlanParser.sanitizeId(it.taskId)) }
            .distinctBy { it.taskId }

        val taskIds = cleanTasks.map { it.taskId }.toMutableSet()

        // Auto-sanitize daily occurrences: deduplicate and guarantee matching task references
        val cleanDaily = mutableListOf<DailyOccurrenceJson>()
        val seenDailyKeys = mutableSetOf<String>()

        for (daily in plan.dailyOccurrences) {
            val cleanTaskId = SimplePlanParser.sanitizeId(daily.taskId)
            if (!taskIds.contains(cleanTaskId)) {
                taskIds.add(cleanTaskId)
            }

            var key = "$cleanTaskId:${daily.date}"
            if (seenDailyKeys.contains(key)) {
                var idx = 2
                while (seenDailyKeys.contains("${cleanTaskId}_$idx:${daily.date}")) {
                    idx++
                }
                key = "${cleanTaskId}_$idx:${daily.date}"
            }

            seenDailyKeys.add(key)
            cleanDaily.add(daily.copy(occurrenceKey = key, taskId = cleanTaskId))
        }

        // Auto-sanitize weekly occurrences: deduplicate
        val cleanWeekly = mutableListOf<WeeklyOccurrenceJson>()
        val seenWeeklyKeys = mutableSetOf<String>()

        for (weekly in plan.weeklyOccurrences) {
            val cleanTaskId = SimplePlanParser.sanitizeId(weekly.taskId)
            if (!taskIds.contains(cleanTaskId)) {
                taskIds.add(cleanTaskId)
            }

            val key = "$cleanTaskId:${weekly.weekId}"
            if (!seenWeeklyKeys.contains(key)) {
                seenWeeklyKeys.add(key)
                cleanWeekly.add(weekly.copy(occurrenceKey = key, taskId = cleanTaskId))
            }
        }

        val sanitizedPlan = plan.copy(
            schemaVersion = 1,
            tasks = cleanTasks,
            dailyOccurrences = cleanDaily,
            weeklyOccurrences = cleanWeekly
        )

        return Pair(sanitizedPlan, ValidationResult.Valid)
    }
}
