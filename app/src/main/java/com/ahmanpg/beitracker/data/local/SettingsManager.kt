package com.ahmanpg.beitracker.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("beitracker_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CHECK_INTERVAL = "check_interval"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_CURRENCY = "preferred_currency"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_BIO = "user_bio"
        private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"
        private const val KEY_JOIN_DATE = "join_date"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_REGION = "preferred_region"
        private const val KEY_LANGUAGE = "preferred_language"
        private const val KEY_ALERT_THRESHOLD = "alert_threshold"
        private const val KEY_THEME_MODE = "theme_mode" // 0: System, 1: Light, 2: Dark
        private const val KEY_TOTAL_SAVINGS = "total_savings"
        private const val KEY_ACCOUNT_TYPE = "account_type" // Free, Pro
    }

    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME_MODE, 0))
    val themeModeFlow = _themeMode.asStateFlow()

    var checkIntervalHours: Int
        get() = prefs.getInt(KEY_CHECK_INTERVAL, 6)
        set(value) = prefs.edit().putInt(KEY_CHECK_INTERVAL, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var preferredCurrency: String
        get() = prefs.getString(KEY_CURRENCY, "TZS") ?: "TZS"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Guest User") ?: "Guest User"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "guest@beitracker.tz") ?: "guest@beitracker.tz"
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userBio: String
        get() = prefs.getString(KEY_USER_BIO, "Smart shopping enthusiast.") ?: "Smart shopping enthusiast."
        set(value) = prefs.edit().putString(KEY_USER_BIO, value).apply()

    var profileImageUri: String?
        get() = prefs.getString(KEY_PROFILE_IMAGE_URI, null)
        set(value) = prefs.edit().putString(KEY_PROFILE_IMAGE_URI, value).apply()

    var joinDate: Long
        get() {
            val saved = prefs.getLong(KEY_JOIN_DATE, 0L)
            if (saved == 0L) {
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_JOIN_DATE, now).apply()
                return now
            }
            return saved
        }
        set(value) = prefs.edit().putLong(KEY_JOIN_DATE, value).apply()

    var totalSavings: Double
        get() = prefs.getFloat(KEY_TOTAL_SAVINGS, 0.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_TOTAL_SAVINGS, value.toFloat()).apply()

    var accountType: String
        get() = prefs.getString(KEY_ACCOUNT_TYPE, "Free") ?: "Free"
        set(value) = prefs.edit().putString(KEY_ACCOUNT_TYPE, value).apply()

    var preferredRegion: String
        get() = prefs.getString(KEY_REGION, "Tanzania") ?: "Tanzania"
        set(value) = prefs.edit().putString(KEY_REGION, value).apply()

    var preferredLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var alertThresholdPercent: Int
        get() = prefs.getInt(KEY_ALERT_THRESHOLD, 0) // 0 means any drop
        set(value) = prefs.edit().putInt(KEY_ALERT_THRESHOLD, value).apply()

    var themeMode: Int
        get() = _themeMode.value
        set(value) {
            prefs.edit().putInt(KEY_THEME_MODE, value).apply()
            _themeMode.value = value
        }

    var searchHistory: List<String>
        get() = prefs.getString(KEY_SEARCH_HISTORY, "")?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
        set(value) = prefs.edit().putString(KEY_SEARCH_HISTORY, value.take(10).joinToString("|")).apply()

    fun addSearchQuery(query: String) {
        val current = searchHistory.toMutableList()
        current.remove(query)
        current.add(0, query)
        searchHistory = current
    }

    fun clearSearchHistory() {
        searchHistory = emptyList()
    }
}
