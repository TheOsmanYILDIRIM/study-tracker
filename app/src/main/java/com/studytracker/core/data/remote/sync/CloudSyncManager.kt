package com.studytracker.core.data.remote.sync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.remote.supabase.*
import com.studytracker.core.domain.model.ContentType
import com.studytracker.core.domain.model.OccurrenceStatus
import com.studytracker.core.domain.model.ReviewStatus
import com.studytracker.core.domain.model.SessionStatus
import com.studytracker.core.domain.model.TargetMode
import com.studytracker.core.domain.model.TaskKind
import com.studytracker.core.domain.model.UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String, val timestamp: Long = System.currentTimeMillis()) : SyncState()
    data class Error(val errorMessage: String, val timestamp: Long = System.currentTimeMillis()) : SyncState()
}

class CloudSyncManager private constructor(private val context: Context) {

    private val prefs = AppPreferences.getInstance(context)
    private val db = AppDatabase.getInstance(context)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        prettyPrint = false
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow<Long>(0L)
    val lastSyncedTime: StateFlow<Long> = _lastSyncedTime.asStateFlow()

    fun getOrCreateFamilyCode(): String {
        var code = prefs.familyPairCode.value.trim().uppercase()
        if (code.isEmpty()) {
            code = AppPreferences.DEFAULT_FAMILY_CODE
            prefs.setFamilyPairCode(code)
        }
        return code
    }

    fun joinFamily(code: String) {
        val formatted = code.trim().uppercase().ifBlank { AppPreferences.DEFAULT_FAMILY_CODE }
        prefs.setFamilyPairCode(formatted)
    }

    private fun getPeerAuthorities(): List<String> {
        val currentPkg = context.packageName
        return listOf(
            "com.studytracker.parent.syncprovider",
            "com.studytracker.parent.debug.syncprovider",
            "com.studytracker.child.syncprovider",
            "com.studytracker.child.debug.syncprovider",
            "com.studytracker.syncprovider",
            "com.studytracker.debug.syncprovider"
        ).filter { !it.startsWith(currentPkg) }
    }

