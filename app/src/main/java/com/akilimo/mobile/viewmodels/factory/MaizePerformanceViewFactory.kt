package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.viewmodels.MaizePerformanceViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class MaizePerformanceViewFactory(private val application: Application) :
    BaseViewModelFactory<MaizePerformanceViewModel>() {
    override fun createViewModel(): MaizePerformanceViewModel {
        return MaizePerformanceViewModel(application)
    }
}