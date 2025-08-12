package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.utils.MathHelper

class ManualOperationCostViewModel(
    private val application: Application,
    private val mathHelper: MathHelper,
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val db: AppDatabase = AppDatabase.getInstance(application),
) : OperationCostViewModel(application, mathHelper, akilimoService, db) {

    private val _manualPloughCost = MutableLiveData(0.0)
    val manualPloughCost: MutableLiveData<Double> = _manualPloughCost
    private val _manualRidgeCost = MutableLiveData(0.0)
    val manualRidgeCost: MutableLiveData<Double> = _manualRidgeCost
    private val _dataValid = MutableLiveData(false)
    val dataValid: MutableLiveData<Boolean> = _dataValid

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    fun loadInitialData(onMetaReady: (areaUnit: String, fieldSize: Double, currencySymbol: String) -> Unit) {
        val mandatory = db.mandatoryInfoDao().findOne()
        val profile = db.profileInfoDao().findOne()
        val currency = profile?.currencyCode?.let { db.currencyDao().findOneByCurrencyCode(it) }

        val fieldCost = db.fieldOperationCostDao().findOne()
        _manualPloughCost.postValue(fieldCost?.manualPloughCost ?: 0.0)
        _manualRidgeCost.postValue(fieldCost?.manualRidgeCost ?: 0.0)

        if (mandatory != null && profile != null && currency != null) {
            onMetaReady(mandatory.areaUnit, mandatory.areaSize, currency.currencySymbol)
        }
    }

    fun saveCosts(plough: Double, ridge: Double) {
        if (plough <= 0.0) {
            _errorMessage.postValue("Plough cost is required")
            _dataValid.value = false
            return
        }
        if (ridge <= 0.0) {
            _errorMessage.postValue("Ridge cost is required")
            _dataValid.postValue(false)
            return
        }

        try {
            val cost = db.fieldOperationCostDao().findOne() ?: FieldOperationCost()
            cost.manualPloughCost = plough
            cost.manualRidgeCost = ridge
            db.fieldOperationCostDao().insert(cost)

            _dataValid.postValue(true)
        } catch (ex: Exception) {
            _dataValid.postValue(false)
            _errorMessage.postValue(ex.message)
            showSnackBar(ex.message ?: "An unexpected error occurred")
        }
    }
}