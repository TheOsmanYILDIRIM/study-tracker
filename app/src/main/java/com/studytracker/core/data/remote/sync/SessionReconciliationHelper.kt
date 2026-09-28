package com.studytracker.core.data.remote.sync

import com.studytracker.core.domain.model.SessionStatus

object SessionReconciliationHelper {

    /**
     * Resolves the legacy incoming session timestamp using the deterministic fallback order:
     * 1. [updatedAt] if > 0L
     * 2. [endTime] if > 0L
     * 3. [startTime] if > 0L
     * 4. 0L
     *
     * Note: Local existing session timestamps must NEVER be used as a fallback here,
     * as doing so would artificially elevate a stale legacy packet to the local freshness level.
     */
    fun resolveIncomingSessionTimestamp(
        updatedAt: Long,
        endTime: Long?,
        startTime: Long
    ): Long {
        return when {
            updatedAt > 0L -> updatedAt
            (endTime ?: 0L) > 0L -> endTime!!
            startTime > 0L -> startTime
            else -> 0L
        }
    }

    /**
     * Maps [SessionStatus] to a binary completion state.
     * [SessionStatus.ACTIVE] represents an ongoing (incomplete) session.
     * All other statuses ([SessionStatus.WAITING_REVIEW], [SessionStatus.APPROVED],
     * [SessionStatus.REJECTED], [SessionStatus.INVALID]) represent finished/finalized states.
     */
    fun isSessionStatusCompleted(status: SessionStatus?): Boolean {
        return when (status) {
            null, SessionStatus.ACTIVE -> false
            SessionStatus.WAITING_REVIEW,
            SessionStatus.APPROVED,
            SessionStatus.REJECTED,
            SessionStatus.INVALID -> true
        }
    }

    /**
     * Pure winner decision function matching the Cloudflare Worker Görev 4C1 semantics:
     *
     * Rules:
     * - incomingUpdatedAt > existingUpdatedAt => true (Newer incoming wins)
     * - incomingUpdatedAt < existingUpdatedAt => false (Stale incoming rejected)
     * - incomingUpdatedAt == existingUpdatedAt:
     *     - !existingCompleted && incomingCompleted => true (Completion advancement on equal timestamp)
     *     - existingCompleted && !incomingCompleted => false (Completion regression rejected)
     *     - existingCompleted == incomingCompleted => false (Same completion state rejected to avoid redundant writes)
     */
    fun shouldApplyIncomingSessionVersion(
        existingUpdatedAt: Long,
        incomingUpdatedAt: Long,
        existingCompleted: Boolean,
        incomingCompleted: Boolean
    ): Boolean {
        if (incomingUpdatedAt > existingUpdatedAt) return true
        if (incomingUpdatedAt < existingUpdatedAt) return false

        // Timestamps are equal (tie-breaker on completion advancement)
        if (!existingCompleted && incomingCompleted) return true
        return false
    }
}
