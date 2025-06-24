package com.akilimo.mobile.viewmodels.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException

abstract class BaseViewModelFactory<T : ViewModel> : ViewModelProvider.Factory {

    abstract fun createViewModel(): T

    @Suppress("UNCHECKED_CAST")
    override fun <V : ViewModel> create(modelClass: Class<V>): V {
        try {
            return createViewModel() as V
        } catch (e: Exception) {
            throw UnknownViewModelClassException("Error creating ViewModel")
        }
    }
}