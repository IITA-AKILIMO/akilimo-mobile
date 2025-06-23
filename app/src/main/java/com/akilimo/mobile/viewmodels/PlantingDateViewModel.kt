package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.DateHelper
import io.sentry.Sentry
import kotlinx.coroutines.launch

class PlantingDateViewModel(
    private val application: Application,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    val repo = database.scheduleDateDao()

    private val _plantingDate = MutableLiveData<String>()
    val plantingDate: LiveData<String> = _plantingDate

    private val _harvestDate = MutableLiveData<String>()
    val harvestDate: LiveData<String> = _harvestDate

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _alreadyPlanted = MutableLiveData<Boolean>()
    val alreadyPlanted: LiveData<Boolean> = _alreadyPlanted

    fun loadInitialDates() {
        viewModelScope.launch(dispatchers.io) {
            repo.findOne()?.let {
                _plantingDate.postValue(it.plantingDate)
                _harvestDate.postValue(it.harvestDate)
            }
        }
    }


    fun setPlantingDate(date: String) {
        _plantingDate.postValue(date)
        _harvestDate.postValue("")
    }

    fun setHarvestDate(date: String) {
        _harvestDate.postValue(date)
    }

    fun saveSchedule() {
        viewModelScope.launch(dispatchers.io) {
            val planting = _plantingDate.value.orEmpty()
            val harvest = _harvestDate.value.orEmpty()

            val error = when {
                planting.isBlank() -> "Please select a planting date"
                harvest.isBlank() -> "Please select a harvest date"
                else -> null
            }

            if (error != null) {
                _errorMessage.postValue(error)
                return@launch
            }

            try {
                val cropSchedule = repo.findOne() ?: CropSchedule()
                cropSchedule.apply {
                    plantingDate = planting
                    harvestDate = harvest
                    alreadyPlanted = DateHelper.olderThanCurrent(planting)
                }
                repo.insert(cropSchedule)
                _alreadyPlanted.postValue(cropSchedule.alreadyPlanted)
            } catch (ex: Exception) {
                _errorMessage.postValue(ex.message ?: "An unexpected error occurred")
                Sentry.captureException(ex)
            }
        }
    }
}