package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.viewmodels.InvestmentAmountViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class InvestmentAmountViewModelFactory(
    private val application: Application,
    private val mathHelper: MathHelper,
) : BaseViewModelFactory<InvestmentAmountViewModel>() {
    override fun createViewModel(): InvestmentAmountViewModel {
        return InvestmentAmountViewModel(application, mathHelper)
    }
}