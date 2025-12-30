package com.akilimo.mobile.network

import com.akilimo.mobile.rest.response.WeatherResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") coordinates: String,
        @Query("aqi") aqi: String = "no"
    ): WeatherResponse
}