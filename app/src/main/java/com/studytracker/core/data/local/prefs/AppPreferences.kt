package com.studytracker.core.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _hasCompletedTutorial = MutableStateFlow(prefs.getBoolean(KEY_HAS_COMPLETED_TUTORIAL, false))
    val hasCompletedTutorial: StateFlow<Boolean> = _hasCompletedTutorial.asStateFlow()

    private val _isFakeCaptureEnabled = MutableStateFlow(prefs.getBoolean(KEY_FAKE_CAPTURE, false))
    val isFakeCaptureEnabled: StateFlow<Boolean> = _isFakeCaptureEnabled.asStateFlow()

    private val _isNightMode = MutableStateFlow(prefs.getBoolean(KEY_NIGHT_MODE, true))
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    fun setHasCompletedTutorial(completed: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_TUTORIAL, completed).apply()
        _hasCompletedTutorial.value = completed
    }

    fun setFakeCaptureEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FAKE_CAPTURE, enabled).apply()
        _isFakeCaptureEnabled.value = enabled
    }

    fun setNightMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
        _isNightMode.value = enabled
    }

    fun toggleNightMode() {
        setNightMode(!_isNightMode.value)
    }

    fun resetAllPreferences() {
        prefs.edit().clear().apply()
        _hasCompletedTutorial.value = false
        _isFakeCaptureEnabled.value = false
        _isNightMode.value = true
    }

    companion object {
        private const val PREFS_NAME = "study_tracker_prefs"
        private const val KEY_HAS_COMPLETED_TUTORIAL = "has_completed_tutorial"
        private const val KEY_FAKE_CAPTURE = "is_fake_capture_enabled"
        private const val KEY_NIGHT_MODE = "is_night_mode"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AppPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

