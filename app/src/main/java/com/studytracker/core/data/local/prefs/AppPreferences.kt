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

    private val _familyPairCode = MutableStateFlow(
        prefs.getString(KEY_FAMILY_PAIR_CODE, null) ?: generateRandomFamilyCode().also {
            prefs.edit().putString(KEY_FAMILY_PAIR_CODE, it).apply()
        }
    )
    val familyPairCode: StateFlow<String> = _familyPairCode.asStateFlow()

    fun setFamilyPairCode(code: String) {
        val clean = code.trim().uppercase().ifBlank { generateRandomFamilyCode() }
        prefs.edit().putString(KEY_FAMILY_PAIR_CODE, clean).apply()
        _familyPairCode.value = clean
    }

    fun generateNewFamilyCode(): String {
        val newCode = generateRandomFamilyCode()
        setFamilyPairCode(newCode)
        return newCode
    }

    private fun generateRandomFamilyCode(): String {
        val num = (1000..9999).random()
        return "ST-$num"
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

    private val _isNotificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _lastUnreadMessage = MutableStateFlow(prefs.getString(KEY_LAST_UNREAD_MESSAGE, null))
    val lastUnreadMessage: StateFlow<String?> = _lastUnreadMessage.asStateFlow()

    var lastNotifiedMessageTime: Long
        get() = prefs.getLong(KEY_LAST_NOTIFIED_MESSAGE_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_NOTIFIED_MESSAGE_TIME, value).apply()

    var lastStudyReminderDate: String?
        get() = prefs.getString(KEY_LAST_STUDY_REMINDER_DATE, null)
        set(value) = prefs.edit().putString(KEY_LAST_STUDY_REMINDER_DATE, value).apply()

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _isNotificationsEnabled.value = enabled
    }

    fun setLastUnreadMessage(messageJson: String?) {
        prefs.edit().putString(KEY_LAST_UNREAD_MESSAGE, messageJson).apply()
        _lastUnreadMessage.value = messageJson
    }

    fun clearLastUnreadMessage() {
        setLastUnreadMessage(null)
    }

    fun resetAllPreferences() {
        prefs.edit().clear().apply()
        _hasCompletedTutorial.value = false
        _isFakeCaptureEnabled.value = false
        _isNightMode.value = true
        _isNotificationsEnabled.value = true
        _lastUnreadMessage.value = null
    }

    companion object {
        private const val PREFS_NAME = "study_tracker_prefs"
        private const val KEY_HAS_COMPLETED_TUTORIAL = "has_completed_tutorial"
        private const val KEY_FAKE_CAPTURE = "is_fake_capture_enabled"
        private const val KEY_NIGHT_MODE = "is_night_mode"
        private const val KEY_FAMILY_PAIR_CODE = "family_pair_code"
        private const val KEY_NOTIFICATIONS_ENABLED = "is_notifications_enabled"
        private const val KEY_LAST_NOTIFIED_MESSAGE_TIME = "last_notified_message_time"
        private const val KEY_LAST_STUDY_REMINDER_DATE = "last_study_reminder_date"
        private const val KEY_LAST_UNREAD_MESSAGE = "last_unread_message"

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

