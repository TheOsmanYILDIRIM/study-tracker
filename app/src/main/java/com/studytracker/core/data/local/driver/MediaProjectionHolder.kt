package com.studytracker.core.data.local.driver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

object MediaProjectionHolder {

    var mediaProjectionResultCode: Int? = null
    var mediaProjectionData: Intent? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    var screenWidth: Int = 720
    var screenHeight: Int = 1280
    var screenDensity: Int = 320

    fun hasPermission(): Boolean {
        return mediaProjectionResultCode != null && mediaProjectionData != null
    }

    fun setPermissionResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            mediaProjectionResultCode = resultCode
            mediaProjectionData = data
        }
    }

    fun initDisplayMetrics(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            val realWidth = bounds.width()
            val realHeight = bounds.height()
            // Scale down to 720p for fast capture and low memory
            val scale = 720f / Math.min(realWidth, realHeight).coerceAtLeast(1)
            screenWidth = (realWidth * scale).toInt().coerceAtLeast(480)
            screenHeight = (realHeight * scale).toInt().coerceAtLeast(800)
            screenDensity = context.resources.displayMetrics.densityDpi
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            val scale = 720f / Math.min(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(1)
            screenWidth = (metrics.widthPixels * scale).toInt().coerceAtLeast(480)
            screenHeight = (metrics.heightPixels * scale).toInt().coerceAtLeast(800)
            screenDensity = metrics.densityDpi
        }
    }

    @Synchronized
    fun getOrCreateVirtualDisplay(context: Context): ImageReader? {
        if (imageReader != null && virtualDisplay != null && mediaProjection != null) {
            return imageReader
        }

        val resultCode = mediaProjectionResultCode ?: return null
        val data = mediaProjectionData ?: return null

        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val projection = projectionManager.getMediaProjection(resultCode, data) ?: return null
        mediaProjection = projection

        initDisplayMetrics(context)

        val reader = ImageReader.newInstance(screenWidth, screenHeight, android.graphics.PixelFormat.RGBA_8888, 2)
        imageReader = reader

        val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        virtualDisplay = projection.createVirtualDisplay(
            "StudyTrackerScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            flags,
            reader.surface,
            null,
            null
        )

        return reader
    }

    @Synchronized
    fun release() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (_: Exception) {}
    }
}
