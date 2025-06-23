package com.akilimo.mobile.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresPermission
import io.sentry.Sentry

class LiveLocationProvider(private val context: Context) : LocationListener {


    private var location: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var canGetLocation = false

    private var locationManager: LocationManager? = null
    private var callback: ((Location?) -> Unit)? = null


    companion object {
        private const val MIN_DISTANCE = 10f
        private const val MIN_TIME = 60_000L // 1 minute
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startListening(onLocationUpdate: (Location?) -> Unit) {
        callback = onLocationUpdate
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        try {
            val isNetworkEnabled =
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            val isGpsEnabled =
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true

            if (!isNetworkEnabled && !isGpsEnabled) {
                callback?.invoke(null)
                return
            }

            if (isNetworkEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME,
                    MIN_DISTANCE,
                    this
                )
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                    callback?.invoke(it)
                }
            }

            if (isGpsEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME,
                    MIN_DISTANCE,
                    this
                )
                if (!isNetworkEnabled) {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                        callback?.invoke(it)
                    }
                }
            }

        } catch (e: Exception) {
            Sentry.captureException(e)
            callback?.invoke(null)
        }
    }

    fun stopListening() {
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }


    fun canGetLocation(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                manager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun onLocationChanged(p0: Location) {
        callback?.invoke(location)
    }


    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    override fun onProviderEnabled(provider: String) = Unit
    override fun onProviderDisabled(provider: String) = Unit
}
