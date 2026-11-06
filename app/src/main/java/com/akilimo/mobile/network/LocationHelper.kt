package com.akilimo.mobile.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.sentry.Sentry
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
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
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedLocationClient.lastLocation.await()

            if (location != null && isValidLocation(location)) {
                LocationResult.Success(location)
            } else {
                // Fallback to fresh location with timeout
                withTimeoutOrNull(10_000) {
                    getFreshLocation(context)
                } ?: LocationResult.Error("Timeout getting fresh location")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            Sentry.captureException(e)
            LocationResult.Error("Location permission denied")
        } catch (e: Exception) {
            LocationResult.Error("Error getting location: ${e.message ?: "Unknown error"}")
        }
    }

    private suspend fun getFreshLocation(context: Context): LocationResult {
        val cancellationTokenSource = CancellationTokenSource()
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            // Fused getCurrentLocation works on all API levels. The old pre-Android 11
            // fallback re-read lastLocation, which had just failed the freshness check,
            // so location always failed on Android 5–10 once the cached fix was stale.
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null && (location.latitude != 0.0 || location.longitude != 0.0)) {
                LocationResult.Success(location)
            } else {
                LocationResult.Error("Unable to get current location")
            }
        } catch (e: CancellationException) {
            throw e // timeout from the caller's withTimeoutOrNull
        } catch (e: SecurityException) {
            Sentry.captureException(e)
            LocationResult.Error("Location permission denied")
        } catch (e: Exception) {
            LocationResult.Error("Error getting fresh location: ${e.message ?: "Unknown error"}")
        } finally {
            // Stop the GPS request also on timeout/cancellation, not only on success
            cancellationTokenSource.cancel()
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
}
