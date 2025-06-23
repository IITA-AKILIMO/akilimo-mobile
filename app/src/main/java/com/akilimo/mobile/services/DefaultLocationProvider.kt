package com.akilimo.mobile.services

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.akilimo.mobile.interfaces.LocationProvider

class DefaultLocationProvider(private val context: Context) : LocationProvider {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCurrentLocation(callback: (Triple<Double, Double, Double>?) -> Unit) {
        val provider = LiveLocationProvider(context)
        if (!provider.canGetLocation()) {
            provider.openLocationSettings()
            callback(null)
            return
        }

        provider.startListening { location ->

            if (location != null) {
                callback(Triple(location.latitude, location.longitude, location.altitude))
            } else {
                callback(null)
            }
            provider.stopListening()

        }
    }
}