package com.studytracker.core.data.local.driver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.Image
import android.media.ImageReader
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.repository.toEntity
import com.studytracker.core.domain.model.Screenshot
import com.studytracker.core.domain.model.UploadStatus
import com.studytracker.core.domain.repository.CaptureDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RealMediaProjectionCaptureDriver(
    private val context: Context,
    private val db: AppDatabase
) : CaptureDriver {

    private var activeSessionId: String? = null
    private var activeOccurrenceKey: String? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun start(sessionId: String, occurrenceKey: String) {
        activeSessionId = sessionId
        activeOccurrenceKey = occurrenceKey
        MediaProjectionHolder.getOrCreateVirtualDisplay(context)
    }

    override suspend fun captureNow(): Screenshot = withContext(Dispatchers.IO) {
        val sessionId = activeSessionId ?: "sess_unknown"
        val occurrenceKey = activeOccurrenceKey ?: "task_unknown"
        val timestamp = System.currentTimeMillis()
        val screenshotId = "real_ss_" + UUID.randomUUID().toString().take(8)

        val reader = MediaProjectionHolder.getOrCreateVirtualDisplay(context)
        var bitmap: Bitmap? = null

        if (reader != null) {
            // Attempt to acquire latest image from projection
            var image: Image? = null
            for (i in 0 until 5) {
                image = reader.acquireLatestImage()
                if (image != null) break
                delay(40)
            }

            if (image != null) {
                try {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val width = reader.width
                    val height = reader.height
                    val rowPadding = rowStride - pixelStride * width

                    val tempBitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride,
                        height,
                        Bitmap.Config.ARGB_8888
                    )
                    tempBitmap.copyPixelsFromBuffer(buffer)

                    bitmap = if (rowPadding > 0) {
                        val cropped = Bitmap.createBitmap(tempBitmap, 0, 0, width, height)
                        tempBitmap.recycle()
                        cropped
                    } else {
                        tempBitmap
                    }
                } catch (_: Exception) {
                    bitmap = null
                } finally {
                    image.close()
                }
            }
        }

        // Fallback banner bitmap if MediaProjection permission was not yet initiated
        if (bitmap == null) {
            val width = MediaProjectionHolder.screenWidth.coerceAtLeast(720)
            val height = MediaProjectionHolder.screenHeight.coerceAtLeast(1280)
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(15, 23, 42)) // Slate Dark

            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(239, 68, 68) // Red Warning
                textSize = 42f
                isFakeBoldText = true
            }

            val subPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(203, 213, 225)
                textSize = 28f
            }

            canvas.drawText("📸 GERÇEK EKRAN YAKALAMA", 50f, 150f, paint)
            paint.color = Color.WHITE
            canvas.drawText("Oturum: $sessionId", 50f, 230f, subPaint)
            canvas.drawText("Görev: $occurrenceKey", 50f, 290f, subPaint)
            canvas.drawText("Tarih: ${dateFormat.format(Date(timestamp))}", 50f, 350f, subPaint)

            canvas.drawText("⚠️ Ekran Kaydı İzni Alındığında", 50f, 500f, subPaint)
            canvas.drawText("Bu alanda gerçek cihaz ekran görüntüsü yer alacaktır.", 50f, 550f, subPaint)
        }

        val fileDir = File(context.filesDir, "screenshots")
        if (!fileDir.exists()) fileDir.mkdirs()

        val file = File(fileDir, "$screenshotId.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
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
        MediaProjectionHolder.release()
        return finalScreenshot
    }
}
