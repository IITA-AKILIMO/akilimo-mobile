package com.akilimo.mobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.SharedPreferencesMigration
import com.akilimo.mobile.Locales
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
    produceMigrations = { context ->
        // Migrate legacy language + dark-mode values from the old SessionManager prefs file
        // so existing users keep their settings after upgrading.
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "new-akilimo-config",
                keysToMigrate = setOf(Keys.LANGUAGE_CODE.name, Keys.DARK_MODE.name)
            )
        )
    }
)

private object Keys {
    // Key names intentionally match the old SharedPrefs keys so migration works automatically.
    val LANGUAGE_CODE = stringPreferencesKey("languageCode")
    val DARK_MODE = booleanPreferencesKey("darkMode")
}

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        /**
         * Synchronous language tag read for use in [android.app.Activity.attachBaseContext]
         * — before Hilt injection is available.
         */
        fun readLanguageTagSync(context: Context): String = runBlocking {
            context.appSettingsDataStore.data
                .map { prefs -> Locales.normalize(prefs[Keys.LANGUAGE_CODE] ?: "") }
                .first()
        }
    }
    val languageTagFlow: Flow<String> = context.appSettingsDataStore.data
        .map { prefs -> Locales.normalize(prefs[Keys.LANGUAGE_CODE] ?: "") }

    val darkModeFlow: Flow<Boolean> = context.appSettingsDataStore.data
        .map { prefs -> prefs[Keys.DARK_MODE] ?: false }

    suspend fun setLanguageTag(tag: String) {
        context.appSettingsDataStore.edit { it[Keys.LANGUAGE_CODE] = tag }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.appSettingsDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    /** Synchronous read — only call from Application.onCreate() on the main thread. */
    fun getLanguageTagSync(): String = runBlocking { languageTagFlow.first() }

    /** Synchronous read — only call from Application.onCreate() on the main thread. */
    fun getDarkModeSync(): Boolean = runBlocking { darkModeFlow.first() }
}
