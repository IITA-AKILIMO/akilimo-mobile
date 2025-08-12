package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.rest.request.SurveyRequest
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.PreferenceManager
import com.akilimo.mobile.viewmodels.base.BaseViewModel

class MySurveyViewModel(
    private val application: Application,
    private val preferenceManager: PreferenceManager,
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider(),
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
) : BaseViewModel(application) {
    private val _akilimoUsage = MutableLiveData<String>()
    val akilimoUsage: LiveData<String> = _akilimoUsage

    private val _akilimoRecRating = MutableLiveData<Int>()
    val akilimoRecRating: LiveData<Int> = _akilimoRecRating

    private val _akilimoUsefulRating = MutableLiveData<Int>()
    val akilimoUsefulRating: LiveData<Int> = _akilimoUsefulRating

    private val _successEvent = MutableLiveData<Unit>()
    val successEvent: LiveData<Unit> = _successEvent

    fun setAkilimoUsage(value: String) {
        _akilimoUsage.postValue(value)
    }

    fun setRecRating(value: Int) {
        _akilimoRecRating.postValue(value)
    }

    fun setUsefulRating(value: Int) {
        _akilimoUsefulRating.postValue(value)
    }

    fun submitSurvey() = launchWithState {
        val surveyRequest = SurveyRequest(
            akilimoUsage = _akilimoUsage.value ?: "",
            akilimoRecRating = _akilimoRecRating.value ?: 0,
            akilimoUsefulRating = _akilimoUsefulRating.value ?: 0,
            language = LanguageManager.getLanguage(application),
            deviceToken = preferenceManager.deviceToken
        )

        val resp = akilimoService.submitUserReview(surveyRequest)
        if (resp.isSuccessful) {
            showSnackBar("Feedback submitted successfully")
            _successEvent.postValue(Unit)
        } else {
            showSnackBar("Failed to submit feedback")
        }
    }
}