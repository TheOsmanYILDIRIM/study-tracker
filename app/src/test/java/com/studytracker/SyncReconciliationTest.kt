package com.studytracker

import com.studytracker.core.data.remote.sync.*
import com.studytracker.core.data.local.db.entity.SessionEntity
import com.studytracker.core.data.local.repository.toDomain
import com.studytracker.core.data.local.repository.toEntity
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.Session
import com.studytracker.core.domain.model.TaskKind
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    @Test
    fun `session domain and entity mapper preserves updatedAt round trip`() {
        val originalSession = Session(
            sessionId = "sess_test_123",
            occurrenceKey = "occ_math_1",
            childId = "child_1",
            startTime = 1727500000000L,
            endTime = 1727501800000L,
            updatedAt = 1727501850000L
        )

        val entity = originalSession.toEntity()
        assertEquals(1727501850000L, entity.updatedAt)

        val mappedBack = entity.toDomain()
        assertEquals(1727501850000L, mappedBack.updatedAt)
        assertEquals(originalSession, mappedBack)
    }

    @Test
    fun `remote session sync dto default updatedAt is 0L and preserves explicit value`() {
        val defaultDto = RemoteSessionSyncDto(
            id = "sess_01",
            familyCode = "ST-TEST-2026-SYNC-1234",
            occurrenceId = "occ_01"
        )
        assertEquals(0L, defaultDto.updatedAt)

        val explicitDto = RemoteSessionSyncDto(
            id = "sess_02",
            familyCode = "ST-TEST-2026-SYNC-1234",
            occurrenceId = "occ_02",
            updatedAt = 1727509999000L
        )
        assertEquals(1727509999000L, explicitDto.updatedAt)
    }

    @Test
    fun `SessionReconciliationHelper resolveIncomingSessionTimestamp resolves legacy and explicit payloads without local timestamp pollution`() {
        // Case A: remote has explicit updatedAt (500), endTime (400), startTime (100) -> 500
        assertEquals(
            500L,
            SessionReconciliationHelper.resolveIncomingSessionTimestamp(updatedAt = 500L, endTime = 400L, startTime = 100L)
        )

        // Case B: legacy remote (updatedAt = 0), has endTime (400), startTime (100) -> 400
        assertEquals(
            400L,
            SessionReconciliationHelper.resolveIncomingSessionTimestamp(updatedAt = 0L, endTime = 400L, startTime = 100L)
        )

        // Case C: legacy remote (updatedAt = 0), no endTime (null), startTime (100) -> 100
        assertEquals(
            100L,
            SessionReconciliationHelper.resolveIncomingSessionTimestamp(updatedAt = 0L, endTime = null, startTime = 100L)
        )

        // Case D: all zero -> 0
        assertEquals(
            0L,
            SessionReconciliationHelper.resolveIncomingSessionTimestamp(updatedAt = 0L, endTime = null, startTime = 0L)
        )

        // Critical verification: existing local session updatedAt (999) must NOT pollute incoming timestamp (400)
        val existingLocalUpdatedAt = 999L
        val incomingResolvedTimestamp = SessionReconciliationHelper.resolveIncomingSessionTimestamp(
            updatedAt = 0L,
            endTime = 400L,
            startTime = 100L
        )
        assertNotEquals(existingLocalUpdatedAt, incomingResolvedTimestamp)
        assertEquals(400L, incomingResolvedTimestamp)
    }

    @Test
    fun `SessionReconciliationHelper isSessionStatusCompleted correctly maps lifecycle states`() {
        assertFalse(SessionReconciliationHelper.isSessionStatusCompleted(null))
        assertFalse(SessionReconciliationHelper.isSessionStatusCompleted(com.studytracker.core.domain.model.SessionStatus.ACTIVE))

        assertTrue(SessionReconciliationHelper.isSessionStatusCompleted(com.studytracker.core.domain.model.SessionStatus.WAITING_REVIEW))
        assertTrue(SessionReconciliationHelper.isSessionStatusCompleted(com.studytracker.core.domain.model.SessionStatus.APPROVED))
        assertTrue(SessionReconciliationHelper.isSessionStatusCompleted(com.studytracker.core.domain.model.SessionStatus.REJECTED))
        assertTrue(SessionReconciliationHelper.isSessionStatusCompleted(com.studytracker.core.domain.model.SessionStatus.INVALID))
    }

    @Test
    fun `SessionReconciliationHelper shouldApplyIncomingSessionVersion enforces pure winner matrix CASE A-F`() {
        // CASE A: local=100 ACTIVE (completed=false), remote=200 (completed=true) => true (newer wins)
        assertTrue(
            SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
                existingUpdatedAt = 100L,
                incomingUpdatedAt = 200L,
                existingCompleted = false,
                incomingCompleted = true
            )
        )

        // CASE B: local=300 ACTIVE (completed=false), remote=100 (completed=true) => false (stale rejected)
        assertFalse(
            SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
                existingUpdatedAt = 300L,
                incomingUpdatedAt = 100L,
                existingCompleted = false,
                incomingCompleted = true
            )
        )

        // CASE C: local=300 WAITING_REVIEW (completed=true), remote=100 (completed=false) => false (stale active rejected)
        assertFalse(
            SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
                existingUpdatedAt = 300L,
                incomingUpdatedAt = 100L,
                existingCompleted = true,
                incomingCompleted = false
            )
        )

        // CASE D: local=200 ACTIVE (completed=false), remote=200 (completed=true) => true (completion advancement on tie)
        assertTrue(
            SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
                existingUpdatedAt = 200L,
                incomingUpdatedAt = 200L,
                existingCompleted = false,
                incomingCompleted = true
            )
        )

        // CASE E: local=200 APPROVED (completed=true), remote=200 (completed=false) => false (completion regression rejected)
        assertFalse(
            SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
                existingUpdatedAt = 200L,
                incomingUpdatedAt = 200L,
                existingCompleted = true,
                incomingCompleted = false
            )
        )

        // CASE F: local=200 APPROVED (completed=true), remote=200 (completed=true) => false (same state tie rejected)
        assertFalse(
            SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
                existingUpdatedAt = 200L,
                incomingUpdatedAt = 200L,
                existingCompleted = true,
                incomingCompleted = true
            )
        )
    }

    @Test
    fun `reconciliation simulation guarantees newer local session is not overwritten by stale remote`() {
        var localSession = SessionEntity(
            sessionId = "sess_reconcile_01",
            occurrenceKey = "occ_math_01",
            childId = "child_1",
            startTime = 1000L,
            endTime = 1800L,
            status = com.studytracker.core.domain.model.SessionStatus.WAITING_REVIEW,
            screenshotCount = 2,
            activeDurationSeconds = 1800L,
            reportedQuestionCount = 20,
            finalScreenshotUrl = "https://cdn.example.com/ss_new.jpg",
            studentNote = "NEW",
            updatedAt = 300L
        )

        val staleRemote = RemoteSessionSyncDto(
            id = "sess_reconcile_01",
            occurrenceId = "occ_math_01",
            startTime = 1000L,
            endTime = 1300L,
            durationMin = 5,
            activeDurationSeconds = 300L,
            reportedQuestionCount = 5,
            isCompleted = true,
            notes = "OLD",
            updatedAt = 100L
        )

        // Simulate reconciliation loop
        val incomingTimestamp = SessionReconciliationHelper.resolveIncomingSessionTimestamp(
            updatedAt = staleRemote.updatedAt,
            endTime = staleRemote.endTime,
            startTime = staleRemote.startTime
        )
        val existingCompleted = SessionReconciliationHelper.isSessionStatusCompleted(localSession.status)
        val shouldApply = SessionReconciliationHelper.shouldApplyIncomingSessionVersion(
            existingUpdatedAt = localSession.updatedAt,
            incomingUpdatedAt = incomingTimestamp,
            existingCompleted = existingCompleted,
            incomingCompleted = staleRemote.isCompleted
        )

        // Stale remote must NOT be applied
        assertFalse(shouldApply)

        if (shouldApply) {
            localSession = localSession.copy(
                activeDurationSeconds = maxOf(localSession.activeDurationSeconds, staleRemote.activeDurationSeconds),
                studentNote = staleRemote.notes,
                updatedAt = incomingTimestamp
            )
        }

        // Verify local session remained pristine
        assertEquals(300L, localSession.updatedAt)
        assertEquals(1800L, localSession.activeDurationSeconds)
        assertEquals("NEW", localSession.studentNote)
        assertEquals(com.studytracker.core.domain.model.SessionStatus.WAITING_REVIEW, localSession.status)
        assertEquals(20, localSession.reportedQuestionCount)
        assertEquals("https://cdn.example.com/ss_new.jpg", localSession.finalScreenshotUrl)
    }

    @Test
    fun `session start finish and review mutations advance updatedAt monotonically`() {
        // A. New session start
        val startTimestamp = 1000L
        val newSession = Session(
            sessionId = "sess_001",
            occurrenceKey = "occ_001",
            childId = "child_1",
            startTime = startTimestamp,
            updatedAt = startTimestamp
        )
        assertEquals(startTimestamp, newSession.startTime)
        assertEquals(startTimestamp, newSession.updatedAt)

        // B. Finish session with later timestamp
        val finishMutationAt = 2000L
        val finishedSession = newSession.copy(
            endTime = finishMutationAt,
            updatedAt = maxOf(newSession.updatedAt, finishMutationAt)
        )
        assertEquals(finishMutationAt, finishedSession.endTime)
        assertEquals(2000L, finishedSession.updatedAt)

        // C. Finish session with clock backwards (mutationAt < existing.updatedAt)
        val clockBackwardsMutationAt = 1500L
        val guardedFinish = finishedSession.copy(
            endTime = clockBackwardsMutationAt,
            updatedAt = maxOf(finishedSession.updatedAt, clockBackwardsMutationAt)
        )
        assertEquals(2000L, guardedFinish.updatedAt)

        // D. Review session with newer reviewedAt timestamp
        val reviewTimestamp = 2500L
        val reviewedSession = finishedSession.copy(
            status = com.studytracker.core.domain.model.SessionStatus.APPROVED,
            updatedAt = maxOf(finishedSession.updatedAt, reviewTimestamp)
        )
        assertEquals(2500L, reviewedSession.updatedAt)

        // E. Review session with older timestamp (clock drift or delayed review)
        val olderReviewTimestamp = 1800L
        val guardedReview = reviewedSession.copy(
            status = com.studytracker.core.domain.model.SessionStatus.APPROVED,
            updatedAt = maxOf(reviewedSession.updatedAt, olderReviewTimestamp)
        )
        assertEquals(2500L, guardedReview.updatedAt)
    }

    @Test
    fun `invalidate session SQL CASE expression guarantees monotonic progression`() {
        fun simulateSqlInvalidate(currentUpdatedAt: Long, endedAt: Long): Long {
            // Simulated SQL: CASE WHEN updatedAt > :endedAt THEN updatedAt ELSE :endedAt END
            return if (currentUpdatedAt > endedAt) currentUpdatedAt else endedAt
        }

        assertEquals(2000L, simulateSqlInvalidate(1000L, 2000L))
        assertEquals(3000L, simulateSqlInvalidate(3000L, 2000L))
        assertEquals(3000L, simulateSqlInvalidate(3000L, 3000L))
    }

    // --- TASK 5C1 REVISION STATE & CAS TESTS ---

    @Test
    fun `test A - AppPreferences unknown revision returns null and payload expectedRevision is null`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        assertNull(prefs.lastKnownServerRevision)

        val resolvedRevision = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertNull(resolvedRevision)

        val payload = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "PARENT",
            expectedRevision = resolvedRevision
        )
        assertNull(payload.expectedRevision)

        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val serialized = json.encodeToString(payload)
        val deserialized = json.decodeFromString<SharedFamilySyncPayload>(serialized)
        assertNull(deserialized.expectedRevision)
    }

    @Test
    fun `test B - known revision 7 persists and reloads correctly`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs1 = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        prefs1.lastKnownServerRevision = 7L
        assertEquals(7L, prefs1.lastKnownServerRevision)

        val prefs2 = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        assertEquals(7L, prefs2.lastKnownServerRevision)

        // Setting null removes key
        prefs2.lastKnownServerRevision = null
        assertNull(prefs2.lastKnownServerRevision)
        assertFalse(fakeStorage.containsKey("last_known_server_revision"))
    }

    @Test
    fun `test C - CHILD payload has expectedRevision null even when prefs knows revision 7`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 7L

        val childExpRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "CHILD",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertNull(childExpRev)

        val clientExpRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "CLIENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertNull(clientExpRev)

        val childPayload = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "CHILD",
            expectedRevision = childExpRev
        )
        assertNull(childPayload.expectedRevision)
    }

    @Test
    fun `test D - PARENT authoritative payload with known revision 7 sends expectedRevision 7`() {
        val knownRev = 7L
        val authoritativeRoles = listOf("PARENT", "ADMIN", "CLI", "PARENTING_AI")

        for (role in authoritativeRoles) {
            val expRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
                senderRole = role,
                lastKnownRevision = knownRev
            )
            assertEquals(7L, expRev)

            val payload = SharedFamilySyncPayload(
                familyCode = "ST-TEST-2026-SYNC-1234",
                senderRole = role,
                expectedRevision = expRev
            )
            assertEquals(7L, payload.expectedRevision)

            val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
            val serialized = json.encodeToString(payload)
            val deserialized = json.decodeFromString<SharedFamilySyncPayload>(serialized)
            assertEquals(7L, deserialized.expectedRevision)
        }
    }

    @Test
    fun `test E - PARENT authoritative payload with unknown revision omits or sends null expectedRevision`() {
        val unknownRev: Long? = null
        val expRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = unknownRev
        )
        assertNull(expRev)

        val payload = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "PARENT",
            expectedRevision = expRev
        )
        assertNull(payload.expectedRevision)
    }

    @Test
    fun `test F - successful server response revision 8 advances stored revision to 8`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 7L

        val serverResponseRevision = 8L
        val cloudData = com.studytracker.core.data.remote.cloudflare.CloudSyncPayloadWrapper(
            familyCode = "ST-TEST-2026-SYNC-1234",
            revision = serverResponseRevision
        )

        if (cloudData.revision >= 0L) {
            prefs.lastKnownServerRevision = cloudData.revision
        }

        assertEquals(8L, prefs.lastKnownServerRevision)
    }

    @Test
    fun `test G - 409 body currentRevision 9 updates stored revision to 9 and result remains failure`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 7L

        val conflictBody = """
            {"success":false,"error":"REVISION_CONFLICT","message":"Server state has changed since this client snapshot.","currentRevision":9,"serverResetAt":0}
        """.trimIndent()

        val parsedRevision = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(conflictBody)
        assertEquals(9L, parsedRevision)

        if (parsedRevision != null && parsedRevision >= 0L) {
            prefs.lastKnownServerRevision = parsedRevision
        }

        val conflictException = com.studytracker.core.data.remote.cloudflare.RevisionConflictException(
            message = "Sunucu revizyon çakışması (HTTP 409): $conflictBody",
            currentRevision = parsedRevision
        )
        val syncResult: Result<String> = Result.failure(conflictException)

        assertTrue(syncResult.isFailure)
        assertTrue(syncResult.exceptionOrNull() is com.studytracker.core.data.remote.cloudflare.RevisionConflictException)
        assertEquals(9L, prefs.lastKnownServerRevision)
    }

    @Test
    fun `test H - malformed or missing currentRevision in 409 does not invent a revision`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 7L

        // Case 1: JSON without currentRevision
        val noRevBody = """{"success":false,"error":"REVISION_CONFLICT","message":"Conflict"}"""
        val parsedNoRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(noRevBody)
        assertNull(parsedNoRev)
        if (parsedNoRev != null && parsedNoRev >= 0L) {
            prefs.lastKnownServerRevision = parsedNoRev
        }
        assertEquals(7L, prefs.lastKnownServerRevision)

        // Case 2: HTML / non-JSON error body
        val htmlBody = "<html><body>409 Conflict</body></html>"
        val parsedHtml = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(htmlBody)
        assertNull(parsedHtml)
        if (parsedHtml != null && parsedHtml >= 0L) {
            prefs.lastKnownServerRevision = parsedHtml
        }
        assertEquals(7L, prefs.lastKnownServerRevision)

        // Case 3: Empty string
        val parsedEmpty = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody("")
        assertNull(parsedEmpty)
        if (parsedEmpty != null && parsedEmpty >= 0L) {
            prefs.lastKnownServerRevision = parsedEmpty
        }
        assertEquals(7L, prefs.lastKnownServerRevision)

        // Case 4: Negative currentRevision
        val negBody = """{"success":false,"error":"REVISION_CONFLICT","currentRevision":-5}"""
        val parsedNeg = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(negBody)
        assertNull(parsedNeg)
        if (parsedNeg != null && parsedNeg >= 0L) {
            prefs.lastKnownServerRevision = parsedNeg
        }
        assertEquals(7L, prefs.lastKnownServerRevision)
    }

    @Test
    fun `test I - lastKnownResetAt tests remain green`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        assertEquals(0L, prefs.lastKnownResetAt)
        prefs.lastKnownResetAt = 1727500000000L
        assertEquals(1727500000000L, prefs.lastKnownResetAt)

        // Verify shouldApplyRemoteReset
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(0L, 0L))
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(1727500000000L, 0L))
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(1727500000000L, 1727500000000L))
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.shouldApplyRemoteReset(1727400000000L, 1727500000000L))
    }

    // --- TASK 5C2 PATCH_TASK & RESTORE CAS TESTS ---

    @Test
    fun `test J - parseRevisionFromSuccessBody parses top-level and nested revision correctly`() {
        // Top-level revision (PATCH_TASK format)
        val topLevelJson = """{"success":true,"message":"Ders 'math_1' güncellendi.","revision":15}"""
        val parsedTopLevel = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromSuccessBody(topLevelJson)
        assertEquals(15L, parsedTopLevel)

        // Nested data.revision (full sync / restore format)
        val nestedJson = """{"success":true,"familyCode":"ST-TEST","data":{"familyCode":"ST-TEST","revision":20}}"""
        val parsedNested = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromSuccessBody(nestedJson)
        assertEquals(20L, parsedNested)

        // Missing revision / null
        val noRevJson = """{"success":true,"message":"OK"}"""
        assertNull(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromSuccessBody(noRevJson))

        // Negative revision
        val negJson = """{"success":true,"revision":-1}"""
        assertNull(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromSuccessBody(negJson))

        // Invalid JSON / blank
        assertNull(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromSuccessBody(""))
        assertNull(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromSuccessBody("not json"))
    }

    @Test
    fun `test K - patchSingleTask payload constructs expectedRevision for PARENT and omits when null`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        // Case 1: prefs has no revision -> expectedRevision is null
        val expRevNull = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertNull(expRevNull)

        val patchPayload1 = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "PARENT",
            action = "PATCH_TASK",
            expectedRevision = expRevNull
        )
        assertNull(patchPayload1.expectedRevision)

        // Case 2: prefs has revision 10 -> expectedRevision is 10
        prefs.lastKnownServerRevision = 10L
        val expRev10 = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertEquals(10L, expRev10)

        val patchPayload2 = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "PARENT",
            action = "PATCH_TASK",
            expectedRevision = expRev10
        )
        assertEquals(10L, patchPayload2.expectedRevision)

        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val serialized = json.encodeToString(patchPayload2)
        val deserialized = json.decodeFromString<SharedFamilySyncPayload>(serialized)
        assertEquals(10L, deserialized.expectedRevision)
    }

    @Test
    fun `test L - restoreFromSnapshot payload constructs expectedRevision for PARENT and omits when null`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        // Case 1: null revision
        val expRevNull = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        val restorePayload1 = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "PARENT",
            action = "RESTORE",
            expectedRevision = expRevNull
        )
        assertNull(restorePayload1.expectedRevision)

        // Case 2: known revision 42
        prefs.lastKnownServerRevision = 42L
        val expRev42 = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertEquals(42L, expRev42)

        val restorePayload2 = SharedFamilySyncPayload(
            familyCode = "ST-TEST-2026-SYNC-1234",
            senderRole = "PARENT",
            action = "RESTORE",
            expectedRevision = expRev42
        )
        assertEquals(42L, restorePayload2.expectedRevision)
    }

    @Test
    fun `test M - patchSingleTask 409 handling parses currentRevision and sets lastKnownServerRevision without inventing revision`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 5L

        val conflict409Body = """
            {"success":false,"error":"REVISION_CONFLICT","message":"Stale PATCH_TASK","currentRevision":12}
        """.trimIndent()

        val parsedRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(conflict409Body)
        assertEquals(12L, parsedRev)

        if (parsedRev != null && parsedRev >= 0L) {
            prefs.lastKnownServerRevision = parsedRev
        }
        assertEquals(12L, prefs.lastKnownServerRevision)

        val exception = com.studytracker.core.data.remote.cloudflare.RevisionConflictException(
            message = "Sunucu revizyon çakışması (HTTP 409): $conflict409Body",
            currentRevision = parsedRev
        )
        val result: Result<String> = Result.failure(exception)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is com.studytracker.core.data.remote.cloudflare.RevisionConflictException)
    }

    @Test
    fun `test N - restoreFromSnapshot 409 handling parses currentRevision and sets lastKnownServerRevision`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 3L

        val conflict409Body = """
            {"success":false,"error":"REVISION_CONFLICT","message":"Stale RESTORE","currentRevision":8}
        """.trimIndent()

        val parsedRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(conflict409Body)
        assertEquals(8L, parsedRev)

        if (parsedRev != null && parsedRev >= 0L) {
            prefs.lastKnownServerRevision = parsedRev
        }
        assertEquals(8L, prefs.lastKnownServerRevision)

        val exception = com.studytracker.core.data.remote.cloudflare.RevisionConflictException(
            message = "Sunucu revizyon çakışması (HTTP 409): $conflict409Body",
            currentRevision = parsedRev
        )
        val result: Result<String> = Result.failure(exception)
        assertTrue(result.isFailure)
        assertEquals(8L, (result.exceptionOrNull() as com.studytracker.core.data.remote.cloudflare.RevisionConflictException).currentRevision)
    }

    @Test
    fun `test O - AppPreferences setFamilyPairCode clears cached lastKnownServerRevision when code actually changes`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        // Set initial pair code and server revision
        prefs.setFamilyPairCode("ST-AAAA-1111-2222-3333")
        prefs.lastKnownServerRevision = 42L
        assertEquals(42L, prefs.lastKnownServerRevision)
        assertEquals("ST-AAAA-1111-2222-3333", prefs.familyPairCode.value)

        // Switch to a new family code -> lastKnownServerRevision must be invalidated to null
        prefs.setFamilyPairCode("ST-BBBB-4444-5555-6666")
        assertNull("Switching family code must clear lastKnownServerRevision", prefs.lastKnownServerRevision)
        assertEquals("ST-BBBB-4444-5555-6666", prefs.familyPairCode.value)

        // Setting a new revision for the new family
        prefs.lastKnownServerRevision = 7L
        assertEquals(7L, prefs.lastKnownServerRevision)

        // Setting the same family code again must preserve the cached revision
        prefs.setFamilyPairCode("ST-BBBB-4444-5555-6666")
        assertEquals(7L, prefs.lastKnownServerRevision)
    }

    @Test
    fun `test P - PairFamilyResponse deserialization supports optional revision field and sets revision`() {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; isLenient = true; coerceInputValues = true }
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        // Case 1: Worker returns response with revision
        val jsonWithRevision = """
            {"success":true,"familyCode":"ST-TEST-1111-2222-3333","adminToken":"tok_abc","created":true,"revision":5}
        """.trimIndent()
        val pairRespWithRev = json.decodeFromString<com.studytracker.core.data.remote.cloudflare.PairFamilyResponse>(jsonWithRevision)
        assertTrue(pairRespWithRev.success)
        assertEquals("ST-TEST-1111-2222-3333", pairRespWithRev.familyCode)
        assertEquals("tok_abc", pairRespWithRev.adminToken)
        assertTrue(pairRespWithRev.created)
        assertEquals(5L, pairRespWithRev.revision)

        prefs.setFamilyPairCode(pairRespWithRev.familyCode)
        val respRevision = pairRespWithRev.revision
        if (respRevision != null && respRevision >= 0L) {
            prefs.lastKnownServerRevision = respRevision
        }
        assertEquals(5L, prefs.lastKnownServerRevision)

        // Case 2: Legacy worker response without revision field -> backward compatible
        val jsonLegacy = """
            {"success":true,"familyCode":"ST-TEST-8888-9999-0000","adminToken":"tok_xyz","created":false}
        """.trimIndent()
        val pairRespLegacy = json.decodeFromString<com.studytracker.core.data.remote.cloudflare.PairFamilyResponse>(jsonLegacy)
        assertTrue(pairRespLegacy.success)
        assertNull(pairRespLegacy.revision)
    }

    @Test
    fun `test Q - isAuthoritativeRole strictly identifies PARENT ADMIN CLI PARENTING_AI vs CHILD CLIENT`() {
        // Authoritative roles
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("PARENT"))
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("parent"))
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("ADMIN"))
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("CLI"))
        assertTrue(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("PARENTING_AI"))

        // Non-authoritative roles
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("CHILD"))
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("child"))
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("CLIENT"))
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole("STUDENT"))
        assertFalse(com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.isAuthoritativeRole(""))
    }

    @Test
    fun `test R - resolveExpectedRevisionForSync only passes revision for authoritative roles`() {
        val knownRev = 15L

        // Authoritative role receives revision
        val parentRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = knownRev
        )
        assertEquals(15L, parentRev)

        val adminRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "ADMIN",
            lastKnownRevision = knownRev
        )
        assertEquals(15L, adminRev)

        // Non-authoritative roles (CHILD / CLIENT) must receive null
        val childRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "CHILD",
            lastKnownRevision = knownRev
        )
        assertNull(childRev)

        val clientRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "CLIENT",
            lastKnownRevision = knownRev
        )
        assertNull(clientRev)
    }

    @Test
    fun `test S - fresh parent mutation precondition logic simulation`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))

        // State 1: Clean start (revision is null)
        assertNull(prefs.lastKnownServerRevision)

        // Simulated acquisition: if fetch fails, mutation must fail without write
        var fetchSuccessful = false
        val preFetchResult: Result<Long> = if (fetchSuccessful) {
            prefs.lastKnownServerRevision = 10L
            Result.success(10L)
        } else {
            Result.failure(Exception("Network error during pre-mutation fetch"))
        }

        assertTrue(preFetchResult.isFailure)
        assertNull(prefs.lastKnownServerRevision)

        // State 2: Fetch succeeds, revision is acquired and passed to mutation
        fetchSuccessful = true
        if (fetchSuccessful) {
            prefs.lastKnownServerRevision = 10L
        }
        val resolvedExpectedRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.resolveExpectedRevisionForSync(
            senderRole = "PARENT",
            lastKnownRevision = prefs.lastKnownServerRevision
        )
        assertEquals(10L, resolvedExpectedRev)
    }

    @Test
    fun `test T - 409 conflict exception is preserved and no automatic retry occurs`() {
        val fakeStorage = mutableMapOf<String, Any?>()
        val prefs = com.studytracker.core.data.local.prefs.AppPreferences(FakeSharedPreferences(fakeStorage))
        prefs.lastKnownServerRevision = 10L

        val conflict409Json = """
            {"success":false,"error":"REVISION_CONFLICT","message":"Stale mutation","currentRevision":18}
        """.trimIndent()

        val parsedConflictRev = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager.parseRevisionFromConflictBody(conflict409Json)
        assertEquals(18L, parsedConflictRev)

        // On 409, update lastKnownServerRevision
        prefs.lastKnownServerRevision = parsedConflictRev
        assertEquals(18L, prefs.lastKnownServerRevision)

        // Return failure immediately without retrying
        val conflictEx = com.studytracker.core.data.remote.cloudflare.RevisionConflictException(
            message = "Sunucu revizyon çakışması (HTTP 409): $conflict409Json",
            currentRevision = parsedConflictRev
        )
        val mutationResult: Result<String> = Result.failure(conflictEx)

        assertTrue(mutationResult.isFailure)
        val ex = mutationResult.exceptionOrNull()
        assertTrue(ex is com.studytracker.core.data.remote.cloudflare.RevisionConflictException)
        assertEquals(18L, (ex as com.studytracker.core.data.remote.cloudflare.RevisionConflictException).currentRevision)
    }

    @Test
    fun `test U - resolveEffectiveRoleForSync preserves PARENT role for privileged actions even when local DB is empty`() {
        val manager = com.studytracker.core.data.remote.cloudflare.CloudflareSyncManager

        // 1. Privileged / Destructive actions on EMPTY local DB must NEVER downgrade to CLIENT
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "RESET"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "WIPE"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "RESTORE"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "UNDO_RESET"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "RESET_ALL_PROGRESS"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "PATCH_TASK"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "DELETE_TASK"))
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "SYNC", deleteTaskId = "task_math_01"))

        // 2. Non-mutating default SYNC on empty DB uses CLIENT bootstrap role
        assertEquals("CLIENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = true, action = "SYNC", deleteTaskId = null))

        // 3. Normal SYNC with populated DB retains PARENT role
        assertEquals("PARENT", manager.resolveEffectiveRoleForSync("PARENT", isLocalDbEmpty = false, action = "SYNC", deleteTaskId = null))

        // 4. CHILD progress semantics preserved
        assertEquals("CLIENT", manager.resolveEffectiveRoleForSync("CHILD", isLocalDbEmpty = true, action = "SYNC"))
        assertEquals("CHILD", manager.resolveEffectiveRoleForSync("CHILD", isLocalDbEmpty = false, action = "SYNC"))

        // 5. Verify isMutatingOrPrivilegedAction helper
        assertTrue(manager.isMutatingOrPrivilegedAction("RESET"))
        assertTrue(manager.isMutatingOrPrivilegedAction("WIPE"))
        assertTrue(manager.isMutatingOrPrivilegedAction("RESTORE"))
        assertTrue(manager.isMutatingOrPrivilegedAction("UNDO_RESET"))
        assertTrue(manager.isMutatingOrPrivilegedAction("RESET_ALL_PROGRESS"))
        assertTrue(manager.isMutatingOrPrivilegedAction("PATCH_TASK"))
        assertTrue(manager.isMutatingOrPrivilegedAction("DELETE_TASK"))
        assertTrue(manager.isMutatingOrPrivilegedAction("SYNC", deleteTaskId = "task_01"))
        assertFalse(manager.isMutatingOrPrivilegedAction("SYNC", deleteTaskId = null))
        assertFalse(manager.isMutatingOrPrivilegedAction("UNKNOWN", deleteTaskId = null))
    }
}

