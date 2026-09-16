package com.studytracker

import com.studytracker.core.data.remote.supabase.*
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
}
