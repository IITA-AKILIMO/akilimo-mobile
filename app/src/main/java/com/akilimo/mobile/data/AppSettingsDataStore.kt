package com.akilimo.mobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
    val DARK_MODE = booleanPreferencesKey("darkMode")
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
}

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val DEFAULT_USER = "akilimo_user"
        private const val DEFAULT_TERMS_URL = "https://akilimo.org/index.php/akilimo-privacy-policy"

        /** Language tag — synchronous read for use in [android.app.Activity.attachBaseContext]. */
        fun readLanguageTagSync(context: Context): String = runBlocking {
            context.appSettingsDataStore.data
                .map { prefs -> Locales.normalize(prefs[Keys.LANGUAGE_CODE] ?: "") }
                .first()
        }

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

    val darkModeFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[Keys.DARK_MODE] ?: false }

    // ── Synchronous property accessors ────────────────────────────────────────
    // After the first DataStore read (done in AkilimoApp.onCreate via getLanguageTagSync /
    // getDarkModeSync), subsequent data.first() calls are served from DataStore's in-memory
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

    var darkMode: Boolean
        get() = syncGet(Keys.DARK_MODE, false)
        set(value) = asyncSet(Keys.DARK_MODE, value)

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

    var notificationCount: Int
        get() = syncGet(Keys.NOTIFICATION_COUNT, 3)
        set(value) = asyncSet(Keys.NOTIFICATION_COUNT, value)

    var termsLink: String
        get() = syncGet(Keys.TERMS_LINK, DEFAULT_TERMS_URL).ifBlank { DEFAULT_TERMS_URL }
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
    fun getDarkModeSync(): Boolean = darkMode

    // Suspend versions for coroutine-friendly callers
    suspend fun setLanguageTag(tag: String) {
        dataStore.edit { it[Keys.LANGUAGE_CODE] = tag }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }
}
