package com.iita.akilimo.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.iita.akilimo.R


class GPSTracker : Service, LocationListener {

    private var LOG_TAG = GPSTracker::class.java.simpleName
    private var mContext: Context? = null
    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var canGetLocation = false

    private var location: Location? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private var locationManager: LocationManager? = null

    @Suppress("unused")
    constructor()

    constructor(context: Context) {
        this.mContext = context
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 60).toLong() // 1 minute
    }

    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        try {
            locationManager =
                mContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // getting GPS status
            isGPSEnabled = locationManager!!
                .isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isGPSEnabled || isNetworkEnabled) {
                this.canGetLocation = true
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                    )
                    if (locationManager != null) {
                        location = locationManager!!
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
                        )
                        if (locationManager != null) {
                            location = locationManager!!
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }
            }

            // no network provider is enabled
        } catch (ex: Exception) {
            Toast.makeText(mContext, ex.message, Toast.LENGTH_SHORT).show()
//            Crashlytics.log(Log.ERROR, "GPS_TRACKER", ex.message)
//            Crashlytics.logException(ex)
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }

        return location
    }


    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }


    fun getLatitude(): Double {
        return if (location != null) location!!.latitude else latitude
    }


    fun getLongitude(): Double {
        return if (location != null) location!!.longitude else longitude
    }


    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }


    fun showSettingsAlert() {
        try {
            val alertDialog = AlertDialog.Builder(mContext!!)
            alertDialog.setTitle(getString(R.string.lbl_gps_settings))
            alertDialog.setMessage(getString(R.string.lbl_gps_not_enabled))
            alertDialog.setPositiveButton(getString(R.string.action_settings)) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext?.startActivity(intent)
                dialog.cancel()
            }

            alertDialog.setCancelable(false)
            alertDialog.show()
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
    }

    override fun onLocationChanged(location: Location) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }
}
