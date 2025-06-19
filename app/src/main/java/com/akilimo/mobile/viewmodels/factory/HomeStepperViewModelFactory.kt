package com.akilimo.mobile.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.data.ConfigRepository
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.interfaces.FuelrodApi
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import com.akilimo.mobile.viewmodels.HomeStepperViewModel

class HomeStepperViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeStepperViewModel::class.java)) {
            val fuelrodApi = FuelrodApi(RetrofitManager)
            val repository = ConfigRepository(fuelrodApi)
            return HomeStepperViewModel(repository) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}