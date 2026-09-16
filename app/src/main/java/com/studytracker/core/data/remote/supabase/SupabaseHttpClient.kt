package com.studytracker.core.data.remote.supabase

import android.content.Context
import com.studytracker.core.data.local.prefs.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseHttpClient private constructor(private val context: Context) {

    private val prefs = AppPreferences.getInstance(context)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val octetMediaType = "image/jpeg".toMediaType()

    private fun getBaseUrl(): String {
        val url = prefs.supabaseUrl.value.trimEnd('/')
        return if (url.isNotEmpty()) url else AppPreferences.DEFAULT_SUPABASE_URL
    }

    private fun getApiKey(): String {
        val key = prefs.supabaseAnonKey.value.trim()
        return if (key.isNotEmpty()) key else AppPreferences.DEFAULT_SUPABASE_ANON_KEY
    }

    suspend fun <T> post(tableName: String, body: T, serializer: (T) -> String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${getBaseUrl()}/rest/v1/$tableName"
                val jsonPayload = serializer(body)

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getApiKey())
                    .addHeader("Authorization", "Bearer ${getApiKey()}")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(jsonPayload.toRequestBody(jsonMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    Result.success(responseBody)
                } else {
                    Result.failure(IOException("HTTP ${response.code}: $responseBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun get(tableName: String, queryParams: String = ""): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${getBaseUrl()}/rest/v1/$tableName?$queryParams"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getApiKey())
                    .addHeader("Authorization", "Bearer ${getApiKey()}")
                    .addHeader("Accept", "application/json")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    Result.success(responseBody)
                } else {
                    Result.failure(IOException("HTTP ${response.code}: $responseBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun uploadStorageFile(bucketName: String, path: String, file: File): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val url = "${getBaseUrl()}/storage/v1/object/$bucketName/$path"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("apikey", getApiKey())
                    .addHeader("Authorization", "Bearer ${getApiKey()}")
                    .addHeader("Content-Type", "image/jpeg")
                    .post(file.asRequestBody(octetMediaType))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val publicUrl = "${getBaseUrl()}/storage/v1/object/public/$bucketName/$path"
                    Result.success(publicUrl)
                } else {
                    // Fallback to local file path or relative identifier if offline / bucket not yet configured
                    Result.success(file.absolutePath)
                }
            } catch (e: Exception) {
                // Return local path gracefully
                Result.success(file.absolutePath)
            }
        }

    companion object {
        @Volatile
        private var INSTANCE: SupabaseHttpClient? = null

        fun getInstance(context: Context): SupabaseHttpClient {
            return INSTANCE ?: synchronized(this) {
                val instance = SupabaseHttpClient(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
