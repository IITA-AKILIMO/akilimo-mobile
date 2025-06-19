package com.akilimo.mobile

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import com.akilimo.mobile.utils.PreferenceManager
import com.blongho.country_data.World
import com.jakewharton.threetenabp.AndroidThreeTen


class Akilimo : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        // TODO: Revise and remove in future releases
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        val sessionManager = PreferenceManager(this@Akilimo)
        val akilimoEndpoint = sessionManager.akilimoEndpoint
        val fuelrodEndpoint = sessionManager.fuelrodEndpoint

        RetrofitManager.init(this@Akilimo, akilimoEndpoint, fuelrodEndpoint)

        //@FIX This is used to enable proper vector support for android 4.4 and below, it causes crashing when firing up the application
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AndroidThreeTen.init(this@Akilimo)
        World.init(this@Akilimo)
    }
}
