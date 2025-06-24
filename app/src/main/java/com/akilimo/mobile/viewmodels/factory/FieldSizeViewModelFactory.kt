package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.viewmodels.FieldSizeViewModel

class FieldSizeViewModelFactory(
    private val application: Application,
    private val mathHelper: MathHelper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FieldSizeViewModel::class.java)) {
            return FieldSizeViewModel(application, mathHelper) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}