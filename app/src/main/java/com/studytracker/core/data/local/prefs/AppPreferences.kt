package com.studytracker.core.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isTestModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_TEST_MODE, true))
    val isTestModeEnabled: StateFlow<Boolean> = _isTestModeEnabled.asStateFlow()

    private val _hasCompletedTutorial = MutableStateFlow(prefs.getBoolean(KEY_HAS_COMPLETED_TUTORIAL, false))
    val hasCompletedTutorial: StateFlow<Boolean> = _hasCompletedTutorial.asStateFlow()

    private val _isFakeCaptureEnabled = MutableStateFlow(prefs.getBoolean(KEY_FAKE_CAPTURE, true))
    val isFakeCaptureEnabled: StateFlow<Boolean> = _isFakeCaptureEnabled.asStateFlow()

    private val _isNightMode = MutableStateFlow(prefs.getBoolean(KEY_NIGHT_MODE, true))
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    // Test mode simulation overrides for real-time background brightness & flying star previews
    private val _testProgressOverride = MutableStateFlow<Float?>(null)
    val testProgressOverride: StateFlow<Float?> = _testProgressOverride.asStateFlow()

    private val _testFlyingStarTrigger = MutableStateFlow<Long>(0L)
    val testFlyingStarTrigger: StateFlow<Long> = _testFlyingStarTrigger.asStateFlow()

    fun setTestProgressOverride(ratio: Float?) {
        _testProgressOverride.value = ratio
    }

    fun triggerTestFlyingStar() {
        _testFlyingStarTrigger.value = System.currentTimeMillis()
    }

    fun setTestModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TEST_MODE, enabled).apply()
        _isTestModeEnabled.value = enabled
        if (!enabled) {
            _testProgressOverride.value = null
        }
    }

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

    // Cloud Sync & Supabase preferences
    private val _familyPairCode = MutableStateFlow(prefs.getString(KEY_FAMILY_PAIR_CODE, "") ?: "")
    val familyPairCode: StateFlow<String> = _familyPairCode.asStateFlow()

    private val _isCloudSyncEnabled = MutableStateFlow(prefs.getBoolean(KEY_CLOUD_SYNC_ENABLED, true))
    val isCloudSyncEnabled: StateFlow<Boolean> = _isCloudSyncEnabled.asStateFlow()

    private val _supabaseUrl = MutableStateFlow(prefs.getString(KEY_SUPABASE_URL, DEFAULT_SUPABASE_URL) ?: DEFAULT_SUPABASE_URL)
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(prefs.getString(KEY_SUPABASE_ANON_KEY, DEFAULT_SUPABASE_ANON_KEY) ?: DEFAULT_SUPABASE_ANON_KEY)
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    fun setFamilyPairCode(code: String) {
        prefs.edit().putString(KEY_FAMILY_PAIR_CODE, code).apply()
        _familyPairCode.value = code
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_ENABLED, enabled).apply()
        _isCloudSyncEnabled.value = enabled
    }

    fun setSupabaseConfig(url: String, anonKey: String) {
        prefs.edit()
            .putString(KEY_SUPABASE_URL, url)
            .putString(KEY_SUPABASE_ANON_KEY, anonKey)
            .apply()
        _supabaseUrl.value = url
        _supabaseAnonKey.value = anonKey
    }

    fun resetAllPreferences() {
        prefs.edit().clear().apply()
        _isTestModeEnabled.value = true
        _hasCompletedTutorial.value = false
        _isFakeCaptureEnabled.value = true
        _isNightMode.value = true
        _familyPairCode.value = ""
        _isCloudSyncEnabled.value = true
        _supabaseUrl.value = DEFAULT_SUPABASE_URL
        _supabaseAnonKey.value = DEFAULT_SUPABASE_ANON_KEY
    }

    companion object {
        private const val PREFS_NAME = "study_tracker_prefs"
        private const val KEY_TEST_MODE = "is_test_mode_enabled"
        private const val KEY_HAS_COMPLETED_TUTORIAL = "has_completed_tutorial"
        private const val KEY_FAKE_CAPTURE = "is_fake_capture_enabled"
        private const val KEY_NIGHT_MODE = "is_night_mode"
        private const val KEY_FAMILY_PAIR_CODE = "family_pair_code"
        private const val KEY_CLOUD_SYNC_ENABLED = "is_cloud_sync_enabled"
        private const val KEY_SUPABASE_URL = "supabase_url"
        private const val KEY_SUPABASE_ANON_KEY = "supabase_anon_key"

        // Default public demo project or placeholder endpoints (easily overridden in UI / DevConsole)
        const val DEFAULT_SUPABASE_URL = "https://wixmpyfegcvyzomgopte.supabase.co"
        const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndpeG1weWZlZ2N2eXpvbWdvcHRlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTAwMDAwMDAsImV4cCI6MjAyMDAwMDAwMH0.demo_placeholder_token"

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
