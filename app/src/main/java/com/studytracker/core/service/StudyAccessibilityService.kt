package com.studytracker.core.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.os.Build
import android.provider.Settings
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class StudyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.flags = info.flags or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            serviceInfo = info
        } catch (e: Exception) {
            android.util.Log.e("StudyAccessibility", "Error configuring serviceInfo", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event processing needed, service is used for background screenshot capture
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    suspend fun captureScreenBitmap(): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return null
        }

        // Up to 3 attempts to tolerate window transitions and rate limits
        for (attempt in 1..3) {
            val resultBitmap = withTimeoutOrNull(2500L) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        takeScreenshot(
                            Display.DEFAULT_DISPLAY,
                            ContextCompat.getMainExecutor(this@StudyAccessibilityService),
                            object : TakeScreenshotCallback {
                                override fun onSuccess(screenshotResult: ScreenshotResult) {
                                    try {
                                        val hardwareBuffer = screenshotResult.hardwareBuffer
                                        val colorSpace = screenshotResult.colorSpace
                                        val softwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && colorSpace != null) {
                                            Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                                                ?.copy(Bitmap.Config.ARGB_8888, false)
                                        } else {
                                            Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
                                                ?.copy(Bitmap.Config.ARGB_8888, false)
                                        }
                                        hardwareBuffer.close()
                                        if (continuation.isActive) {
                                            continuation.resume(softwareBitmap)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.w("StudyAccessibility", "Buffer copy failed", e)
                                        if (continuation.isActive) {
                                            continuation.resume(null)
                                        }
                                    }
                                }

                                override fun onFailure(errorCode: Int) {
                                    android.util.Log.w("StudyAccessibility", "takeScreenshot onFailure errorCode=$errorCode attempt=$attempt")
                                    if (continuation.isActive) {
                                        continuation.resume(null)
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.w("StudyAccessibility", "takeScreenshot exception", e)
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }
            }

            if (resultBitmap != null) {
                return resultBitmap
            }

            if (attempt < 3) {
                delay(350L)
            }
        }
        return null
    }

    companion object {
        @Volatile
        var instance: StudyAccessibilityService? = null
            private set

        fun isServiceRunning(): Boolean {
            return instance != null
        }

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (instance != null) return true

            val expectedServiceName = "${context.packageName}/${StudyAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(':').any {
                it.equals(expectedServiceName, ignoreCase = true) ||
                it.contains(StudyAccessibilityService::class.java.simpleName, ignoreCase = true) ||
                it.contains("StudyAccessibilityService", ignoreCase = true)
            }
        }

        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "👉 Yüklü Uygulamalar / İndirilen Servisler -> StudyTracker'ı Açık yapın.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }
}
