package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.viewmodels.base.BaseNetworkViewModel
import kotlinx.coroutines.launch

open class OperationCostViewModel(
    private val application: Application,
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseNetworkViewModel(application, dispatchers) {
    private val _operationCosts = MutableLiveData<List<OperationCost>>()
    val operationCosts: LiveData<List<OperationCost>> get() = _operationCosts

    fun loadOperationCosts(
        countryCode: String,
        operationName: EnumOperation,
        operationType: EnumOperationMethod
    ) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val local = database.operationCostDao().findAllFiltered(
                    operationName.name,
                    operationType.name,
                    countryCode
                )
                if (local.isNotEmpty()) {
                    _operationCosts.postValue(local)
                    return@launch
                }

                val queryParams = mapOf(
                    "operation_type" to operationType.name,
                    "operation_name" to operationName.name,
                )
                val response = akilimoService.getOperationCosts(countryCode, queryParams)
                _operationCosts.value = response.data
            } catch (e: Exception) {
                _operationCosts.value = emptyList()
                showSnackBar(e.message ?: "An error occurred")
            }
        }

    }
}