package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.viewmodels.SummaryViewModel


class SummaryViewModelFactory(
    private val application: Application,
    private val mathHelper: MathHelper
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SummaryViewModel(
                application = application,
                mathHelper = mathHelper
            ) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}