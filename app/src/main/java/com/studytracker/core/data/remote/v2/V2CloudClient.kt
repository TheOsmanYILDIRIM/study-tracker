package com.studytracker.core.data.remote.v2

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object V2CloudClient {

    private const val TAG = "V2CloudClient"
    const val CLOUD_BASE_URL = "https://studytracker-sync.osman13241429.workers.dev"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun fetchCatalog(
        familyCode: String,
        adminToken: String? = null,
        role: String = "CLIENT"
    ): Result<List<V2CourseDto>> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = familyCode.trim().uppercase()
            val targetUrl = URL("$CLOUD_BASE_URL/api/v3/catalog?code=$cleanCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", cleanCode)
                setRequestProperty("X-Sender-Role", role)
                if (!adminToken.isNullOrBlank()) {
                    setRequestProperty("X-Admin-Token", adminToken)
                }
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("V2 Catalog GET Hatası ($responseCode): $errorStream"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val catalogRes = json.decodeFromString<V2CatalogResponseDto>(responseText)

            if (!catalogRes.success) {
                return@withContext Result.failure(Exception(catalogRes.error ?: "V2 Kataloğu sunucudan alınamadı"))
            }

            Result.success(catalogRes.curriculum)
        } catch (e: Exception) {
            Log.e(TAG, "fetchCatalog failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun recordAttempt(
        familyCode: String,
        attemptRequest: V2AttemptRequestDto,
        adminToken: String? = null,
        role: String = "CLIENT"
    ): Result<V2AttemptResponseDto> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = familyCode.trim().uppercase()
            val targetUrl = URL("$CLOUD_BASE_URL/api/v3/attempts?code=$cleanCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", cleanCode)
                setRequestProperty("X-Sender-Role", role)
                if (!adminToken.isNullOrBlank()) {
                    setRequestProperty("X-Admin-Token", adminToken)
                }
            }

            val payloadJson = json.encodeToString(attemptRequest)
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payloadJson)
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("V2 Attempt POST Hatası ($responseCode): $errorStream"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val attemptRes = json.decodeFromString<V2AttemptResponseDto>(responseText)

            if (!attemptRes.success) {
                return@withContext Result.failure(Exception(attemptRes.error ?: "Deneme kaydı başarısız"))
            }

            Result.success(attemptRes)
        } catch (e: Exception) {
            Log.e(TAG, "recordAttempt failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAttempts(
        familyCode: String,
        studentId: String = "student_default",
        limit: Int = 100,
        role: String = "CLIENT"
    ): Result<List<V2AttemptDto>> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = familyCode.trim().uppercase()
            val targetUrl = URL("$CLOUD_BASE_URL/api/v3/attempts?code=$cleanCode&studentId=$studentId&limit=$limit")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", cleanCode)
                setRequestProperty("X-Sender-Role", role)
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errorStream = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: "HTTP $responseCode"
                return@withContext Result.failure(Exception("V2 Attempts GET Hatası ($responseCode): $errorStream"))
            }

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            val attemptsRes = json.decodeFromString<V2AttemptsListResponseDto>(responseText)

            if (!attemptsRes.success) {
                return@withContext Result.failure(Exception(attemptsRes.error ?: "Öğrenci denemeleri alınamadı"))
            }

            Result.success(attemptsRes.attempts)
        } catch (e: Exception) {
            Log.e(TAG, "fetchAttempts failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
