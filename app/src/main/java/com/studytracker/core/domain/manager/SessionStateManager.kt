package com.studytracker.core.domain.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.driver.FakeCaptureDriver
import com.studytracker.core.data.local.repository.LocalOccurrenceRepositoryImpl
import com.studytracker.core.data.local.repository.LocalSessionRepositoryImpl
import com.studytracker.core.domain.model.Session
import com.studytracker.core.domain.repository.CaptureDriver
import com.studytracker.core.ui.overlay.FloatingButtonService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveSessionState(
    val session: Session,
    val occurrenceTitle: String,
    val elapsedSeconds: Long = 0,
    val screenshotCount: Int = 0,
    val isFinishing: Boolean = false,
    val isPaused: Boolean = false
)

class SessionStateManager private constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val db = AppDatabase.getInstance(context)
    val sessionRepository = LocalSessionRepositoryImpl(db)
    val occurrenceRepository = LocalOccurrenceRepositoryImpl(db)

    // Default to FakeCaptureDriver for test mode / out-of-box operation
    var captureDriver: CaptureDriver = FakeCaptureDriver(context, db)

    private val _activeState = MutableStateFlow<ActiveSessionState?>(null)
    val activeState: StateFlow<ActiveSessionState?> = _activeState.asStateFlow()

    private var tickerJob: Job? = null
    private var periodicCaptureJob: Job? = null

    fun startSession(occurrenceKey: String, occurrenceTitle: String, childId: String = "child_1") {
        scope.launch {
            if (_activeState.value != null) return@launch // Zaten aktif session var

            val session = sessionRepository.startSession(occurrenceKey, childId)
            captureDriver.start(session.sessionId, occurrenceKey)

            // Initial capture
            val firstSs = captureDriver.captureNow()

            _activeState.value = ActiveSessionState(
                session = session,
                occurrenceTitle = occurrenceTitle,
                elapsedSeconds = 0,
                screenshotCount = 1,
                isPaused = false
            )

            startFloatingService()
            startTicker()
            startPeriodicCapture()
        }
    }

    fun pauseSession() {
        _activeState.value = _activeState.value?.copy(isPaused = true)
    }

    fun resumeSession() {
        _activeState.value = _activeState.value?.copy(isPaused = false)
    }

    fun togglePause() {
        _activeState.value = _activeState.value?.let {
            it.copy(isPaused = !it.isPaused)
        }
    }

    fun captureManual() {
        scope.launch {
            val current = _activeState.value ?: return@launch
            val ss = captureDriver.captureNow()
            _activeState.value = current.copy(
                screenshotCount = current.screenshotCount + 1
            )
        }
    }

    fun finishSession(onFinished: (() -> Unit)? = null) {
        scope.launch {
            val current = _activeState.value ?: return@launch
            _activeState.value = current.copy(isFinishing = true)

            // Final screenshot
            val finalSs = captureDriver.stop()

            // Update session in DB
            sessionRepository.finishSession(current.session.sessionId, finalSs?.url)

            stopPeriodicCapture()
            stopTicker()
            stopFloatingService()

            _activeState.value = null
            onFinished?.invoke()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _activeState.value != null) {
                delay(1000)
                _activeState.value = _activeState.value?.let { current ->
                    if (!current.isPaused) {
                        current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun startPeriodicCapture() {
        periodicCaptureJob?.cancel()
        periodicCaptureJob = scope.launch {
            while (isActive && _activeState.value != null) {
                delay(60_000) // Her 60 saniyede bir otomatik screenshot
                val current = _activeState.value
                if (current != null && !current.isPaused) {
                    val ss = captureDriver.captureNow()
                    _activeState.value = current.copy(screenshotCount = current.screenshotCount + 1)
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun stopPeriodicCapture() {
        periodicCaptureJob?.cancel()
        periodicCaptureJob = null
    }

    private fun startFloatingService() {
        val intent = Intent(context, FloatingButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopFloatingService() {
        val intent = Intent(context, FloatingButtonService::class.java)
        context.stopService(intent)
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionStateManager? = null

        fun getInstance(context: Context): SessionStateManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionStateManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
