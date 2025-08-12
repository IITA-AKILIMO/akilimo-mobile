package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.viewmodels.RootYieldViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class RootYieldViewModelFactory(private val application: Application) :
    BaseViewModelFactory<RootYieldViewModel>() {
    override fun createViewModel(): RootYieldViewModel {
        return RootYieldViewModel(application)
    }
}