package com.akilimo.mobile.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.Locales
import com.akilimo.mobile.enums.EnumServiceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// File-level extension — DataStore is a singleton per process.
// SharedPreferencesMigration automatically copies all matching keys from the old
// "new-akilimo-config" SharedPrefs file on the first DataStore access.
private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "new-akilimo-config"
                // No keysToMigrate filter → migrates ALL keys automatically
            )
        )
    }
)

private object Keys {
    // Names intentionally match the old SharedPrefs keys so migration is lossless.
    val LANGUAGE_CODE = stringPreferencesKey("languageCode")
    /** Legacy on/off dark mode; read only to migrate to [THEME_MODE], kept in step for downgrades */
    val DARK_MODE = booleanPreferencesKey("darkMode")
    val THEME_MODE = stringPreferencesKey("themeMode")
    val AKILIMO_USER = stringPreferencesKey("userName")
    val AKILIMO_ENDPOINT = stringPreferencesKey("apiResource")
    val FUELROD_ENDPOINT = stringPreferencesKey("fuelrodResource")
    val API_TOKEN = stringPreferencesKey("apiToken")
    val API_REFRESH_TOKEN = stringPreferencesKey("apiRefreshToken")
    val MAPBOX_KEY = stringPreferencesKey("mapBoxKey")
    val LOCATION_IQ_TOKEN = stringPreferencesKey("locationIqToken")
    val FIRST_RUN = booleanPreferencesKey("firstRun")
    val NOTIFICATION_COUNT = intPreferencesKey("notificationCount")
    val DEVICE_TOKEN = stringPreferencesKey("deviceToken")
    val TERMS_ACCEPTED = booleanPreferencesKey("termsAccepted")
    val DISCLAIMER_READ = booleanPreferencesKey("disclaimerRead")
    val TERMS_LINK = stringPreferencesKey("termsLink")
    val FERTILIZER_GRID = booleanPreferencesKey("isFertilizerGrid")
    val REMEMBER_AREA_UNIT = booleanPreferencesKey("rememberAreaUnit")
    val LOCK_APP_LANGUAGE = booleanPreferencesKey("lockAppLanguage")
    val READING_TEXT_SCALE = floatPreferencesKey("readingTextScale")
}

/** Reader-chosen text size for long-form advice, on top of the phone's own font scale. */
object ReadingTextScale {
    const val DEFAULT = 1f
    val options = listOf(DEFAULT, 1.25f, 1.5f)

    /** Snaps any stored value to the nearest supported step so a bad value can't break layout. */
    fun normalize(value: Float?): Float =
        if (value == null || value.isNaN()) DEFAULT
        else options.minByOrNull { kotlin.math.abs(it - value) } ?: DEFAULT
}

/** App theme choice. SYSTEM follows the phone's dark-theme setting. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** AppCompat night mode for the remaining View-based screens. */
fun ThemeMode.toNightMode(): Int = when (this) {
    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
}

/**
 * Reads [Keys.THEME_MODE]; before it existed only the on/off "darkMode" flag was stored. That flag
 * defaulted to off and cannot tell "chose light" from "never touched", so on maps to DARK and off
 * (or missing) maps to SYSTEM.
 */
internal fun Preferences.themeMode(): ThemeMode =
    this[Keys.THEME_MODE]?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
        ?: if (this[Keys.DARK_MODE] == true) ThemeMode.DARK else ThemeMode.SYSTEM

