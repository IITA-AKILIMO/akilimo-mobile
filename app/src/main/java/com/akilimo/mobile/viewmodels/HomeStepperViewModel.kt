package com.akilimo.mobile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.interfaces.FuelrodApi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class HomeStepperViewModel : ViewModel() {
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
                val configList = FuelrodApi.apiService.readConfig("akilimo")
                if (configList.isNotEmpty()) {
                    val configMap = configList.associate { it.configName to it.configValue }
                    _configData.postValue(configMap)
                } else {
                    _errorMessage.postValue("Config list is empty")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Exception: ${e.localizedMessage}")
            }
        }
    }

}