package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.viewmodels.PlantingDateViewModel

class PlantingDateViewModelFactory(
    private val application: Application,
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantingDateViewModel::class.java)) {
            return PlantingDateViewModel(
                application = application
            ) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}