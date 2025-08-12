package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.PotatoMarket
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumPotatoProduceType
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.viewmodels.base.BaseNetworkViewModel
import io.sentry.Sentry

class SweetPotatoMarketViewModel(
    private val application: Application,
    private val mathHelper: MathHelper,
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider(),
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
) :
    BaseNetworkViewModel(application, dispatchers) {
    private val _closeEvent = MutableLiveData<Boolean>()
    val closeEvent: LiveData<Boolean> = _closeEvent

    private val _unitOfSale = MutableLiveData<EnumUnitOfSale>()
    val unitOfSale: LiveData<EnumUnitOfSale> = _unitOfSale

    private val _unitWeight = MutableLiveData<Double>()
    val unitWeight: LiveData<Double> = _unitWeight

    private val _unitPrice = MutableLiveData<Double>()
    val unitPrice: LiveData<Double> = _unitPrice

    private var produceType = EnumPotatoProduceType.TUBERS.name.lowercase()

    // Called when a unit of sale radio is selected
    fun setUnitOfSale(enumUnitOfSale: EnumUnitOfSale) {
        _unitOfSale.postValue(enumUnitOfSale)
        _unitWeight.postValue(enumUnitOfSale.unitWeight())
    }

    // Called when dialog sets price
    fun setUnitPriceFromDialog(
        selectedPrice: Double,
        isExactPrice: Boolean,
    ) {
        val weight = unitWeight.value ?: EnumUnitOfSale.FIFTY_KG.unitWeight()
        val finalPrice = if (isExactPrice) selectedPrice else mathHelper.convertToUnitWeightPrice(
            selectedPrice,
            weight
        )
        _unitPrice.postValue(finalPrice)
    }

    fun validateSelection(
        produceTypeId: Int?,
        unitOfSale: String?,
        price: Double?,
        backPressed: Boolean
    ) {
        when {
            produceType.isEmpty() -> {
                showSnackBar(R.string.lbl_potato_produce_prompt)
            }

            unitOfSale.isNullOrEmpty() -> {
                showSnackBar(R.string.lbl_potato_sale_unit_prompt)
            }

            price == null || price <= 0 -> {
                showSnackBar(R.string.lbl_tuber_price_prompt)
            }

            else -> {
                saveMarket(produceTypeId, unitOfSale, price)
                _closeEvent.postValue(!backPressed)
            }
        }
    }

    private fun saveMarket(produceTypeId: Int?, saleUnit: String, price: Double) {
        val market = PotatoMarket().apply {
            produceType = this@SweetPotatoMarketViewModel.produceType
            unitOfSale = saleUnit
            unitWeight = this@SweetPotatoMarketViewModel.unitWeight.value ?: 0.0
            unitPrice = price
            produceTypeIdx = produceTypeId ?: 0
        }
        database.potatoMarketDao().insert(market)
    }

    fun fetchPotatoPrices(countryCode: String) = launchWithState {
        try {
            val response = akilimoService.getPotatoPrices(countryCode)
            val data = response.data
            if (data.isNotEmpty()) {
                database.potatoPriceDao().insertAll(data)
            }
        } catch (t: Throwable) {
            Sentry.captureException(t)
            showSnackBar(t.message ?: "Error fetching prices")
        }
    }
}