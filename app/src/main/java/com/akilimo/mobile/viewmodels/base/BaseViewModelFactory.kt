package com.akilimo.mobile.viewmodels.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.ViewModelFactoryException

abstract class BaseViewModelFactory<T : ViewModel> : ViewModelProvider.Factory {

    abstract fun createViewModel(): T

    @Suppress("UNCHECKED_CAST")
    override fun <V : ViewModel> create(modelClass: Class<V>): V {
        try {
            return createViewModel() as V
        } catch (e: Exception) {
            throw ViewModelFactoryException(e.message ?: "Error creating ViewModel")
        }
    }
}