package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.network.GeocodingService
import com.akilimo.mobile.network.LocationHelper
import com.akilimo.mobile.network.WeatherService
import com.akilimo.mobile.repos.CoverageRepo
import com.akilimo.mobile.utils.CoverageArea
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
    private val coverageRepo: CoverageRepo,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    data class UiState(
        val addressText: String? = null,
        val weatherData: WeatherService.WeatherData? = null,
        val locationResult: LocationHelper.LocationResult? = null,
        /** GeoJSON of the supported area, for the map overlay; null when none is known */
        val coverageGeoJson: String? = null,
        val coverageArea: CoverageArea? = null,
        /** Where the selected point stands; UNKNOWN when nothing is selected or no area is known */
        val coverageStatus: EnumCoverageStatus = EnumCoverageStatus.UNKNOWN,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var coverageRequested: String? = null
    private var selected: Pair<Double, Double>? = null

    /** Loads the supported area of [countryCode] once; a blank code leaves the map without overlay. */
    fun loadCoverage(countryCode: String) {
        if (countryCode.isBlank() || coverageRequested == countryCode) return
        coverageRequested = countryCode
        viewModelScope.launch {
            val coverage = coverageRepo.get(countryCode) ?: return@launch
            _uiState.update { it.copy(coverageGeoJson = coverage.geoJson, coverageArea = coverage.area) }
            // A point picked before the area arrived is checked now
            selected?.let { (lat, lon) -> checkCoverage(lat, lon) }
        }
    }

    /** Records the selected point and works out whether it is inside the supported area. */
    fun checkCoverage(lat: Double, lon: Double) {
        selected = lat to lon
        val area = _uiState.value.coverageArea
        val status = when {
            area == null -> EnumCoverageStatus.UNKNOWN
            area.contains(lat, lon) -> EnumCoverageStatus.INSIDE
            else -> EnumCoverageStatus.OUTSIDE
        }
        _uiState.update { it.copy(coverageStatus = status) }
    }

    fun fetchCurrentLocation() = viewModelScope.launch {
        val result = LocationHelper().getCurrentLocation(context)
        if (result is LocationHelper.LocationResult.Success) {
            checkCoverage(result.location.latitude, result.location.longitude)
        }
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
