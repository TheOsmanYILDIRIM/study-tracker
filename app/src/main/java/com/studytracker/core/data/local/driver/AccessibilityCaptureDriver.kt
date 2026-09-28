package com.studytracker.core.data.local.driver

import android.content.Context
import android.graphics.Bitmap
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.toEntity
import com.studytracker.core.domain.model.Screenshot
import com.studytracker.core.domain.model.UploadStatus
import com.studytracker.core.domain.repository.CaptureDriver
import com.studytracker.core.service.StudyAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AccessibilityCaptureDriver(
    private val context: Context,
    private val db: AppDatabase
) : CaptureDriver {

    private var activeSessionId: String? = null
    private var activeOccurrenceKey: String? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun start(sessionId: String, occurrenceKey: String) {
        activeSessionId = sessionId
        activeOccurrenceKey = occurrenceKey
    }

    override suspend fun captureNow(): Screenshot = withContext(Dispatchers.IO) {
        val sessionId = activeSessionId ?: "sess_unknown"
        val occurrenceKey = activeOccurrenceKey ?: "task_unknown"
        val timestamp = System.currentTimeMillis()
        val screenshotId = "acc_ss_" + UUID.randomUUID().toString().take(8)

        var bitmap: Bitmap? = null

        // Try silent capture from StudyAccessibilityService
        var service = StudyAccessibilityService.instance
        if (service == null && StudyAccessibilityService.isAccessibilityServiceEnabled(context)) {
            kotlinx.coroutines.delay(350)
            service = StudyAccessibilityService.instance
        }

        if (service != null) {
            try {
                bitmap = service.captureScreenBitmap()
            } catch (e: Exception) {
                android.util.Log.w("AccessibilityCaptureDriver", "Silent capture error", e)
                bitmap = null
            }
        }

        if (bitmap == null) {
            throw IllegalStateException("Gerçek ekran görüntüsü alınamadı; sentetik kanıt üretilmedi")
        }

        val fileDir = File(context.filesDir, "screenshots")
        if (!fileDir.exists()) fileDir.mkdirs()

        val file = File(fileDir, "$screenshotId.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 65, out)
        }
        bitmap.recycle()

        val screenshot = Screenshot(
            screenshotId = screenshotId,
            sessionId = sessionId,
            occurrenceKey = occurrenceKey,
            capturedAt = timestamp,
            url = file.absolutePath,
            sizeKb = (file.length() / 1024).toInt(),
            uploadStatus = UploadStatus.UPLOADED
        )

        db.screenshotDao().insertScreenshot(screenshot.toEntity())
        return@withContext screenshot
    }

    override suspend fun stop(): Screenshot? {
        return try {
            captureNow()
        } catch (e: Exception) {
            android.util.Log.w("AccessibilityCaptureDriver", "Final capture unavailable", e)
            null
        } finally {
            activeSessionId = null
            activeOccurrenceKey = null
        }
    }
}