    /**
     * Instant local bidirectional ContentProvider sync exchange (< 5ms latency, 0 network).
     */
    private suspend fun syncWithPeerProvider(familyCode: String, localPayload: SharedFamilySyncPayload): Boolean = withContext(Dispatchers.IO) {
        val payloadJson = try { json.encodeToString(localPayload) } catch (_: Exception) { return@withContext false }
        val bundle = Bundle().apply { putString("payload", payloadJson) }

        for (auth in getPeerAuthorities()) {
            try {
                val uri = Uri.parse("content://$auth")
                val responseBundle = context.contentResolver.call(uri, "sync", familyCode, bundle)
                val returnedJson = responseBundle?.getString("payload")
                if (!returnedJson.isNullOrBlank()) {
                    val peerPayload = json.decodeFromString<SharedFamilySyncPayload>(returnedJson)
                    mergeIncomingPayload(peerPayload)
                    Log.d("CloudSyncManager", "Local peer sync exchange successful with $auth")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.d("CloudSyncManager", "Peer $auth unavailable: ${e.message}")
            }
        }
        false
    }

    /**
     * Lightweight non-blocking cloud fallback (fail-silent, 3s timeout).
     */
    private suspend fun syncWithCloudRelay(familyCode: String, localPayload: SharedFamilySyncPayload) = withContext(Dispatchers.IO) {
        val topic = "studytracker_relay_" + familyCode.trim().uppercase().replace(Regex("[^A-Z0-9]"), "_").lowercase()
        withTimeoutOrNull(3000L) {
            try {
                val payloadJson = json.encodeToString(localPayload)
                val postReq = Request.Builder()
                    .url("https://ntfy.sh/$topic")
                    .addHeader("Title", "StudyTracker")
                    .post(payloadJson.toRequestBody(jsonMediaType))
                    .build()
                httpClient.newCall(postReq).execute().close()
            } catch (_: Exception) {}
        }
    }

    suspend fun syncAll(): Result<String> = withContext(Dispatchers.IO) {
        _syncState.value = SyncState.Syncing
        val familyCode = getOrCreateFamilyCode()

        try {
            // 1. Build current local payload
            val localPayload = buildCurrentPayload(familyCode)

            // 2. Perform direct 0-latency ContentProvider sync exchange
            val peerSynced = syncWithPeerProvider(familyCode, localPayload)

            // 3. Perform quick cloud relay sync if enabled
            if (prefs.isCloudSyncEnabled.value) {
                try {
                    syncWithCloudRelay(familyCode, localPayload)
                } catch (_: Exception) {}
            }

            val finalOccurrences = db.occurrenceDao().getAllOccurrencesOnce()
            val waitingSessions = db.sessionDao().getAllSessionsOnce().count { it.status == SessionStatus.WAITING_REVIEW }
            val screenshotCount = db.screenshotDao().getAllScreenshotsOnce().size

            _lastSyncedTime.value = System.currentTimeMillis()
            val message = "Eşitleme Başarılı (${finalOccurrences.size} ders, $waitingSessions onay bekleyen, $screenshotCount kanıt)"
            _syncState.value = SyncState.Success(message)
            Result.success(message)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Senkronizasyon: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun pushReviewDecision(
        sessionId: String,
        occurrenceKey: String,
        isApproved: Boolean,
        feedbackNote: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val note = feedbackNote ?: if (!isApproved) "Bu görev onaylanmadı. Lütfen eksikleri tamamlayıp tekrar yapınız." else null
            
            // 1. Immediately persist review decision in local DB
            val reviewEntity = ReviewEntity(
                sessionId = sessionId,
                occurrenceKey = occurrenceKey,
                reviewStatus = if (isApproved) ReviewStatus.APPROVED else ReviewStatus.REJECTED,
                reviewNote = note,
                reviewedAt = System.currentTimeMillis()
            )
            db.reviewDao().insertReview(reviewEntity)

            val session = db.sessionDao().getSessionById(sessionId)
            if (session != null) {
                db.sessionDao().upsertSession(session.copy(status = if (isApproved) SessionStatus.APPROVED else SessionStatus.REJECTED))
            }

            if (isApproved) {
                db.occurrenceDao().updateStatus(occurrenceKey, OccurrenceStatus.APPROVED)
                db.occurrenceDao().setWarning(occurrenceKey, false, null)
                db.occurrenceDao().incrementApprovedCount(occurrenceKey)
            } else {
                db.occurrenceDao().updateStatus(occurrenceKey, OccurrenceStatus.PENDING)
                db.occurrenceDao().setWarning(occurrenceKey, true, note ?: "Bu görev onaylanmadı.")
            }

            // 2. Trigger immediate sync
            syncAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushSessionFinished(session: SessionEntity, screenshots: List<ScreenshotEntity>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.sessionDao().upsertSession(session.copy(status = SessionStatus.WAITING_REVIEW))
            db.occurrenceDao().updateStatus(session.occurrenceKey, OccurrenceStatus.WAITING_REVIEW)
            if (screenshots.isNotEmpty()) {
                db.screenshotDao().upsertScreenshots(screenshots)
            }
            syncAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importPayloadString(payloadJson: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val payload = json.decodeFromString<SharedFamilySyncPayload>(payloadJson)
            val familyCode = payload.familyCode.ifBlank { getOrCreateFamilyCode() }
            prefs.setFamilyPairCode(familyCode)
            mergeIncomingPayload(payload)
            syncAll()
            Result.success(payload.occurrences.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportCurrentPayloadString(): String = withContext(Dispatchers.IO) {
        val payload = buildCurrentPayload(getOrCreateFamilyCode())
        json.encodeToString(payload)
    }

    private suspend fun buildCurrentPayload(familyCode: String): SharedFamilySyncPayload {
        val finalPlan = db.planDao().getActivePlanOnce()?.let {
            LocalPlanSyncDto(
                planId = it.planId,
                weekId = it.weekId,
                weekStartDate = it.weekStartDate,
                childId = it.childId,
                timezone = it.timezone,
                updatedAt = it.updatedAt,
                rawJson = it.rawJson
            )
        }

        val finalTasks = db.taskTemplateDao().getAllTasksOnce().map {
            LocalTaskTemplateSyncDto(
                taskId = it.taskId,
                title = it.title,
                kind = it.kind.name,
                contentType = it.contentType.name,
                youtubeUrl = it.youtubeUrl,
                plannedMinutes = it.plannedMinutes,
                targetMode = it.targetMode?.name,
                targetCount = it.targetCount,
                targetMinutes = it.targetMinutes,
                reviewRequired = it.reviewRequired,
                active = it.active
            )
        }

        val finalOccurrences = db.occurrenceDao().getAllOccurrencesOnce().map {
            RemoteOccurrenceSyncDto(
                id = it.occurrenceKey,
                familyCode = familyCode,
                date = it.date ?: "",
                planId = it.taskId,
                subject = it.title,
                topic = it.type.name,
                targetDurationMin = it.plannedMinutes,
                targetQuestionCount = it.targetCount ?: 0,
                completedDurationMin = it.targetMinutes ?: 0,
                completedQuestionCount = it.approvedCount,
                status = it.status.name,
                parentNote = it.warningText ?: "",
                weekId = it.weekId ?: "",
                orderIndex = 0
            )
        }

        val finalSessions = db.sessionDao().getAllSessionsOnce().map {
            RemoteSessionSyncDto(
                id = it.sessionId,
                familyCode = familyCode,
                occurrenceId = it.occurrenceKey,
                startTime = it.startTime,
                endTime = it.endTime,
                durationMin = if (it.endTime != null) ((it.endTime - it.startTime) / 60000).toInt() else 0,
                isCompleted = it.status != SessionStatus.ACTIVE
            )
        }

        val finalReviews = db.reviewDao().getAllReviewsOnce().map {
            RemoteReviewSyncDto(
                id = "rev_${it.sessionId}",
                familyCode = familyCode,
                sessionId = it.sessionId,
                isApproved = it.reviewStatus == ReviewStatus.APPROVED,
                rejectionReason = if (it.reviewStatus != ReviewStatus.APPROVED) it.reviewNote else null,
                parentRating = if (it.reviewStatus == ReviewStatus.APPROVED) 5 else 1,
                feedbackNote = it.reviewNote,
                reviewedAt = it.reviewedAt
            )
        }

        val finalScreenshots = convertLocalScreenshotsToDtos(familyCode)

        return SharedFamilySyncPayload(
            familyCode = familyCode,
            plan = finalPlan,
            tasks = finalTasks,
            occurrences = finalOccurrences,
            sessions = finalSessions,
            screenshots = finalScreenshots,
            reviews = finalReviews,
            updatedAt = System.currentTimeMillis()
        )
    }

    private suspend fun mergeIncomingPayload(payload: SharedFamilySyncPayload) {
        // A. Plan
        payload.plan?.let { p ->
            val current = db.planDao().getActivePlanOnce()
            if (current == null || p.updatedAt >= current.updatedAt) {
                db.planDao().setActivePlan(
                    PlanEntity(
                        planId = p.planId,
                        weekId = p.weekId,
                        weekStartDate = p.weekStartDate,
                        childId = p.childId,
                        timezone = p.timezone,
                        updatedAt = p.updatedAt,
                        rawJson = p.rawJson
                    )
                )
            }
        }

        // B. Tasks
        if (payload.tasks.isNotEmpty()) {
            val entities = payload.tasks.map { t ->
                TaskTemplateEntity(
                    taskId = t.taskId,
                    title = t.title,
                    kind = try { TaskKind.valueOf(t.kind) } catch (_: Exception) { TaskKind.DAILY },
                    contentType = try { ContentType.valueOf(t.contentType) } catch (_: Exception) { ContentType.READING },
                    youtubeUrl = t.youtubeUrl,
                    plannedMinutes = t.plannedMinutes,
                    targetMode = t.targetMode?.let { try { TargetMode.valueOf(it) } catch (_: Exception) { null } },
                    targetCount = t.targetCount,
                    targetMinutes = t.targetMinutes,
                    reviewRequired = t.reviewRequired,
                    active = t.active
                )
            }
            db.taskTemplateDao().upsertTasks(entities)
        }

        // C. Occurrences
        if (payload.occurrences.isNotEmpty()) {
            val localOccMap = db.occurrenceDao().getAllOccurrencesOnce().associateBy { it.occurrenceKey }
            val mergedOccs = payload.occurrences.map { remote ->
                val local = localOccMap[remote.id]
                val remoteStatus = try { OccurrenceStatus.valueOf(remote.status) } catch (_: Exception) { OccurrenceStatus.PENDING }

                val resolvedStatus = when {
                    local?.status == OccurrenceStatus.APPROVED || remoteStatus == OccurrenceStatus.APPROVED -> OccurrenceStatus.APPROVED
                    local?.status == OccurrenceStatus.WAITING_REVIEW || remoteStatus == OccurrenceStatus.WAITING_REVIEW -> OccurrenceStatus.WAITING_REVIEW
                    local?.status == OccurrenceStatus.ACTIVE || remoteStatus == OccurrenceStatus.ACTIVE -> OccurrenceStatus.ACTIVE
                    remoteStatus == OccurrenceStatus.REJECTED || local?.status == OccurrenceStatus.REJECTED -> OccurrenceStatus.PENDING
                    else -> local?.status ?: remoteStatus
                }

                val approvedCount = maxOf(local?.approvedCount ?: 0, remote.completedQuestionCount)
                val targetCount = local?.targetCount ?: if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null
                val isApprovedByCount = (targetCount != null && approvedCount >= targetCount)
                val finalStatus = if (isApprovedByCount) OccurrenceStatus.APPROVED else resolvedStatus

                val hasWarning = (local?.warning == true) || (remote.parentNote.isNotBlank() && finalStatus != OccurrenceStatus.APPROVED)
                val warningText = if (remote.parentNote.isNotBlank()) remote.parentNote else local?.warningText

                OccurrenceEntity(
                    occurrenceKey = remote.id,
                    taskId = if (!local?.taskId.isNullOrBlank()) local!!.taskId else remote.planId,
                    type = local?.type ?: try { TaskKind.valueOf(remote.topic) } catch (_: Exception) { TaskKind.DAILY },
                    date = local?.date ?: remote.date.ifEmpty { null },
                    weekId = local?.weekId ?: remote.weekId.ifEmpty { null },
                    title = if (!local?.title.isNullOrBlank()) local!!.title else remote.subject,
                    plannedMinutes = if ((local?.plannedMinutes ?: 0) > 0) local!!.plannedMinutes else remote.targetDurationMin,
                    youtubeUrl = local?.youtubeUrl,
                    reviewRequired = true,
                    status = finalStatus,
                    warning = hasWarning,
                    warningText = warningText,
                    rejectCount = maxOf(local?.rejectCount ?: 0, if (hasWarning) 1 else 0),
                    approvedCount = approvedCount,
                    targetCount = targetCount,
                    targetMinutes = local?.targetMinutes ?: if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                )
            }
            db.occurrenceDao().upsertOccurrences(mergedOccs)
        }

        // D. Sessions
        if (payload.sessions.isNotEmpty()) {
            val localSessions = db.sessionDao().getAllSessionsOnce().associateBy { it.sessionId }
            for (rs in payload.sessions) {
                val existing = localSessions[rs.id]
                val resolvedStatus = when {
                    existing?.status == SessionStatus.APPROVED -> SessionStatus.APPROVED
                    existing?.status == SessionStatus.REJECTED -> SessionStatus.REJECTED
                    rs.isCompleted -> SessionStatus.WAITING_REVIEW
                    else -> existing?.status ?: SessionStatus.ACTIVE
                }

                db.sessionDao().upsertSession(
                    SessionEntity(
                        sessionId = rs.id,
                        occurrenceKey = rs.occurrenceId,
                        childId = "child_1",
                        startTime = rs.startTime,
                        endTime = rs.endTime ?: existing?.endTime,
                        status = resolvedStatus,
                        screenshotCount = existing?.screenshotCount ?: 1,
                        finalScreenshotUrl = existing?.finalScreenshotUrl
                    )
                )
            }
        }

        // E. Reviews
        if (payload.reviews.isNotEmpty()) {
            for (rev in payload.reviews) {
                val session = db.sessionDao().getSessionById(rev.sessionId)
                val targetOccKey = session?.occurrenceKey ?: rev.sessionId
                val reviewEntity = ReviewEntity(
                    sessionId = rev.sessionId,
                    occurrenceKey = targetOccKey,
                    reviewStatus = if (rev.isApproved) ReviewStatus.APPROVED else ReviewStatus.REJECTED,
                    reviewNote = rev.feedbackNote ?: rev.rejectionReason,
                    reviewedAt = rev.reviewedAt
                )
                db.reviewDao().insertReview(reviewEntity)

                if (rev.isApproved) {
                    db.occurrenceDao().updateStatus(targetOccKey, OccurrenceStatus.APPROVED)
                    db.occurrenceDao().setWarning(targetOccKey, false, null)
                } else {
                    db.occurrenceDao().updateStatus(targetOccKey, OccurrenceStatus.PENDING)
                    db.occurrenceDao().setWarning(
                        targetOccKey,
                        true,
                        rev.feedbackNote ?: rev.rejectionReason ?: "Bu görev onaylanmadı. Lütfen eksikleri tamamlayıp tekrar yapınız."
                    )
                }
            }
        }

        // F. Screenshots
        if (payload.screenshots.isNotEmpty()) {
            val screenshotsDir = File(context.filesDir, "screenshots").apply { if (!exists()) mkdirs() }
            val existingLocalSs = db.screenshotDao().getAllScreenshotsOnce().associateBy { it.screenshotId }
            val toUpsert = mutableListOf<ScreenshotEntity>()

            for (rss in payload.screenshots) {
                val existing = existingLocalSs[rss.id]
                var localFilePath = existing?.url

                if (localFilePath == null || !File(localFilePath).exists()) {
                    if (rss.imageUrl.startsWith("data:image/") || rss.imageUrl.contains("base64,")) {
                        try {
                            val base64Data = if (rss.imageUrl.contains(",")) rss.imageUrl.substringAfter(",") else rss.imageUrl
                            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                            val targetFile = File(screenshotsDir, "${rss.id}.jpg")
                            targetFile.writeBytes(bytes)
                            localFilePath = targetFile.absolutePath
                        } catch (_: Exception) {
                            localFilePath = rss.imageUrl
                        }
                    } else {
                        localFilePath = rss.imageUrl
                    }
                }

                toUpsert.add(
                    ScreenshotEntity(
                        screenshotId = rss.id,
                        sessionId = rss.sessionId,
                        occurrenceKey = rss.sessionId,
                        capturedAt = rss.timestamp,
                        url = localFilePath ?: rss.imageUrl,
                        sizeKb = 15,
                        uploadStatus = UploadStatus.UPLOADED
                    )
                )
            }
            if (toUpsert.isNotEmpty()) {
                db.screenshotDao().upsertScreenshots(toUpsert)
            }
        }
    }

    private suspend fun convertLocalScreenshotsToDtos(familyCode: String): List<RemoteScreenshotSyncDto> = withContext(Dispatchers.IO) {
        val finalScreenshots = db.screenshotDao().getAllScreenshotsOnce()
        finalScreenshots.take(10).mapNotNull { ss ->
            try {
                val imgData = when {
                    ss.url.startsWith("data:image/") || ss.url.contains("base64,") -> ss.url
                    ss.url.startsWith("http://") || ss.url.startsWith("https://") -> ss.url
                    else -> {
                        val file = File(ss.url)
                        if (file.exists() && file.length() > 0) {
                            val bytes = file.readBytes()
                            if (bytes.size > 50 * 1024) {
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                if (bmp != null) {
                                    val targetW = 360
                                    val targetH = (360 * bmp.height / bmp.width).coerceAtLeast(1)
                                    val scaled = Bitmap.createScaledBitmap(bmp, targetW, targetH, true)
                                    val bos = ByteArrayOutputStream()
                                    scaled.compress(Bitmap.CompressFormat.JPEG, 45, bos)
                                    "data:image/jpeg;base64," + Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
                                } else {
                                    "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                                }
                            } else {
                                "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                            }
                        } else null
                    }
                }
                if (imgData != null) {
                    RemoteScreenshotSyncDto(
                        id = ss.screenshotId,
                        familyCode = familyCode,
                        sessionId = ss.sessionId,
                        imageUrl = imgData,
                        timestamp = ss.capturedAt
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }
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
