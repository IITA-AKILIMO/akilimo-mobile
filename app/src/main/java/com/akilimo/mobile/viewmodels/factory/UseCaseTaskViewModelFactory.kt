package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.viewmodels.UseCaseTasksViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class UseCaseTaskViewModelFactory(
    private val application: Application,
    private val useCaseTasks: List<UseCaseTask>
) : BaseViewModelFactory<UseCaseTasksViewModel>() {
    override fun createViewModel(): UseCaseTasksViewModel {
        return UseCaseTasksViewModel(application, useCaseTasks)
    }

}