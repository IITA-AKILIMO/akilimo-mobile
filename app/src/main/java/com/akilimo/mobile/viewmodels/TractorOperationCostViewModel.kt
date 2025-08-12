package com.akilimo.mobile.viewmodels


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.ui.SingleLiveEvent
import io.sentry.Sentry


class TractorOperationCostViewModel(
    private val application: Application,
    private val mathHelper: MathHelper,
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
) : OperationCostViewModel(application, mathHelper, akilimoService, database) {
    private val _hasTractor = MutableLiveData(false)
    val hasTractor: LiveData<Boolean> = _hasTractor

    private val _hasPlough = MutableLiveData(false)
    private val _hasRidger = MutableLiveData(false)

    private val _tractorPloughCost = MutableLiveData(0.0)
    private val _tractorRidgeCost = MutableLiveData(0.0)

    private var exactPloughCost = false
    private var exactRidgeCost = false

    var countryCode: String = ""
    private var currencyCode: String = ""
    var currencySymbol: String = ""

    private var areaUnit: String = "acre"

    private var fieldSize: Double = 0.0
    private var fieldOperationCost: FieldOperationCost? = null
    private var currentPractice: CurrentPractice? = null

    val showOperationDialog = SingleLiveEvent<OperationDialogParams>()
    val closeActivity = SingleLiveEvent<Boolean>()

    init {
        loadInitialData()
    }

    fun loadInitialData() = launchWithState {
        database.mandatoryInfoDao().findOne()?.let {
            areaUnit = it.areaUnit
            fieldSize = it.areaSize
        }
        database.profileInfoDao().findOne()?.let { profile ->
            countryCode = profile.countryCode
            currencyCode = profile.currencyCode
            database.currencyDao().findOneByCurrencyCode(currencyCode)?.let {
                currencySymbol = it.currencySymbol
            }
        }

        fieldOperationCost = database.fieldOperationCostDao().findOne()?.also {
            _tractorPloughCost.value = it.tractorPloughCost
            _tractorRidgeCost.value = it.tractorRidgeCost
        }
        currentPractice = database.currentPracticeDao().findOne()
    }

    fun onTractorSelected(has: Boolean) {
        _hasTractor.postValue(has)
        if (!has) {
            _hasPlough.value = false
            _hasRidger.value = false
        }
    }

    fun onImplementSelected(operation: EnumOperation) {
        val (titleResId, hintResId) = when (operation) {
            EnumOperation.TILLAGE -> R.string.lbl_tractor_plough_cost to R.string.lbl_tractor_plough_cost_hint
            EnumOperation.RIDGING -> R.string.lbl_tractor_ridge_cost to R.string.lbl_tractor_ridge_cost_hint
            else -> return
        }
        val params = OperationDialogParams(
            operation = operation,
            operationType = EnumOperationMethod.TRACTOR,
            countryCode = countryCode,
            currencySymbol = currencySymbol,
            dialogTitle = getCostTitle(titleResId),
            hintText = getCostTitle(hintResId)
        )
        showOperationDialog.postValue(params)
    }

    fun setOperationCost(operation: EnumOperation, cost: Double, isExact: Boolean) {
        when (operation) {
            EnumOperation.TILLAGE -> {
                _tractorPloughCost.value = cost
                exactPloughCost = isExact
                _hasPlough.value = true
            }

            EnumOperation.RIDGING -> {
                _tractorRidgeCost.value = cost
                exactRidgeCost = isExact
                _hasRidger.value = true
            }

            else -> Unit
        }
    }

    fun validateAndSave(backPressed: Boolean) = launchWithState {
        try {
            if (fieldOperationCost == null) fieldOperationCost = FieldOperationCost()
            if (currentPractice == null) currentPractice = CurrentPractice()

            currentPractice?.apply {
                tractorAvailable = _hasTractor.value ?: false
                tractorPlough = _hasPlough.value ?: false
                tractorHarrow = false
                tractorRidger = _hasRidger.value ?: false
                database.currentPracticeDao().insert(this)
            }

            fieldOperationCost?.apply {
                tractorPloughCost = _tractorPloughCost.value ?: 0.0
                tractorRidgeCost = _tractorRidgeCost.value ?: 0.0
                exactTractorPloughPrice = exactPloughCost
                exactTractorRidgePrice = exactRidgeCost
                database.fieldOperationCostDao().insert(this)
            }

            closeActivity.value = backPressed
        } catch (ex: Exception) {
            showSnackBar(ex.message ?: "An error occurred")
            Sentry.captureException(ex)
        }
    }

    private fun getCostTitle(resId: Int): String {
        val language = LanguageManager.getLanguage(getApplication())
        val unit = when (areaUnit.lowercase()) {
            "ha" -> getApplication<Application>().getString(R.string.lbl_ha)
            "acre" -> getApplication<Application>().getString(R.string.lbl_acre)
            else -> areaUnit
        }.lowercase()
        val formattedSize = mathHelper.removeLeadingZero(fieldSize)
        return if (language.lowercase() == "sw") {
            getApplication<Application>().getString(resId, unit, formattedSize)
        } else {
            getApplication<Application>().getString(resId, formattedSize, unit)
        }
    }
}