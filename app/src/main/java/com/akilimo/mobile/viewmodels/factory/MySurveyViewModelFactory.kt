package com.akilimo.mobile.viewmodels.factory

import android.app.Application
import com.akilimo.mobile.utils.PreferenceManager
import com.akilimo.mobile.viewmodels.MySurveyViewModel
import com.akilimo.mobile.viewmodels.base.BaseViewModelFactory

class MySurveyViewModelFactory(
    private val application: Application,
    private val preferenceManager: PreferenceManager
) :
    BaseViewModelFactory<MySurveyViewModel>() {
    override fun createViewModel(): MySurveyViewModel {
        return MySurveyViewModel(application, preferenceManager)
    }
}