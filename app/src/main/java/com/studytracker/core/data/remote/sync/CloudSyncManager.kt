package com.studytracker.core.data.remote.sync

import android.content.Context
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.OccurrenceEntity
import com.studytracker.core.data.local.db.entity.ReviewEntity
import com.studytracker.core.data.local.db.entity.ScreenshotEntity
import com.studytracker.core.data.local.db.entity.SessionEntity
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.remote.supabase.*
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.ReviewStatus
import com.studytracker.core.domain.model.SessionStatus
import com.studytracker.core.domain.model.UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String, val timestamp: Long = System.currentTimeMillis()) : SyncState()
    data class Error(val errorMessage: String, val timestamp: Long = System.currentTimeMillis()) : SyncState()
}

class CloudSyncManager private constructor(private val context: Context) {

    private val prefs = AppPreferences.getInstance(context)
    private val supabaseClient = SupabaseHttpClient.getInstance(context)
    private val db = AppDatabase.getInstance(context)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow<Long>(0L)
    val lastSyncedTime: StateFlow<Long> = _lastSyncedTime.asStateFlow()

    fun getOrCreateFamilyCode(): String {
        var code = prefs.familyPairCode.value.trim().uppercase()
        if (code.isEmpty()) {
            val randomNum = Random.nextInt(1000, 9999)
            code = "ST-$randomNum"
            prefs.setFamilyPairCode(code)
        }
        return code
    }

    fun joinFamily(code: String) {
        val formatted = code.trim().uppercase()
        prefs.setFamilyPairCode(formatted)
    }

    suspend fun syncAll(): Result<String> = withContext(Dispatchers.IO) {
        if (!prefs.isCloudSyncEnabled.value) {
            return@withContext Result.success("Bulut senkronizasyonu devre dışı")
        }

        _syncState.value = SyncState.Syncing
        val familyCode = getOrCreateFamilyCode()

        try {
            // 1. Push Occurrences Up
            val occurrences = db.occurrenceDao().getAllOccurrencesOnce()
            for (occ in occurrences) {
                val dto = RemoteOccurrenceSyncDto(
                    id = occ.occurrenceKey,
                    familyCode = familyCode,
                    date = occ.date ?: "",
                    planId = occ.taskId,
                    subject = occ.title,
                    topic = occ.type.name,
                    targetDurationMin = occ.plannedMinutes,
                    targetQuestionCount = occ.targetCount ?: 0,
                    completedDurationMin = occ.targetMinutes ?: 0,
                    completedQuestionCount = occ.approvedCount,
                    status = occ.status.name,
                    parentNote = occ.warningText ?: "",
                    weekId = occ.weekId ?: "",
                    orderIndex = 0
                )
                supabaseClient.post("occurrences", dto) { json.encodeToString(it) }
            }

            // 2. Push Sessions Up
            val sessions = db.sessionDao().getAllSessionsOnce()
            for (sess in sessions) {
                val dto = RemoteSessionSyncDto(
                    id = sess.sessionId,
                    familyCode = familyCode,
                    occurrenceId = sess.occurrenceKey,
                    startTime = sess.startTime,
                    endTime = sess.endTime,
                    durationMin = if (sess.endTime != null) ((sess.endTime - sess.startTime) / 60000).toInt() else 0,
                    isCompleted = sess.status == SessionStatus.COMPLETED
                )
                supabaseClient.post("sessions", dto) { json.encodeToString(it) }
            }

            // 3. Pull Remote Reviews from Supabase (Veli Onayları)
            val reviewsResponse = supabaseClient.get("reviews", "family_code=eq.$familyCode")
            if (reviewsResponse.isSuccess) {
                val responseText = reviewsResponse.getOrNull() ?: ""
                if (responseText.isNotEmpty() && responseText != "[]") {
                    try {
                        val remoteReviews: List<RemoteReviewSyncDto> = json.decodeFromString(responseText)
                        for (remoteReview in remoteReviews) {
                            val reviewEntity = ReviewEntity(
                                sessionId = remoteReview.sessionId,
                                occurrenceKey = remoteReview.occurrenceIdOrKey(db),
                                reviewStatus = if (remoteReview.isApproved) ReviewStatus.APPROVED else ReviewStatus.REJECTED,
                                reviewNote = remoteReview.feedbackNote,
                                reviewedAt = remoteReview.reviewedAt
                            )
                            db.reviewDao().insertReview(reviewEntity)

                            // Update corresponding occurrence status
                            if (remoteReview.isApproved) {
                                db.occurrenceDao().updateStatus(reviewEntity.occurrenceKey, OccurrenceStatus.APPROVED)
                            } else {
                                db.occurrenceDao().updateStatus(reviewEntity.occurrenceKey, OccurrenceStatus.REJECTED)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("CloudSyncManager", "Error parsing remote reviews: ${e.message}")
                    }
                }
            }

            _lastSyncedTime.value = System.currentTimeMillis()
            _syncState.value = SyncState.Success("Senkronizasyon Başarılı (${occurrences.size} görev, ${sessions.size} oturum)")
            Result.success("Senkronizasyon tamamlandı")
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Senkronizasyon hatası: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun pushSessionFinished(session: SessionEntity, screenshots: List<ScreenshotEntity>): Result<Unit> =
        withContext(Dispatchers.IO) {
            val familyCode = getOrCreateFamilyCode()
            try {
                val sessionDto = RemoteSessionSyncDto(
                    id = session.sessionId,
                    familyCode = familyCode,
                    occurrenceId = session.occurrenceKey,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    durationMin = if (session.endTime != null) ((session.endTime - session.startTime) / 60000).toInt() else 0,
                    isCompleted = true
                )
                supabaseClient.post("sessions", sessionDto) { json.encodeToString(it) }

                // Upload screenshot files
                for (sc in screenshots) {
                    val file = File(sc.url)
                    val uploadResultUrl = if (file.exists()) {
                        supabaseClient.uploadStorageFile("study-evidence", "${familyCode}/${sc.screenshotId}.jpg", file).getOrDefault(sc.url)
                    } else {
                        sc.url
                    }

                    val scDto = RemoteScreenshotSyncDto(
                        id = sc.screenshotId,
                        familyCode = familyCode,
                        sessionId = sc.sessionId,
                        imageUrl = uploadResultUrl,
                        timestamp = sc.capturedAt
                    )
                    supabaseClient.post("screenshots", scDto) { json.encodeToString(it) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun pushReviewDecision(
        sessionId: String,
        occurrenceKey: String,
        isApproved: Boolean,
        feedbackNote: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val familyCode = getOrCreateFamilyCode()
        try {
            val reviewDto = RemoteReviewSyncDto(
                id = "rev_${sessionId}",
                familyCode = familyCode,
                sessionId = sessionId,
                isApproved = isApproved,
                rejectionReason = if (!isApproved) feedbackNote else null,
                parentRating = if (isApproved) 5 else 1,
                feedbackNote = feedbackNote,
                reviewedAt = System.currentTimeMillis()
            )
            supabaseClient.post("reviews", reviewDto) { json.encodeToString(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun RemoteReviewSyncDto.occurrenceIdOrKey(db: AppDatabase): String {
        val session = db.sessionDao().getSessionById(this.sessionId)
        return session?.occurrenceKey ?: this.sessionId
    }

    companion object {
        @Volatile
        private var INSTANCE: CloudSyncManager? = null

        fun getInstance(context: Context): CloudSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = CloudSyncManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
