package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.viewmodels.FertilizersViewModel

class FertilizersViewModelFactory(
    private val application: Application,
    private val minSelection: Int = 2,
    private val useCase: String?
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FertilizersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FertilizersViewModel(
                application = application,
                minSelection = minSelection,
                useCase = useCase
            ) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}