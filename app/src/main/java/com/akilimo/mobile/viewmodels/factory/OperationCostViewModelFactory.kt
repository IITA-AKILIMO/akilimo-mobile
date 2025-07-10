package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.viewmodels.OperationCostViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class OperationCostViewModelFactory(private val application: Application) :
    BaseViewModelFactory<OperationCostViewModel>() {
    override fun createViewModel(): OperationCostViewModel {
        return OperationCostViewModel(application)
    }

}