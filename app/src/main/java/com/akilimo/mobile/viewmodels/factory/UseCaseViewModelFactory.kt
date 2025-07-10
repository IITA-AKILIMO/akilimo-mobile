package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.viewmodels.UseCaseViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class UseCaseViewModelFactory(
    private val application: Application,
    private val useCaseWithTasks: List<UseCaseWithTasks>
) : BaseViewModelFactory<UseCaseViewModel>() {
    override fun createViewModel(): UseCaseViewModel {
        return UseCaseViewModel(application, useCaseWithTasks)
    }

}