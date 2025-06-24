package com.akilimo.mobile.repo

import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapBoxLocationRepository(private val token: String) : LocationRepository {
    override suspend fun reverseGeocode(lat: Double, lon: Double): Pair<String, String>? {
        return suspendCancellableCoroutine { cont ->
            MapboxGeocoding.builder()
                .accessToken(token)
                .query(Point.fromLngLat(lon, lat))
                .fuzzyMatch(true)
                .build()
                .enqueueCall(object : Callback<GeocodingResponse?> {
                    override fun onResponse(
                        call: Call<GeocodingResponse?>,
                        response: Response<GeocodingResponse?>
                    ) {
                        val feature = response.body()?.features()?.lastOrNull()
                        val countryCode =
                            feature?.properties()?.get("short_code")?.asString?.uppercase()
                        val countryName = feature?.placeName()
                        cont.resumeWith(Result.success(countryCode?.let { cc ->
                            cc to (countryName ?: "")
                        }))
                    }

                    override fun onFailure(call: Call<GeocodingResponse?>, t: Throwable) {
                        cont.resumeWith(Result.success(null))
                    }
                })
        }
    }
}