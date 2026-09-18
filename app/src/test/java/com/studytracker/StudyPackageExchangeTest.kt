package com.studytracker

import com.studytracker.core.data.package_exchange.PackageType
import com.studytracker.core.data.package_exchange.StudyTrackerPackage
import com.studytracker.core.data.remote.sync.LocalPlanSyncDto
import com.studytracker.core.data.remote.sync.RemoteOccurrenceSyncDto
import com.studytracker.core.data.remote.sync.RemoteScreenshotSyncDto
import com.studytracker.core.data.remote.sync.RemoteSessionSyncDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class StudyPackageExchangeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @Test
    fun `plan distribution package serializes and deserializes cleanly`() {
        val planDto = LocalPlanSyncDto(
            planId = "plan_w38",
            weekId = "2026-W38",
            weekStartDate = "2026-09-14",
            childId = "child_1",
            timezone = "Europe/Istanbul",
            updatedAt = "2026-09-14T00:00:00Z",
            rawJson = "{}"
        )

        val occ = RemoteOccurrenceSyncDto(
            id = "occ_math_1",
            familyCode = "ST-2026",
            date = "2026-09-15",
            planId = "task_math",
            subject = "Matematik - Fonksiyonlar",
            topic = "DAILY",
            targetDurationMin = 45,
            targetQuestionCount = 20,
            completedDurationMin = 0,
            completedQuestionCount = 0,
            status = "PENDING",
            parentNote = "",
            weekId = "2026-W38",
            orderIndex = 1,
            youtubeUrl = "https://youtu.be/dQw4w9WgXcQ"
        )

        val pkg = StudyTrackerPackage(
            formatVersion = 1,
            packageType = PackageType.PLAN_DISTRIBUTION,
            familyCode = "ST-2026",
            senderRole = "PARENT",
            title = "Haftalık Çalışma Planı",
            plan = planDto,
            occurrences = listOf(occ)
        )

        val jsonStr = json.encodeToString(pkg)
        assertNotNull(jsonStr)
        assertTrue(jsonStr.contains(".studyplan") || jsonStr.contains("PLAN_DISTRIBUTION"))

        val decoded = json.decodeFromString<StudyTrackerPackage>(jsonStr)
        assertEquals(PackageType.PLAN_DISTRIBUTION, decoded.packageType)
        assertEquals("ST-2026", decoded.familyCode)
        assertEquals(1, decoded.occurrences.size)
        assertEquals("Matematik - Fonksiyonlar", decoded.occurrences.first().subject)
        assertEquals("https://youtu.be/dQw4w9WgXcQ", decoded.occurrences.first().youtubeUrl)
    }

    @Test
    fun `study report package embeds lossy WebP screenshots and session records`() {
        val screenshot = RemoteScreenshotSyncDto(
            id = "ss_01",
            familyCode = "ST-2026",
            sessionId = "sess_01",
            imageUrl = "data:image/webp;base64,UklGRkAAAABXRUJQVlA4IDQAAADwAQCdASoFAAUAP/Z/v/8AAP7/9wAAAAAAAAAAAA==",
            timestamp = 1720000010000L
        )

        val session = RemoteSessionSyncDto(
            id = "sess_01",
            familyCode = "ST-2026",
            occurrenceId = "occ_math_1",
            startTime = 1720000000000L,
            endTime = 1720002700000L,
            durationMin = 45,
            isCompleted = true
        )

        val pkg = StudyTrackerPackage(
            formatVersion = 1,
            packageType = PackageType.STUDY_REPORT,
            familyCode = "ST-2026",
            senderRole = "CHILD",
            title = "Günlük Çalışma Raporu",
            sessions = listOf(session),
            screenshots = listOf(screenshot)
        )

        val jsonStr = json.encodeToString(pkg)
        val decoded = json.decodeFromString<StudyTrackerPackage>(jsonStr)

        assertEquals(PackageType.STUDY_REPORT, decoded.packageType)
        assertEquals(1, decoded.screenshots.size)
        assertTrue(decoded.screenshots.first().imageUrl.startsWith("data:image/webp"))
        assertEquals(1, decoded.sessions.size)
        assertTrue(decoded.sessions.first().isCompleted)
    }
}
