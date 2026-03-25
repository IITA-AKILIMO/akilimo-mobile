package com.akilimo.mobile.data

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    /**
     * Wraps the given context with the requested locale and returns a ContextWrapper
     * that uses the new configuration. Use this in attachBaseContext and before recreating UI.
     */
    fun wrap(context: Context, language: String): ContextWrapper {
        val locale = Locale.Builder().setLanguageTag(language).build()
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return ContextWrapper(context.createConfigurationContext(config))
    }
}
