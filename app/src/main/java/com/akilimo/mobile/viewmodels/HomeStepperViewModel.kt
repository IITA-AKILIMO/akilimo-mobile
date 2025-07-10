package com.akilimo.mobile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.ConfigRepository
import com.akilimo.mobile.utils.retryWithBackoff
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class HomeStepperViewModel(private val repository: ConfigRepository) : ViewModel() {
    private val _configData = MutableLiveData<Map<String, String>>()
    val configData: LiveData<Map<String, String>> = _configData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadRemoteConfig() {
        val handler = CoroutineExceptionHandler { _, exception ->
            _errorMessage.postValue(exception.localizedMessage)
        }

        viewModelScope.launch(handler) {
            try {
                val config = retryWithBackoff { repository.fetchConfig("akilimo") }
                _configData.postValue(config)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to fetch config: ${e.localizedMessage}")
            }
        }
    }

}