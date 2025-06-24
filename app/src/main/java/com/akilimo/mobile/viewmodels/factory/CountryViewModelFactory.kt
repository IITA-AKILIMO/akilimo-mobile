package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.viewmodels.CountryViewModel

class CountryViewModelFactory(
    private val application: Application,
    private val allowedCountries: Set<EnumCountry>
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountryViewModel::class.java)) {
            return CountryViewModel(application, allowedCountries) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}