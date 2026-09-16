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

    // Dedicated high-performance seconds ticker isolated from parent state recompositions
    private val _elapsedSeconds = MutableStateFlow<Long>(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

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
        _elapsedSeconds.value = 0L
        _activeState.value = ActiveSessionState(
            session = tempSession,
            occurrenceTitle = occurrenceTitle,
            screenshotCount = 1,
            isPaused = false
        )

        startFloatingService()
        startTicker()
        startPeriodicCapture()

        // 2. Perform DB write & screenshot driver setup asynchronously in IO
        scope.launch(Dispatchers.IO) {
            val session = sessionRepository.startSession(occurrenceKey, childId, tempSessionId)
            val driver = getEffectiveCaptureDriver()
            driver.start(session.sessionId, occurrenceKey)

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
        _activeState.value = current.copy(
            screenshotCount = current.screenshotCount + 1
        )
        scope.launch(Dispatchers.IO) {
            getEffectiveCaptureDriver().captureNow()
        }
    }

    fun cancelSession(onCancelled: (() -> Unit)? = null) {
        val current = _activeState.value ?: return
        _activeState.value = current.copy(isFinishing = true)

        stopPeriodicCapture()
        stopTicker()
        stopFloatingService()

        scope.launch(Dispatchers.IO) {
            getEffectiveCaptureDriver().stop()
            // Reset task status to PENDING so student can start whenever desired
            occurrenceRepository.updateStatus(
                current.session.occurrenceKey,
                com.studytracker.core.domain.model.OccurrenceStatus.PENDING
            )

            withContext(Dispatchers.Main) {
                _activeState.value = null
                _elapsedSeconds.value = 0L
                onCancelled?.invoke()
            }
        }
    }

    fun finishSession(studentNote: String? = null, onFinished: (() -> Unit)? = null) {
        val current = _activeState.value ?: return
        _activeState.value = current.copy(isFinishing = true)

        stopPeriodicCapture()
        stopTicker()
        stopFloatingService()

        scope.launch(Dispatchers.IO) {
            val finalSs = getEffectiveCaptureDriver().stop()
            sessionRepository.finishSession(current.session.sessionId, finalSs?.url, studentNote)

            // Auto sync to cloud in background
            try {
                com.studytracker.core.data.remote.sync.CloudSyncManager.getInstance(context).syncAll()
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                _activeState.value = null
                _elapsedSeconds.value = 0L
                onFinished?.invoke()
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _activeState.value != null) {
                delay(1000)
                val current = _activeState.value
                if (current != null && !current.isPaused) {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    private fun startPeriodicCapture() {
        periodicCaptureJob?.cancel()
        periodicCaptureJob = scope.launch(Dispatchers.IO) {
            while (isActive && _activeState.value != null) {
                delay(60_000)
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
