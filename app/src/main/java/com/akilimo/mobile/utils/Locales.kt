package com.akilimo.mobile.utils


import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

data class LanguageOption(val code: String, val displayName: String)

object Locales {
    private val LOCAL_TZ_SWA = Locale("sw", "TZ")
    private val LOCAL_NG_ENGLISH = Locale("en", "NG")
    private val LOCAL_RW_KINYARWANDA = Locale("rw", "RW")

    val LOCALE_COUNTRIES: List<Locale> =
        listOf(LOCAL_NG_ENGLISH, LOCAL_TZ_SWA, LOCAL_RW_KINYARWANDA)
}

@Suppress(
    "kotlin:S6291",
    "kotlin:S2068"
) // Unencrypted preferences (justified: no sensitive info stored)
object LanguageManager {
    private const val LANGUAGE_KEY = "language_key"
    private const val DEFAULT_LANGUAGE = "en"

    fun saveLanguage(context: Context, language: String) {
        val sharedPreferences = context.getSharedPreferences("akilimo-prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit { putString(LANGUAGE_KEY, language) }
    }

    fun getLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("akilimo-prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
