package com.akilimo.mobile.network

import android.content.Context
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.WeatherApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class WeatherService(
    context: Context
) {

    companion object {
        private const val BASE_URL = "https://api.weatherapi.com/v1/"
    }

    private val api: WeatherApi =
        ApiClient.createService(
            context = context.applicationContext,
            baseUrl = BASE_URL,
            sendAkilimoApiKey = false
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
        // emit() stays outside try/catch to preserve Flow exception transparency
        val result = try {
            val response = api.getCurrentWeather(
                apiKey = BuildConfig.WEATHER_API_KEY,
                coordinates = "$lat,$lon"
            )
            WeatherResult.Success(
                WeatherData(
                    temperature = response.current.tempCelsius,
                    feelsLike = response.current.feelslikeCelsius,
                    condition = response.current.condition.text,
                    humidity = response.current.humidity,
                    windSpeed = response.current.windKph / 3.6 // kph → m/s
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            reportUnlessNetworkError(e)
            WeatherResult.Error(e)
        }
        emit(result)
    }.flowOn(Dispatchers.IO) // network call runs on IO, collector can be Main
}
