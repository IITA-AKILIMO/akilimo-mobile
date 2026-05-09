package com.akilimo.mobile.network

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
import io.sentry.Sentry
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.concurrent.TimeUnit

class LocationHelper {

    companion object {
        private const val LOCATION_MAX_AGE_MINUTES = 5L
    }

    sealed class LocationResult {
        data class Success(val location: Location) : LocationResult()
        data class Error(val message: String) : LocationResult()
        object LocationDisabled : LocationResult()
        object PermissionDenied : LocationResult()
    }

    suspend fun getCurrentLocation(context: Context): LocationResult = when {
        !isLocationEnabled(context) -> LocationResult.LocationDisabled
        !hasLocationPermission(context) -> LocationResult.PermissionDenied
        else -> try {
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
            Sentry.captureException(e)
            LocationResult.Error("Location permission denied")
        } catch (e: IOException) {
            LocationResult.Error("Error getting location: ${e.message ?: "Unknown error"}")
        } catch (e: Exception) {
            LocationResult.Error("Error getting location: ${e.message ?: "Unknown error"}")
        }
    }

    private suspend fun getFreshLocation(context: Context): LocationResult {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

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
            Sentry.captureException(e)
            LocationResult.Error("Location permission denied")
        } catch (e: IOException) {
            LocationResult.Error("Error getting fresh location: ${e.message ?: "Unknown error"}")
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
        val maxLocationAge = TimeUnit.MINUTES.toMillis(LOCATION_MAX_AGE_MINUTES)

        return (location.latitude != 0.0 || location.longitude != 0.0) &&
                locationAge < maxLocationAge
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
            Sentry.captureException(e)
            null
        }
    }
}
