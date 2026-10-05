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
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL

object V2CloudClient {

    private const val TAG = "V2CloudClient"
    private var customBaseUrl: String? = null

    val CLOUD_BASE_URL: String
        get() = customBaseUrl ?: com.studytracker.BuildConfig.V2_BASE_URL

    fun setCustomBaseUrl(url: String?) {
        customBaseUrl = url?.trim()?.removeSuffix("/")
    }

    fun resetBaseUrl() {
        customBaseUrl = null
    }

    private const val MAX_TRANSIENT_ATTEMPTS = 3
    private val transientRetryDelaysMs = longArrayOf(250L, 750L)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    private fun isTransientNetworkFailure(error: Throwable): Boolean =
        error is SocketException ||
            error is SocketTimeoutException ||
            error.cause?.let(::isTransientNetworkFailure) == true

    private suspend fun <T> withTransientNetworkRetry(
        operation: String,
        block: () -> T
    ): T {
        var lastError: Throwable? = null
        repeat(MAX_TRANSIENT_ATTEMPTS) { attempt ->
            try {
                return block()
            } catch (error: Throwable) {
                lastError = error
                val retryable = isTransientNetworkFailure(error)
                Log.w(
                    TAG,
                    "$operation failed attempt ${attempt + 1}/$MAX_TRANSIENT_ATTEMPTS " +
                        "(${error.javaClass.simpleName}: ${error.message}), retryable=$retryable"
                )
                if (!retryable || attempt == MAX_TRANSIENT_ATTEMPTS - 1) throw error
                kotlinx.coroutines.delay(transientRetryDelaysMs[attempt])
            }
        }
        throw lastError ?: IllegalStateException("$operation failed")
    }

    suspend fun fetchCatalog(
        familyCode: String,
        adminToken: String? = null,
        role: String = "CLIENT"
    ): Result<List<V2CourseDto>> = withContext(Dispatchers.IO) {
        val cleanCode = familyCode.trim().uppercase()
        val targetUrl = URL("$CLOUD_BASE_URL/api/v3/catalog?code=$cleanCode")
        try {
            withTransientNetworkRetry("catalog GET ${targetUrl.host}") {
                val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    useCaches = false
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Connection", "close")
                    setRequestProperty("X-Family-Code", cleanCode)
                    setRequestProperty("X-Sender-Role", role)
                    if (!adminToken.isNullOrBlank()) {
                        setRequestProperty("X-Admin-Token", adminToken)
                    }
                }
                try {
                    val responseCode = conn.responseCode
                    if (responseCode !in 200..299) {
                        val errorStream = conn.errorStream
                            ?.let { BufferedReader(InputStreamReader(it, "UTF-8")).readText() }
                            ?: "HTTP $responseCode"
                        throw IllegalStateException("V2 Catalog GET Hatası ($responseCode): $errorStream")
                    }
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                    val catalogRes = json.decodeFromString<V2CatalogResponseDto>(responseText)
                    if (!catalogRes.success) {
                        throw IllegalStateException(catalogRes.error ?: "V2 Kataloğu sunucudan alınamadı")
                    }
                    catalogRes.curriculum
                } finally {
                    conn.disconnect()
                }
            }.let { Result.success(it) }
        } catch (e: Exception) {
            Log.e(TAG, "fetchCatalog failed host=${targetUrl.host} path=${targetUrl.path}: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun importCatalog(
        familyCode: String,
        catalogJson: String,
        adminToken: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = familyCode.trim().uppercase()
            val targetUrl = URL("$CLOUD_BASE_URL/api/v3/catalog/import?code=$cleanCode")
            val conn = (targetUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("X-Family-Code", cleanCode)
                setRequestProperty("X-Sender-Role", "PARENT")
                setRequestProperty("X-Admin-Token", adminToken)
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(catalogJson) }

            val responseCode = conn.responseCode
            val responseText = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).readText()
            } else {
                conn.errorStream?.let { BufferedReader(InputStreamReader(it, "UTF-8")).readText() }
                    ?: "HTTP $responseCode"
            }

            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception("V2 katalog yükleme hatası ($responseCode): $responseText"))
            }

            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "importCatalog failed: ${e.message}", e)
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
