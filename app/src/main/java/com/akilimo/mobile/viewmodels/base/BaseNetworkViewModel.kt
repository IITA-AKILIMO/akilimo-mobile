package com.akilimo.mobile.viewmodels.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.ui.SnackBarMessage
import io.sentry.Sentry
import kotlinx.coroutines.launch

abstract class BaseNetworkViewModel(
    application: Application,
    private val dispatchers: IDispatcherProvider
) : AndroidViewModel(application) {

    private var networkFailureCount = 0
    private var lastFailureTime: Long = 0
    private val failureCooldownMillis = 30 * 60 * 1000L // 30 minutes
    private val maxFailures = 3

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val _showSnackBarEvent = MutableLiveData<SnackBarMessage?>()
    val showSnackBarEvent: LiveData<SnackBarMessage?> = _showSnackBarEvent


    protected fun launchWithState(block: suspend () -> Unit) =
        viewModelScope.launch(dispatchers.io) {
            _loading.postValue(true)
            _error.postValue(false)
            try {
                block()
                resetFailureCount()
            } catch (e: Exception) {
                trackFailure()
                handleError(e)
            } finally {
                _loading.postValue(false)
            }
        }

    protected fun launchIgnoreErrors(block: suspend () -> Unit) =
        viewModelScope.launch(dispatchers.io) {
            try {
                block()
                resetFailureCount()
            } catch (e: Exception) {
                handleError(e)
            }
        }

    protected fun canMakeNetworkCall(): Boolean {
        val now = System.currentTimeMillis()
        return if (networkFailureCount >= maxFailures && (now - lastFailureTime) < failureCooldownMillis) {
            showSnackBar("Using offline data due to network issues.")
            false
        } else true
    }

    protected fun showSnackBar(message: String) {
        _showSnackBarEvent.postValue(SnackBarMessage.Text(message))
    }

    protected fun showSnackBar(@androidx.annotation.StringRes resId: Int) {
        _showSnackBarEvent.postValue(SnackBarMessage.Resource(resId))
    }

    private fun trackFailure() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFailureTime > failureCooldownMillis) {
            networkFailureCount = 1
        } else {
            networkFailureCount++
        }
        lastFailureTime = currentTime
    }

    private fun resetFailureCount() {
        networkFailureCount = 0
    }

    private fun handleError(e: Exception) {
        _error.postValue(true)
        Sentry.captureException(e)
        showSnackBar(e.message ?: "Unknown error")
    }

    fun clearSnackBarEvent() {
        _showSnackBarEvent.postValue(null)
    }
}