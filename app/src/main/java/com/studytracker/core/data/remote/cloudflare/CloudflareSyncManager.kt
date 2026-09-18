package com.studytracker.core.data.remote.cloudflare

import android.content.Context
import android.util.Log
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.package_exchange.StudyPackageExchangeManager
import com.studytracker.core.data.remote.sync.*
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
    val plan: LocalPlanSyncDto? = null,
    val tasks: List<LocalTaskTemplateSyncDto> = emptyList(),
    val occurrences: List<RemoteOccurrenceSyncDto> = emptyList(),
    val sessions: List<RemoteSessionSyncDto> = emptyList(),
    val reviews: List<RemoteReviewSyncDto> = emptyList()
)

object CloudflareSyncManager {

    private const val TAG = "CloudflareSync"
    const val CLOUD_WORKER_URL = "https://studytracker-sync.osman13241429.workers.dev"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    /**
     * Uçtan uca çift yönlü Cloudflare KV senkronizasyonu (Veli <-> Öğrenci).
     * Yerel Room DB değişikliklerini buluta yükler ve buluttaki en güncel değişiklikleri indirip birleştirir.
     */
    suspend fun syncWithCloud(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val prefs = AppPreferences.getInstance(context)
            val familyCode = prefs.familyPairCode.value.ifBlank { "ST-2026" }

            // 1. Yerel verileri topla
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
                    isCompleted = it.status != com.studytracker.core.domain.model.SessionStatus.ACTIVE,
                    notes = it.studentNote ?: ""
                )
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

            val payload = SharedFamilySyncPayload(
                familyCode = familyCode,
                plan = plan,
                tasks = tasks,
                occurrences = occurrences,
                sessions = sessions,
                reviews = reviews
            )

            val payloadJson = json.encodeToString(payload)

            // 2. Cloudflare Worker POST isteği gönder
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

            // 3. Buluttan dönen birleştirilmiş verileri yerel Room DB'ye aktar
            val cloudData = syncRes.data
            val studyPackage = com.studytracker.core.data.package_exchange.StudyTrackerPackage(
                familyCode = familyCode,
                plan = cloudData.plan,
                tasks = cloudData.tasks,
                occurrences = cloudData.occurrences,
                sessions = cloudData.sessions,
                reviews = cloudData.reviews
            )

            StudyPackageExchangeManager.importPackageString(context, json.encodeToString(studyPackage))

            Result.success("Bulut Senkronizasyonu Başarılı ($familyCode)")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
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
}
