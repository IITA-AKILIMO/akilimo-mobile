package com.akilimo.mobile.viewmodels // Or your preferred package

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TillageOperationViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TillageOperationViewModel::class.java)) {
            return TillageOperationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}