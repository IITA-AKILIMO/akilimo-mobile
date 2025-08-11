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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatesViewModel(
    private val application: Application,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {
    private val _schedule = MutableLiveData<CropSchedule>()
    val schedule: LiveData<CropSchedule> = _schedule

    private val _plantingDate = MutableLiveData<String>()
    val plantingDate: LiveData<String> = _plantingDate

    private val _harvestDate = MutableLiveData<String>()
    val harvestDate: LiveData<String> = _harvestDate
    private val _plantingWindow = MutableLiveData(0)
    val plantingWindow: LiveData<Int> = _plantingWindow
    private val _harvestWindow = MutableLiveData(0)
    val harvestWindow: LiveData<Int> = _harvestWindow
    private val _alternativeDate = MutableLiveData(false)
    val alternativeDate: LiveData<Boolean> = _alternativeDate
    private val _alreadyPlanted = MutableLiveData(false)
    val alreadyPlanted: LiveData<Boolean> = _alreadyPlanted

    fun loadSchedule() {
        viewModelScope.launch(dispatchers.io) {
            val scheduleData = database.scheduleDateDao().findOne() ?: CropSchedule()
            _schedule.postValue(scheduleData)
            _plantingDate.postValue(scheduleData.plantingDate)
            _harvestDate.postValue(scheduleData.harvestDate)
            _plantingWindow.postValue(scheduleData.plantingWindow)
            _harvestWindow.postValue(scheduleData.harvestWindow)
            _alternativeDate.postValue(scheduleData.alternativeDate)
            _alreadyPlanted.postValue(scheduleData.alreadyPlanted)
        }
    }

    fun updatePlantingDate(date: String) {
        val planted = DateHelper.olderThanCurrent(date)
        _plantingDate.postValue(date)
        _harvestDate.postValue("")
        _alreadyPlanted.postValue(planted)
        if (planted) {
            _plantingWindow.postValue(0)
        }
    }

    fun updateHarvestDate(date: String) {
        _harvestDate.value = date
    }

    fun setPlantingWindow(window: Int) {
        _plantingWindow.postValue(window)
    }

    fun clearPlantingWindow() {
        _plantingWindow.postValue(0)
    }

    fun setHarvestWindow(window: Int) {
        _harvestWindow.postValue(window)
    }

    fun clearHarvestWindow() {
        _harvestWindow.postValue(0)
    }

    fun setAlternativeDate(value: Boolean) {
        _alternativeDate.postValue(value)
    }

    fun saveSchedule(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val cropSchedule = database.scheduleDateDao().findOne() ?: CropSchedule()
                cropSchedule.apply {
                    plantingDate = _plantingDate.value.orEmpty()
                    harvestDate = _harvestDate.value.orEmpty()
                    plantingWindow = _plantingWindow.value ?: 0
                    harvestWindow = _harvestWindow.value ?: 0
                    alternativeDate = _alternativeDate.value ?: false
                    alreadyPlanted = _alreadyPlanted.value ?: false
                }
                database.scheduleDateDao().insert(cropSchedule)
                withContext(dispatchers.main) { onSuccess() }
            } catch (e: Exception) {
                withContext(dispatchers.main) { onError(e) }
            }
        }
    }
}