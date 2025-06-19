package com.akilimo.mobile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.data.ConfigRepository
import com.akilimo.mobile.interfaces.FuelrodApi
import com.akilimo.mobile.rest.retrofit.RetrofitManager

class HomeStepperViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val fuelrodApi = FuelrodApi(RetrofitManager)
        val repository = ConfigRepository(fuelrodApi)
        return HomeStepperViewModel(repository) as T
    }
}