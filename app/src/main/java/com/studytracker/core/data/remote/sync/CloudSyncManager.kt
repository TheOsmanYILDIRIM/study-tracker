package com.studytracker.core.data.remote.sync

import android.content.Context
import android.net.Uri
import android.os.Bundle
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

    private fun readFromPeerProvider(familyCode: String): SharedFamilySyncPayload? {
        for (auth in getPeerAuthorities()) {
            try {
                val uri = Uri.parse("content://$auth")
                val bundle = context.contentResolver.call(uri, "getSyncPayload", familyCode, null)
                val payloadJson = bundle?.getString("payload")
                if (!payloadJson.isNullOrBlank()) {
                    val payload = json.decodeFromString<SharedFamilySyncPayload>(payloadJson)
                    Log.d("CloudSyncManager", "Successfully read peer payload from $auth: ${payload.occurrences.size} tasks")
                    return payload
                }
            } catch (e: Exception) {
                Log.d("CloudSyncManager", "Peer provider $auth unavailable: ${e.message}")
            }
        }
        return null
    }

    private fun writeToPeerProvider(familyCode: String, payload: SharedFamilySyncPayload) {
        val payloadJson = try { json.encodeToString(payload) } catch (_: Exception) { return }
        val bundle = Bundle().apply { putString("payload", payloadJson) }
        for (auth in getPeerAuthorities()) {
            try {
                val uri = Uri.parse("content://$auth")
                context.contentResolver.call(uri, "putSyncPayload", familyCode, bundle)
                Log.d("CloudSyncManager", "Successfully broadcast payload to peer provider $auth")
            } catch (e: Exception) {
                Log.d("CloudSyncManager", "Failed broadcasting to peer provider $auth: ${e.message}")
            }
        }
    }

    private fun getBridgeFiles(familyCode: String): List<File> {
        val files = LinkedHashSet<File>()
        val fileName = "study_tracker_sync_${familyCode}.json"
        val hiddenFileName = ".study_tracker_sync_${familyCode}.json"

        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null) {
                files.add(File(downloadDir, fileName))
                files.add(File(downloadDir, hiddenFileName))
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Downloads directory access error: ${e.message}")
        }

        try {
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null) {
                files.add(File(docsDir, fileName))
                files.add(File(docsDir, hiddenFileName))
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Documents directory access error: ${e.message}")
        }

        files.add(File("/sdcard/Download/$fileName"))
        files.add(File("/sdcard/Download/$hiddenFileName"))
        files.add(File("/sdcard/Documents/$fileName"))
        files.add(File("/storage/emulated/0/Download/$fileName"))
        files.add(File("/storage/emulated/0/Download/$hiddenFileName"))
        files.add(File("/storage/emulated/0/Documents/$fileName"))

        try {
            val extFiles = context.getExternalFilesDir(null)
            if (extFiles != null) {
                files.add(File(extFiles, fileName))
            }
        } catch (_: Exception) {}

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

    suspend fun importPayloadString(payloadJson: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val payload = json.decodeFromString<SharedFamilySyncPayload>(payloadJson)
            val familyCode = payload.familyCode.ifBlank { getOrCreateFamilyCode() }
            prefs.setFamilyPairCode(familyCode)

            payload.plan?.let { p ->
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

            if (payload.tasks.isNotEmpty()) {
                db.taskTemplateDao().upsertTasks(payload.tasks.map {
                    TaskTemplateEntity(
                        taskId = it.taskId,
                        title = it.title,
                        kind = try { TaskKind.valueOf(it.kind) } catch (_: Exception) { TaskKind.DAILY },
                        contentType = try { ContentType.valueOf(it.contentType) } catch (_: Exception) { ContentType.READING },
                        youtubeUrl = it.youtubeUrl,
                        plannedMinutes = it.plannedMinutes,
                        targetMode = it.targetMode?.let { tm -> try { TargetMode.valueOf(tm) } catch (_: Exception) { null } },
                        targetCount = it.targetCount,
                        targetMinutes = it.targetMinutes,
                        reviewRequired = it.reviewRequired,
                        active = it.active
                    )
                })
            }

            if (payload.occurrences.isNotEmpty()) {
                db.occurrenceDao().upsertOccurrences(payload.occurrences.map {
                    OccurrenceEntity(
                        occurrenceKey = it.id,
                        taskId = it.planId,
                        type = try { TaskKind.valueOf(it.topic) } catch (_: Exception) { TaskKind.DAILY },
                        date = it.date.ifEmpty { null },
                        weekId = it.weekId.ifEmpty { null },
                        title = it.subject,
                        plannedMinutes = it.targetDurationMin,
                        youtubeUrl = null,
                        reviewRequired = true,
                        status = try { OccurrenceStatus.valueOf(it.status) } catch (_: Exception) { OccurrenceStatus.PENDING },
                        warning = it.parentNote.isNotBlank() && it.status != "APPROVED",
                        warningText = it.parentNote.ifEmpty { null },
                        rejectCount = 0,
                        approvedCount = it.completedQuestionCount,
                        targetCount = if (it.targetQuestionCount > 0) it.targetQuestionCount else null,
                        targetMinutes = if (it.completedDurationMin > 0) it.completedDurationMin else null
                    )
                })
            }

            // Broadcast to peer and files
            writeToPeerProvider(familyCode, payload)
            writeToBridgeFile(familyCode, payload)

            syncAll()
            Result.success(payload.occurrences.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportCurrentPayloadString(): String = withContext(Dispatchers.IO) {
        val familyCode = getOrCreateFamilyCode()
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
        }

        val taskDtos = finalTasks.map {
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

        val payload = SharedFamilySyncPayload(
            familyCode = familyCode,
            plan = planDto,
            tasks = taskDtos,
            occurrences = occDtos,
            sessions = sessDtos,
            reviews = reviewDtos,
            updatedAt = System.currentTimeMillis()
        )
        json.encodeToString(payload)
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

            // 1. --- READ FROM PEER IPC PROVIDER FIRST (0ms direct cross-APK sync) ---
            val peerPayload = readFromPeerProvider(familyCode)
            val filePayload = readFromBridgeFile(familyCode)
            val incomingPayload = when {
                peerPayload != null && filePayload != null -> if (peerPayload.updatedAt >= filePayload.updatedAt) peerPayload else filePayload
                peerPayload != null -> peerPayload
                else -> filePayload
            }

            if (incomingPayload != null) {
                // A. Reconcile Plan & Task Templates
                incomingPayload.plan?.let { p ->
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

                if (incomingPayload.tasks.isNotEmpty()) {
                    val taskEntities = incomingPayload.tasks.map { t ->
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
                val remoteOccMap = incomingPayload.occurrences.associateBy { it.id }
                val localOccMap = localOccurrences.associateBy { it.occurrenceKey }
                val allOccKeys = (localOccMap.keys + remoteOccMap.keys).distinct()

                val mergedOccurrences = mutableListOf<OccurrenceEntity>()

                for (key in allOccKeys) {
                    val local = localOccMap[key]
                    val remote = remoteOccMap[key]

                    if (local != null && remote != null) {
                        val remoteStatus = try { OccurrenceStatus.valueOf(remote.status) } catch (_: Exception) { OccurrenceStatus.PENDING }
                        
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

                // C. Reconcile Sessions
                for (rs in incomingPayload.sessions) {
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
                for (rv in incomingPayload.reviews) {
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
            } catch (netEx: Exception) {
                Log.d("CloudSyncManager", "Remote Supabase sync skipped/offline: ${netEx.message}")
            }

            // 3. --- RE-EXPORT FULL CONSOLIDATED STATE TO PEER PROVIDER & BRIDGE FILES ---
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
            } ?: incomingPayload?.plan

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
            } else (incomingPayload?.tasks ?: emptyList())

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

            val finalPayload = SharedFamilySyncPayload(
                familyCode = familyCode,
                plan = planDto,
                tasks = taskDtos,
                occurrences = occDtos,
                sessions = sessDtos,
                reviews = reviewDtos,
                updatedAt = System.currentTimeMillis()
            )

            // Direct Binder IPC broadcast to the other APK
            writeToPeerProvider(familyCode, finalPayload)

            // Shared disk files fallback
            writeToBridgeFile(familyCode, finalPayload)

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

                val peer = readFromPeerProvider(familyCode) ?: readFromBridgeFile(familyCode)
                val updatedSessions = (peer?.sessions?.filter { it.id != sessionDto.id } ?: emptyList()) + sessionDto
                val updatedOccurrences = (peer?.occurrences ?: emptyList()).map { occ ->
                    if (occ.id == session.occurrenceKey) {
                        occ.copy(status = OccurrenceStatus.WAITING_REVIEW.name)
                    } else occ
                }

                val payload = SharedFamilySyncPayload(
                    familyCode = familyCode,
                    plan = peer?.plan,
                    tasks = peer?.tasks ?: emptyList(),
                    occurrences = updatedOccurrences,
                    sessions = updatedSessions,
                    reviews = peer?.reviews ?: emptyList(),
                    updatedAt = System.currentTimeMillis()
                )

                writeToPeerProvider(familyCode, payload)
                writeToBridgeFile(familyCode, payload)

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

            val peer = readFromPeerProvider(familyCode) ?: readFromBridgeFile(familyCode)
            val updatedReviews = (peer?.reviews?.filter { it.sessionId != sessionId } ?: emptyList()) + reviewDto
            val updatedOccurrences = (peer?.occurrences ?: emptyList()).map { occ ->
                if (occ.id == occurrenceKey) {
                    occ.copy(
                        status = if (isApproved) OccurrenceStatus.APPROVED.name else OccurrenceStatus.PENDING.name,
                        parentNote = if (!isApproved) (feedbackNote ?: "") else "",
                        completedQuestionCount = if (isApproved) occ.completedQuestionCount + 1 else occ.completedQuestionCount
                    )
                } else occ
            }

            val updatedSessions = (peer?.sessions ?: emptyList()).map { sess ->
                if (sess.id == sessionId) {
                    sess.copy(isCompleted = true)
                } else sess
            }

            val payload = SharedFamilySyncPayload(
                familyCode = familyCode,
                plan = peer?.plan,
                tasks = peer?.tasks ?: emptyList(),
                occurrences = updatedOccurrences,
                sessions = updatedSessions,
                reviews = updatedReviews,
                updatedAt = System.currentTimeMillis()
            )

            writeToPeerProvider(familyCode, payload)
            writeToBridgeFile(familyCode, payload)

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
