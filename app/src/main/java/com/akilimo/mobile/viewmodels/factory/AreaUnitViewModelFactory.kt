package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.ViewModelFactoryException
import com.akilimo.mobile.viewmodels.AreaUnitViewModel

class AreaUnitViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AreaUnitViewModel::class.java)) {
            return AreaUnitViewModel(application) as T
        }
        throw ViewModelFactoryException("Unknown ViewModel class")
    }
}