package com.studytracker.core.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences internal constructor(private val prefs: SharedPreferences) {

    private constructor(context: Context) : this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))


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

    private val _familyAdminToken = MutableStateFlow(prefs.getString(KEY_FAMILY_ADMIN_TOKEN, "") ?: "")
    val familyAdminToken: StateFlow<String> = _familyAdminToken.asStateFlow()

    private val _hasParentPin = MutableStateFlow(prefs.contains(KEY_PARENT_PIN_HASH))
    val hasParentPin: StateFlow<Boolean> = _hasParentPin.asStateFlow()

    fun setParentPin(pin: String): Boolean {
        if (!pin.matches(Regex("""\d{4,6}"""))) return false
        prefs.edit().putString(KEY_PARENT_PIN_HASH, hashPin(pin)).apply()
        _hasParentPin.value = true
        return true
    }

    fun verifyParentPin(pin: String): Boolean {
        val expected = prefs.getString(KEY_PARENT_PIN_HASH, null) ?: return false
        return MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            hashPin(pin).toByteArray(Charsets.UTF_8)
        )
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(("studytracker-parent-pin-v1:" + pin).toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun setFamilyAdminToken(token: String) {
        prefs.edit().putString(KEY_FAMILY_ADMIN_TOKEN, token.trim()).apply()
        _familyAdminToken.value = token.trim()
    }

    fun clearFamilyAdminToken() = setFamilyAdminToken("")

    fun setFamilyPairCode(code: String) {
        val clean = code.trim().uppercase().ifBlank { generateRandomFamilyCode() }
        if (clean != _familyPairCode.value) {
            clearFamilyAdminToken()
            lastKnownServerRevision = null
        }
        prefs.edit().putString(KEY_FAMILY_PAIR_CODE, clean).apply()
        _familyPairCode.value = clean
    }

    fun generateNewFamilyCode(): String {
        val newCode = generateRandomFamilyCode()
        setFamilyPairCode(newCode)
        return newCode
    }

    private fun generateRandomFamilyCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()
        val body = buildString {
            repeat(16) {
                append(alphabet[random.nextInt(alphabet.length)])
            }
        }
        return "ST-" + body.chunked(4).joinToString("-")
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

    var studentResumeCacheJson: String?
        get() = prefs.getString(KEY_STUDENT_RESUME_CACHE, null)
        set(value) {
            if (value.isNullOrBlank()) {
                prefs.edit().remove(KEY_STUDENT_RESUME_CACHE).apply()
            } else {
                prefs.edit().putString(KEY_STUDENT_RESUME_CACHE, value).apply()
            }
        }

    var lastKnownResetAt: Long
        get() = prefs.getLong(KEY_LAST_KNOWN_RESET_AT, 0L).coerceAtLeast(0L)
        set(value) = prefs.edit().putLong(KEY_LAST_KNOWN_RESET_AT, value.coerceAtLeast(0L)).apply()

    var lastKnownServerRevision: Long?
        get() {
            if (!prefs.contains(KEY_LAST_KNOWN_SERVER_REVISION)) return null
            val rev = prefs.getLong(KEY_LAST_KNOWN_SERVER_REVISION, -1L)
            return if (rev >= 0L) rev else null
        }
        set(value) {
            if (value != null && value >= 0L) {
                prefs.edit().putLong(KEY_LAST_KNOWN_SERVER_REVISION, value).apply()
            } else if (value == null) {
                prefs.edit().remove(KEY_LAST_KNOWN_SERVER_REVISION).apply()
            }
        }

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
        _familyAdminToken.value = ""
        _hasParentPin.value = false
    }

    companion object {
        private const val PREFS_NAME = "study_tracker_prefs"
        private const val KEY_HAS_COMPLETED_TUTORIAL = "has_completed_tutorial"
        private const val KEY_FAKE_CAPTURE = "is_fake_capture_enabled"
        private const val KEY_NIGHT_MODE = "is_night_mode"
        private const val KEY_FAMILY_PAIR_CODE = "family_pair_code"
        private const val KEY_FAMILY_ADMIN_TOKEN = "family_admin_token"
        private const val KEY_PARENT_PIN_HASH = "parent_pin_hash"
        private const val KEY_NOTIFICATIONS_ENABLED = "is_notifications_enabled"
        private const val KEY_LAST_NOTIFIED_MESSAGE_TIME = "last_notified_message_time"
        private const val KEY_LAST_STUDY_REMINDER_DATE = "last_study_reminder_date"
        private const val KEY_LAST_UNREAD_MESSAGE = "last_unread_message"
        private const val KEY_STUDENT_RESUME_CACHE = "student_resume_cache"
        private const val KEY_LAST_KNOWN_RESET_AT = "last_known_reset_at"
        private const val KEY_LAST_KNOWN_SERVER_REVISION = "last_known_server_revision"

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

