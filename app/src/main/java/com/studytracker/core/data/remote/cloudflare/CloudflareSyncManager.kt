package com.studytracker.core.data.remote.cloudflare

import android.content.Context
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.toDomain
import com.studytracker.core.data.package_exchange.StudyPackageExchangeManager
import com.studytracker.core.data.remote.sync.*
import com.studytracker.core.domain.model.Occurrence
import com.studytracker.core.ui.components.PlanVersionSummary
import com.studytracker.core.ui.components.SyncConflictData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class CloudSyncResponse(
    val success: Boolean = false,
    val familyCode: String? = null,
    val data: CloudSyncPayloadWrapper? = null,
    val error: String? = null
)

@Serializable
data class CloudSyncPayloadWrapper(
    val familyCode: String = "",
    val updatedAt: Long = 0L,
    val planSource: String? = null,
    val plan: LocalPlanSyncDto? = null,
    val tasks: List<LocalTaskTemplateSyncDto> = emptyList(),
    val occurrences: List<RemoteOccurrenceSyncDto> = emptyList(),
    val sessions: List<RemoteSessionSyncDto> = emptyList(),
    val screenshots: List<RemoteScreenshotSyncDto> = emptyList(),
    val reviews: List<RemoteReviewSyncDto> = emptyList(),
    val quizzes: List<com.studytracker.core.domain.model.Quiz> = emptyList(),
    val messages: List<RemoteMessageSyncDto> = emptyList()
)

sealed class SyncCheckResult {
    data class Success(val message: String) : SyncCheckResult()
    data class Conflict(val conflictData: SyncConflictData, val cloudData: CloudSyncPayloadWrapper) : SyncCheckResult()
    data class Error(val message: String) : SyncCheckResult()
}

enum class ConflictResolutionStrategy {
    DOWNLOAD_CLOUD,
    SMART_MERGE,
    UPLOAD_LOCAL
}

object CloudflareSyncManager {