@Singleton
class AppSettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val DEFAULT_USER = "akilimo_user"
        // Same bundled page Settings opens under Terms (works offline)
        private const val DEFAULT_TERMS_URL = "file:///android_asset/terms.html"
        private const val LEGACY_TERMS_URL = "file:///android_asset/privacy_policy.html"

        /** Language tag — synchronous read for use in [android.app.Activity.attachBaseContext]. */
        fun readLanguageTagSync(context: Context): String = runBlocking {
            context.appSettingsDataStore.data
                .map { prefs -> Locales.normalize(prefs[Keys.LANGUAGE_CODE] ?: "") }
                .first()
        }

        /**
         * The language the app must use, or null to follow the phone's language.
         * Synchronous for [android.app.Activity.attachBaseContext] (runs before Hilt injection).
         */
        fun readAppliedLanguageTagSync(context: Context): String? = runBlocking {
            context.appSettingsDataStore.data.map { prefs ->
                if (isLanguageLocked(prefs)) Locales.normalize(prefs[Keys.LANGUAGE_CODE] ?: "") else null
            }.first()
        }

        /**
         * "Override system language". When never set explicitly it defaults to ON if the user
         * has picked a language, so people who chose a language before this setting existed
         * keep it instead of silently switching to the phone's language.
         */
        private fun isLanguageLocked(prefs: Preferences): Boolean =
            prefs[Keys.LOCK_APP_LANGUAGE] ?: (prefs[Keys.LANGUAGE_CODE] != null)

        /** Endpoint — synchronous read for [com.akilimo.mobile.config.AppConfig] / Workers. */
        fun readEndpointSync(context: Context, service: EnumServiceType): String = runBlocking {
            context.appSettingsDataStore.data.map { prefs ->
                when (service) {
                    EnumServiceType.AKILIMO -> prefs[Keys.AKILIMO_ENDPOINT]
                    EnumServiceType.FUELROD -> prefs[Keys.FUELROD_ENDPOINT]
                }.takeIf { !it.isNullOrBlank() } ?: ""
            }.first()
        }

        /** Endpoint — synchronous write for [com.akilimo.mobile.config.AppConfig]. */
        fun writeEndpointSync(context: Context, service: EnumServiceType, url: String) {
            runBlocking {
                context.appSettingsDataStore.edit { prefs ->
                    when (service) {
                        EnumServiceType.AKILIMO -> prefs[Keys.AKILIMO_ENDPOINT] = url
                        EnumServiceType.FUELROD -> prefs[Keys.FUELROD_ENDPOINT] = url
                    }
                }
            }
        }
    }

    private val dataStore = context.appSettingsDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Reactive Flows (for ViewModels / lifecycle-aware collectors) ──────────

    val languageTagFlow: Flow<String> =
        dataStore.data.map { prefs -> Locales.normalize(prefs[Keys.LANGUAGE_CODE] ?: "") }

    val themeModeFlow: Flow<ThemeMode> =
        dataStore.data.map { prefs -> prefs.themeMode() }

    val rememberAreaUnitFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.REMEMBER_AREA_UNIT] ?: false }

    val fertilizerGridFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.FERTILIZER_GRID] ?: false }

    val lockAppLanguageFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> isLanguageLocked(prefs) }

    val readingTextScaleFlow: Flow<Float> =
        dataStore.data.map { prefs -> ReadingTextScale.normalize(prefs[Keys.READING_TEXT_SCALE]) }

    // ── Synchronous property accessors ────────────────────────────────────────
    // After the first DataStore read (done in AkilimoApp.onCreate via getLanguageTagSync /
    // getThemeModeSync), subsequent data.first() calls are served from DataStore's in-memory
    // cache — no disk I/O, safe to call on the main thread.

    private fun <T> syncGet(key: Preferences.Key<T>, default: T): T =
        runBlocking { dataStore.data.map { it[key] ?: default }.first() }

    private fun <T> asyncSet(key: Preferences.Key<T>, value: T) {
        scope.launch { dataStore.edit { it[key] = value } }
    }

    // App / locale settings
    var languageTag: String
        get() = syncGet(Keys.LANGUAGE_CODE, Locales.english.toLanguageTag())
            .let { Locales.normalize(it) }
        set(value) = asyncSet(Keys.LANGUAGE_CODE, value)

    val themeMode: ThemeMode
        get() = runBlocking { dataStore.data.map { it.themeMode() }.first() }

    // User identity
    var akilimoUser: String
        get() = syncGet(Keys.AKILIMO_USER, DEFAULT_USER)
        set(value) = asyncSet(Keys.AKILIMO_USER, value)

    // API endpoints (override BuildConfig defaults when persisted)
    var akilimoEndpoint: String
        get() = syncGet(Keys.AKILIMO_ENDPOINT, "")
        set(value) = asyncSet(Keys.AKILIMO_ENDPOINT, value)

    var fuelrodEndpoint: String
        get() = syncGet(Keys.FUELROD_ENDPOINT, "")
        set(value) = asyncSet(Keys.FUELROD_ENDPOINT, value)

    // Auth
    var apiToken: String
        get() = syncGet(Keys.API_TOKEN, "")
        set(value) = asyncSet(Keys.API_TOKEN, value)

    var apiRefreshToken: String
        get() = syncGet(Keys.API_REFRESH_TOKEN, "")
        set(value) = asyncSet(Keys.API_REFRESH_TOKEN, value)

    // SDK keys (fall back to BuildConfig values baked in at compile time)
    var mapBoxApiKey: String
        get() = syncGet(Keys.MAPBOX_KEY, "").ifBlank { BuildConfig.MAPBOX_RUNTIME_TOKEN }
        set(value) = asyncSet(Keys.MAPBOX_KEY, value)

    var locationIqToken: String
        get() = syncGet(Keys.LOCATION_IQ_TOKEN, "").ifBlank { BuildConfig.LOCATION_IQ_TOKEN }
        set(value) = asyncSet(Keys.LOCATION_IQ_TOKEN, value)

    // App state flags
    var isFirstRun: Boolean
        get() = syncGet(Keys.FIRST_RUN, true)
        set(value) = asyncSet(Keys.FIRST_RUN, value)

    var termsAccepted: Boolean
        get() = syncGet(Keys.TERMS_ACCEPTED, false)
        set(value) = asyncSet(Keys.TERMS_ACCEPTED, value)

    var disclaimerRead: Boolean
        get() = syncGet(Keys.DISCLAIMER_READ, false)
        set(value) = asyncSet(Keys.DISCLAIMER_READ, value)

    var rememberAreaUnit: Boolean
        get() = syncGet(Keys.REMEMBER_AREA_UNIT, false)
        set(value) = asyncSet(Keys.REMEMBER_AREA_UNIT, value)

    var isFertilizerGrid: Boolean
        get() = syncGet(Keys.FERTILIZER_GRID, false)
        set(value) = asyncSet(Keys.FERTILIZER_GRID, value)

    var lockAppLanguage: Boolean
        get() = runBlocking { dataStore.data.map { isLanguageLocked(it) }.first() }
        set(value) = asyncSet(Keys.LOCK_APP_LANGUAGE, value)

    /** Language tag to apply, or null to follow the phone's language. */
    val appliedLanguageTag: String?
        get() = if (lockAppLanguage) languageTag else null

    var notificationCount: Int
        get() = syncGet(Keys.NOTIFICATION_COUNT, 3)
        set(value) = asyncSet(Keys.NOTIFICATION_COUNT, value)

    var termsLink: String
        // The old default pointed at the privacy policy: treat a stored copy of it as unset
        get() = syncGet(Keys.TERMS_LINK, DEFAULT_TERMS_URL)
            .takeUnless { it.isBlank() || it == LEGACY_TERMS_URL } ?: DEFAULT_TERMS_URL
        set(value) = asyncSet(Keys.TERMS_LINK, value)

    // Device token — auto-generates a UUID on first access and persists it
    var deviceToken: String
        get() {
            val saved = runBlocking { dataStore.data.first()[Keys.DEVICE_TOKEN] }
            return if (saved.isNullOrEmpty()) {
                val token = UUID.randomUUID().toString()
                asyncSet(Keys.DEVICE_TOKEN, token)
                token
            } else saved
        }
        set(value) = asyncSet(Keys.DEVICE_TOKEN, value)

    fun decrementNotificationCount() {
        val current = notificationCount
        if (current > 0) notificationCount = current - 1
    }

    // Convenience sync reads used in Application.onCreate (before reactive collection starts)
    fun getLanguageTagSync(): String = languageTag
    fun getThemeModeSync(): ThemeMode = themeMode

    // Suspend versions for coroutine-friendly callers
    suspend fun setLanguageTag(tag: String) {
        dataStore.edit { it[Keys.LANGUAGE_CODE] = tag }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit {
            it[Keys.THEME_MODE] = mode.name
            it[Keys.DARK_MODE] = mode == ThemeMode.DARK
        }
    }

    /** Picking a language explicitly means "use this language": also turns the override on. */
    suspend fun setLanguageTagAndOverride(tag: String) {
        dataStore.edit {
            it[Keys.LANGUAGE_CODE] = tag
            it[Keys.LOCK_APP_LANGUAGE] = true
        }
    }

    suspend fun setLockAppLanguage(locked: Boolean) {
        dataStore.edit { it[Keys.LOCK_APP_LANGUAGE] = locked }
    }

    suspend fun setRememberAreaUnit(value: Boolean) {
        dataStore.edit { it[Keys.REMEMBER_AREA_UNIT] = value }
    }

    suspend fun setReadingTextScale(value: Float) {
        dataStore.edit { it[Keys.READING_TEXT_SCALE] = ReadingTextScale.normalize(value) }
    }

    suspend fun setFertilizerGrid(value: Boolean) {
        dataStore.edit { it[Keys.FERTILIZER_GRID] = value }
    }
}
