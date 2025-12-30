package com.akilimo.mobile.utils

import android.content.Context
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.WeatherApi
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class WeatherService(
    context: Context
) {

    companion object {
        private const val WEATHER_API_KEY = "4538add05d16412f80a222914252912"
        private const val BASE_URL = "https://api.weatherapi.com/v1/"
    }

    private val api: WeatherApi =
        ApiClient.createService(
            context = context.applicationContext,
            baseUrl = BASE_URL
        )

    data class WeatherData(
        val temperature: Double,
        val feelsLike: Double,
        val condition: String,
        val humidity: Int,
        val windSpeed: Double // meters per second
    )

    sealed class WeatherResult {
        data class Success(val data: WeatherData) : WeatherResult()
        data class Error(val exception: Exception) : WeatherResult()
    }

    fun fetchWeatherFlow(lat: Double, lon: Double): Flow<WeatherResult> = flow {
        try {
            val coordinates = "$lat,$lon"
            val response = api.getCurrentWeather(
                apiKey = WEATHER_API_KEY,
                coordinates = coordinates
            )

            emit(
                WeatherResult.Success(
                    WeatherData(
                        temperature = response.current.tempCelsius,
                        feelsLike = response.current.feelslikeCelsius,
                        condition = response.current.condition.text,
                        humidity = response.current.humidity,
                        windSpeed = response.current.windKph / 3.6 // kph → m/s
                    )
                )
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
            emit(WeatherResult.Error(e))
        }
    }.flowOn(Dispatchers.IO) // network call runs on IO, collector can be Main
}
