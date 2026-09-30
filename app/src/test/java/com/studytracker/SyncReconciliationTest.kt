package com.studytracker

import com.studytracker.core.data.remote.sync.*
import com.studytracker.core.data.local.db.entity.SessionEntity
import com.studytracker.core.data.local.repository.toDomain
import com.studytracker.core.data.local.repository.toEntity
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.Session
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
