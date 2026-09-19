package com.studytracker.core.data.package_exchange

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.db.entity.*
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.plan_engine.PlanMergeEngine
import com.studytracker.core.data.plan_engine.SimplePlanParser
import com.studytracker.core.data.remote.sync.*
import com.studytracker.core.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import com.studytracker.core.data.local.repository.toDomain
import com.studytracker.core.data.local.repository.toEntity

@Serializable
enum class PackageType {
    PLAN_DISTRIBUTION, // Veli -> Öğrenci (Haftalık Plan veya Revize Plan)
    STUDY_REPORT,      // Öğrenci -> Veli (Günlük Çalışma Kanıtları, Oturumlar ve Tamamlanan Dersler)
    REVIEW_FEEDBACK    // Veli -> Öğrenci (Onaylanan/Reddedilen Dersler ve Açıklama Notları)
}

@Serializable
data class StudyTrackerPackage(
    val formatVersion: Int = 1,
    val packageType: PackageType = PackageType.PLAN_DISTRIBUTION,
    val familyCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val senderRole: String = "APP", // "PARENT" or "CHILD" or "CLOUD"
    val title: String = "StudyTracker Data Package",
    val note: String? = null,
    val plan: LocalPlanSyncDto? = null,
    val tasks: List<LocalTaskTemplateSyncDto> = emptyList(),
    val occurrences: List<RemoteOccurrenceSyncDto> = emptyList(),
    val sessions: List<RemoteSessionSyncDto> = emptyList(),
    val screenshots: List<RemoteScreenshotSyncDto> = emptyList(),
    val reviews: List<RemoteReviewSyncDto> = emptyList(),
    val quizzes: List<Quiz> = emptyList()
)

object StudyPackageExchangeManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
    }

    /**
     * Lossy WebP Image Compressor (Extreme size reduction to ~10-18 KB per screenshot).
     */
    fun compressBitmapToWebpBase64(file: File, maxDimension: Int = 640, quality: Int = 65): String? {
        if (!file.exists() || file.length() == 0L) return null
        return try {
            val bytes = file.readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val (w, h) = if (bmp.width > maxDimension || bmp.height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(bmp.width, bmp.height)
                (bmp.width * scale).toInt() to (bmp.height * scale).toInt()
            } else {
                bmp.width to bmp.height
            }
            val scaled = Bitmap.createScaledBitmap(bmp, maxOf(w, 1), maxOf(h, 1), true)
            val bos = ByteArrayOutputStream()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, bos)
            } else {
                @Suppress("DEPRECATION")
                scaled.compress(Bitmap.CompressFormat.WEBP, quality, bos)
            }
            "data:image/webp;base64," + Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.w("PackageExchange", "WebP compression error: ${e.message}")
            null
        }
    }

    /**
     * Export weekly plan as a `.studyplan` file (Veli -> Öğrenci).
     */
    suspend fun exportPlanPackage(context: Context): File = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val prefs = AppPreferences.getInstance(context)
        val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }

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

        val quizzes = db.quizDao().getAllQuizzesOnce().map { it.toDomain() }

        val pkg = StudyTrackerPackage(
            packageType = PackageType.PLAN_DISTRIBUTION,
            familyCode = familyCode,
            senderRole = "PARENT",
            title = "Haftalık Çalışma Planı (${plan?.weekId ?: "Hafta"})",
            plan = plan,
            tasks = tasks,
            occurrences = occurrences,
            quizzes = quizzes
        )

        val outDir = File(context.cacheDir, "plans").apply { if (!exists()) mkdirs() }
        val file = File(outDir, "StudyTracker_Plan_${familyCode}.studyplan")
        file.writeText(json.encodeToString(pkg))
        file
    }

    /**
     * Export daily study report & compressed WebP screenshots (Öğrenci -> Veli).
     */
    suspend fun exportDailyReportPackage(context: Context): File = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val prefs = AppPreferences.getInstance(context)
        val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

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

        // Convert and compress local screenshots into tiny WebP Base64 strings
        val localScreenshots = db.screenshotDao().getAllScreenshotsOnce().take(15)
        val screenshotDtos = localScreenshots.mapNotNull { ss ->
            val webpData = when {
                ss.url.startsWith("data:image/webp") -> ss.url
                ss.url.startsWith("data:image/") -> ss.url
                else -> {
                    val f = File(ss.url)
                    if (f.exists()) compressBitmapToWebpBase64(f) else null
                }
            }
            if (webpData != null) {
                RemoteScreenshotSyncDto(
                    id = ss.screenshotId,
                    familyCode = familyCode,
                    sessionId = ss.sessionId,
                    imageUrl = webpData,
                    timestamp = ss.capturedAt
                )
            } else null
        }

        val completedCount = occurrences.count { it.status == OccurrenceStatus.APPROVED.name || it.status == OccurrenceStatus.WAITING_REVIEW.name }
        val quizzes = db.quizDao().getAllQuizzesOnce().map { it.toDomain() }

        val pkg = StudyTrackerPackage(
            packageType = PackageType.STUDY_REPORT,
            familyCode = familyCode,
            senderRole = "CHILD",
            title = "Günlük Çalışma Raporu ($todayStr)",
            note = "$completedCount ders tamamlandı, ${screenshotDtos.size} kanıt görseli eklendi.",
            occurrences = occurrences,
            sessions = sessions,
            screenshots = screenshotDtos,
            quizzes = quizzes
        )

        val outDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(outDir, "StudyTracker_Rapor_${familyCode}_${todayStr}.studyplan")
        file.writeText(json.encodeToString(pkg))
        file
    }

    /**
     * Export review decision feedback package (Veli -> Öğrenci).
     */
    suspend fun exportReviewFeedbackPackage(context: Context): File = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val prefs = AppPreferences.getInstance(context)
        val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }

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

        val pkg = StudyTrackerPackage(
            packageType = PackageType.REVIEW_FEEDBACK,
            familyCode = familyCode,
            senderRole = "PARENT",
            title = "Ebeveyn İnceleme & Onay Geri Bildirimi",
            occurrences = occurrences,
            reviews = reviews
        )

        val outDir = File(context.cacheDir, "reports").apply { if (!exists()) mkdirs() }
        val file = File(outDir, "StudyTracker_Inceleme_${familyCode}.studyplan")
        file.writeText(json.encodeToString(pkg))
        file
    }

    /**
     * Share a `.studyplan` file via Android native share sheet (WhatsApp, Telegram, QuickShare, etc.)
     */
    fun sharePackageFile(context: Context, file: File, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.studytracker.plan"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TITLE, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e("StudyPackageExchange", "Failed to share package: ${e.message}", e)
        }
    }

    /**
     * Import a `.studyplan` file from an Intent / Uri when clicked in WhatsApp / Files.
     */
    suspend fun importPackageFromUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext Result.failure(Exception("Dosya okunamadı"))

            importPackageString(context, content)
        } catch (e: Exception) {
            Log.e("PackageExchange", "Import error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Core Import & Smart Reconciliation Logic (Secere & Kayıpsız Revize Plan Birleştirme)
     */
    suspend fun importPackageString(context: Context, packageContent: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val prefs = AppPreferences.getInstance(context)
            val pkg = json.decodeFromString<StudyTrackerPackage>(packageContent)

            // 1. Update family code
            if (pkg.familyCode.isNotBlank()) {
                prefs.setFamilyPairCode(pkg.familyCode)
            }

            // 2. Process Plan & Task Templates with Lossless Merge Engine
            if (pkg.plan != null) {
                val currentPlan = db.planDao().getActivePlanOnce()
                db.planDao().setActivePlan(
                    PlanEntity(
                        planId = pkg.plan.planId,
                        weekId = pkg.plan.weekId,
                        weekStartDate = pkg.plan.weekStartDate,
                        childId = pkg.plan.childId,
                        timezone = pkg.plan.timezone,
                        updatedAt = pkg.plan.updatedAt,
                        rawJson = pkg.plan.rawJson
                    )
                )
            }

            if (pkg.tasks.isNotEmpty()) {
                db.taskTemplateDao().upsertTasks(pkg.tasks.map {
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

            // 3. Smart Occurrences Reconciliation (Preserve prior work on revisions & reflect parent edits/deletions)
            val isParentPlan = pkg.senderRole == "PARENT" || pkg.packageType == PackageType.PLAN_DISTRIBUTION
            if (pkg.occurrences.isNotEmpty()) {
                val localOccMap = db.occurrenceDao().getAllOccurrencesOnce().associateBy { it.occurrenceKey }
                val taskTemplateMap = pkg.tasks.associateBy { it.taskId }
                val urlRegex = Regex("""(https?://(?:www\.)?(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/shorts/)[\w-]+[^\s]*)""", RegexOption.IGNORE_CASE)

                val mergedOccs = pkg.occurrences.map { remote ->
                    val local = localOccMap[remote.id]
                    val remoteStatus = try { OccurrenceStatus.valueOf(remote.status) } catch (_: Exception) { OccurrenceStatus.PENDING }

                    // Status resolution hierarchy:
                    // If either side approved OR approved review exists -> APPROVED
                    // If student sent report with WAITING_REVIEW -> WAITING_REVIEW
                    // If student was ACTIVE -> ACTIVE
                    // If rejected -> PENDING with warning
                    val cleanPlanId = remote.planId.ifBlank { remote.id.substringAfterLast("_", "").ifBlank { remote.id.substringBefore(":", "") } }
                    val hasApprovedReview = pkg.reviews.any { rev ->
                        (rev.sessionId == remote.id || rev.sessionId == cleanPlanId || rev.sessionId.endsWith("_$cleanPlanId") || (local != null && rev.sessionId == local.occurrenceKey)) && rev.isApproved
                    }

                    val resolvedStatus = when {
                        isParentPlan && remoteStatus == OccurrenceStatus.PENDING && (local?.status == OccurrenceStatus.WAITING_REVIEW || local?.status == OccurrenceStatus.REJECTED) -> OccurrenceStatus.PENDING
                        local?.status == OccurrenceStatus.APPROVED || remoteStatus == OccurrenceStatus.APPROVED || hasApprovedReview -> OccurrenceStatus.APPROVED
                        remoteStatus == OccurrenceStatus.WAITING_REVIEW || local?.status == OccurrenceStatus.WAITING_REVIEW -> OccurrenceStatus.WAITING_REVIEW
                        local?.status == OccurrenceStatus.ACTIVE || remoteStatus == OccurrenceStatus.ACTIVE -> OccurrenceStatus.ACTIVE
                        remoteStatus == OccurrenceStatus.REJECTED || local?.status == OccurrenceStatus.REJECTED -> OccurrenceStatus.PENDING
                        else -> local?.status ?: remoteStatus
                    }

                    val approvedCount = maxOf(local?.approvedCount ?: 0, remote.completedQuestionCount)
                    val targetCount = if (isParentPlan && remote.targetQuestionCount > 0) remote.targetQuestionCount else (local?.targetCount ?: if (remote.targetQuestionCount > 0) remote.targetQuestionCount else null)
                    val isApprovedByTarget = (targetCount != null && approvedCount >= targetCount)
                    val finalStatus = if (isApprovedByTarget) OccurrenceStatus.APPROVED else resolvedStatus

                    val hasWarning = (local?.warning == true) || (remote.parentNote.isNotBlank() && finalStatus != OccurrenceStatus.APPROVED)
                    val warningText = if (remote.parentNote.isNotBlank()) remote.parentNote else local?.warningText

                    val derivedTaskId = when {
                        remote.planId.isNotBlank() -> remote.planId
                        !local?.taskId.isNullOrBlank() -> local!!.taskId
                        remote.id.contains("_") -> remote.id.substringAfterLast("_")
                        remote.id.contains(":") -> remote.id.substringBefore(":")
                        else -> remote.subject.replace(Regex("""[^a-zA-Z0-9_-]"""), "_").lowercase()
                    }

                    // Resolve video URL with thorough fallbacks (never match empty planId)
                    val templateTask = if (derivedTaskId.isNotBlank()) taskTemplateMap[derivedTaskId] else null
                    val extractedFromText = urlRegex.find(remote.subject)?.value 
                        ?: urlRegex.find(remote.parentNote)?.value 
                        ?: remote.studentNote?.let { urlRegex.find(it)?.value }

                    val resolvedYoutubeUrl = if (isParentPlan) {
                        if (!remote.youtubeUrl.isNullOrBlank()) {
                            remote.youtubeUrl
                        } else {
                            templateTask?.youtubeUrl ?: extractedFromText
                        }
                    } else {
                        remote.youtubeUrl ?: local?.youtubeUrl ?: templateTask?.youtubeUrl ?: extractedFromText
                    }

                    val finalTitle = if (isParentPlan && remote.subject.isNotBlank()) remote.subject else (local?.title ?: remote.subject)
                    val finalPlannedMinutes = if (isParentPlan && remote.targetDurationMin > 0) remote.targetDurationMin else ((local?.plannedMinutes ?: 0).takeIf { it > 0 } ?: remote.targetDurationMin)
                    val finalDate = if (isParentPlan && remote.date.isNotBlank()) remote.date else (local?.date ?: remote.date.ifEmpty { null })

                    OccurrenceEntity(
                        occurrenceKey = remote.id,
                        taskId = derivedTaskId,
                        type = local?.type ?: try { TaskKind.valueOf(remote.topic) } catch (_: Exception) { TaskKind.DAILY },
                        date = finalDate,
                        weekId = local?.weekId ?: remote.weekId.ifEmpty { null },
                        title = finalTitle,
                        plannedMinutes = finalPlannedMinutes,
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

                // If this is a parent plan sync, remove any local occurrences that were deleted by parent
                if (isParentPlan) {
                    val remoteKeys = pkg.occurrences.map { it.id }.toSet()
                    val allLocal = db.occurrenceDao().getAllOccurrencesOnce()
                    allLocal.filter { it.occurrenceKey !in remoteKeys && (pkg.plan?.weekId == null || it.weekId == pkg.plan?.weekId) }
                        .forEach { db.occurrenceDao().deleteOccurrence(it.occurrenceKey) }
                }

                db.occurrenceDao().upsertOccurrences(mergedOccs)
            } else if (isParentPlan && pkg.plan == null && pkg.tasks.isEmpty()) {
                // Parent wiped everything
                db.occurrenceDao().clearOccurrences()
                db.planDao().clearActivePlan()
                db.taskTemplateDao().clearTasks()
                db.quizDao().clearQuizzes()
            }

            // 4. Reconcile Sessions (Seceresini tutar)
            if (pkg.sessions.isNotEmpty()) {
                val localSessions = db.sessionDao().getAllSessionsOnce().associateBy { it.sessionId }
                for (rs in pkg.sessions) {
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

            // 5. Reconcile Reviews (Feedback & Badges)
            if (pkg.reviews.isNotEmpty()) {
                val allOccs = db.occurrenceDao().getAllOccurrencesOnce()
                for (rev in pkg.reviews) {
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

            // 6. Decode WebP Screenshots to local app storage
            if (pkg.screenshots.isNotEmpty()) {
                val screenshotsDir = File(context.filesDir, "screenshots").apply { if (!exists()) mkdirs() }
                val existingSs = db.screenshotDao().getAllScreenshotsOnce().associateBy { it.screenshotId }
                val toUpsert = mutableListOf<ScreenshotEntity>()

                for (rss in pkg.screenshots) {
                    val existing = existingSs[rss.id]
                    var localFilePath = existing?.url

                    if (localFilePath == null || !File(localFilePath).exists()) {
                        if (rss.imageUrl.startsWith("data:image/") || rss.imageUrl.contains("base64,")) {
                            try {
                                val base64Data = if (rss.imageUrl.contains(",")) rss.imageUrl.substringAfter(",") else rss.imageUrl
                                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                                val ext = if (rss.imageUrl.startsWith("data:image/webp")) "webp" else "jpg"
                                val targetFile = File(screenshotsDir, "${rss.id}.$ext")
                                targetFile.writeBytes(bytes)
                                localFilePath = targetFile.absolutePath
                            } catch (e: Exception) {
                                Log.w("PackageExchange", "Screenshot write error: ${e.message}")
                                localFilePath = rss.imageUrl
                            }
                        } else {
                            localFilePath = rss.imageUrl
                        }
                    }

                    // Resolve true occurrenceKey from package sessions or local DB
                    val resolvedOccKey = pkg.sessions.find { it.id == rss.sessionId }?.occurrenceId
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

            // 7. Reconcile Quizzes & Student Quiz Submissions
            if (pkg.quizzes.isNotEmpty()) {
                val localQuizzes = db.quizDao().getAllQuizzesOnce().associateBy { it.quizId }
                val mergedQuizzes = pkg.quizzes.map { remoteQ ->
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

            val summary = "${pkg.title} başarıyla yüklendi! (${pkg.occurrences.size} ders, ${pkg.quizzes.size} test, ${pkg.screenshots.size} kanıt)"
            Result.success(summary)
        } catch (e: Exception) {
            Log.e("PackageExchange", "Import error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reset all student progress, sessions, and screenshots (İlerleme Sıfırlama).
     */
    suspend fun resetAllProgress(context: Context, weekId: String? = null): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            if (weekId != null) {
                db.occurrenceDao().resetWeeklyOccurrencesProgress(weekId)
            } else {
                db.occurrenceDao().resetAllOccurrencesProgress()
            }
            db.sessionDao().clearSessions()
            db.screenshotDao().clearScreenshots()
            db.reviewDao().clearReviews()
            db.quizDao().resetAllQuizzesProgress()

            // Clean screenshots directory
            val screenshotsDir = File(context.filesDir, "study_screenshots")
            if (screenshotsDir.exists()) {
                screenshotsDir.listFiles()?.forEach { it.delete() }
            }

            "Tüm çalışma ve kanıt verileri sıfırlandı."
        }
    }

    /**
     * Wipe all plans, tasks, occurrences, sessions, screenshots, reviews and quizzes (Komple Sıfırla & Temiz Sayfa).
     */
    suspend fun clearAllData(context: Context): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            db.planDao().clearActivePlan()
            db.taskTemplateDao().clearTasks()
            db.occurrenceDao().clearOccurrences()
            db.sessionDao().clearSessions()
            db.screenshotDao().clearScreenshots()
            db.reviewDao().clearReviews()
            db.quizDao().clearQuizzes()

            val screenshotsDir = File(context.filesDir, "study_screenshots")
            if (screenshotsDir.exists()) {
                screenshotsDir.listFiles()?.forEach { it.delete() }
            }

            "Tüm ders planları, görevler, testler ve geçmiş kayıtlar tamamen temizlendi."
        }
    }
}
