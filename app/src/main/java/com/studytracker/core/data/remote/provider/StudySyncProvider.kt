package com.studytracker.core.data.remote.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.data.remote.sync.*
import com.studytracker.core.domain.model.*
import com.studytracker.core.data.local.repository.toDomain
import com.studytracker.core.data.local.repository.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException

class StudySyncProvider : ContentProvider() {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val ctx = context ?: return null
        val db = AppDatabase.getInstance(ctx)
        val familyCode = arg?.trim()?.uppercase()?.ifBlank { "ST-2026" } ?: "ST-2026"

        return try {
            when (method) {
                "sync", "syncExchange" -> {
                    val incomingJson = extras?.getString("payload")
                    runBlocking(Dispatchers.IO) {
                        if (!incomingJson.isNullOrBlank()) {
                            try {
                                val incomingPayload = json.decodeFromString<SharedFamilySyncPayload>(incomingJson)
                                mergePayloadIntoDb(ctx, db, incomingPayload)
                            } catch (e: Exception) {
                                Log.w("StudySyncProvider", "Failed to merge incoming payload in sync: ${e.message}")
                            }
                        }
                    }

                    val currentPayload = runBlocking(Dispatchers.IO) {
                        buildConsolidatedPayload(ctx, db, familyCode)
                    }
                    Bundle().apply {
                        putString("payload", json.encodeToString(currentPayload))
                        putBoolean("success", true)
                    }
                }

                "getSyncPayload" -> {
                    val currentPayload = runBlocking(Dispatchers.IO) {
                        buildConsolidatedPayload(ctx, db, familyCode)
                    }
                    Bundle().apply {
                        putString("payload", json.encodeToString(currentPayload))
                        putBoolean("success", true)
                    }
                }

                "putSyncPayload" -> {
                    val incomingJson = extras?.getString("payload")
                    if (!incomingJson.isNullOrBlank()) {
                        runBlocking(Dispatchers.IO) {
                            val incomingPayload = json.decodeFromString<SharedFamilySyncPayload>(incomingJson)
                            mergePayloadIntoDb(ctx, db, incomingPayload)
                        }
                    }
                    Bundle().apply { putBoolean("success", true) }
                }

                else -> null
            }
        } catch (e: Exception) {
            Log.e("StudySyncProvider", "Error in ContentProvider call($method): ${e.message}", e)
            null
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ctx = context ?: throw FileNotFoundException("Context is null")
        val path = uri.path ?: throw FileNotFoundException("Path is null")
        val filename = path.substringAfterLast("/")
        val cleanName = if (filename.endsWith(".jpg") || filename.endsWith(".png")) filename else "$filename.jpg"
        
        val file = File(File(ctx.filesDir, "screenshots"), cleanName)
        if (file.exists() && file.length() > 0) {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        
        // Check exact path match
        val directFile = File(path)
        if (directFile.exists() && directFile.length() > 0) {
            return ParcelFileDescriptor.open(directFile, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        
        throw FileNotFoundException("Screenshot not found at $path")
    }

    private suspend fun mergePayloadIntoDb(ctx: android.content.Context, db: AppDatabase, payload: SharedFamilySyncPayload) {
        // 1. Plan
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

        // 2. Tasks
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

        // 3. Occurrences
        if (payload.occurrences.isNotEmpty()) {
            val localOccMap = db.occurrenceDao().getAllOccurrencesOnce().associateBy { it.occurrenceKey }
            val taskTemplateMap = payload.tasks.associateBy { it.taskId }
            val urlRegex = Regex("""(https?://(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w-]+[^\s]*)""", RegexOption.IGNORE_CASE)

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

                val templateTask = taskTemplateMap[remote.planId] ?: (local?.taskId?.let { taskTemplateMap[it] })
                val extractedFromText = urlRegex.find(remote.subject)?.value 
                    ?: urlRegex.find(remote.parentNote)?.value 
                    ?: remote.studentNote?.let { urlRegex.find(it)?.value }

                val resolvedYoutubeUrl = remote.youtubeUrl
                    ?: local?.youtubeUrl
                    ?: templateTask?.youtubeUrl
                    ?: extractedFromText

                OccurrenceEntity(
                    occurrenceKey = remote.id,
                    taskId = if (!local?.taskId.isNullOrBlank()) local!!.taskId else remote.planId,
                    type = local?.type ?: try { TaskKind.valueOf(remote.topic) } catch (_: Exception) { TaskKind.DAILY },
                    date = local?.date ?: remote.date.ifEmpty { null },
                    weekId = local?.weekId ?: remote.weekId.ifEmpty { null },
                    title = if (!local?.title.isNullOrBlank()) local!!.title else remote.subject,
                    plannedMinutes = if ((local?.plannedMinutes ?: 0) > 0) local!!.plannedMinutes else remote.targetDurationMin,
                    youtubeUrl = resolvedYoutubeUrl,
                    reviewRequired = true,
                    status = finalStatus,
                    warning = hasWarning,
                    warningText = warningText,
                    rejectCount = maxOf(local?.rejectCount ?: 0, if (hasWarning) 1 else 0),
                    approvedCount = approvedCount,
                    targetCount = targetCount,
                    targetMinutes = local?.targetMinutes ?: if (remote.completedDurationMin > 0) remote.completedDurationMin else null,
                    studentNote = remote.studentNote ?: local?.studentNote
                )
            }
            db.occurrenceDao().upsertOccurrences(mergedOccs)
        }

        // 4. Sessions
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
                        finalScreenshotUrl = existing?.finalScreenshotUrl,
                        studentNote = rs.notes.ifBlank { null } ?: existing?.studentNote
                    )
                )
            }
        }

        // 5. Reviews
        if (payload.reviews.isNotEmpty()) {
            val allOccs = db.occurrenceDao().getAllOccurrencesOnce()
            for (rev in payload.reviews) {
                val session = db.sessionDao().getSessionById(rev.sessionId)
                val rawTargetKey = session?.occurrenceKey ?: rev.sessionId
                val cleanRevSessionId = rev.sessionId.substringAfterLast("_", "").ifBlank { rev.sessionId.substringBefore(":", "") }
                val targetOcc = allOccs.find { 
                    it.occurrenceKey == rawTargetKey || 
                    it.occurrenceKey == rev.sessionId || 
                    it.occurrenceKey.endsWith("_${rev.sessionId}") || 
                    it.taskId == rev.sessionId ||
                    it.taskId == cleanRevSessionId ||
                    (session?.occurrenceKey != null && it.occurrenceKey == session.occurrenceKey)
                }
                val targetOccKey = targetOcc?.occurrenceKey ?: rawTargetKey

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
                        rev.feedbackNote ?: rev.rejectionReason ?: "Bu görev onaylanmadı. Lütfen tekrar yapınız."
                    )
                }
            }
        }

        // 6. Screenshots
        if (payload.screenshots.isNotEmpty()) {
            val screenshotsDir = File(ctx.filesDir, "screenshots").apply { if (!exists()) mkdirs() }
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

                val resolvedOccKey = payload.sessions.find { it.id == rss.sessionId }?.occurrenceId
                    ?: db.sessionDao().getSessionById(rss.sessionId)?.occurrenceKey
                    ?: rss.sessionId

                toUpsert.add(
                    ScreenshotEntity(
                        screenshotId = rss.id,
                        sessionId = rss.sessionId,
                        occurrenceKey = resolvedOccKey,
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

        // 7. Quizzes
        if (payload.quizzes.isNotEmpty()) {
            val localQuizzes = db.quizDao().getAllQuizzesOnce().associateBy { it.quizId }
            val mergedQuizzes = payload.quizzes.map { remoteQ ->
                val local = localQuizzes[remoteQ.quizId]?.toDomain()
                if (remoteQ.completed || (remoteQ.studentAnswers.isNotEmpty() && local?.completed != true)) {
                    remoteQ.toEntity()
                } else if (local?.completed == true) {
                    local.toEntity()
                } else {
                    remoteQ.toEntity()
                }
            }
            db.quizDao().upsertQuizzes(mergedQuizzes)
        }
    }

    private suspend fun buildConsolidatedPayload(ctx: android.content.Context, db: AppDatabase, familyCode: String): SharedFamilySyncPayload {
        val plan = db.planDao().getActivePlanOnce()?.let {
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

        val tasks = db.taskTemplateDao().getAllTasksOnce().map {
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

        val occurrences = db.occurrenceDao().getAllOccurrencesOnce().map {
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
                orderIndex = 0,
                studentNote = it.studentNote,
                youtubeUrl = it.youtubeUrl
            )
        }

        val sessions = db.sessionDao().getAllSessionsOnce().map {
            RemoteSessionSyncDto(
                id = it.sessionId,
                familyCode = familyCode,
                occurrenceId = it.occurrenceKey,
                startTime = it.startTime,
                endTime = it.endTime,
                durationMin = if (it.endTime != null) ((it.endTime - it.startTime) / 60000).toInt() else 0,
                isCompleted = it.status != SessionStatus.ACTIVE,
                notes = it.studentNote ?: ""
            )
        }

        val reviews = db.reviewDao().getAllReviewsOnce().map {
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

        val screenshots = db.screenshotDao().getAllScreenshotsOnce().take(10).mapNotNull { ss ->
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

        val quizzes = db.quizDao().getAllQuizzesOnce().map { it.toDomain() }

        return SharedFamilySyncPayload(
            familyCode = familyCode,
            plan = plan,
            tasks = tasks,
            occurrences = occurrences,
            sessions = sessions,
            screenshots = screenshots,
            reviews = reviews,
            quizzes = quizzes,
            updatedAt = System.currentTimeMillis()
        )
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.studytracker.sync"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
