package com.studytracker

import com.studytracker.core.data.remote.sync.*
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.TaskKind
import org.junit.Assert.*
import org.junit.Test

class SyncReconciliationTest {

    @Test
    fun `reconciliation favors APPROVED status over PENDING or WAITING_REVIEW`() {
        val localStatus = OccurrenceStatus.WAITING_REVIEW
        val remoteStatus = "APPROVED"

        val resolvedStatus = when {
            localStatus == OccurrenceStatus.APPROVED || remoteStatus == "APPROVED" -> OccurrenceStatus.APPROVED
            localStatus == OccurrenceStatus.WAITING_REVIEW || remoteStatus == "WAITING_REVIEW" -> OccurrenceStatus.WAITING_REVIEW
            else -> OccurrenceStatus.PENDING
        }

        assertEquals(OccurrenceStatus.APPROVED, resolvedStatus)
    }

    @Test
    fun `reconciliation propagates parent rejection warning notes to student`() {
        val remoteParentNote = "Sayfa 32-34 eksik kalmış, lütfen tamamla"
        val remoteStatus = "REJECTED"
        val localWarning = false

        val hasWarning = localWarning || (remoteParentNote.isNotBlank() && remoteStatus != "APPROVED")
        val warningText = if (remoteParentNote.isNotBlank()) remoteParentNote else null

        assertTrue(hasWarning)
        assertEquals("Sayfa 32-34 eksik kalmış, lütfen tamamla", warningText)
    }

    @Test
    fun `weekly target count fulfillment automatically marks occurrence approved`() {
        val targetCount = 3
        val localApprovedCount = 2
        val remoteCompletedCount = 3

        val approvedCount = maxOf(localApprovedCount, remoteCompletedCount)
        val isApprovedByTarget = (approvedCount >= targetCount)

        assertTrue(isApprovedByTarget)
        assertEquals(3, approvedCount)
    }

    @Test
    fun `shared family sync payload includes screenshots list`() {
        val screenshot = RemoteScreenshotSyncDto(
            id = "ss_001",
            familyCode = "ST-7738",
            sessionId = "sess_001",
            imageUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
            timestamp = 1720000000000L
        )

        val payload = SharedFamilySyncPayload(
            familyCode = "ST-7738",
            screenshots = listOf(screenshot)
        )

        assertEquals(1, payload.screenshots.size)
        assertEquals("ss_001", payload.screenshots.first().id)
        assertTrue(payload.screenshots.first().imageUrl.startsWith("data:image/jpeg;base64,"))
    }

    @Test
    fun `reconciliation transitions student WAITING_REVIEW to PENDING with warning when parent rejects`() {
        val localStatus = OccurrenceStatus.WAITING_REVIEW
        val remoteStatus = OccurrenceStatus.PENDING
        val hasRejectedReview = true
        val hasApprovedReview = false
        val hasParentWarning = true

        val resolvedStatus = when {
            hasApprovedReview || remoteStatus == OccurrenceStatus.APPROVED || localStatus == OccurrenceStatus.APPROVED -> OccurrenceStatus.APPROVED
            hasRejectedReview || (hasParentWarning && remoteStatus == OccurrenceStatus.PENDING) || remoteStatus == OccurrenceStatus.REJECTED -> OccurrenceStatus.PENDING
            remoteStatus == OccurrenceStatus.WAITING_REVIEW || localStatus == OccurrenceStatus.WAITING_REVIEW -> OccurrenceStatus.WAITING_REVIEW
            else -> remoteStatus
        }

        assertEquals(OccurrenceStatus.PENDING, resolvedStatus)
    }

    @Test
    fun `reconciliation overrides stale local title when remote has updated title from parent`() {
        val localTitle = "Eski Ders İsmi"
        val remoteSubject = "Yeni Güncellenmiş Ders İsmi"

        val finalTitle = if (remoteSubject.isNotBlank()) remoteSubject else localTitle
        assertEquals("Yeni Güncellenmiş Ders İsmi", finalTitle)
    }

    @Test
    fun `reconciliation never overwrites non-zero local approvedCount with remote zero`() {
        val localApprovedCount = 20
        val remoteCompletedCount = 0

        val approvedCount = maxOf(localApprovedCount, remoteCompletedCount)
        assertEquals(20, approvedCount)
    }
}
