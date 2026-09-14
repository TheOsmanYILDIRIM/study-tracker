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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FakeCaptureDriver(
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
        val screenshotId = "fake_ss_" + UUID.randomUUID().toString().take(8)

        // 1. Create a 720x1280 lightweight simulated screenshot bitmap
        val width = 720
        val height = 1280
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)

        // Background gradient / dark surface
        canvas.drawColor(Color.rgb(20, 24, 33))

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 36f
        }

        val headerPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(96, 165, 250) // Sapphire Accent
            textSize = 48f
            isFakeBoldText = true
        }

        val subPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(156, 163, 175)
            textSize = 28f
        }

        canvas.drawText("StudyTracker TEST EKRANI", 60f, 150f, headerPaint)
        canvas.drawText("Oturum ID: $sessionId", 60f, 240f, paint)
        canvas.drawText("Görev: $occurrenceKey", 60f, 310f, paint)
        canvas.drawText("Zaman: ${dateFormat.format(Date(timestamp))}", 60f, 380f, subPaint)

        // Simulated Study Screen Mock Card
        val cardPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(50f, 450f, 670f, 900f, 24f, 24f, cardPaint)

        val cardTextPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            textSize = 32f
        }
        canvas.drawText("📚 Ders Çalışma Simülasyonu", 90f, 540f, cardTextPaint)
        canvas.drawText("• Konu: Soru Çözümü & Video", 90f, 620f, subPaint)
        canvas.drawText("• Odaklanma Modu: AKTİF", 90f, 700f, subPaint)
        canvas.drawText("• Durum: Doğrulanmış Test Kanıtı", 90f, 780f, subPaint)

        // Save JPEG
        val fileDir = File(context.cacheDir, "test_screenshots")
        if (!fileDir.exists()) fileDir.mkdirs()

        val file = File(fileDir, "$screenshotId.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out)
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
