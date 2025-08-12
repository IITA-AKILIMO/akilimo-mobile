package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.viewmodels.SweetPotatoMarketViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class SweetPotatoMarketViewModelFactory(
    private val application: Application,
    private val mathHelper: MathHelper,
) :
    BaseViewModelFactory<SweetPotatoMarketViewModel>() {
    override fun createViewModel(): SweetPotatoMarketViewModel {
        return SweetPotatoMarketViewModel(application, mathHelper)
    }
}