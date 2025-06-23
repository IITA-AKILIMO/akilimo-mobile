package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.interfaces.LocationProvider
import com.akilimo.mobile.repo.LocationRepository
import com.akilimo.mobile.viewmodels.LocationViewModel

class LocationViewModelFactory(
    private val application: Application,
    private val repo: LocationRepository,
    private val locationProvider: LocationProvider
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(
                application = application,
                repo = repo,
                locationProvider = locationProvider
            ) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}