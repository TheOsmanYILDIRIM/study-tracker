package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

object SimplePlanParser {

    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val weekIdRegex = Regex("""^\d{4}-W(0[1-9]|[1-4][0-9]|5[0-3])$""")

    /**
     * Basit metin tabanlı (Key-Value / DSL) plan formatını doğrular ve Plan modeline dönüştürür.
     */
    fun parse(rawText: String): Pair<Plan?, ValidationResult> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("//") }

        var weekId = ""
        var weekStartDate = ""
        var childId = "child_1"
        val schemaVersion = 1
        var timezone = "Europe/Istanbul"

        // Section tracking: 0 = HEADER, 1 = TASKS, 2 = DAYS/DAILY, 3 = WEEKLY
        var currentSection = 0

        val taskDefMap = mutableMapOf<String, TaskTemplate>() // id -> TaskTemplate
        val dailyMap = mutableMapOf<String, MutableList<String>>() // date -> list of taskIds/titles
        val weeklyOccurrences = mutableListOf<WeeklyOccurrenceJson>()

        // Date calculation helper
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var activeDayDate: String? = null

        for (line in lines) {
            val upper = line.uppercase(Locale.ROOT)

            // Section markers
            when {
                upper.startsWith("[DERSLER]") || upper.startsWith("[TASKS]") || upper.startsWith("[GOREVLER]") -> {
                    currentSection = 1
                    continue
                }
                upper.startsWith("[GUNLER]") || upper.startsWith("[DAYS]") || upper.startsWith("[GUNLUK]") || upper.startsWith("[DAILY]") -> {
                    currentSection = 2
                    continue
                }
                upper.startsWith("[HAFTALIK]") || upper.startsWith("[WEEKLY]") || upper.startsWith("[HEDEFLER]") -> {
                    currentSection = 3
                    continue
                }
            }

            // Key = Value headers
            if (line.contains("=") || line.contains(":")) {
                val delimiter = if (line.contains("=")) "=" else ":"
                val key = line.substringBefore(delimiter).trim().uppercase(Locale.ROOT)
                val value = line.substringAfter(delimiter).trim()

                when (key) {
                    "HAFTA", "WEEK", "WEEKID", "WEEK_ID" -> {
                        weekId = value
                        continue
                    }
                    "BASLANGIC", "START", "START_DATE", "WEEK_START_DATE", "TARIH" -> {
                        weekStartDate = value
                        continue
                    }
                    "OGRENCI", "CHILD", "CHILD_ID", "STUDENT" -> {
                        childId = value
                        continue
                    }
                    "TIMEZONE", "ZAMAN_DILIMI" -> {
                        timezone = value
                        continue
                    }
                }
            }

            // If we are in Days section and line starts with a day name (e.g. "Pazartesi = mat, turkce" or "Pazartesi:")
            val dayOffset = parseDayOfWeekOffset(line.substringBefore("=").substringBefore(":").trim())
            if (dayOffset != null && weekStartDate.isNotBlank() && dateRegex.matches(weekStartDate)) {
                val cal = Calendar.getInstance(Locale.US)
                try {
                    cal.time = dateFormat.parse(weekStartDate) ?: Date()
                    cal.add(Calendar.DAY_OF_MONTH, dayOffset)
                    val targetDate = dateFormat.format(cal.time)

                    val afterSymbol = if (line.contains("=")) line.substringAfter("=") else if (line.contains(":")) line.substringAfter(":") else ""
                    val content = afterSymbol.trim()

                    if (content.isNotBlank()) {
                        // "Pazartesi = mat, turkce, kitap"
                        val tasksInDay = content.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        dailyMap.getOrPut(targetDate) { mutableListOf() }.addAll(tasksInDay)
                    } else {
                        // "Pazartesi:" followed by "- Matematik | 40 dk"
                        activeDayDate = targetDate
                    }
                    continue
                } catch (_: Exception) {}
            }

            // Handle section 1: Task definitions ("mat = Matematik | 40 dk | Soru Çözümü")
            if (currentSection == 1 || (line.contains("=") && currentSection == 0)) {
                if (line.contains("=")) {
                    val id = line.substringBefore("=").trim().lowercase(Locale.ROOT).replace(" ", "_")
                    val parts = line.substringAfter("=").split("|").map { it.trim() }
                    val title = parts.getOrNull(0) ?: id
                    val durationMin = extractMinutes(parts.getOrNull(1) ?: "30")

                    if (id.isNotBlank() && title.isNotBlank()) {
                        taskDefMap[id] = TaskTemplate(
                            taskId = id,
                            title = title,
                            plannedMinutes = durationMin,
                            kind = TaskKind.DAILY
                        )
                    }
                    continue
                }
            }

            // Handle daily bullet points ("- Matematik | 40 dk | ..." or "- mat")
            if (line.startsWith("-") || line.startsWith("*")) {
                val item = line.removePrefix("-").removePrefix("*").trim()
                if (item.contains("|")) {
                    val parts = item.split("|").map { it.trim() }
                    val title = parts.getOrNull(0) ?: "Ders"
                    val durationMin = extractMinutes(parts.getOrNull(1) ?: "30")
                    val id = sanitizeId(title)

                    taskDefMap.putIfAbsent(id, TaskTemplate(
                        taskId = id,
                        title = title,
                        plannedMinutes = durationMin,
                        kind = if (currentSection == 3) TaskKind.WEEKLY else TaskKind.DAILY
                    ))

                    if (currentSection == 3) {
                        // Weekly task
                        weeklyOccurrences.add(WeeklyOccurrenceJson(
                            occurrenceKey = "$id:$weekId",
                            taskId = id,
                            weekId = weekId,
                            title = title,
                            targetMode = TargetMode.COUNT,
                            targetCount = 1,
                            plannedMinutes = durationMin,
                            reviewRequired = true
                        ))
                    } else if (activeDayDate != null) {
                        dailyMap.getOrPut(activeDayDate!!) { mutableListOf() }.add(id)
                    }
                } else {
                    // Just task id or task title
                    val id = sanitizeId(item)
                    if (currentSection == 3) {
                        weeklyOccurrences.add(WeeklyOccurrenceJson(
                            occurrenceKey = "$id:$weekId",
                            taskId = id,
                            weekId = weekId,
                            title = item,
                            targetMode = TargetMode.COUNT,
                            targetCount = 1,
                            plannedMinutes = 60,
                            reviewRequired = true
                        ))
                    } else if (activeDayDate != null) {
                        dailyMap.getOrPut(activeDayDate!!) { mutableListOf() }.add(item)
                    }
                }
                continue
            }

            // Handle Section 3: Weekly tasks ("deneme = Deneme Sınavı | 90 dk | Açıklama")
            if (currentSection == 3 && line.contains("=")) {
                val id = line.substringBefore("=").trim().lowercase(Locale.ROOT).replace(" ", "_")
                val parts = line.substringAfter("=").split("|").map { it.trim() }
                val title = parts.getOrNull(0) ?: id
                val durationMin = extractMinutes(parts.getOrNull(1) ?: "45")

                taskDefMap[id] = TaskTemplate(
                    taskId = id,
                    title = title,
                    plannedMinutes = durationMin,
                    kind = TaskKind.WEEKLY
                )

                weeklyOccurrences.add(WeeklyOccurrenceJson(
                    occurrenceKey = "$id:$weekId",
                    taskId = id,
                    weekId = weekId,
                    title = title,
                    targetMode = TargetMode.COUNT,
                    targetCount = 1,
                    plannedMinutes = durationMin,
                    reviewRequired = true
                ))
                continue
            }
        }

        // Auto-derive weekId or weekStartDate if missing
        if (weekId.isBlank() && weekStartDate.isNotBlank() && dateRegex.matches(weekStartDate)) {
            weekId = deriveWeekIdFromDate(weekStartDate)
        } else if (weekStartDate.isBlank() && weekId.isNotBlank() && weekIdRegex.matches(weekId)) {
            weekStartDate = deriveStartDateFromWeekId(weekId)
        }

        if (weekId.isBlank()) {
            return Pair(null, ValidationResult.Invalid("Hafta bilgisi eksik. Örn: 'HAFTA = 2026-W38'"))
        }
        if (weekStartDate.isBlank() || !dateRegex.matches(weekStartDate)) {
            return Pair(null, ValidationResult.Invalid("Başlangıç tarihi eksik veya hatalı. Örn: 'BASLANGIC = 2026-09-14'"))
        }

        // If daily occurrences were defined by day names/dates, construct Daily Occurrences list
        val dailyOccurrences = mutableListOf<DailyOccurrenceJson>()
        for ((date, taskRefList) in dailyMap) {
            for (ref in taskRefList) {
                val cleanRef = ref.lowercase(Locale.ROOT).replace(" ", "_")
                val template = taskDefMap[cleanRef] ?: taskDefMap.values.find { it.title.equals(ref, ignoreCase = true) }
                val taskId = template?.taskId ?: cleanRef
                val title = template?.title ?: ref
                val duration = template?.plannedMinutes ?: 30

                if (!taskDefMap.containsKey(taskId)) {
                    taskDefMap[taskId] = TaskTemplate(
                        taskId = taskId,
                        title = title,
                        plannedMinutes = duration,
                        kind = TaskKind.DAILY
                    )
                }

                dailyOccurrences.add(DailyOccurrenceJson(
                    occurrenceKey = "$taskId:$date",
                    taskId = taskId,
                    date = date,
                    title = title,
                    plannedMinutes = duration,
                    reviewRequired = true
                ))
            }
        }

        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())

        val plan = Plan(
            schemaVersion = schemaVersion,
            planId = "plan_${weekId}_${childId}_${System.currentTimeMillis()}",
            childId = childId,
            weekId = weekId,
            weekStartDate = weekStartDate,
            timezone = timezone,
            updatedAt = nowIso,
            tasks = taskDefMap.values.toList(),
            dailyOccurrences = dailyOccurrences,
            weeklyOccurrences = weeklyOccurrences
        )

        return Pair(plan, ValidationResult.Valid)
    }

    /**
     * Plan nesnesini temiz, okunabilir Basit Değişken/DSL formatına çevirir.
     */
    fun exportToSimpleText(plan: Plan): String {
        val sb = StringBuilder()
        sb.appendLine("# StudyTracker Haftalık Plan")
        sb.appendLine("HAFTA = ${plan.weekId}")
        sb.appendLine("BASLANGIC = ${plan.weekStartDate}")
        sb.appendLine("OGRENCI = ${plan.childId}")
        sb.appendLine("TIMEZONE = ${plan.timezone}")
        sb.appendLine()

        sb.appendLine("[DERSLER]")
        for (task in plan.tasks.filter { it.kind == TaskKind.DAILY }) {
            sb.appendLine("${task.taskId} = ${task.title} | ${task.plannedMinutes} dk")
        }
        sb.appendLine()

        sb.appendLine("[GUNLER]")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameFormat = SimpleDateFormat("EEEE", Locale("tr", "TR"))
        val groupedByDate = plan.dailyOccurrences.groupBy { it.date }

        // Sort dates
        val sortedDates = groupedByDate.keys.filter { it.isNotBlank() }.sorted()
        for (dateStr in sortedDates) {
            val occurrences = groupedByDate[dateStr] ?: emptyList()
            val dayName = try {
                val d = dateFormat.parse(dateStr)
                if (d != null) dayNameFormat.format(d).replaceFirstChar { it.uppercase() } else dateStr
            } catch (_: Exception) {
                dateStr
            }
            val taskIds = occurrences.joinToString(", ") { it.taskId }
            sb.appendLine("$dayName = $taskIds")
        }
        sb.appendLine()

        if (plan.weeklyOccurrences.isNotEmpty()) {
            sb.appendLine("[HAFTALIK]")
            for (weekly in plan.weeklyOccurrences) {
                sb.appendLine("${weekly.taskId} = ${weekly.title} | ${weekly.plannedMinutes} dk")
            }
        }

        return sb.toString()
    }

    private fun parseDayOfWeekOffset(name: String): Int? {
        val n = name.trim().lowercase(Locale("tr", "TR"))
        return when {
            n.startsWith("pazartesi") || n.startsWith("mon") || n == "pzt" -> 0
            n.startsWith("sali") || n.startsWith("salı") || n.startsWith("tue") || n == "sal" -> 1
            n.startsWith("carsamba") || n.startsWith("çarşamba") || n.startsWith("wed") || n == "çar" || n == "car" -> 2
            n.startsWith("persembe") || n.startsWith("perşembe") || n.startsWith("thu") || n == "per" -> 3
            n.startsWith("cuma") || n.startsWith("fri") || n == "cum" -> 4
            n.startsWith("cumartesi") || n.startsWith("sat") || n == "cmt" -> 5
            n.startsWith("pazar") || n.startsWith("sun") || n == "paz" -> 6
            else -> null
        }
    }

    private fun extractMinutes(str: String): Int {
        val digits = str.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 30
    }

    private fun sanitizeId(title: String): String {
        return title.trim().lowercase(Locale("tr", "TR"))
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
            .replace(Regex("""[^a-z0-9_]"""), "_")
            .replace(Regex("""_+"""), "_")
            .trim('_')
    }

    private fun deriveWeekIdFromDate(dateStr: String): String {
        val cal = Calendar.getInstance(Locale.US)
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: Date()
        cal.time = d
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.minimalDaysInFirstWeek = 4
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    private fun deriveStartDateFromWeekId(weekIdStr: String): String {
        val parts = weekIdStr.split("-W")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val week = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val cal = Calendar.getInstance(Locale.US)
        cal.clear()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.minimalDaysInFirstWeek = 4
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.WEEK_OF_YEAR, week)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
}
