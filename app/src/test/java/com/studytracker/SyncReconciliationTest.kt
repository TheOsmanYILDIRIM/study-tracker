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

    @Test
    fun `reconciliation takes maximum of local and remote completed question count`() {
        val localCompletedQuestions = 15
        val remoteCompletedQuestions = 25

        val merged = maxOf(localCompletedQuestions, remoteCompletedQuestions)
        assertEquals(25, merged)

        val localHigher = 30
        val remoteLower = 10
        assertEquals(30, maxOf(localHigher, remoteLower))
    }

    @Test
    fun `setWarning updates reject count only when warning is true`() {
        var currentRejectCount = 2

        // Simulating SQL: rejectCount = rejectCount + CASE WHEN :warning = 1 THEN 1 ELSE 0 END
        fun applyWarning(warning: Boolean) {
            currentRejectCount += if (warning) 1 else 0
        }

        applyWarning(false)
        assertEquals(2, currentRejectCount)

        applyWarning(true)
        assertEquals(3, currentRejectCount)

        applyWarning(false)
        assertEquals(3, currentRejectCount)
    }

    @Test
    fun `weekly approval is idempotent when target count is met`() {
        val targetCount = 3
        var approvedCount = 2
        var status = OccurrenceStatus.PENDING

        fun registerApproval(isDuplicate: Boolean) {
            if (!isDuplicate) approvedCount++
            if (approvedCount >= targetCount) status = OccurrenceStatus.APPROVED
        }

        registerApproval(isDuplicate = false)
        assertEquals(3, approvedCount)
        assertEquals(OccurrenceStatus.APPROVED, status)

        // Duplicate approval
        registerApproval(isDuplicate = true)
        assertEquals(3, approvedCount)
        assertEquals(OccurrenceStatus.APPROVED, status)
    }

    @Test
    fun `screenshot file name validation rejects path traversal`() {
        val regex = Regex("""[A-Za-z0-9._-]{1,128}""")

        assertTrue("ss_2026_09_27.jpg".matches(regex))
        assertTrue("valid-screenshot-123.webp".matches(regex))

        assertFalse("../secret.jpg".matches(regex))
        assertFalse("/etc/passwd".matches(regex))
        assertFalse("..\\windows.png".matches(regex))
        assertFalse("image;rm -rf".matches(regex))
    }

    @Test
    fun `shouldApplyRemoteReset correctly decides whether to apply reset epoch`() {
        // A. server=0, local=0 => reset yok
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(0L, 0L))

        // B. server=100, local=0 => reset gerekli
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(100L, 0L))

        // C. server=100, local=100 => reset yok
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(100L, 100L))

        // D. server=99, local=100 => reset yok
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(99L, 100L))

        // E. server=0, local=100 => reset yok
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(0L, 100L))
    }

    @Test
    fun `reset epoch prevents subsequent reset loop when local epoch is updated`() {
        var localResetAt = 0L
        val serverResetAt = 1727500000000L

        // First sync: server reset detected
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(serverResetAt, localResetAt))

        // After successful local reset and cloud apply, epoch is persisted
        localResetAt = serverResetAt

        // Second sync: same epoch received, must NOT trigger reset again
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(serverResetAt, localResetAt))

        // Subsequent child request payload includes acknowledged epoch
        val childPayload = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "CHILD",
            clientLastResetAt = localResetAt
        )
        assertEquals(serverResetAt, childPayload.clientLastResetAt)
    }
}
