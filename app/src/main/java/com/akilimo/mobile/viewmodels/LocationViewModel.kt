package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.interfaces.LocationProvider
import com.akilimo.mobile.repo.LocationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationViewModel(
    private val application: Application,
    private val repo: LocationRepository,
    private val locationProvider: LocationProvider,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {


    private val profileDao = database.profileInfoDao()
    private val locationDao = database.locationInfoDao()


    private val _farmName = MutableLiveData<String>()
    val farmName: LiveData<String> = _farmName

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> = _fullName

    private val _locationInfo = MutableLiveData<UserLocation?>()
    val locationInfo: LiveData<UserLocation?> = _locationInfo


    fun fetchAndSaveCurrentLocation(onFail: (String) -> Unit) {
        locationProvider.getCurrentLocation { coords ->
            if (coords == null) {
                viewModelScope.launch(dispatchers.main) {
                    onFail("Could not get current location")
                }
                return@getCurrentLocation
            }

            val (lat, lon, alt) = coords

            viewModelScope.launch(dispatchers.io) {
                val result = repo.reverseGeocode(lat, lon)
                if (result != null) {
                    val (code, name) = result
                    saveLocation(code, name, lat, lon, alt)
                } else {
                    withContext(dispatchers.main) {
                        onFail("Reverse geocode failed")
                    }
                }
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch(dispatchers.io) {
            val profile = profileDao.findOne()
            val location = locationDao.findOne()

            profile?.let {
                _farmName.postValue(it.farmName)
                _fullName.postValue("${it.firstName} ${it.lastName}")
            }

            _locationInfo.postValue(location)
        }
    }

    fun saveFarmName(name: String) {
        viewModelScope.launch(dispatchers.io) {
            val profile = profileDao.findOne() ?: return@launch
            profile.farmName = name
            profileDao.insert(profile)
            _farmName.postValue(name)
        }
    }

    fun saveLocation(
        countryCode: String,
        countryName: String,
        lat: Double,
        lon: Double,
        alt: Double
    ) {
        viewModelScope.launch(dispatchers.io) {
            val location = locationDao.findOne() ?: UserLocation()
            location.apply {
                locationCountryCode = countryCode
                locationCountryName = countryName
                latitude = lat
                longitude = lon
                altitude = alt
            }
            locationDao.insert(location)
            _locationInfo.postValue(location)
        }
    }


    /**
     * Perform reverse geocoding and save result.
     */
    fun reverseGeocodeAndSave(
        lat: Double,
        lon: Double,
        alt: Double = 0.0,
        onFail: (String) -> Unit
    ) {
        viewModelScope.launch(dispatchers.io) {
            val result = repo.reverseGeocode(lat, lon)
            if (result != null) {
                val (code, name) = result
                saveLocation(code, name, lat, lon, alt)
            } else {
                withContext(dispatchers.main) {
                    onFail("Unable to reverse geocode selected location.")
                }
            }
        }
    }
}