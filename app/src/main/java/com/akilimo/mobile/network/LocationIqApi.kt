package com.akilimo.mobile.network

import com.akilimo.mobile.rest.response.ReverseGeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIqApi {
    @GET("v1/reverse")
    suspend fun reverseGeocode(
        @Query("key") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): ReverseGeocodeResponse
}