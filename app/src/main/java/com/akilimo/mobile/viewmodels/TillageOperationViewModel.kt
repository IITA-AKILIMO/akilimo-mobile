package com.akilimo.mobile.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TillageOperationViewModel(
    application: Application,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _currentPractice = MutableLiveData<CurrentPractice?>()
    val currentPractice: LiveData<CurrentPractice?> get() = _currentPractice

    private val _dataIsValid = MutableLiveData(true)
    val dataIsValid: LiveData<Boolean> get() = _dataIsValid

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    companion object {
        private const val TAG = "TillageOperationVM"
    }

    init {
        loadCurrentPractice()
    }

    fun loadCurrentPractice() {
        viewModelScope.launch {
            Log.d(TAG, "Loading current practice...")
            try {
                val practice = withContext(dispatchers.io) {
                    database.currentPracticeDao().findOne() ?: CurrentPractice()
                }
                _currentPractice.postValue(practice)
            } catch (e: Exception) {
                val message = "Failed to load practice: ${e.localizedMessage}"
                Log.e(TAG, message, e)
                _errorMessage.postValue(message)
                Sentry.captureException(e)
            }
        }
    }

    fun updatePloughing(perform: Boolean, method: EnumOperationMethod = EnumOperationMethod.NONE) {
        updatePracticeField(
            field = "ploughing",
            perform = perform,
            method = method,
            update = { practice ->
                practice.performPloughing = perform
                practice.ploughingMethod = if (perform) method else EnumOperationMethod.NONE
            }
        )
    }

    fun updateRidging(perform: Boolean, method: EnumOperationMethod = EnumOperationMethod.NONE) {
        updatePracticeField(
            field = "ridging",
            perform = perform,
            method = method,
            update = { practice ->
                practice.performRidging = perform
                practice.ridgingMethod = if (perform) method else EnumOperationMethod.NONE
            }
        )
    }

    private fun updatePracticeField(
        field: String,
        perform: Boolean,
        method: EnumOperationMethod,
        update: (CurrentPractice) -> Unit
    ) {
        val current = currentPractice.value ?: CurrentPractice()
        val hasChanged = when (field) {
            "ploughing" -> current.performPloughing != perform || (perform && current.ploughingMethod != method)
            "ridging" -> current.performRidging != perform || (perform && current.ridgingMethod != method)
            else -> false
        }

        if (hasChanged) {
            update(current)
            _currentPractice.postValue(current)
            persistPractice(current)
        }
    }

    private fun persistPractice(practice: CurrentPractice) {
        viewModelScope.launch {
            try {
                withContext(dispatchers.io) {
                    database.currentPracticeDao().insert(practice)
                }
                _dataIsValid.postValue(true)
                _errorMessage.postValue(null)
            } catch (ex: Exception) {
                val message = ex.message ?: "Unknown error while saving practice"
                Log.e(TAG, message, ex)
                _dataIsValid.postValue(false)
                _errorMessage.postValue(message)
                Sentry.captureException(ex)
            }
        }
    }

    fun errorShown() {
        _errorMessage.value = null
    }
}
