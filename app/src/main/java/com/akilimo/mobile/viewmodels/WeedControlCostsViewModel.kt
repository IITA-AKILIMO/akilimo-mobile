package com.akilimo.mobile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.repo.DatabaseRepository
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumWeedControlMethod
import kotlinx.coroutines.launch

class WeedControlCostsViewModel(private val repo: DatabaseRepository) : ViewModel() {

    private val _currentPractice = MutableLiveData(CurrentPractice())
    val currentPractice: LiveData<CurrentPractice> = _currentPractice

    private val _fieldOperationCost = MutableLiveData(FieldOperationCost())
    val fieldOperationCost: LiveData<FieldOperationCost> = _fieldOperationCost

    private val _saveStatus = MutableLiveData<Result<Unit>>()
    val saveStatus: LiveData<Result<Unit>> = _saveStatus

    val areaUnit = MutableLiveData<String>()
    val fieldSize = MutableLiveData<Double>()
    val currencyCode = MutableLiveData<String>()
    val currencyName = MutableLiveData<String>()

    fun loadInitialData() {
        viewModelScope.launch {
            repo.getCurrentPractice()?.let { _currentPractice.value = it }
            repo.getFieldOperationCost()?.let { _fieldOperationCost.value = it }
        }
    }

    fun updateWeedControlMethod(
        method: EnumWeedControlMethod,
        radioId: Int,
        usesHerbicide: Boolean
    ) {
        _currentPractice.value = _currentPractice.value?.copy(
            weedControlMethod = method,
            usesHerbicide = usesHerbicide,
            weedRadioIndex = radioId
        )
    }

    fun updateWeedingCosts(firstCost: Double, secondCost: Double) {
        _fieldOperationCost.value = _fieldOperationCost.value?.copy(
            firstWeedingOperationCost = firstCost,
            secondWeedingOperationCost = secondCost
        )
    }

    fun saveData() {
        viewModelScope.launch {
            try {
                _currentPractice.value?.let { repo.saveCurrentPractice(it) }
                _fieldOperationCost.value?.let { repo.saveFieldOperationCost(it) }
                repo.saveAdviceStatus(AdviceStatus(EnumAdviceTask.COST_OF_WEED_CONTROL.name, true))
                _saveStatus.value = Result.success(Unit)
            } catch (ex: Exception) {
                _saveStatus.value = Result.failure(ex)
            }
        }
    }
}
