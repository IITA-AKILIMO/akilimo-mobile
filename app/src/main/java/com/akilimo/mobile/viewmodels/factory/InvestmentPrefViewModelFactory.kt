package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.data.RiskOption
import com.akilimo.mobile.exceptions.UnknownViewModelClassException
import com.akilimo.mobile.viewmodels.InvestmentPrefViewModel

class InvestmentPrefViewModelFactory(
    private val application: Application,
    private val riskOptions: List<RiskOption>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvestmentPrefViewModel::class.java)) {
            return InvestmentPrefViewModel(application, riskOptions) as T
        }
        throw UnknownViewModelClassException("Unknown ViewModel class")
    }
}