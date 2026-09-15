package com.studytracker.core.domain.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Immutable
import com.studytracker.core.data.local.db.AppDatabase
import com.studytracker.core.data.local.driver.AccessibilityCaptureDriver
import com.studytracker.core.data.local.driver.FakeCaptureDriver
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.core.data.local.repository.LocalOccurrenceRepositoryImpl
import com.studytracker.core.data.local.repository.LocalSessionRepositoryImpl
import com.studytracker.core.domain.model.Session
import com.studytracker.core.domain.repository.CaptureDriver
import com.studytracker.core.ui.overlay.FloatingButtonService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@Immutable
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
    val appPreferences = AppPreferences.getInstance(context)

    val sessionRepository = LocalSessionRepositoryImpl(db)
    val occurrenceRepository = LocalOccurrenceRepositoryImpl(db)

    private val fakeCaptureDriver = FakeCaptureDriver(context, db)
    private val accessibilityCaptureDriver = AccessibilityCaptureDriver(context, db)

    fun getEffectiveCaptureDriver(): CaptureDriver {
        return if (appPreferences.isFakeCaptureEnabled.value) {
            fakeCaptureDriver
        } else {
            accessibilityCaptureDriver
        }
    }

    private val _activeState = MutableStateFlow<ActiveSessionState?>(null)
    val activeState: StateFlow<ActiveSessionState?> = _activeState.asStateFlow()

    // Dedicated high-performance boolean state flow to prevent recomposition storms on root screens
    val isSessionActive: StateFlow<Boolean> = _activeState
        .map { it != null }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, false)

    private var tickerJob: Job? = null
    private var periodicCaptureJob: Job? = null

    fun startSession(occurrenceKey: String, occurrenceTitle: String, childId: String = "child_1") {
        if (_activeState.value != null) return // Already active

        val tempSessionId = "sess_" + java.util.UUID.randomUUID().toString().take(8)
        val tempSession = Session(
            sessionId = tempSessionId,
            occurrenceKey = occurrenceKey,
            childId = childId,
            startTime = System.currentTimeMillis(),
            status = com.studytracker.core.domain.model.SessionStatus.ACTIVE
        )

        // 1. Instant Optimistic UI State Transition (0ms latency touch response)
        _activeState.value = ActiveSessionState(
            session = tempSession,
            occurrenceTitle = occurrenceTitle,
            elapsedSeconds = 0,
            screenshotCount = 1,
            isPaused = false
        )

        startFloatingService()
        startTicker()
        startPeriodicCapture()

        // 2. Perform DB write & screenshot driver setup asynchronously in IO
        scope.launch(Dispatchers.IO) {
            val session = sessionRepository.startSession(occurrenceKey, childId)
            val driver = getEffectiveCaptureDriver()
            driver.start(session.sessionId, occurrenceKey)

            // Update with persisted session if ID differed
            if (session.sessionId != tempSessionId) {
                withContext(Dispatchers.Main) {
                    _activeState.value = _activeState.value?.copy(session = session)
                }
            }

            // Capture initial evidence photo asynchronously
            driver.captureNow()
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
        val current = _activeState.value ?: return
        // Instant visual feedback
        _activeState.value = current.copy(
            screenshotCount = current.screenshotCount + 1
        )
        // Background capture
        scope.launch(Dispatchers.IO) {
            getEffectiveCaptureDriver().captureNow()
        }
    }

    fun finishSession(onFinished: (() -> Unit)? = null) {
        val current = _activeState.value ?: return
        _activeState.value = current.copy(isFinishing = true)

        stopPeriodicCapture()
        stopTicker()
        stopFloatingService()

        scope.launch(Dispatchers.IO) {
            // Final screenshot
            val finalSs = getEffectiveCaptureDriver().stop()

            // Update session in DB
            sessionRepository.finishSession(current.session.sessionId, finalSs?.url)

            withContext(Dispatchers.Main) {
                _activeState.value = null
                onFinished?.invoke()
            }
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
        periodicCaptureJob = scope.launch(Dispatchers.IO) {
            while (isActive && _activeState.value != null) {
                delay(60_000) // Her 60 saniyede bir otomatik screenshot
                val current = _activeState.value
                if (current != null && !current.isPaused) {
                    getEffectiveCaptureDriver().captureNow()
                    withContext(Dispatchers.Main) {
                        _activeState.value = _activeState.value?.let { it.copy(screenshotCount = it.screenshotCount + 1) }
                    }
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
        try {
            val intent = Intent(context, FloatingButtonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun stopFloatingService() {
        try {
            val intent = Intent(context, FloatingButtonService::class.java)
            context.stopService(intent)
        } catch (_: Exception) {}
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

