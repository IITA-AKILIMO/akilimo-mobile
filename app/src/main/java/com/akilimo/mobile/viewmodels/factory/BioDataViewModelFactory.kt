package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.utils.PreferenceManager
import com.akilimo.mobile.viewmodels.BioDataViewModel

class BioDataViewModelFactory(
    private val application: Application,
    private val prefs: PreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BioDataViewModel::class.java)) {
            return BioDataViewModel(application, prefs) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}
