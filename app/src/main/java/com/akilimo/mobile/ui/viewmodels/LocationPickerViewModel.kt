package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    private val coverageRepo: CoverageRepo,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    /** Why the phone's own location could not be used; each maps to a localised message. */
    enum class LocationProblem { DISABLED, PERMISSION_DENIED, FAILED }

    data class SelectedLocation(val lat: Double, val lon: Double)

    data class UiState(
        /** The farm point the user (or GPS) picked; Confirm is enabled once this is set */
        val selected: SelectedLocation? = null,
        val addressText: String? = null,
        val weatherData: WeatherService.WeatherData? = null,
        val isLocating: Boolean = false,
        val locationProblem: LocationProblem? = null,
        /** GeoJSON of the supported area, for the map overlay; null when none is known */
        val coverageGeoJson: String? = null,
        val coverageArea: CoverageArea? = null,
        val isLoadingCoverage: Boolean = false,
        /** The supported area could not be loaded (offline with no cached copy, or none on the server) */
        val coverageUnavailable: Boolean = false,
        /** Where the selected point stands; UNKNOWN when nothing is selected or no area is known */
        val coverageStatus: EnumCoverageStatus = EnumCoverageStatus.UNKNOWN,
    )

    sealed interface Effect {
        /** A GPS fix arrived: move the camera there */
        data class FlyTo(val lat: Double, val lon: Double) : Effect
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    private var coverageRequested: String? = null
    private var savedLocationSeeded = false
    /** Last point checked, re-checked when the area arrives after it */
    private var lastChecked: Pair<Double, Double>? = null

    /**
     * Starts from the farm location that is already saved, so Confirm works without re-picking.
     * Runs once per ViewModel (a rotation does not re-fetch the address and weather).
     */
    fun seedSavedLocation(lat: Double, lon: Double) {
        if (savedLocationSeeded || _uiState.value.selected != null) return
        savedLocationSeeded = true
        selectPoint(lat, lon)
    }

    /** Loads the supported area of [countryCode] once; a blank code leaves the map without overlay. */
    fun loadCoverage(countryCode: String) {
        if (countryCode.isBlank() || coverageRequested == countryCode) return
        coverageRequested = countryCode
        _uiState.update { it.copy(isLoadingCoverage = true, coverageUnavailable = false) }
        viewModelScope.launch {
            val coverage = coverageRepo.get(countryCode)
            if (coverage == null) {
                // Never blocks: the user can still pick a point, it just is not checked
                _uiState.update { it.copy(isLoadingCoverage = false, coverageUnavailable = true) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoadingCoverage = false,
                    coverageGeoJson = coverage.geoJson,
                    coverageArea = coverage.area,
                )
            }
            // A point picked before the area arrived is checked now
            lastChecked?.let { (lat, lon) -> checkCoverage(lat, lon) }
        }
    }

    /**
     * The user (map tap) or GPS picked a point: remember it, check it against the supported area and
     * look up its address and weather.
     */
    fun selectPoint(lat: Double, lon: Double) {
        _uiState.update { it.copy(selected = SelectedLocation(lat, lon), locationProblem = null) }
        checkCoverage(lat, lon)
        fetchAddress(lat, lon)
        fetchWeather(lat, lon)
    }

    /** Works out whether the point is inside the supported area. */
    fun checkCoverage(lat: Double, lon: Double) {
        lastChecked = lat to lon
        val area = _uiState.value.coverageArea
        val status = when {
            area == null -> EnumCoverageStatus.UNKNOWN
            area.contains(lat, lon) -> EnumCoverageStatus.INSIDE
            else -> EnumCoverageStatus.OUTSIDE
        }
        _uiState.update { it.copy(coverageStatus = status) }
    }

    /** The location permission request was refused. */
    fun onPermissionDenied() {
        _uiState.update { it.copy(isLocating = false, locationProblem = LocationProblem.PERMISSION_DENIED) }
    }

    fun dismissLocationProblem() {
        _uiState.update { it.copy(locationProblem = null) }
    }

    fun fetchCurrentLocation() {
        if (_uiState.value.isLocating) return
        _uiState.update { it.copy(isLocating = true, locationProblem = null) }
        viewModelScope.launch {
            val result = LocationHelper().getCurrentLocation(context)
            _uiState.update { it.copy(isLocating = false) }
            when (result) {
                is LocationHelper.LocationResult.Success -> {
                    val lat = result.location.latitude
                    val lon = result.location.longitude
                    selectPoint(lat, lon)
                    _effect.send(Effect.FlyTo(lat, lon))
                }
                LocationHelper.LocationResult.LocationDisabled ->
                    _uiState.update { it.copy(locationProblem = LocationProblem.DISABLED) }
                LocationHelper.LocationResult.PermissionDenied ->
                    _uiState.update { it.copy(locationProblem = LocationProblem.PERMISSION_DENIED) }
                is LocationHelper.LocationResult.Error ->
                    _uiState.update { it.copy(locationProblem = LocationProblem.FAILED) }
            }
        }
    }

    private fun fetchAddress(lat: Double, lng: Double) = viewModelScope.launch {
        val service = GeocodingService(context, appSettings.locationIqToken)
        service.fetchAddressFlow(lat, lng).collect { result ->
            when (result) {
                is GeocodingService.GeocodingResult.Success ->
                    _uiState.update { it.copy(addressText = result.data.formattedAddress) }
                // No address (often offline): show the coordinates instead of an English error
                is GeocodingService.GeocodingResult.Error ->
                    _uiState.update {
                        it.copy(addressText = context.getString(R.string.lbl_lat_lng_format, lat, lng))
                    }
            }
        }
    }

    private fun fetchWeather(lat: Double, lng: Double) = viewModelScope.launch {
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
