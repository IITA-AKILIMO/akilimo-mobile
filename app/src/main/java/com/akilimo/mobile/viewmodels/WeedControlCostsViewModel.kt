package com.akilimo.mobile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.repo.DatabaseRepository
import com.akilimo.mobile.utils.enums.EnumWeedControlMethod
import kotlinx.coroutines.launch

class WeedControlCostsViewModel(private val repo: DatabaseRepository) : ViewModel() {

    private val _useCaseTask = MutableLiveData(UseCaseTask())
    val useCaseTask: LiveData<UseCaseTask> = _useCaseTask
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

    fun loadInitialData(useCaseId: Long) {
        viewModelScope.launch {
            repo.getCurrentPractice()?.let { _currentPractice.postValue(it) }
            repo.getFieldOperationCost()?.let { _fieldOperationCost.postValue(it) }
            repo.getUseCaseTask(useCaseId)?.let { _useCaseTask.postValue(it) }
        }
    }

    fun updateWeedControlMethod(
        method: EnumWeedControlMethod,
        radioId: Int,
        usesHerbicide: Boolean
    ) {
        _currentPractice.postValue(
            _currentPractice.value?.copy(
                weedControlMethod = method,
                usesHerbicide = usesHerbicide,
                weedRadioIndex = radioId
            )
        )
    }

    fun updateWeedingCosts(firstCost: Double, secondCost: Double) {
        _fieldOperationCost.postValue(
            _fieldOperationCost.value?.copy(
                firstWeedingOperationCost = firstCost,
                secondWeedingOperationCost = secondCost
            )
        )
    }

    fun saveData() {
        viewModelScope.launch {
            try {
                _currentPractice.value?.let { repo.saveCurrentPractice(it) }
                _fieldOperationCost.value?.let { repo.saveFieldOperationCost(it) }
                useCaseTask.value?.let { task ->
                    val updatedTask = task.copy(completed = true)
                    repo.updateUseCaseTask(updatedTask)
                    _useCaseTask.postValue(updatedTask) // Reflect change in UI
                }
                _saveStatus.postValue(Result.success(Unit))
            } catch (ex: Exception) {
                _saveStatus.postValue(Result.failure(ex))
            }
        }
    }
}
