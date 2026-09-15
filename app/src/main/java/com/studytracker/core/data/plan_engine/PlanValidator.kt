package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.Plan
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
        val plan: Plan = if (trimmed.startsWith("{")) {
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

        if (plan.schemaVersion != 1) {
            return Pair(plan, ValidationResult.Invalid("Desteklenmeyen schemaVersion: ${plan.schemaVersion}. Beklenen: 1"))
        }

        if (plan.planId.isBlank()) {
            return Pair(plan, ValidationResult.Invalid("planId alanı boş bırakılamaz."))
        }

        if (!weekIdRegex.matches(plan.weekId)) {
            return Pair(plan, ValidationResult.Invalid("Hatalı weekId formatı: '${plan.weekId}'. Örnek: '2026-W25'"))
        }

        if (!dateRegex.matches(plan.weekStartDate)) {
            return Pair(plan, ValidationResult.Invalid("Hatalı weekStartDate formatı: '${plan.weekStartDate}'. Örnek: '2026-06-15'"))
        }

        val taskIds = mutableSetOf<String>()
        for ((index, task) in plan.tasks.withIndex()) {
            if (task.taskId.isBlank()) {
                return Pair(plan, ValidationResult.Invalid("tasks[$index].taskId alanı boş olamaz."))
            }
            if (!taskIdRegex.matches(task.taskId)) {
                return Pair(plan, ValidationResult.Invalid("tasks[$index].taskId geçersiz: '${task.taskId}'. Sadece küçük harf, rakam ve alt çizgi kullanılabilir."))
            }
            if (!taskIds.add(task.taskId)) {
                return Pair(plan, ValidationResult.Invalid("Yinelenen taskId tespit edildi: '${task.taskId}'"))
            }
        }

        val dailyKeys = mutableSetOf<String>()
        for ((index, daily) in plan.dailyOccurrences.withIndex()) {
            if (daily.occurrenceKey.isBlank()) {
                return Pair(plan, ValidationResult.Invalid("dailyOccurrences[$index].occurrenceKey boş olamaz."))
            }
            val expectedKey = "${daily.taskId}:${daily.date}"
            if (daily.occurrenceKey != expectedKey) {
                return Pair(plan, ValidationResult.Invalid("dailyOccurrences[$index].occurrenceKey uyumsuz: '${daily.occurrenceKey}'. Beklenen: '$expectedKey'"))
            }
            if (!taskIds.contains(daily.taskId)) {
                return Pair(plan, ValidationResult.Invalid("dailyOccurrences[$index] tanımsız taskId referans veriyor: '${daily.taskId}'"))
            }
            if (!dateRegex.matches(daily.date)) {
                return Pair(plan, ValidationResult.Invalid("dailyOccurrences[$index].date formatı hatalı: '${daily.date}'"))
            }
            if (!dailyKeys.add(daily.occurrenceKey)) {
                return Pair(plan, ValidationResult.Invalid("Yinelenen daily occurrenceKey tespit edildi: '${daily.occurrenceKey}'"))
            }
        }

        val weeklyKeys = mutableSetOf<String>()
        for ((index, weekly) in plan.weeklyOccurrences.withIndex()) {
            if (weekly.occurrenceKey.isBlank()) {
                return Pair(plan, ValidationResult.Invalid("weeklyOccurrences[$index].occurrenceKey boş olamaz."))
            }
            val expectedKey = "${weekly.taskId}:${weekly.weekId}"
            if (weekly.occurrenceKey != expectedKey) {
                return Pair(plan, ValidationResult.Invalid("weeklyOccurrences[$index].occurrenceKey uyumsuz: '${weekly.occurrenceKey}'. Beklenen: '$expectedKey'"))
            }
            if (!taskIds.contains(weekly.taskId)) {
                return Pair(plan, ValidationResult.Invalid("weeklyOccurrences[$index] tanımsız taskId referans veriyor: '${weekly.taskId}'"))
            }
            if (!weekIdRegex.matches(weekly.weekId)) {
                return Pair(plan, ValidationResult.Invalid("weeklyOccurrences[$index].weekId formatı hatalı: '${weekly.weekId}'"))
            }
            if (!weeklyKeys.add(weekly.occurrenceKey)) {
                return Pair(plan, ValidationResult.Invalid("Yinelenen weekly occurrenceKey tespit edildi: '${weekly.occurrenceKey}'"))
            }
        }

        return Pair(plan, ValidationResult.Valid)
    }
}
