package com.akilimo.mobile.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.repo.DatabaseRepository
import com.akilimo.mobile.viewmodels.WeedControlCostsViewModel

class WeedControlCostsViewModelFactory(private val repository: DatabaseRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeedControlCostsViewModel::class.java)) {
            return WeedControlCostsViewModel(repository) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}