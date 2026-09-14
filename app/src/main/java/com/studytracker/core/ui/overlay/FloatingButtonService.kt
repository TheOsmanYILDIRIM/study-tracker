package com.studytracker.core.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.*
import com.studytracker.core.domain.manager.SessionStateManager
import com.studytracker.core.ui.theme.StudyTrackerTheme

class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class FloatingButtonService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private lateinit var stateManager: SessionStateManager

    override fun onCreate() {
        super.onCreate()
        stateManager = SessionStateManager.getInstance(this)
        startForegroundServiceNotification()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            showFloatingWindow()
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "study_session_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Aktif Ders Oturumu",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ders Takibi Aktif")
            .setContentText("Çalışma oturumu devam ediyor. Kanıt ekran görüntüleri alınıyor.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun showFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }

        val lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner.onCreate()
        overlayLifecycleOwner = lifecycleOwner

        val composeView = ComposeView(this).apply {
            ViewTreeLifecycleOwner.set(this, lifecycleOwner)
            ViewTreeSavedStateRegistryOwner.set(this, lifecycleOwner)
            ViewTreeViewModelStoreOwner.set(this, lifecycleOwner)

            setContent {
                StudyTrackerTheme {
                    val activeState by stateManager.activeState.collectAsState()
                    val state = activeState
                    if (state != null) {
                        FloatingHUDView(
                            elapsedSeconds = state.elapsedSeconds,
                            screenshotCount = state.screenshotCount,
                            isFinishing = state.isFinishing,
                            onSingleTapCapture = {
                                stateManager.captureManual()
                            },
                            onLongPressFinish = {
                                stateManager.finishSession()
                            }
                        )
                    }
                }
            }
        }

        // Drag listener for magnetic positioning
        composeView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return false // Allow compose tap gestures
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager?.updateViewLayout(composeView, params)
                            return true
                        }
                    }
                }
                return false
            }
        })

        floatingView = composeView
        windowManager?.addView(composeView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayLifecycleOwner?.onDestroy()
        overlayLifecycleOwner = null
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
