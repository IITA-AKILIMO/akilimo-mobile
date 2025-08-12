package com.akilimo.mobile.viewmodels.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.utils.ui.SnackBarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Suppress("PropertyName")
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    protected val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    protected val _showSnackBarEvent = MutableLiveData<SnackBarMessage?>()
    val showSnackBarEvent: LiveData<SnackBarMessage?> = _showSnackBarEvent

    protected open fun launchWithState(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewModelScope.launch(start = start) {
            try {
                block()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    protected open fun onError(e: Exception) {
        e.printStackTrace()
    }

    protected fun showSnackBar(message: String) {
        _showSnackBarEvent.postValue(SnackBarMessage.Text(message))
    }

    protected fun showSnackBar(@androidx.annotation.StringRes resId: Int) {
        _showSnackBarEvent.postValue(SnackBarMessage.Resource(resId))
    }

}
