package com.akilimo.mobile

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.akilimo.mobile.utils.Locales
import com.blongho.country_data.World
import com.jakewharton.threetenabp.AndroidThreeTen
import dev.b3nedikt.app_locale.AppLocale.appLocaleRepository
import dev.b3nedikt.app_locale.AppLocale.supportedLocales
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import dev.b3nedikt.reword.RewordInterceptor
import io.github.inflationx.viewpump.ViewPump


class Akilimo : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        supportedLocales = Locales.APP_LOCALES
        val prefs = SharedPrefsAppLocaleRepository(this@Akilimo)
        appLocaleRepository = prefs


        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(RewordInterceptor)
                .build()
        )
        //@FIX This is used to enable proper vector support for android 4.4 and below, it causes crashing when firing up the application
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AndroidThreeTen.init(this@Akilimo) // Initialize the library
        World.init(this@Akilimo)
    }
}
