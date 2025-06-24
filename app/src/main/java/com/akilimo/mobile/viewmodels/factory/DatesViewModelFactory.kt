package com.akilimo.mobile.viewmodels.factory // Or your preferred package

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.viewmodels.DatesViewModel

class DatesViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatesViewModel::class.java)) {
            return DatesViewModel(application) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}