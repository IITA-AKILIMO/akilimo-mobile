package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.viewmodels.FertilizersViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class FertilizersViewModelFactory(
    private val application: Application,
    private val minSelection: Int = 2,
    private val useCase: String?
) : BaseViewModelFactory<FertilizersViewModel>() {
    override fun createViewModel(): FertilizersViewModel {
        return FertilizersViewModel(
            application = application,
            minSelection = minSelection,
            useCase = useCase
        )
    }

}