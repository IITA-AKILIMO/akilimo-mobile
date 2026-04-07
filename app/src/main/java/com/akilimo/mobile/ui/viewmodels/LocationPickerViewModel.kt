package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.network.GeocodingService
import com.akilimo.mobile.network.LocationHelper
import com.akilimo.mobile.network.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class UiState(
        val addressText: String? = null,
        val weatherData: WeatherService.WeatherData? = null,
        val locationResult: LocationHelper.LocationResult? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun fetchCurrentLocation() = viewModelScope.launch {
        val result = LocationHelper().getCurrentLocation(context)
        _uiState.update { it.copy(locationResult = result) }
    }

    fun fetchAddress(lat: Double, lng: Double) = viewModelScope.launch {
        val service = GeocodingService(context, appSettings.locationIqToken)
        service.fetchAddressFlow(lat, lng).collect { result ->
            when (result) {
                is GeocodingService.GeocodingResult.Success ->
                    _uiState.update { it.copy(addressText = result.data.formattedAddress) }
                is GeocodingService.GeocodingResult.Error ->
                    _uiState.update { it.copy(addressText = result.fallbackMessage) }
            }
        }
    }

    fun fetchWeather(lat: Double, lng: Double) = viewModelScope.launch {
        try {
            WeatherService(context).fetchWeatherFlow(lat, lng).collect { result ->
                when (result) {
                    is WeatherService.WeatherResult.Success ->
                        _uiState.update { it.copy(weatherData = result.data) }
                    is WeatherService.WeatherResult.Error ->
                        _uiState.update { it.copy(weatherData = null) }
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}
