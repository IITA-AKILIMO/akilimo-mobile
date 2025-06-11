package com.akilimo.mobile.viewmodels // Or your preferred package

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
    private val application: Application,
    private val database: AppDatabase = AppDatabase.getDatabase(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _currentPractice = MutableLiveData<CurrentPractice?>()
    val currentPractice: LiveData<CurrentPractice?> = _currentPractice

    private val _dataIsValid = MutableLiveData<Boolean>(true)
    val dataIsValid: LiveData<Boolean> = _dataIsValid

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    companion object {
        private const val TAG = "TillageOperationVM"
    }

    init {
        loadCurrentPractice()
    }

    fun loadCurrentPractice() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d(TAG, "Loading current practice from database...")
            try {
                // Perform database read on a background thread
                val practice = withContext(dispatchers.io) {
                    database.currentPracticeDao().findOne() ?: CurrentPractice()
                }
                _currentPractice.value = practice
                val endTime = System.currentTimeMillis()
                Log.d(TAG, "Current practice loaded in ${endTime - startTime} ms")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading current practice", e)
                _errorMessage.value = "Error loading data: ${e.localizedMessage}"
                Sentry.captureException(e)
            }
        }
    }

    fun updatePloughing(perform: Boolean, method: EnumOperationMethod = EnumOperationMethod.NONE) {
        val practice = _currentPractice.value ?: CurrentPractice()
        if (practice.performPloughing != perform || (perform && practice.ploughingMethod != method)) {
            practice.performPloughing = perform
            practice.ploughingMethod = if (perform) method else EnumOperationMethod.NONE
            _currentPractice.value = practice // Trigger LiveData update
            saveCurrentPracticeToDatabase()
        }
    }

    fun updateRidging(perform: Boolean, method: EnumOperationMethod = EnumOperationMethod.NONE) {
        val practice = _currentPractice.value ?: CurrentPractice()
        if (practice.performRidging != perform || (perform && practice.ridgingMethod != method)) {
            practice.performRidging = perform
            practice.ridgingMethod = if (perform) method else EnumOperationMethod.NONE
            _currentPractice.value = practice // Trigger LiveData update
            saveCurrentPracticeToDatabase()
        }
    }


    private fun saveCurrentPracticeToDatabase() {
        val practiceToSave = _currentPractice.value ?: return // Nothing to save

        viewModelScope.launch {
            try {
                withContext(dispatchers.io) {
                    database.currentPracticeDao().insert(practiceToSave)
                }
                _dataIsValid.value = true
                _errorMessage.value = null
            } catch (ex: Exception) {
                _dataIsValid.value = false
                _errorMessage.value = ex.message ?: "Unknown error during save"
                Log.e(TAG, "Error saving current practice: ${_errorMessage.value}", ex)
                Sentry.captureException(ex)
            }
        }
    }

    fun errorShown() {
        _errorMessage.value = null
    }
}