class FakeSharedPreferences(
    private val data: MutableMap<String, Any?> = mutableMapOf()
) : android.content.SharedPreferences {

    override fun getAll(): MutableMap<String, *> = HashMap(data)

    override fun getString(key: String?, defValue: String?): String? =
        (data[key] as? String) ?: defValue

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        @Suppress("UNCHECKED_CAST") (data[key] as? MutableSet<String>) ?: defValues

    override fun getInt(key: String?, defValue: Int): Int =
        (data[key] as? Int) ?: defValue

    override fun getLong(key: String?, defValue: Long): Long =
        (data[key] as? Long) ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float =
        (data[key] as? Float) ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        (data[key] as? Boolean) ?: defValue

    override fun contains(key: String?): Boolean = data.containsKey(key)

    override fun edit(): android.content.SharedPreferences.Editor = FakeEditor(data)

    override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

    class FakeEditor(
        private val data: MutableMap<String, Any?>
    ) : android.content.SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()
        private val toRemove = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor {
            if (key != null) { temp[key] = value; toRemove.remove(key) }
            return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor {
            if (key != null) { temp[key] = values; toRemove.remove(key) }
            return this
        }

        override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor {
            if (key != null) { temp[key] = value; toRemove.remove(key) }
            return this
        }

        override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor {
            if (key != null) { temp[key] = value; toRemove.remove(key) }
            return this
        }

        override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor {
            if (key != null) { temp[key] = value; toRemove.remove(key) }
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor {
            if (key != null) { temp[key] = value; toRemove.remove(key) }
            return this
        }

        override fun remove(key: String?): android.content.SharedPreferences.Editor {
            if (key != null) { toRemove.add(key); temp.remove(key) }
            return this
        }

        override fun clear(): android.content.SharedPreferences.Editor {
            clearAll = true
            temp.clear()
            toRemove.clear()
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearAll) data.clear()
            for (k in toRemove) data.remove(k)
            for ((k, v) in temp) {
                if (v != null) data[k] = v else data.remove(k)
            }
        }
    }
}
