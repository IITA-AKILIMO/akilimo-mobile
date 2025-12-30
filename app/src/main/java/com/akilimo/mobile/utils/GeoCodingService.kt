package com.akilimo.mobile.utils

import android.content.Context
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.LocationIqApi
import com.akilimo.mobile.rest.response.Address
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GeocodingService(
    private val context: Context,
    private val apiKey: String
) {

    companion object {
        private const val BASE_URL = "https://eu1.locationiq.com/"
    }

    private val api: LocationIqApi =
        ApiClient.createService(
            context = context.applicationContext,
            baseUrl = BASE_URL
        )

    data class AddressData(
        val formattedAddress: String
    )

    sealed class GeocodingResult {
        data class Success(val data: AddressData) : GeocodingResult()
        data class Error(val exception: Exception, val fallbackMessage: String) : GeocodingResult()
    }

    /**
     * Returns a Flow emitting GeocodingResult for the given latitude and longitude.
     * Network calls are performed on Dispatchers.IO automatically.
     */
    fun fetchAddressFlow(lat: Double, lon: Double): Flow<GeocodingResult> = flow {
        try {
            val response = api.reverseGeocode(
                apiKey = apiKey,
                latitude = lat,
                longitude = lon
            )

            val addressText = buildFormattedAddress(response.address, lat, lon)

            emit(
                GeocodingResult.Success(
                    AddressData(formattedAddress = addressText)
                )
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
            emit(
                GeocodingResult.Error(
                    exception = e,
                    fallbackMessage = "Unable to fetch address"
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Formats the Address object into a readable string.
     * Falls back to lat/lon if no meaningful address is available.
     */
    private fun buildFormattedAddress(address: Address, lat: Double, lon: Double): String {
        return buildString {
            address.road?.takeIf { it.isNotEmpty() }?.let { append(it) }
            address.suburb?.takeIf { it.isNotEmpty() }?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
            address.city?.takeIf { it.isNotEmpty() }?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
            address.state?.takeIf { it.isNotEmpty() }?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
            address.country?.takeIf { it.isNotEmpty() }?.let {
                if (isNotEmpty()) append(", ")
                append(it)
            }
        }.ifEmpty {
            "Lat: ${String.format("%.6f", lat)}, Lon: ${String.format("%.6f", lon)}"
        }
    }
}