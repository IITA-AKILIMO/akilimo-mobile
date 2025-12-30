package com.akilimo.mobile.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class LocationHelper {

    sealed class LocationResult {
        data class Success(val location: Location) : LocationResult()
        data class Error(val message: String) : LocationResult()
        object LocationDisabled : LocationResult()
        object PermissionDenied : LocationResult()
    }

    suspend fun getCurrentLocation(context: Context): LocationResult {
        return try {
            if (!isLocationEnabled(context)) {
                return LocationResult.LocationDisabled
            }

            if (!hasLocationPermission(context)) {
                return LocationResult.PermissionDenied
            }

            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            val location = fusedLocationClient.lastLocation.await()

            if (location != null && isValidLocation(location)) {
                LocationResult.Success(location)
            } else {
                // Try to get fresh location if last known is null or stale
                getFreshLocation(context)
            }
        } catch (e: SecurityException) {
            LocationResult.Error("Location permission denied")
        } catch (e: Exception) {
            LocationResult.Error("Error getting location: ${e.message ?: "Unknown error"}")
        }
    }

    private suspend fun getFreshLocation(context: Context): LocationResult {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = TimeUnit.SECONDS.toMillis(10)
                fastestInterval = TimeUnit.SECONDS.toMillis(5)
                maxWaitTime = TimeUnit.SECONDS.toMillis(15)
            }

            // Use getCurrentLocation API which is more battery efficient
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()
            } else {
                // Fallback for older versions
                fusedLocationClient.lastLocation.await()
            }

            if (location != null && isValidLocation(location)) {
                LocationResult.Success(location)
            } else {
                LocationResult.Error("Unable to get current location")
            }
        } catch (e: SecurityException) {
            LocationResult.Error("Location permission denied")
        } catch (e: Exception) {
            LocationResult.Error("Error getting fresh location: ${e.message ?: "Unknown error"}")
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.let {
            it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } ?: false
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isValidLocation(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        val MAX_LOCATION_AGE = TimeUnit.MINUTES.toMillis(5) // 5 minutes

        return (location.latitude != 0.0 || location.longitude != 0.0) &&
                locationAge < MAX_LOCATION_AGE
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getLastKnownLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) {
            return null
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // This returns immediately with last known location
            val task = fusedLocationClient.lastLocation
            task.result // This will block, but in a coroutine context it's fine
        } catch (e: Exception) {
            null
        }
    }
}