package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.DateHelper
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatesViewModel(
    private val application: Application,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {
    private val _schedule = MutableLiveData<CropSchedule>()
    val schedule: LiveData<CropSchedule> get() = _schedule

    val plantingDate = MutableLiveData<String>()
    val harvestDate = MutableLiveData<String>()
    val plantingWindow = MutableLiveData(0)
    val harvestWindow = MutableLiveData(0)
    val alternativeDate = MutableLiveData(false)
    val alreadyPlanted = MutableLiveData(false)

    fun loadSchedule() {
        viewModelScope.launch(dispatchers.io) {
            val scheduleData = database.scheduleDateDao().findOne() ?: CropSchedule()
            withContext(dispatchers.main) {
                _schedule.value = scheduleData
                plantingDate.value = scheduleData.plantingDate
                harvestDate.value = scheduleData.harvestDate
                plantingWindow.value = scheduleData.plantingWindow
                harvestWindow.value = scheduleData.harvestWindow
                alternativeDate.value = scheduleData.alternativeDate
                alreadyPlanted.value = scheduleData.alreadyPlanted
            }
        }
    }

    fun updatePlantingDate(date: String) {
        plantingDate.value = date
        harvestDate.value = ""
        alreadyPlanted.value = DateHelper.olderThanCurrent(date)
        if (alreadyPlanted.value == true) {
            plantingWindow.value = 0
        }
    }

    fun updateHarvestDate(date: String) {
        harvestDate.value = date
    }

    fun saveSchedule(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val cropSchedule = database.scheduleDateDao().findOne() ?: CropSchedule()
                cropSchedule.apply {
                    plantingDate = this@DatesViewModel.plantingDate.value.orEmpty()
                    harvestDate = this@DatesViewModel.harvestDate.value.orEmpty()
                    plantingWindow = this@DatesViewModel.plantingWindow.value ?: 0
                    harvestWindow = this@DatesViewModel.harvestWindow.value ?: 0
                    alternativeDate = this@DatesViewModel.alternativeDate.value ?: false
                    alreadyPlanted = this@DatesViewModel.alreadyPlanted.value ?: false
                }
                database.scheduleDateDao().insert(cropSchedule)
                database.adviceStatusDao()
                    .insert(AdviceStatus(EnumAdviceTask.PLANTING_AND_HARVEST.name, true))
                withContext(dispatchers.main) { onSuccess() }
            } catch (e: Exception) {
                withContext(dispatchers.main) { onError(e) }
            }
        }
    }
}