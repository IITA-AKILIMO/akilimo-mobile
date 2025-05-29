package com.akilimo.mobile

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.blongho.country_data.World
import com.jakewharton.threetenabp.AndroidThreeTen


class Akilimo : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        //@FIX This is used to enable proper vector support for android 4.4 and below, it causes crashing when firing up the application
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AndroidThreeTen.init(this@Akilimo) // Initialize the library
        World.init(this@Akilimo)
    }
}
