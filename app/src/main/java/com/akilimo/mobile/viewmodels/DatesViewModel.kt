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
    val schedule: LiveData<CropSchedule> get() = _schedule

    private val _plantingDate = MutableLiveData<String>()
    val plantingDate: LiveData<String> get() = _plantingDate

    private val _harvestDate = MutableLiveData<String>()
    val harvestDate: LiveData<String> get() = _harvestDate
    private val _plantingWindow = MutableLiveData(0)
    val plantingWindow: LiveData<Int> get() = _plantingWindow
    private val _harvestWindow = MutableLiveData(0)
    val harvestWindow: LiveData<Int> get() = _harvestWindow
    private val _alternativeDate = MutableLiveData(false)
    val alternativeDate: LiveData<Boolean> get() = _alternativeDate
    private val _alreadyPlanted = MutableLiveData(false)
    val alreadyPlanted: LiveData<Boolean> get() = _alreadyPlanted

    fun loadSchedule() {
        viewModelScope.launch(dispatchers.io) {
            val scheduleData = database.scheduleDateDao().findOne() ?: CropSchedule()
            withContext(dispatchers.main) {
                _schedule.value = scheduleData
                _plantingDate.value = scheduleData.plantingDate
                _harvestDate.value = scheduleData.harvestDate
                _plantingWindow.value = scheduleData.plantingWindow
                _harvestWindow.value = scheduleData.harvestWindow
                _alternativeDate.value = scheduleData.alternativeDate
                _alreadyPlanted.value = scheduleData.alreadyPlanted
            }
        }
    }

    fun updatePlantingDate(date: String) {
        _plantingDate.value = date
        _harvestDate.value = ""
        _alreadyPlanted.value = DateHelper.olderThanCurrent(date)
        if (_alreadyPlanted.value == true) {
            _plantingWindow.value = 0
        }
    }

    fun updateHarvestDate(date: String) {
        _harvestDate.value = date
    }

    fun saveSchedule(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val cropSchedule = database.scheduleDateDao().findOne() ?: CropSchedule()
                cropSchedule.apply {
                    plantingDate = this@DatesViewModel._plantingDate.value.orEmpty()
                    harvestDate = this@DatesViewModel._harvestDate.value.orEmpty()
                    plantingWindow = this@DatesViewModel._plantingWindow.value ?: 0
                    harvestWindow = this@DatesViewModel._harvestWindow.value ?: 0
                    alternativeDate = this@DatesViewModel._alternativeDate.value ?: false
                    alreadyPlanted = this@DatesViewModel._alreadyPlanted.value ?: false
                }
                database.scheduleDateDao().insert(cropSchedule)
                withContext(dispatchers.main) { onSuccess() }
            } catch (e: Exception) {
                withContext(dispatchers.main) { onError(e) }
            }
        }
    }
}