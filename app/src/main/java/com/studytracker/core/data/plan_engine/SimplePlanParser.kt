package com.studytracker.core.data.plan_engine

import com.studytracker.core.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

object SimplePlanParser {

    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
    private val weekIdRegex = Regex("""^\d{4}-W(0[1-9]|[1-4][0-9]|5[0-3])$""")
    private val URL_REGEX = Regex("""(https?://[^\s|"'<>)]+|(?:\bwww\.|(?:\bm\.)?youtube\.com/|youtu\.be/)[^\s|"'<>)]+)""", RegexOption.IGNORE_CASE)

    fun extractUrl(text: String): String? {
        val m = URL_REGEX.find(text) ?: return null
        var u = m.value.trim()
        while (u.isNotEmpty() && (u.endsWith(")") || u.endsWith("]") || u.endsWith(".") || u.endsWith(",") || u.endsWith(";"))) {
            u = u.dropLast(1).trim()
        }
        if (u.isBlank()) return null
        return if (!u.startsWith("http://", ignoreCase = true) && !u.startsWith("https://", ignoreCase = true)) {
            "https://$u"
        } else u
    }

    /**
     * Basit metin tabanlı (Key-Value / DSL) plan formatını aşırı toleranslı şekilde ayrıştırır.
     * Kullanıcı prompt metni, sohbet açıklamaları, şablon örnekleri veya çift blok yapıştırsa bile
     * gerçek planı bulur, yinelenen anahtarları otomatik tekilleştirir.
     */
    fun parse(rawText: String): Pair<Plan?, ValidationResult> {
        // Remove markdown code blocks (```text, ```json, etc.)
        val cleanText = rawText
            .replace(Regex("""```[a-zA-Z]*"""), "")
            .replace("```", "")

        val allLines = cleanText.lines().map { it.trim() }.filter {
            it.isNotEmpty() &&
            !it.startsWith("#") &&
            !it.startsWith("//") &&
            !it.startsWith("---") &&
            !it.startsWith("Sen uzman", ignoreCase = true) &&
            !it.startsWith("Amacın:", ignoreCase = true) &&
            !it.startsWith("ÇIKTI FORMATI", ignoreCase = true) &&
            !it.startsWith("Aşağıdaki basit", ignoreCase = true) &&
            !it.startsWith("Şimdi hiçbir", ignoreCase = true)
        }

        var weekId = ""
        var weekStartDate = ""
        var childId = "child_1"
        val schemaVersion = 1
        var timezone = "Europe/Istanbul"

        // Section tracking: 0 = HEADER, 1 = TASKS, 2 = DAYS/DAILY, 3 = WEEKLY
        var currentSection = 0

        var taskDefMap = mutableMapOf<String, TaskTemplate>()
        var dailyMap = mutableMapOf<String, MutableList<String>>() // date -> list of taskIds/titles
        var weeklyOccurrences = mutableListOf<WeeklyOccurrenceJson>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var activeDayDate: String? = null

        var hasProcessedDays = false

        for (line in allLines) {
            val upper = line.uppercase(Locale.ROOT)

            // Ignore prompt artifact lines
            if (upper.startsWith("ÖRNEK ÇIKTI") || upper.startsWith("ORNEK CIKTI") ||
                upper.startsWith("HEDEF HAFTA") || upper.startsWith("MEVCUT PLAN") ||
                upper.startsWith("KULLANICI ÖZEL") || upper.startsWith("KULLANICI OZEL")) {
                continue
            }

            // Section markers
            when {
                upper.startsWith("[DERSLER]") || upper.startsWith("[TASKS]") || upper.startsWith("[GOREVLER]") -> {
                    // If we previously completed days and encounter a new [DERSLER] block (e.g. prompt example then real plan), reset to real plan!
                    if (hasProcessedDays) {
                        taskDefMap.clear()
                        dailyMap.clear()
                        weeklyOccurrences.clear()
                        hasProcessedDays = false
                    }
                    currentSection = 1
                    continue
                }
                upper.startsWith("[GUNLER]") || upper.startsWith("[DAYS]") || upper.startsWith("[GUNLUK]") || upper.startsWith("[DAILY]") -> {
                    currentSection = 2
                    hasProcessedDays = true
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
                        val cleanVal = value.filter { it.isLetterOrDigit() || it == '-' }
                        if (cleanVal.isNotBlank()) weekId = cleanVal
                        continue
                    }
                    "BASLANGIC", "START", "START_DATE", "WEEK_START_DATE", "TARIH" -> {
                        val cleanVal = value.filter { it.isDigit() || it == '-' }
                        if (cleanVal.isNotBlank()) weekStartDate = cleanVal
                        continue
                    }
                    "OGRENCI", "CHILD", "CHILD_ID", "STUDENT" -> {
                        if (value.isNotBlank() && value != "null") childId = value
                        continue
                    }
                    "TIMEZONE", "ZAMAN_DILIMI" -> {
                        if (value.isNotBlank() && value != "null") timezone = value
                        continue
                    }
                }
            }

            // Handle Days section when line starts with day name (e.g. "Pazartesi = mat, turkce" or "Pazartesi:")
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
                        val dayList = dailyMap.getOrPut(targetDate) { mutableListOf() }
                        for (t in tasksInDay) {
                            if (!dayList.contains(t)) {
                                dayList.add(t)
                            }
                        }
                    } else {
                        // "Pazartesi:" followed by "- Matematik | 40 dk"
                        activeDayDate = targetDate
                    }
                    hasProcessedDays = true
                    continue
                } catch (_: Exception) {}
            }

            // Handle section 1: Task definitions ("mat = Matematik | 40 dk | Soru Çözümü | https://youtu.be/...")
            if (currentSection == 1 || (line.contains("=") && currentSection == 0)) {
                if (line.contains("=")) {
                    val rawId = line.substringBefore("=").trim()
                    val id = sanitizeId(rawId)
                    val afterEq = line.substringAfter("=")
                    val parts = afterEq.split("|").map { it.trim() }
                    val title = parts.getOrNull(0) ?: rawId
                    val durationMin = extractMinutes(parts.getOrNull(1) ?: "30")
                    val potentialUrl = extractUrl(afterEq)

                    if (id.isNotBlank() && title.isNotBlank()) {
                        taskDefMap[id] = TaskTemplate(
                            taskId = id,
                            title = title,
                            plannedMinutes = durationMin,
                            youtubeUrl = potentialUrl,
                            contentType = if (potentialUrl != null) ContentType.VIDEO else ContentType.OTHER,
                            kind = TaskKind.DAILY
                        )
                    }
                    continue
                }
            }

            // Handle daily bullet points ("- Matematik | 40 dk | https://..." or "- mat")
            if (line.startsWith("-") || line.startsWith("*")) {
                val item = line.removePrefix("-").removePrefix("*").trim()
                if (item.contains("|")) {
                    val parts = item.split("|").map { it.trim() }
                    val title = parts.getOrNull(0) ?: "Ders"
                    val durationMin = extractMinutes(parts.getOrNull(1) ?: "30")
                    val potentialUrl = extractUrl(item)
                    val id = sanitizeId(title)

                    taskDefMap.putIfAbsent(id, TaskTemplate(
                        taskId = id,
                        title = title,
                        plannedMinutes = durationMin,
                        youtubeUrl = potentialUrl,
                        contentType = if (potentialUrl != null) ContentType.VIDEO else ContentType.OTHER,
                        kind = if (currentSection == 3) TaskKind.WEEKLY else TaskKind.DAILY
                    ))

                    if (currentSection == 3) {
                        val key = "$id:$weekId"
                        if (weeklyOccurrences.none { it.occurrenceKey == key }) {
                            weeklyOccurrences.add(WeeklyOccurrenceJson(
                                occurrenceKey = key,
                                taskId = id,
                                weekId = weekId,
                                title = title,
                                targetMode = TargetMode.COUNT,
                                targetCount = 1,
                                plannedMinutes = durationMin,
                                reviewRequired = true
                            ))
                        }
                    } else if (activeDayDate != null) {
                        val dayList = dailyMap.getOrPut(activeDayDate!!) { mutableListOf() }
                        if (!dayList.contains(id)) {
                            dayList.add(id)
                        }
                    }
                } else {
                    val id = sanitizeId(item)
                    val potentialUrl = extractUrl(item)
                    if (currentSection == 3) {
                        val key = "$id:$weekId"
                        if (weeklyOccurrences.none { it.occurrenceKey == key }) {
                            weeklyOccurrences.add(WeeklyOccurrenceJson(
                                occurrenceKey = key,
                                taskId = id,
                                weekId = weekId,
                                title = item,
                                targetMode = TargetMode.COUNT,
                                targetCount = 1,
                                plannedMinutes = 60,
                                reviewRequired = true
                            ))
                        }
                    } else if (activeDayDate != null) {
                        val dayList = dailyMap.getOrPut(activeDayDate!!) { mutableListOf() }
                        if (!dayList.contains(item)) {
                            dayList.add(item)
                        }
                    }
                }
                continue
            }

            // Handle Section 3: Weekly tasks ("deneme = Deneme Sınavı | 90 dk | https://...")
            if (currentSection == 3 && line.contains("=")) {
                val rawId = line.substringBefore("=").trim()
                val id = sanitizeId(rawId)
                val afterEq = line.substringAfter("=")
                val parts = afterEq.split("|").map { it.trim() }
                val title = parts.getOrNull(0) ?: rawId
                val durationMin = extractMinutes(parts.getOrNull(1) ?: "45")
                val potentialUrl = extractUrl(afterEq)

                taskDefMap[id] = TaskTemplate(
                    taskId = id,
                    title = title,
                    plannedMinutes = durationMin,
                    youtubeUrl = potentialUrl,
                    contentType = if (potentialUrl != null) ContentType.VIDEO else ContentType.OTHER,
                    kind = TaskKind.WEEKLY
                )

                val key = "$id:$weekId"
                if (weeklyOccurrences.none { it.occurrenceKey == key }) {
                    weeklyOccurrences.add(WeeklyOccurrenceJson(
                        occurrenceKey = key,
                        taskId = id,
                        weekId = weekId,
                        title = title,
                        targetMode = TargetMode.COUNT,
                        targetCount = 1,
                        plannedMinutes = durationMin,
                        reviewRequired = true
                    ))
                }
                continue
            }
        }

        // Auto-derive weekId or weekStartDate if missing or malformed
        if (weekId.isBlank() && weekStartDate.isNotBlank() && dateRegex.matches(weekStartDate)) {
            weekId = deriveWeekIdFromDate(weekStartDate)
        } else if (weekStartDate.isBlank() && weekId.isNotBlank() && weekIdRegex.matches(weekId)) {
            weekStartDate = deriveStartDateFromWeekId(weekId)
        }

        if (weekId.isBlank()) {
            val nowCal = Calendar.getInstance(Locale.US)
            val year = nowCal.get(Calendar.YEAR)
            val week = nowCal.get(Calendar.WEEK_OF_YEAR)
            weekId = String.format(Locale.US, "%04d-W%02d", year, week)
        }
        if (weekStartDate.isBlank() || !dateRegex.matches(weekStartDate)) {
            weekStartDate = deriveStartDateFromWeekId(weekId)
        }

        // Build Daily Occurrences list with guaranteed unique occurrenceKeys
        val dailyOccurrences = mutableListOf<DailyOccurrenceJson>()
        val seenDailyKeys = mutableSetOf<String>()

        for ((date, taskRefList) in dailyMap) {
            for (ref in taskRefList) {
                val cleanRef = sanitizeId(ref)
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

                var uniqueKey = "$taskId:$date"
                if (seenDailyKeys.contains(uniqueKey)) {
                    // If same task is added multiple times on same day, give index
                    var idx = 2
                    while (seenDailyKeys.contains("${taskId}_$idx:$date")) {
                        idx++
                    }
                    uniqueKey = "${taskId}_$idx:$date"
                }

                val youtubeUrl = template?.youtubeUrl ?: extractUrl(ref) ?: extractUrl(title)

                seenDailyKeys.add(uniqueKey)
                dailyOccurrences.add(DailyOccurrenceJson(
                    occurrenceKey = uniqueKey,
                    taskId = taskId,
                    date = date,
                    title = title,
                    plannedMinutes = duration,
                    youtubeUrl = youtubeUrl,
                    reviewRequired = true
                ))
            }
        }

        // Clean weekly occurrences with unique keys
        val cleanWeekly = weeklyOccurrences.distinctBy { it.occurrenceKey }

        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())

        val plan = Plan(
            schemaVersion = schemaVersion,
            planId = "plan_${weekId}_${childId}_${System.currentTimeMillis()}",
            childId = childId,
            weekId = weekId,
            weekStartDate = weekStartDate,
            timezone = timezone,
            updatedAt = nowIso,
            tasks = taskDefMap.values.distinctBy { it.taskId },
            dailyOccurrences = dailyOccurrences,
            weeklyOccurrences = cleanWeekly
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
            val urlSuffix = if (!task.youtubeUrl.isNullOrBlank()) " | ${task.youtubeUrl}" else ""
            sb.appendLine("${task.taskId} = ${task.title} | ${task.plannedMinutes} dk$urlSuffix")
        }
        sb.appendLine()

        sb.appendLine("[GUNLER]")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameFormat = SimpleDateFormat("EEEE", Locale("tr", "TR"))
        val groupedByDate = plan.dailyOccurrences.groupBy { it.date }

        val sortedDates = groupedByDate.keys.filter { it.isNotBlank() }.sorted()
        for (dateStr in sortedDates) {
            val occurrences = groupedByDate[dateStr] ?: emptyList()
            val dayName = try {
                val d = dateFormat.parse(dateStr)
                if (d != null) dayNameFormat.format(d).replaceFirstChar { it.uppercase() } else dateStr
            } catch (_: Exception) {
                dateStr
            }
            val taskIds = occurrences.map { it.taskId }.distinct().joinToString(", ")
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

    fun sanitizeId(title: String): String {
        val sanitized = title.trim().lowercase(Locale("tr", "TR"))
            .replace("ç", "c")
            .replace("ğ", "g")
            .replace("ı", "i")
            .replace("ö", "o")
            .replace("ş", "s")
            .replace("ü", "u")
            .replace(Regex("""[^a-z0-9_]"""), "_")
            .replace(Regex("""_+"""), "_")
            .trim('_')
        return if (sanitized.isNotBlank()) sanitized else "task"
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
