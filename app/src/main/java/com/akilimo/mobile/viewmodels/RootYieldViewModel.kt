package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.ui.SingleLiveEvent
import com.akilimo.mobile.viewmodels.base.BaseViewModel

class RootYieldViewModel(
    private val application: Application,
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider(),
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
) : BaseViewModel(application) {

    data class YieldDataHolder(
        val areaUnit: String,
        val countryCode: String,
        val currencyCode: String,
        val useCase: UseCase?,
        val savedYield: FieldYield?,
        val selectedYieldAmount: Double
    )


    private val _yieldData = MutableLiveData<YieldDataHolder>()
    val yieldData: LiveData<YieldDataHolder> = _yieldData

    private val _yieldOptions = MutableLiveData<List<FieldYield>>()
    val yieldOptions: LiveData<List<FieldYield>> = _yieldOptions
    val closeScreenEvent = SingleLiveEvent<Unit>()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        val areaUnit = database.mandatoryInfoDao().findOne()?.areaUnit.orEmpty()
        val countryCode = database.profileInfoDao().findOne()?.countryCode.orEmpty()
        val currencyCode = database.profileInfoDao().findOne()?.currencyCode.orEmpty()
        val useCase = database.useCaseDao().findOne(EnumUseCase.FR)
        val savedYield = database.fieldYieldDao().findOne()
        val selectedYieldAmount = savedYield?.yieldAmount ?: 0.0

        val holder = YieldDataHolder(
            areaUnit,
            countryCode,
            currencyCode,
            useCase,
            savedYield,
            selectedYieldAmount
        )
        _yieldData.value = holder
        _yieldOptions.postValue(generateYieldData(areaUnit))
    }


    fun saveYieldSelection(selected: FieldYield) {
        val updatedYield = (_yieldData.value?.savedYield ?: FieldYield()).apply {
            yieldAmount = selected.yieldAmount
            fieldYieldLabel = selected.fieldYieldLabel
        }
        database.fieldYieldDao().insert(updatedYield)

        _yieldData.value = _yieldData.value?.copy(
            savedYield = updatedYield,
            selectedYieldAmount = updatedYield.yieldAmount
        )
    }


    fun validateAndFinish() {
        if ((_yieldData.value?.selectedYieldAmount ?: 0.0) <= 0) {
            showSnackBar(R.string.lbl_invalid_yield)
            return
        }
        closeScreenEvent.call()
    }

    private fun generateYieldData(areaUnit: String): List<FieldYield> {
        val unitEnum = EnumAreaUnit.valueOf(areaUnit)
        val yieldLabels = unitEnum.yieldLabelIds()

        val yieldDefinitions = listOf(
            Triple(
                com.akilimo.mobile.R.string.fcy_lower,
                com.akilimo.mobile.R.string.lbl_low_yield,
                3.75
            ),
            Triple(
                com.akilimo.mobile.R.string.fcy_about_the_same,
                com.akilimo.mobile.R.string.lbl_normal_yield,
                11.25
            ),
            Triple(
                com.akilimo.mobile.R.string.fcy_somewhat_higher,
                com.akilimo.mobile.R.string.lbl_high_yield,
                18.75
            ),
            Triple(
                com.akilimo.mobile.R.string.fcy_2_3_times_higher,
                com.akilimo.mobile.R.string.lbl_very_high_yield,
                26.25
            ),
            Triple(
                com.akilimo.mobile.R.string.fcy_more_than_3_times_higher,
                com.akilimo.mobile.R.string.lbl_very_high_yield,
                33.75
            )
        )

        val yieldImages = arrayOf(
            com.akilimo.mobile.R.drawable.yield_less_than_7point5,
            com.akilimo.mobile.R.drawable.yield_7point5_to_15,
            com.akilimo.mobile.R.drawable.yield_15_to_22point5,
            com.akilimo.mobile.R.drawable.yield_22_to_30,
            com.akilimo.mobile.R.drawable.yield_more_than_30
        )

        return yieldDefinitions.mapIndexed { index, (labelRes, descRes, amount) ->
            FieldYield().apply {
                imageId = yieldImages[index]
                yieldAmount = amount
                fieldYieldLabel = application.getString(labelRes)
                fieldYieldAmountLabel = application.getString(yieldLabels[index])
                fieldYieldDesc = application.getString(descRes)
            }
        }
    }
}