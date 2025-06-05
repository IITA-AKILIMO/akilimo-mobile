package com.akilimo.mobile.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import com.akilimo.mobile.R
import io.sentry.Sentry

class GPSTracker : Service, LocationListener {

    private val LOG_TAG = GPSTracker::class.java.simpleName
    private var mContext: Context? = null

    private var location: Location? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var canGetLocation = false

    private var locationManager: LocationManager? = null

    @Suppress("unused")
    constructor()

    constructor(context: Context) {
        this.mContext = context
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES: Long = 1000 * 60 // 1 minute
    }

    override fun onBind(intent: Intent): IBinder? = null

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLocation(): Location? {
        return try {
            initLocationManager()
            checkProviders()

            if (canGetLocation) {
                if (isNetworkEnabled()) requestNetworkLocation()
                if (isGPSEnabled() && location == null) requestGpsLocation()
            }

            location
        } catch (ex: Exception) {
            Toast.makeText(mContext, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
            null
        }
    }

    private fun initLocationManager() {
        locationManager = mContext?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    private fun isNetworkEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    private fun isGPSEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }

    private fun checkProviders() {
        canGetLocation = isNetworkEnabled() || isGPSEnabled()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestNetworkLocation() {
        locationManager?.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            MIN_TIME_BW_UPDATES,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
            this
        )
        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
            updateCoordinates(it)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestGpsLocation() {
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MIN_TIME_BW_UPDATES,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
            this
        )
        locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
            updateCoordinates(it)
        }
    }

    private fun updateCoordinates(loc: Location) {
        location = loc
        latitude = loc.latitude
        longitude = loc.longitude
    }

    fun stopUsingGPS() {
        locationManager?.removeUpdates(this)
    }

    val latitudeValue: Double
        get() = location?.latitude ?: latitude

    val longitudeValue: Double
        get() = location?.longitude ?: longitude

    fun canGetLocation(): Boolean = canGetLocation

    fun showSettingsAlert() {
        try {
            mContext?.let {
                AlertDialog.Builder(it)
                    .setTitle(it.getString(R.string.lbl_gps_settings))
                    .setMessage(it.getString(R.string.lbl_gps_not_enabled))
                    .setPositiveButton(it.getString(R.string.action_settings)) { dialog, _ ->
                        it.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        dialog.cancel()
                    }
                    .setCancelable(false)
                    .show()
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    override fun onLocationChanged(location: Location) {
        // Update the stored location and coordinates
        this.location = location
        latitude = location.latitude
        longitude = location.longitude

        // Optional: Notify UI or listeners via a callback or broadcast
        // e.g., myLocationListener?.onLocationUpdated(location)
        Log.d(LOG_TAG, "Location updated: lat=${latitude}, lon=${longitude}")
    }
}
