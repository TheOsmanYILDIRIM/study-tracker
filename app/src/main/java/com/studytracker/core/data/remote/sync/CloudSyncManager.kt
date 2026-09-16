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

    private fun getBridgeFiles(familyCode: String): List<File> {
        val files = LinkedHashSet<File>()
        val fileName = "study_tracker_sync_${familyCode}.json"
        val hiddenFileName = ".study_tracker_sync_${familyCode}.json"

        // 1. Android standard public downloads
        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null) {
                files.add(File(downloadDir, fileName))
                files.add(File(downloadDir, hiddenFileName))
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Downloads directory access error: ${e.message}")
        }

        // 2. Android standard public documents
        try {
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null) {
                files.add(File(docsDir, fileName))
                files.add(File(docsDir, hiddenFileName))
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Documents directory access error: ${e.message}")
        }

        // 3. Absolute unix paths for /sdcard and /storage/emulated/0
        files.add(File("/sdcard/Download/$fileName"))
        files.add(File("/sdcard/Download/$hiddenFileName"))
        files.add(File("/sdcard/Documents/$fileName"))
        files.add(File("/storage/emulated/0/Download/$fileName"))
        files.add(File("/storage/emulated/0/Download/$hiddenFileName"))
        files.add(File("/storage/emulated/0/Documents/$fileName"))

        // 4. App external files dir (accessible if shared storage restricted)
        try {
            val extFiles = context.getExternalFilesDir(null)
            if (extFiles != null) {
                files.add(File(extFiles, fileName))
            }
        } catch (_: Exception) {}

        // 5. Internal private app files
        files.add(File(context.filesDir, fileName))

        return files.toList()
    }

    private fun readFromBridgeFile(familyCode: String): SharedFamilySyncPayload? {
        var latestPayload: SharedFamilySyncPayload? = null
        var maxUpdatedAt = -1L

        for (file in getBridgeFiles(familyCode)) {
            try {
                if (file.exists() && file.canRead()) {
                    val text = file.readText()
                    if (text.isNotBlank()) {
                        val payload = json.decodeFromString<SharedFamilySyncPayload>(text)
                        if (payload.updatedAt > maxUpdatedAt) {
                            maxUpdatedAt = payload.updatedAt
                            latestPayload = payload
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("CloudSyncManager", "Skipping bridge file ${file.absolutePath}: ${e.message}")
            }
        }
        return latestPayload
    }

    private fun writeToBridgeFile(familyCode: String, payload: SharedFamilySyncPayload) {
        val text = try {
            json.encodeToString(payload)
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Failed encoding sync payload: ${e.message}")
            return
        }

        var writeCount = 0
        for (file in getBridgeFiles(familyCode)) {
            try {
                file.parentFile?.mkdirs()
                file.writeText(text)
                writeCount++
            } catch (e: Exception) {
                Log.d("CloudSyncManager", "Could not write bridge file to ${file.absolutePath}: ${e.message}")
            }
        }
        Log.d("CloudSyncManager", "Successfully written sync bridge to $writeCount locations")
    }

    suspend fun syncAll(): Result<String> = withContext(Dispatchers.IO) {
        if (!prefs.isCloudSyncEnabled.value) {
            return@withContext Result.success("Bulut senkronizasyonu devre dışı")
        }

        _syncState.value = SyncState.Syncing
        val familyCode = getOrCreateFamilyCode()

        try {
            var localOccurrences = db.occurrenceDao().getAllOccurrencesOnce()
            var localSessions = db.sessionDao().getAllSessionsOnce()
            var localPlan = db.planDao().getActivePlanOnce()
            var localTasks = db.taskTemplateDao().getAllTasksOnce()
            val localReviews = db.reviewDao().getAllReviewsOnce()

            // 1. --- SMART TWO-WAY LOCAL BRIDGE RECONCILIATION ---
            val bridgePayload = readFromBridgeFile(familyCode)
            if (bridgePayload != null) {
                // A. Reconcile Plan & Task Templates
                bridgePayload.plan?.let { p ->
                    if (localPlan == null || localPlan.weekId != p.weekId || localPlan.rawJson.isBlank()) {
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
                        localPlan = db.planDao().getActivePlanOnce()
                    }
                }

                if (bridgePayload.tasks.isNotEmpty()) {
                    val taskEntities = bridgePayload.tasks.map { t ->
                        TaskTemplateEntity(
                            taskId = t.taskId,
                            title = t.title,
                            kind = try { TaskKind.valueOf(t.kind) } catch (e: Exception) { TaskKind.DAILY },
                            contentType = try { ContentType.valueOf(t.contentType) } catch (e: Exception) { ContentType.READING },
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
                    localTasks = db.taskTemplateDao().getAllTasksOnce()
                }

                // B. Reconcile Occurrences (Two-Way Status & Progress Merging)
                val bridgeOccMap = bridgePayload.occurrences.associateBy { it.id }
                val localOccMap = localOccurrences.associateBy { it.occurrenceKey }
                val allOccKeys = (localOccMap.keys + bridgeOccMap.keys).distinct()

                val mergedOccurrences = mutableListOf<OccurrenceEntity>()

                for (key in allOccKeys) {
                    val local = localOccMap[key]
                    val remote = bridgeOccMap[key]

                    if (local != null && remote != null) {
                        val remoteStatus = try { OccurrenceStatus.valueOf(remote.status) } catch (_: Exception) { OccurrenceStatus.PENDING }
                        
                        // Status resolution hierarchy: APPROVED > WAITING_REVIEW > ACTIVE > PENDING/REJECTED
                        val resolvedStatus = when {
                            local.status == OccurrenceStatus.APPROVED || remoteStatus == OccurrenceStatus.APPROVED -> OccurrenceStatus.APPROVED
                            local.status == OccurrenceStatus.WAITING_REVIEW || remoteStatus == OccurrenceStatus.WAITING_REVIEW -> OccurrenceStatus.WAITING_REVIEW
                            local.status == OccurrenceStatus.ACTIVE || remoteStatus == OccurrenceStatus.ACTIVE -> OccurrenceStatus.ACTIVE
                            remoteStatus == OccurrenceStatus.REJECTED || local.status == OccurrenceStatus.REJECTED -> OccurrenceStatus.PENDING
                            else -> local.status
                        }

                        val approvedCount = maxOf(local.approvedCount, remote.completedQuestionCount)
                        val targetCount = local.targetCount ?: if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null
                        val isApprovedByTarget = (targetCount != null && approvedCount >= targetCount)
                        val finalStatus = if (isApprovedByTarget) OccurrenceStatus.APPROVED else resolvedStatus

                        val hasWarning = local.warning || (remote.parentNote.isNotBlank() && finalStatus != OccurrenceStatus.APPROVED)
                        val warningText = if (remote.parentNote.isNotBlank()) remote.parentNote else local.warningText

                        mergedOccurrences.add(
                            OccurrenceEntity(
                                occurrenceKey = key,
                                taskId = if (local.taskId.isNotBlank()) local.taskId else remote.planId,
                                type = local.type,
                                date = local.date ?: remote.date.ifEmpty { null },
                                weekId = local.weekId ?: remote.weekId.ifEmpty { null },
                                title = if (local.title.isNotBlank()) local.title else remote.subject,
                                plannedMinutes = if (local.plannedMinutes > 0) local.plannedMinutes else remote.targetDurationMin,
                                youtubeUrl = local.youtubeUrl,
                                reviewRequired = true,
                                status = finalStatus,
                                warning = hasWarning,
                                warningText = warningText,
                                rejectCount = maxOf(local.rejectCount, if (hasWarning) 1 else 0),
                                approvedCount = approvedCount,
                                targetCount = targetCount,
                                targetMinutes = local.targetMinutes ?: if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                            )
                        )
                    } else if (local != null) {
                        mergedOccurrences.add(local)
                    } else if (remote != null) {
                        mergedOccurrences.add(
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
                                warning = remote.parentNote.isNotBlank() && remote.status != "APPROVED",
                                warningText = remote.parentNote.ifEmpty { null },
                                rejectCount = 0,
                                approvedCount = remote.completedQuestionCount,
                                targetCount = if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null,
                                targetMinutes = if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                            )
                        )
                    }
                }

                if (mergedOccurrences.isNotEmpty()) {
                    db.occurrenceDao().upsertOccurrences(mergedOccurrences)
                    localOccurrences = db.occurrenceDao().getAllOccurrencesOnce()
                }

                // C. Reconcile Sessions (Preserve Completed & Waiting Review Sessions)
                for (rs in bridgePayload.sessions) {
                    val existing = localSessions.find { it.sessionId == rs.id }
                    val resolvedSessionStatus = when {
                        existing?.status == SessionStatus.APPROVED -> SessionStatus.APPROVED
                        existing?.status == SessionStatus.REJECTED -> SessionStatus.REJECTED
                        rs.isCompleted -> SessionStatus.WAITING_REVIEW
                        else -> existing?.status ?: SessionStatus.ACTIVE
                    }

                    val sessionEntity = SessionEntity(
                        sessionId = rs.id,
                        occurrenceKey = rs.occurrenceId,
                        childId = "child_1",
                        startTime = rs.startTime,
                        endTime = rs.endTime ?: existing?.endTime,
                        status = resolvedSessionStatus,
                        screenshotCount = existing?.screenshotCount ?: 1,
                        finalScreenshotUrl = existing?.finalScreenshotUrl
                    )
                    db.sessionDao().upsertSession(sessionEntity)
                }
                localSessions = db.sessionDao().getAllSessionsOnce()

                // D. Reconcile Reviews
                for (rv in bridgePayload.reviews) {
                    val reviewEntity = ReviewEntity(
                        sessionId = rv.sessionId,
                        occurrenceKey = rv.occurrenceIdOrKey(db),
                        reviewStatus = if (rv.isApproved) ReviewStatus.APPROVED else ReviewStatus.REJECTED,
                        reviewNote = rv.feedbackNote ?: rv.rejectionReason,
                        reviewedAt = rv.reviewedAt
                    )
                    db.reviewDao().insertReview(reviewEntity)

                    if (rv.isApproved) {
                        db.occurrenceDao().updateStatus(reviewEntity.occurrenceKey, OccurrenceStatus.APPROVED)
                        db.occurrenceDao().setWarning(reviewEntity.occurrenceKey, false, null)
                    } else {
                        db.occurrenceDao().updateStatus(reviewEntity.occurrenceKey, OccurrenceStatus.PENDING)
                        db.occurrenceDao().setWarning(
                            reviewEntity.occurrenceKey,
                            true,
                            rv.feedbackNote ?: rv.rejectionReason ?: "Lütfen eksikleri tamamlayıp tekrar yapınız."
                        )
                    }
                }
            }

            // 2. --- REMOTE SUPABASE SYNC (with fail-safe fallback) ---
            try {
                // Push local occurrences
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

                // Push local sessions
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

                // Pull remote occurrences
                val occResponse = supabaseClient.get("occurrences", "family_code=eq.$familyCode")
                if (occResponse.isSuccess) {
                    val occBody = occResponse.getOrNull() ?: ""
                    if (occBody.isNotEmpty() && occBody != "[]") {
                        try {
                            val remoteOccs: List<RemoteOccurrenceSyncDto> = json.decodeFromString(occBody)
                            for (remote in remoteOccs) {
                                val current = db.occurrenceDao().getOccurrenceByKeyOnce(remote.id)
                                val remoteStatus = try { OccurrenceStatus.valueOf(remote.status) } catch (_: Exception) { OccurrenceStatus.PENDING }
                                val status = if (current?.status == OccurrenceStatus.APPROVED) OccurrenceStatus.APPROVED else remoteStatus
                                val entity = OccurrenceEntity(
                                    occurrenceKey = remote.id,
                                    taskId = remote.planId,
                                    type = try { TaskKind.valueOf(remote.topic) } catch (e: Exception) { TaskKind.DAILY },
                                    date = remote.date.ifEmpty { null },
                                    weekId = remote.weekId.ifEmpty { null },
                                    title = remote.subject,
                                    plannedMinutes = remote.targetDurationMin,
                                    youtubeUrl = null,
                                    reviewRequired = true,
                                    status = status,
                                    warning = remote.parentNote.isNotBlank() && status != OccurrenceStatus.APPROVED,
                                    warningText = remote.parentNote.ifEmpty { null },
                                    rejectCount = 0,
                                    approvedCount = maxOf(current?.approvedCount ?: 0, remote.completedQuestionCount),
                                    targetCount = if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null,
                                    targetMinutes = if (remote.completedDurationMin > 0) remote.completedDurationMin else null
                                )
                                db.occurrenceDao().upsertOccurrences(listOf(entity))
                            }
                        } catch (e: Exception) {
                            Log.w("CloudSyncManager", "Error decoding remote occurrences: ${e.message}")
                        }
                    }
                }

                // Pull remote sessions
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
                                    screenshotCount = 1,
                                    finalScreenshotUrl = null
                                )
                                db.sessionDao().upsertSession(sessionEntity)
                            }
                        } catch (e: Exception) {
                            Log.w("CloudSyncManager", "Error decoding remote sessions: ${e.message}")
                        }
                    }
                }

                // Pull remote reviews
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
                                    reviewNote = remoteReview.feedbackNote ?: remoteReview.rejectionReason,
                                    reviewedAt = remoteReview.reviewedAt
                                )
                                db.reviewDao().insertReview(reviewEntity)

                                if (remoteReview.isApproved) {
                                    db.occurrenceDao().updateStatus(reviewEntity.occurrenceKey, OccurrenceStatus.APPROVED)
                                    db.occurrenceDao().setWarning(reviewEntity.occurrenceKey, false, null)
                                } else {
                                    db.occurrenceDao().updateStatus(reviewEntity.occurrenceKey, OccurrenceStatus.PENDING)
                                    db.occurrenceDao().setWarning(
                                        reviewEntity.occurrenceKey,
                                        true,
                                        remoteReview.feedbackNote ?: remoteReview.rejectionReason ?: "Eksikleri tamamlayıp tekrar yapınız."
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("CloudSyncManager", "Error decoding remote reviews: ${e.message}")
                        }
                    }
                }
            } catch (netEx: Exception) {
                Log.d("CloudSyncManager", "Remote Supabase sync skipped/offline: ${netEx.message}")
            }

            // 3. --- RE-EXPORT FULL CONSOLIDATED STATE TO ALL SHARED BRIDGE LOCATIONS ---
            val finalOccurrences = db.occurrenceDao().getAllOccurrencesOnce()
            val finalSessions = db.sessionDao().getAllSessionsOnce()
            val finalPlan = db.planDao().getActivePlanOnce()
            val finalTasks = db.taskTemplateDao().getAllTasksOnce()
            val finalReviews = db.reviewDao().getAllReviewsOnce()

            val occDtos = finalOccurrences.map { occ ->
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

            val sessDtos = finalSessions.map { sess ->
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

            val planDto = finalPlan?.let {
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

            val taskDtos = if (finalTasks.isNotEmpty()) {
                finalTasks.map {
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

            val reviewDtos = finalReviews.map { r ->
                RemoteReviewSyncDto(
                    id = "rev_${r.sessionId}",
                    familyCode = familyCode,
                    sessionId = r.sessionId,
                    isApproved = r.reviewStatus == ReviewStatus.APPROVED,
                    rejectionReason = if (r.reviewStatus != ReviewStatus.APPROVED) r.reviewNote else null,
                    parentRating = if (r.reviewStatus == ReviewStatus.APPROVED) 5 else 1,
                    feedbackNote = r.reviewNote,
                    reviewedAt = r.reviewedAt
                )
            }

            writeToBridgeFile(
                familyCode,
                SharedFamilySyncPayload(
                    familyCode = familyCode,
                    plan = planDto,
                    tasks = taskDtos,
                    occurrences = occDtos,
                    sessions = sessDtos,
                    reviews = reviewDtos,
                    updatedAt = System.currentTimeMillis()
                )
            )

            val finalCount = finalOccurrences.size
            val waitingCount = finalSessions.count { it.status == SessionStatus.WAITING_REVIEW }
            _lastSyncedTime.value = System.currentTimeMillis()
            _syncState.value = SyncState.Success("Eşitleme Başarılı ($finalCount ders, $waitingCount onay bekleyen)")
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

                // Update bridge payload directly
                val bridge = readFromBridgeFile(familyCode)
                val updatedSessions = (bridge?.sessions?.filter { it.id != sessionDto.id } ?: emptyList()) + sessionDto
                val updatedOccurrences = (bridge?.occurrences ?: emptyList()).map { occ ->
                    if (occ.id == session.occurrenceKey) {
                        occ.copy(status = OccurrenceStatus.WAITING_REVIEW.name)
                    } else occ
                }

                writeToBridgeFile(
                    familyCode,
                    SharedFamilySyncPayload(
                        familyCode = familyCode,
                        plan = bridge?.plan,
                        tasks = bridge?.tasks ?: emptyList(),
                        occurrences = updatedOccurrences,
                        sessions = updatedSessions,
                        reviews = bridge?.reviews ?: emptyList(),
                        updatedAt = System.currentTimeMillis()
                    )
                )

                // Also run full sync to broadcast
                syncAll()

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
            val updatedReviews = (bridge?.reviews?.filter { it.sessionId != sessionId } ?: emptyList()) + reviewDto
            val updatedOccurrences = (bridge?.occurrences ?: emptyList()).map { occ ->
                if (occ.id == occurrenceKey) {
                    occ.copy(
                        status = if (isApproved) OccurrenceStatus.APPROVED.name else OccurrenceStatus.PENDING.name,
                        parentNote = if (!isApproved) (feedbackNote ?: "") else "",
                        completedQuestionCount = if (isApproved) occ.completedQuestionCount + 1 else occ.completedQuestionCount
                    )
                } else occ
            }

            val updatedSessions = (bridge?.sessions ?: emptyList()).map { sess ->
                if (sess.id == sessionId) {
                    sess.copy(isCompleted = true)
                } else sess
            }

            writeToBridgeFile(
                familyCode,
                SharedFamilySyncPayload(
                    familyCode = familyCode,
                    plan = bridge?.plan,
                    tasks = bridge?.tasks ?: emptyList(),
                    occurrences = updatedOccurrences,
                    sessions = updatedSessions,
                    reviews = updatedReviews,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Run full sync to broadcast
            syncAll()

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
