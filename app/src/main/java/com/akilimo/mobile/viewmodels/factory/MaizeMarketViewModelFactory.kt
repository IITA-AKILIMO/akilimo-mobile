package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.viewmodels.MaizeMarketViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class MaizeMarketViewModelFactory(
    private val application: Application,
    private val mathHelper: MathHelper
) :
    BaseViewModelFactory<MaizeMarketViewModel>() {
    override fun createViewModel(): MaizeMarketViewModel {
        return MaizeMarketViewModel(application, mathHelper)
    }
}