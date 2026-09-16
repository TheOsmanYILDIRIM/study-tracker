package com.studytracker.core.data.remote.sync

import android.content.Context
import android.os.Environment
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.OccurrenceEntity
import com.studytracker.core.data.local.db.entity.PlanEntity
import com.studytracker.core.data.local.db.entity.ReviewEntity
import com.studytracker.core.data.local.db.entity.ScreenshotEntity
import com.studytracker.core.data.local.db.entity.SessionEntity
import com.studytracker.core.data.local.db.entity.TaskTemplateEntity
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

    private fun getBridgeFiles(familyCode: String): List<File> {
        val files = mutableListOf<File>()
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null) {
                files.add(File(downloadDir, ".study_tracker_sync_${familyCode}.json"))
                files.add(File(downloadDir, "study_tracker_sync_${familyCode}.json"))
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "External storage download dir not accessible: ${e.message}")
        }
        files.add(File("/sdcard/Download/.study_tracker_sync_${familyCode}.json"))
        files.add(File("/sdcard/Download/study_tracker_sync_${familyCode}.json"))
        files.add(File(context.filesDir, "study_tracker_sync_${familyCode}.json"))
        return files
    }

    private fun readFromBridgeFile(familyCode: String): SharedFamilySyncPayload? {
        for (file in getBridgeFiles(familyCode)) {
            try {
                if (file.exists() && file.canRead()) {
                    val text = file.readText()
                    if (text.isNotBlank()) {
                        val payload = json.decodeFromString<SharedFamilySyncPayload>(text)
                        Log.d("CloudSyncManager", "Loaded bridge payload from ${file.absolutePath}")
                        return payload
                    }
                }
            } catch (e: Exception) {
                Log.w("CloudSyncManager", "Failed reading bridge from ${file.absolutePath}: ${e.message}")
            }
        }
        return null
    }

    private fun writeToBridgeFile(familyCode: String, payload: SharedFamilySyncPayload) {
        val text = try { json.encodeToString(payload) } catch (e: Exception) { return }
        for (file in getBridgeFiles(familyCode)) {
            try {
                file.parentFile?.mkdirs()
                file.writeText(text)
            } catch (e: Exception) {
                Log.w("CloudSyncManager", "Failed writing bridge to ${file.absolutePath}: ${e.message}")
            }
        }
    }

    suspend fun syncAll(): Result<String> = withContext(Dispatchers.IO) {
        if (!prefs.isCloudSyncEnabled.value) {
            return@withContext Result.success("Bulut senkronizasyonu devre dışı")
        }

        _syncState.value = SyncState.Syncing
        val familyCode = getOrCreateFamilyCode()

        try {
            var localOccurrences = db.occurrenceDao().getAllOccurrencesOnce()
            val localSessions = db.sessionDao().getAllSessionsOnce()
            val localPlan = db.planDao().getActivePlanOnce()
            val localTasks = db.taskTemplateDao().getAllTasksOnce()

            // --- LOCAL BRIDGE FALLBACK SYNC ---
            val bridgePayload = readFromBridgeFile(familyCode)
            if (localOccurrences.isEmpty() && bridgePayload != null && bridgePayload.occurrences.isNotEmpty()) {
                // Student app has no data, restore from parent's bridge payload
                bridgePayload.plan?.let { p ->
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

                if (bridgePayload.tasks.isNotEmpty()) {
                    val taskEntities = bridgePayload.tasks.map { t ->
                        TaskTemplateEntity(
                            taskId = t.taskId,
                            title = t.title,
                            kind = try { TaskKind.valueOf(t.kind) } catch (e: Exception) { TaskKind.DAILY },
                            contentType = try { ContentType.valueOf(t.contentType) } catch (e: Exception) { ContentType.TEXT },
                            youtubeUrl = t.youtubeUrl,
                            plannedMinutes = t.plannedMinutes,
                            targetMode = t.targetMode?.let { try { TargetMode.valueOf(it) } catch (e: Exception) { null } },
                            targetCount = t.targetCount,
                            targetMinutes = t.targetMinutes,
                            reviewRequired = t.reviewRequired,
                            active = t.active
                        )
                    }
                    db.taskTemplateDao().upsertTasks(taskEntities)
                }

                val occEntities = bridgePayload.occurrences.map { remote ->
                    OccurrenceEntity(
                        occurrenceKey = remote.id,
                        taskId = remote.planId,
                        type = try { TaskKind.valueOf(remote.topic) } catch (e: Exception) { TaskKind.DAILY },
                        date = remote.date.ifEmpty { null },
                        weekId = remote.weekId.ifEmpty { null },
                        title = remote.subject,
                        plannedMinutes = remote.targetDurationMin,
                        youtubeUrl = null,
                        reviewRequired = true,
                        status = try { OccurrenceStatus.valueOf(remote.status) } catch (e: Exception) { OccurrenceStatus.PENDING },
                        warning = false,
                        warningText = remote.parentNote.ifEmpty { null },
                        rejectCount = 0,
                        approvedCount = remote.completedQuestionCount,
                        targetCount = if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null,
                        targetMinutes = if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                    )
                }
                db.occurrenceDao().upsertOccurrences(occEntities)
                localOccurrences = db.occurrenceDao().getAllOccurrencesOnce()
            } else if (localOccurrences.isNotEmpty() || localPlan != null) {
                // Parent app or active student has data, export to bridge payload
                val occDtos = localOccurrences.map { occ ->
                    RemoteOccurrenceSyncDto(
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
                }

                val sessDtos = localSessions.map { sess ->
                    RemoteSessionSyncDto(
                        id = sess.sessionId,
                        familyCode = familyCode,
                        occurrenceId = sess.occurrenceKey,
                        startTime = sess.startTime,
                        endTime = sess.endTime,
                        durationMin = if (sess.endTime != null) ((sess.endTime - sess.startTime) / 60000).toInt() else 0,
                        isCompleted = sess.status != SessionStatus.ACTIVE
                    )
                }

                val planDto = localPlan?.let {
                    LocalPlanSyncDto(
                        planId = it.planId,
                        weekId = it.weekId,
                        weekStartDate = it.weekStartDate,
                        childId = it.childId,
                        timezone = it.timezone,
                        updatedAt = it.updatedAt,
                        rawJson = it.rawJson
                    )
                } ?: bridgePayload?.plan

                val taskDtos = if (localTasks.isNotEmpty()) {
                    localTasks.map {
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
                } else (bridgePayload?.tasks ?: emptyList())

                writeToBridgeFile(
                    familyCode,
                    SharedFamilySyncPayload(
                        familyCode = familyCode,
                        plan = planDto,
                        tasks = taskDtos,
                        occurrences = occDtos,
                        sessions = sessDtos,
                        reviews = bridgePayload?.reviews ?: emptyList()
                    )
                )
            }

            // --- REMOTE SUPABASE SYNC (with fallback) ---
            var pulledOccCount = 0
            var pulledSessCount = 0

            try {
                // 1. Push Local Occurrences Up
                for (occ in localOccurrences) {
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

                // 2. Push Local Sessions Up
                for (sess in localSessions) {
                    val dto = RemoteSessionSyncDto(
                        id = sess.sessionId,
                        familyCode = familyCode,
                        occurrenceId = sess.occurrenceKey,
                        startTime = sess.startTime,
                        endTime = sess.endTime,
                        durationMin = if (sess.endTime != null) ((sess.endTime - sess.startTime) / 60000).toInt() else 0,
                        isCompleted = sess.status != SessionStatus.ACTIVE
                    )
                    supabaseClient.post("sessions", dto) { json.encodeToString(it) }
                }

                // 3. Pull Remote Occurrences (Görevleri İndir)
                val occResponse = supabaseClient.get("occurrences", "family_code=eq.$familyCode")
                if (occResponse.isSuccess) {
                    val occBody = occResponse.getOrNull() ?: ""
                    if (occBody.isNotEmpty() && occBody != "[]") {
                        try {
                            val remoteOccs: List<RemoteOccurrenceSyncDto> = json.decodeFromString(occBody)
                            val entities = remoteOccs.map { remote ->
                                OccurrenceEntity(
                                    occurrenceKey = remote.id,
                                    taskId = remote.planId,
                                    type = try { TaskKind.valueOf(remote.topic) } catch (e: Exception) { TaskKind.DAILY },
                                    date = remote.date.ifEmpty { null },
                                    weekId = remote.weekId.ifEmpty { null },
                                    title = remote.subject,
                                    plannedMinutes = remote.targetDurationMin,
                                    youtubeUrl = null,
                                    reviewRequired = true,
                                    status = try { OccurrenceStatus.valueOf(remote.status) } catch (e: Exception) { OccurrenceStatus.PENDING },
                                    warning = false,
                                    warningText = remote.parentNote.ifEmpty { null },
                                    rejectCount = 0,
                                    approvedCount = remote.completedQuestionCount,
                                    targetCount = if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null,
                                    targetMinutes = if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                                )
                            }
                            if (entities.isNotEmpty()) {
                                db.occurrenceDao().upsertOccurrences(entities)
                                pulledOccCount = entities.size
                            }
                        } catch (e: Exception) {
                            Log.w("CloudSyncManager", "Error parsing remote occurrences: ${e.message}")
                        }
                    }
                }

                // 4. Pull Remote Sessions (Oturumları İndir)
                val sessResponse = supabaseClient.get("sessions", "family_code=eq.$familyCode")
                if (sessResponse.isSuccess) {
                    val sessBody = sessResponse.getOrNull() ?: ""
                    if (sessBody.isNotEmpty() && sessBody != "[]") {
                        try {
                            val remoteSessions: List<RemoteSessionSyncDto> = json.decodeFromString(sessBody)
                            for (rs in remoteSessions) {
                                val sessionEntity = SessionEntity(
                                    sessionId = rs.id,
                                    occurrenceKey = rs.occurrenceId,
                                    childId = "child_1",
                                    startTime = rs.startTime,
                                    endTime = rs.endTime,
                                    status = if (rs.isCompleted) SessionStatus.WAITING_REVIEW else SessionStatus.ACTIVE,
                                    screenshotCount = 0,
                                    finalScreenshotUrl = null
                                )
                                db.sessionDao().upsertSession(sessionEntity)
                            }
                            pulledSessCount = remoteSessions.size
                        } catch (e: Exception) {
                            Log.w("CloudSyncManager", "Error parsing remote sessions: ${e.message}")
                        }
                    }
                }

                // 5. Pull Remote Screenshots (Kanıtları İndir)
                val scResponse = supabaseClient.get("screenshots", "family_code=eq.$familyCode")
                if (scResponse.isSuccess) {
                    val scBody = scResponse.getOrNull() ?: ""
                    if (scBody.isNotEmpty() && scBody != "[]") {
                        try {
                            val remoteScs: List<RemoteScreenshotSyncDto> = json.decodeFromString(scBody)
                            for (rsc in remoteScs) {
                                val scEntity = ScreenshotEntity(
                                    screenshotId = rsc.id,
                                    sessionId = rsc.sessionId,
                                    occurrenceKey = rsc.sessionId,
                                    capturedAt = rsc.timestamp,
                                    url = rsc.imageUrl,
                                    sizeKb = 150,
                                    uploadStatus = UploadStatus.UPLOADED
                                )
                                db.screenshotDao().insertScreenshot(scEntity)
                            }
                        } catch (e: Exception) {
                            Log.w("CloudSyncManager", "Error parsing remote screenshots: ${e.message}")
                        }
                    }
                }

                // 6. Pull Remote Reviews from Supabase (Veli Onayları)
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
            } catch (netEx: Exception) {
                Log.w("CloudSyncManager", "Remote Supabase sync skipped/failed (local bridge used): ${netEx.message}")
            }

            val finalCount = db.occurrenceDao().getAllOccurrencesOnce().size
            _lastSyncedTime.value = System.currentTimeMillis()
            _syncState.value = SyncState.Success("Senkronizasyon Başarılı ($finalCount görev aktif)")
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

                // Update bridge file with session
                val bridge = readFromBridgeFile(familyCode)
                if (bridge != null) {
                    val updatedSessions = bridge.sessions.filter { it.id != sessionDto.id } + sessionDto
                    writeToBridgeFile(familyCode, bridge.copy(sessions = updatedSessions, updatedAt = System.currentTimeMillis()))
                }

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

            // Update bridge file with review decision
            val bridge = readFromBridgeFile(familyCode)
            if (bridge != null) {
                val updatedReviews = bridge.reviews.filter { it.sessionId != sessionId } + reviewDto
                val updatedOccurrences = bridge.occurrences.map { occ ->
                    if (occ.id == occurrenceKey) {
                        occ.copy(status = if (isApproved) OccurrenceStatus.APPROVED.name else OccurrenceStatus.REJECTED.name)
                    } else occ
                }
                writeToBridgeFile(familyCode, bridge.copy(reviews = updatedReviews, occurrences = updatedOccurrences, updatedAt = System.currentTimeMillis()))
            }

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