    private const val TAG = "CloudflareSync"
    const val CLOUD_WORKER_URL = "https://studytracker-sync.osman13241429.workers.dev"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Buluttan sadece okuma yapar (GET). Yerel veriyi asla değiştirmez.
     */
    suspend fun fetchCloudData(context: Context): Result<CloudSyncPayloadWrapper> = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
            val targetUrl = URL("$CLOUD_WORKER_URL/api/sync?code=$familyCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("Cloudflare GET Hatası ($responseCode): $errorStream"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val syncRes = json.decodeFromString<CloudSyncResponse>(responseText)

            if (!syncRes.success || syncRes.data == null) {
                return@withContext Result.failure(Exception(syncRes.error ?: "Buluttan veri alınamadı"))
            }

            Result.success(syncRes.data)
        } catch (e: Exception) {
            Log.e(TAG, "fetchCloudData failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Veli senkronizasyonu öncesi AnkiDroid tarzı çakışma denetimi.
     * Bulut ve yerel plan farklıysa çakışma diyalogu verisi döndürür, farklılık yoksa sessizce eşitler.
     */
    suspend fun syncWithConflictCheck(context: Context): SyncCheckResult = withContext(Dispatchers.IO) {
        try {
            val cloudRes = fetchCloudData(context)
            if (cloudRes.isFailure) {
                return@withContext SyncCheckResult.Error(cloudRes.exceptionOrNull()?.message ?: "Buluta bağlanılamadı")
            }

            val cloudData = cloudRes.getOrNull()!!
            val db = AppDatabase.getInstance(context)
            val localPlan = db.planDao().getActivePlanOnce()
            val localOccs = db.occurrenceDao().getAllOccurrencesOnce()

            // 1. Yerel veritabanı tamamen boşsa doğrudan buluttan indir
            if (localPlan == null && localOccs.isEmpty()) {
                if (cloudData.plan != null || cloudData.occurrences.isNotEmpty()) {
                    applyCloudDataToLocal(context, cloudData)
                    return@withContext SyncCheckResult.Success("Buluttaki plan başarıyla yüklendi (${cloudData.occurrences.size} Ders)")
                } else {
                    return@withContext SyncCheckResult.Success("Bulutta ve cihazda aktif plan bulunmuyor.")
                }
            }

            // 2. Bulutta plan yoksa doğrudan yereli buluta yükle
            if (cloudData.plan == null && cloudData.occurrences.isEmpty()) {
                val pushRes = syncWithCloud(context, action = "SYNC")
                return@withContext if (pushRes.isSuccess) {
                    SyncCheckResult.Success("Bu cihazdaki plan buluta başarıyla yüklendi (${localOccs.size} Ders)")
                } else {
                    SyncCheckResult.Error(pushRes.exceptionOrNull()?.message ?: "Yükleme başarısız")
                }
            }

            // 3. Her iki tarafta da plan var: Çakışma ve Farklılık Kontrolü
            val remoteOccs = cloudData.occurrences
            val remoteOccMap = remoteOccs.associateBy { it.id }
            val localOccMap = localOccs.associateBy { it.occurrenceKey }

            var hasDefinitionMismatch = false
            if (cloudData.plan?.weekId != localPlan?.weekId) {
                hasDefinitionMismatch = true
            } else if (remoteOccs.size != localOccs.size) {
                hasDefinitionMismatch = true
            } else {
                for (local in localOccs) {
                    val remote = remoteOccMap[local.occurrenceKey]
                    if (remote == null) {
                        hasDefinitionMismatch = true
                        break
                    }
                    if (remote.subject != local.title ||
                        remote.targetDurationMin != local.plannedMinutes ||
                        remote.youtubeUrl != local.youtubeUrl
                    ) {
                        hasDefinitionMismatch = true
                        break
                    }
                }
            }

            if (hasDefinitionMismatch) {
                // Çakışma var! AnkiDroid tarzı karar penceresi açılması için bilgileri hazırla
                val timeFormat = SimpleDateFormat("dd MMM HH:mm", Locale("tr"))
                val cloudTimeStr = if (cloudData.updatedAt > 0) timeFormat.format(Date(cloudData.updatedAt)) else (cloudData.plan?.updatedAt ?: "")
                val localTimeStr = localPlan?.updatedAt ?: "Yerel Saat"

                val cloudSummary = PlanVersionSummary(
                    source = cloudData.planSource ?: "CLI / Bulut Master",
                    weekId = cloudData.plan?.weekId ?: "2026-W38",
                    taskCount = cloudData.occurrences.size,
                    sampleTasks = cloudData.occurrences.map { it.subject }.take(4),
                    updatedAtFormatted = cloudTimeStr
                )

                val localSummary = PlanVersionSummary(
                    source = "Veli Masası (Bu Telefon)",
                    weekId = localPlan?.weekId ?: "2026-W38",
                    taskCount = localOccs.size,
                    sampleTasks = localOccs.map { it.title }.take(4),
                    updatedAtFormatted = localTimeStr
                )

                return@withContext SyncCheckResult.Conflict(
                    conflictData = SyncConflictData(cloud = cloudSummary, local = localSummary),
                    cloudData = cloudData
                )
            } else {
                // Tanımlarda fark yok, sadece öğrenci ilerlemelerini ve oturumları eşitle
                val syncRes = syncWithCloud(context, action = "SYNC")
                return@withContext if (syncRes.isSuccess) {
                    SyncCheckResult.Success(syncRes.getOrNull() ?: "Senkronize")
                } else {
                    SyncCheckResult.Error(syncRes.exceptionOrNull()?.message ?: "Eşitleme hatası")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncWithConflictCheck failed: ${e.message}", e)
            SyncCheckResult.Error(e.message ?: "Beklenmeyen hata")
        }
    }

    /**
     * Veli çakışma diyaloğundan bir seçenek belirlediğinde çalıştırılır.
     */
    suspend fun resolveConflict(
        context: Context,
        strategy: ConflictResolutionStrategy,
        cloudData: CloudSyncPayloadWrapper
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (strategy) {
                ConflictResolutionStrategy.DOWNLOAD_CLOUD -> {
                    applyCloudDataToLocal(context, cloudData)
                    Result.success("☁️ Buluttaki taze plan bu cihaza indirildi ve eşitlendi.")
                }
                ConflictResolutionStrategy.SMART_MERGE -> {
                    // Bulut tanımlarını al, yerel öğrenci onaylarını ve oturumlarını koru
                    applyCloudDataToLocal(context, cloudData)
                    // Ardından yerel öğrenci oturumlarını buluta aktar
                    syncWithCloud(context, action = "SYNC")
                    Result.success("🔀 Ders tanımları buluttan güncellendi, onay ve oturumlar korundu.")
                }
                ConflictResolutionStrategy.UPLOAD_LOCAL -> {
                    // Bu cihazdaki planı buluta zorla yaz
                    val pushRes = syncWithCloud(context, action = "SYNC")
                    if (pushRes.isSuccess) {
                        Result.success("📱 Bu cihazdaki plan bulutun üzerine başarıyla yazıldı.")
                    } else {
                        Result.failure(pushRes.exceptionOrNull() ?: Exception("Yükleme başarısız"))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "resolveConflict failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Tek bir dersi diğer dersleri etkilemeden tekil delta olarak buluta günceller (Zero side-effects).
     */
    suspend fun patchSingleTask(
        context: Context,
        occurrence: Occurrence
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }

            val cleanTaskId = when {
                occurrence.taskId.isNotBlank() -> occurrence.taskId
                occurrence.occurrenceKey.contains("_") -> occurrence.occurrenceKey.substringAfterLast("_")
                else -> occurrence.title.replace(Regex("""[^a-zA-Z0-9_-]"""), "_").lowercase()
            }

            val patchDto = RemoteOccurrenceSyncDto(
                id = occurrence.occurrenceKey,
                familyCode = familyCode,
                date = occurrence.date ?: "",
                planId = cleanTaskId,
                subject = occurrence.title,
                topic = occurrence.type.name,
                targetDurationMin = occurrence.plannedMinutes,
                targetQuestionCount = occurrence.targetCount ?: 0,
                completedDurationMin = occurrence.targetMinutes ?: 0,
                completedQuestionCount = occurrence.approvedCount,
                status = occurrence.status.name,
                parentNote = occurrence.warningText ?: "",
                weekId = occurrence.weekId ?: "",
                orderIndex = 0,
                studentNote = occurrence.studentNote,
                youtubeUrl = occurrence.youtubeUrl
            )

            val payload = SharedFamilySyncPayload(
                familyCode = familyCode,
                senderRole = "PARENT",
                action = "PATCH_TASK",
                occurrences = listOf(patchDto)
            )

            // Ayrıca JSON içine patchTask nesnesi koyarak worker ile çift güvence sağlayalım
            val targetUrl = URL("$CLOUD_WORKER_URL/api/sync?code=$familyCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
            }

            val payloadJson = json.encodeToString(payload)
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payloadJson)
                writer.flush()
            }

            if (conn.responseCode in 200..299) {
                Result.success("Ders bulutta güncellendi: ${occurrence.title}")
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "patchSingleTask failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun applyCloudDataToLocal(context: Context, cloudData: CloudSyncPayloadWrapper) {
        val prefs = AppPreferences.getInstance(context)
        val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }

        val studyPackage = com.studytracker.core.data.package_exchange.StudyTrackerPackage(
            formatVersion = 1,
            packageType = com.studytracker.core.data.package_exchange.PackageType.REVIEW_FEEDBACK,
            familyCode = familyCode,
            senderRole = "CLOUD",
            title = "Cloudflare KV Sync ($familyCode)",
            plan = cloudData.plan,
            tasks = cloudData.tasks,
            occurrences = cloudData.occurrences,
            sessions = cloudData.sessions,
            screenshots = cloudData.screenshots,
            reviews = cloudData.reviews,
            quizzes = cloudData.quizzes
        )

        StudyPackageExchangeManager.importPackageString(context, json.encodeToString(studyPackage))
    }

    /**
     * Uçtan uca çift yönlü Cloudflare KV senkronizasyonu.
     */
    suspend fun syncWithCloud(
        context: Context,
        action: String = "SYNC",
        deleteTaskId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }

            val isLocalDbEmpty = (db.planDao().getActivePlanOnce() == null && db.occurrenceDao().getAllOccurrencesOnce().isEmpty())
            val effectiveRole = if (isLocalDbEmpty) "CLIENT" else com.studytracker.BuildConfig.APP_ROLE

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
                val cleanPlanId = if (it.taskId.isNotBlank()) it.taskId else it.occurrenceKey.substringAfterLast("_", "")
                RemoteOccurrenceSyncDto(
                    id = it.occurrenceKey,
                    familyCode = familyCode,
                    date = it.date ?: "",
                    planId = cleanPlanId,
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
                    isCompleted = it.status != com.studytracker.core.domain.model.SessionStatus.ACTIVE,
                    notes = it.studentNote ?: ""
                )
            }

            // Convert and compress local screenshots into compact WebP Base64 strings for Cloudflare sync
            val localScreenshots = db.screenshotDao().getAllScreenshotsOnce().take(15)
            val screenshots = localScreenshots.mapNotNull { ss ->
                try {
                    val imgData = when {
                        ss.url.startsWith("data:image/") || ss.url.contains("base64,") -> ss.url
                        ss.url.startsWith("http://") || ss.url.startsWith("https://") -> ss.url
                        else -> {
                            val f = java.io.File(ss.url)
                            if (f.exists() && f.length() > 0) {
                                StudyPackageExchangeManager.compressBitmapToWebpBase64(f)
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

            val reviews = db.reviewDao().getAllReviewsOnce().map {
                RemoteReviewSyncDto(
                    id = "rev_${it.sessionId}",
                    familyCode = familyCode,
                    sessionId = it.sessionId,
                    isApproved = it.reviewStatus == com.studytracker.core.domain.model.ReviewStatus.APPROVED,
                    rejectionReason = if (it.reviewStatus != com.studytracker.core.domain.model.ReviewStatus.APPROVED) it.reviewNote else null,
                    parentRating = if (it.reviewStatus == com.studytracker.core.domain.model.ReviewStatus.APPROVED) 5 else 1,
                    feedbackNote = it.reviewNote,
                    reviewedAt = it.reviewedAt
                )
            }

            val quizzes = db.quizDao().getAllQuizzesOnce().map {
                it.toDomain(json)
            }

            val payload = SharedFamilySyncPayload(
                familyCode = familyCode,
                senderRole = effectiveRole,
                action = action,
                deleteTaskId = deleteTaskId,
                plan = plan,
                tasks = tasks,
                occurrences = occurrences,
                sessions = sessions,
                screenshots = screenshots,
                reviews = reviews,
                quizzes = quizzes
            )

            val payloadJson = json.encodeToString(payload)

            val targetUrl = URL("$CLOUD_WORKER_URL/api/sync?code=$familyCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payloadJson)
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("Cloudflare Hatası ($responseCode): $errorStream"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val syncRes = json.decodeFromString<CloudSyncResponse>(responseText)

            if (!syncRes.success || syncRes.data == null) {
                return@withContext Result.failure(Exception(syncRes.error ?: "Bilinmeyen senkronizasyon hatası"))
            }

            val cloudData = syncRes.data
            applyCloudDataToLocal(context, cloudData)

            val occCount = cloudData.occurrences.size
            val taskCount = cloudData.tasks.size
            val reviewCount = cloudData.reviews.size
            val summary = if (occCount > 0 || taskCount > 0) {
                "Bulut Eşitlemesi Başarılı: $occCount Ders, $taskCount Şablon, $reviewCount Onay ($familyCode)"
            } else {
                "Bulut Bağlantısı Kuruldu ($familyCode). Henüz buluta yüklenmiş bir plan bulunmuyor."
            }

            Result.success(summary)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sıfırlama öncesi alınan 24 saatlik durum yedeğini (snapshot) geri yükler (Undo Reset).
     */
    suspend fun restoreFromSnapshot(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
            val targetUrl = URL("$CLOUD_WORKER_URL/api/sync?code=$familyCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
                setRequestProperty("X-Sender-Role", "PARENT")
            }

            val payload = SharedFamilySyncPayload(
                familyCode = familyCode,
                senderRole = "PARENT",
                action = "RESTORE"
            )
            val payloadJson = json.encodeToString(payload)
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payloadJson)
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("Geri alma hatası ($responseCode): $errorStream"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val syncRes = json.decodeFromString<CloudSyncResponse>(responseText)
            if (!syncRes.success || syncRes.data == null) {
                return@withContext Result.failure(Exception(syncRes.error ?: "Geri yüklenebilecek yedek bulunamadı"))
            }

            val cloudData = syncRes.data
            applyCloudDataToLocal(context, cloudData)
            Result.success("Önceki durum yedeği başarıyla geri yüklendi! (${cloudData.occurrences.size} ders)")
        } catch (e: Exception) {
            Log.e(TAG, "restoreFromSnapshot failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Aile eşleştirme kodunu doğrular veya yenisini oluşturur.
     */
    suspend fun pairFamilyCode(context: Context, pairCode: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = pairCode.trim().uppercase()
            val targetUrl = URL("$CLOUD_WORKER_URL/api/pair")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val body = "{\"familyCode\": \"$cleanCode\"}"
            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body); it.flush() }

            if (conn.responseCode in 200..299) {
                val prefs = AppPreferences.getInstance(context)
                prefs.setFamilyPairCode(cleanCode)
                Result.success(cleanCode)
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Veli tarafından öğrenciye anlık bildirim / motivasyon mesajı gönderir.
     */
    suspend fun sendMessageToStudent(
        context: Context,
        title: String,
        message: String,
        type: String = "REMINDER",
        targetDate: String? = null,
        targetOccurrenceId: String? = null
    ): Result<RemoteMessageSyncDto> = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
            val targetUrl = URL("$CLOUD_WORKER_URL/api/messages")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
                setRequestProperty("X-Sender-Role", "PARENT")
            }

            val msgPayload = RemoteMessageSyncDto(
                id = "msg_${System.currentTimeMillis()}_${(1000..9999).random()}",
                familyCode = familyCode,
                senderRole = "PARENT",
                title = title.trim(),
                message = message.trim(),
                type = type,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetDate = targetDate,
                targetOccurrenceId = targetOccurrenceId
            )

            val bodyJson = json.encodeToString(msgPayload)
            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(bodyJson); it.flush() }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("Mesaj iletilemedi ($responseCode): $errorStream"))
            }

            Log.d(TAG, "Öğrenciye bildirim mesajı gönderildi: $title -> $message")
            Result.success(msgPayload)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessageToStudent hatası: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cloudflare KV üzerinden aileye ait mesajları çeker.
     */
    suspend fun fetchMessages(context: Context, unreadOnly: Boolean = false): Result<List<RemoteMessageSyncDto>> = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
            val targetUrl = URL("$CLOUD_WORKER_URL/api/messages?code=$familyCode&unread=$unreadOnly")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception("HTTP $responseCode"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val parsed = json.decodeFromString<CloudSyncPayloadWrapper>(responseText)
            Result.success(parsed.messages)
        } catch (e: Exception) {
            // Alternatif direkt JSON listesi denemesi
            try {
                val prefs = AppPreferences.getInstance(context)
                val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
                val targetUrl = URL("$CLOUD_WORKER_URL/api/sync?code=$familyCode")
                val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
                val syncRes = json.decodeFromString<CloudSyncResponse>(responseText)
                Result.success(syncRes.data?.messages ?: emptyList())
            } catch (ex: Exception) {
                Log.e(TAG, "fetchMessages hatası: ${ex.message}", ex)
                Result.failure(ex)
            }
        }
    }

    /**
     * Arkaplanda (5 dakikada bir) çalışarak yeni okunmamış veli mesajlarını Android sistem bildirimi olarak gösterir.
     */
    suspend fun checkAndDeliverPendingNotifications(context: Context): Int = withContext(Dispatchers.IO) {
        var deliveredCount = 0
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
            val lastNotifiedTime = prefs.lastNotifiedMessageTime

            // Cloudflare'den en son veriyi çek
            val cloudRes = fetchCloudData(context)
            if (cloudRes.isFailure) return@withContext 0

            val cloudData = cloudRes.getOrNull() ?: return@withContext 0
            val messages = cloudData.messages

            val unreadNewMessages = messages.filter { msg ->
                !msg.isRead && msg.timestamp > lastNotifiedTime && msg.senderRole != "CHILD"
            }.sortedBy { it.timestamp }

            for (msg in unreadNewMessages) {
                // Bildirim göster
                com.studytracker.core.notification.StudyNotificationManager.showParentNudgeNotification(context, msg)
                deliveredCount++

                if (msg.timestamp > prefs.lastNotifiedMessageTime) {
                    prefs.lastNotifiedMessageTime = msg.timestamp
                }
                prefs.setLastUnreadMessage(json.encodeToString(msg))
            }

            if (deliveredCount > 0) {
                Log.d(TAG, "✅ $deliveredCount yeni veli bildirimi öğrenciye teslim edildi.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAndDeliverPendingNotifications hatası: ${e.message}", e)
        }
        deliveredCount
    }

    /**
     * Mesajı okundu olarak işaretler.
     */
    suspend fun markMessageAsRead(context: Context, messageId: String? = null, all: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }
            val targetUrl = URL("$CLOUD_WORKER_URL/api/messages")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 6000
                readTimeout = 6000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", familyCode)
            }

            val body = if (all) "{\"familyCode\": \"$familyCode\", \"all\": true}" else "{\"familyCode\": \"$familyCode\", \"messageId\": \"$messageId\"}"
            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body); it.flush() }

            prefs.clearLastUnreadMessage()
            conn.responseCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "markMessageAsRead hatası: ${e.message}", e)
            false
        }
    }
}
