package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.viewmodels.base.BaseNetworkViewModel
import io.sentry.Sentry
import kotlinx.coroutines.launch


class UseCaseTasksViewModel(
    private val application: Application,
    private val useCaseWithTasks: List<UseCaseTask>,
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseNetworkViewModel(application, dispatchers) {

    private val _useCaseTasks = MutableLiveData(useCaseWithTasks)
    val useCaseTasks: LiveData<List<UseCaseTask>> =
        _useCaseTasks

    private val _countryCode = MutableLiveData<String>()
    val countryCode: LiveData<String> = _countryCode

    private val _currencyCode = MutableLiveData<String>()
    val currencyCode: LiveData<String> = _currencyCode

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadProfileInfo()
        syncCurrenciesIfEmpty()
    }


    private fun loadProfileInfo() {
        viewModelScope.launch(dispatchers.io) {
            val profileInfo = database.profileInfoDao().findOne()
            _countryCode.postValue(profileInfo?.countryCode)
            _currencyCode.postValue(profileInfo?.currencyCode)
        }
    }

    fun syncCurrenciesIfEmpty() {
        viewModelScope.launch(dispatchers.io) {
            val currencies = database.currencyDao().listAll()
            if (currencies.isNotEmpty()) return@launch
            try {
                val currencyList = akilimoService.listCurrencies().data
                if (currencyList.isNotEmpty()) {
                    database.currencyDao().insertAll(currencyList)
                } else {
                    showSnackBar("Failed to fetch currency list")
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                showSnackBar("Failed to fetch currency list: ${e.message}")
            }
        }
    }

    fun insertUseCaseWithTasks(useCase: EnumUseCase, useCaseTasks: List<UseCaseTask>) {
        viewModelScope.launch {
            try {
                val useCaseDao = database.useCaseDao()
                val existing = useCaseDao.getUseCaseWithTasks(useCase)

                if (existing != null) {
                    showSnackBar("Use case ${useCase.name} already exists.")
                    return@launch
                }

                val newUseCase = UseCase(useCase = useCase, useCaseLabel = 0)
                val useCaseId = useCaseDao.insertUseCase(newUseCase)

                val taskEntities = useCaseTasks.map { task ->
                    task.copy(useCaseId = useCaseId)
                }

                useCaseDao.insertTasks(taskEntities)
                showSnackBar("Use case ${useCase.name} and ${taskEntities.size} tasks inserted successfully.")
            } catch (e: Exception) {
                Sentry.captureException(e)
                showSnackBar("Failed to insert use case with tasks: ${e.message}")
            }
        }
    }


    fun saveUseCaseTask(useCases: List<UseCaseWithTasks>) {
        viewModelScope.launch(dispatchers.io) {
            database.useCaseDao().insertUseCasesWithTasks(useCases)
        }
    }

    fun toggleTaskCompletion(useCase: EnumUseCase, task: EnumTask) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val dao = database.useCaseDao()
                val useCaseId = dao.findOne(useCase)?.id ?: return@launch
                val tasks = dao.getAllTasksForUseCase(useCaseId)
                val taskItem = tasks.find { it.taskName == task } ?: return@launch
                val updatedTask = taskItem.copy(completed = !taskItem.completed)
                dao.updateTaskCompletion(updatedTask)
            } catch (e: Exception) {
                Sentry.captureException(e)
                showSnackBar("Failed to update task completion: ${e.message}")
            }
        }
    }

    suspend fun isUseCaseComplete(useCase: EnumUseCase): Boolean {
        return false
//        val dao = database.useCaseDao()
//        val completed = dao.countCompletedTasks(useCase)
//        val total = dao.countTotalTasks(useCase)
//        return completed == total && total > 0
    }
}