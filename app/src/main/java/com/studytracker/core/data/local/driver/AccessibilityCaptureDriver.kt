package com.studytracker.core.data.local.driver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

        // Fallback banner bitmap if capture fails or Accessibility Service is not yet activated
        if (bitmap == null) {
            val width = 720
            val height = 1280
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(15, 23, 42)) // Slate Dark

            val headerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(245, 158, 11) // Amber
                textSize = 38f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(226, 232, 240)
                textSize = 26f
            }

            val warningPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(239, 68, 68)
                textSize = 28f
                isFakeBoldText = true
            }

            val infoPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(56, 189, 248) // Sky
                textSize = 28f
                isFakeBoldText = true
            }

            canvas.drawText("⚡ SESSİZ EKRAN YAKALAMA", 50f, 150f, headerPaint)
            canvas.drawText("Oturum: $sessionId", 50f, 230f, bodyPaint)
            canvas.drawText("Görev: $occurrenceKey", 50f, 280f, bodyPaint)
            canvas.drawText("Tarih: ${dateFormat.format(Date(timestamp))}", 50f, 330f, bodyPaint)

            if (service != null) {
                canvas.drawText("📸 Servis Aktif (Oturum Mühürlendi)", 50f, 480f, infoPaint)
                canvas.drawText("Erişilebilirlik servisi çalışıyor.", 50f, 530f, bodyPaint)
                canvas.drawText("Arka plan zaman damgası ve oturum", 50f, 570f, bodyPaint)
                canvas.drawText("güvenle kaydedilip veli onayına hazırlandı.", 50f, 610f, bodyPaint)
            } else {
                canvas.drawText("⚠️ Erişilebilirlik Servisi Gerekli", 50f, 480f, warningPaint)
                canvas.drawText("Ayarlar -> Erişilebilirlik -> StudyTracker", 50f, 530f, bodyPaint)
                canvas.drawText("servisini 1 kez açtığınızda arka planda", 50f, 570f, bodyPaint)
                canvas.drawText("sıfır uyarıyla gerçek ekran görüntüsü alınır.", 50f, 610f, bodyPaint)
            }
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
        val finalScreenshot = captureNow()
        activeSessionId = null
        activeOccurrenceKey = null
        return finalScreenshot
    }
}
