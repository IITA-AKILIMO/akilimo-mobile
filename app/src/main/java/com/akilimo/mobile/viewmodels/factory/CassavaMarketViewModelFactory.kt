package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.repo.CassavaMarketRepo
import com.akilimo.mobile.viewmodels.CassavaMarketViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class CassavaMarketViewModelFactory(
    private val application: Application
) : BaseViewModelFactory<CassavaMarketViewModel>() {
    override fun createViewModel(): CassavaMarketViewModel {
        return CassavaMarketViewModel(application)
    }

}