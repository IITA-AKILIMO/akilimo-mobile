package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.data.CountryOption
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumCountry
import kotlinx.coroutines.launch

class CountryViewModel(
    private val application: Application,
    private val allowedCountries: Set<EnumCountry>,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    // @TODO: consider migrating to StateFLow as opposed to LiveData https://medium.com/@codzure/livedata-vs-stateflow-the-battle-of-the-observables-730f846be812
    private val profileDao = database.profileInfoDao()

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    val countries: List<CountryOption> by lazy {
        EnumCountry.entries
            .filter { it in allowedCountries }
            .map {
                CountryOption(
                    displayLabel = it.name,
                    value = it.countryCode(),
                    currencyCode = it.currencyName(application),
                )
            }
    }

    fun loadProfile() {
        viewModelScope.launch(dispatchers.io) {
            val profile = profileDao.findOne()
            _userProfile.postValue(profile)
        }
    }

    fun updateCountrySelection(code: String, name: String, currency: String) {
        viewModelScope.launch(dispatchers.io) {
            val profile = profileDao.findOne() ?: UserProfile()
            profile.apply {
                countryCode = code
                countryName = name
                currencyCode = currency
            }
            profileDao.insert(profile)
            _userProfile.postValue(profile)
        }
    }
}