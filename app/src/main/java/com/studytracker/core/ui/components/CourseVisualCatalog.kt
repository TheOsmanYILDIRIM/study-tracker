package com.studytracker.core.ui.components

import com.studytracker.core.domain.model.CourseVisual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private const val COURSE_VISUAL_CATALOG_URL =
    "https://raw.githubusercontent.com/TheOsmanYILDIRIM/study-tracker/d1130eb2c785791bd2f389f762fbf65ff9cfd8d4/content/v2/course-visuals.json"

private val courseVisualJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

private val courseVisualMapSerializer =
    MapSerializer(String.serializer(), CourseVisual.serializer())

fun parseCourseVisualCatalog(raw: String?): Map<String, CourseVisual> {
    if (raw.isNullOrBlank()) return emptyMap()
    return runCatching {
        courseVisualJson.decodeFromString(courseVisualMapSerializer, raw)
    }.getOrDefault(emptyMap())
}

suspend fun fetchCourseVisualCatalog(): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(COURSE_VISUAL_CATALOG_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 4_000
            readTimeout = 4_000
            requestMethod = "GET"
            useCaches = true
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) {
                error("Course visual catalog HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
