package com.akilimo.mobile.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.entities.MaizePrice
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumMaizeProduceType
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.viewmodels.base.BaseNetworkViewModel
import com.akilimo.mobile.views.fragments.dialog.MaizePriceDialogFragment

class MaizeMarketViewModel(
    private val application: Application,
    private val mathHelper: MathHelper,
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider(),
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
) : BaseNetworkViewModel(application, dispatchers) {

    // --- State LiveData ---
    private val _maizeMarket = MutableLiveData<MaizeMarket?>()
    val maizeMarket: LiveData<MaizeMarket?> = _maizeMarket

    private val _produceType = MutableLiveData<String?>()
    val produceType: LiveData<String?> = _produceType

    private val _unitOfSale = MutableLiveData<String?>()
    val unitOfSale: LiveData<String?> = _unitOfSale

    private val _unitPrice = MutableLiveData(0.0)
    val unitPrice: LiveData<Double> = _unitPrice

    private val _unitWeight = MutableLiveData(0.0)
    val unitWeight: LiveData<Double> = _unitWeight

    private val _maizePriceList = MutableLiveData<List<MaizePrice>>()
    val maizePriceList: LiveData<List<MaizePrice>> = _maizePriceList

    // --- Events ---
    private val _closeScreen = MutableLiveData<Boolean>()
    val closeScreen: LiveData<Boolean> = _closeScreen

    private val _showPriceDialog = MutableLiveData<Bundle>()
    val showPriceDialog: LiveData<Bundle> = _showPriceDialog

    // Cached currency details
    var currencyCode: String = ""
    var currencyName: String = ""
    var countryCode: String = ""

    init {
        loadInitialData()
    }

    private fun loadInitialData() = launchWithState {
        val market = database.maizeMarketDao().findOne()
        _maizeMarket.postValue(market)

        val profileInfo = database.profileInfoDao().findOne()
        profileInfo?.let {
            countryCode = it.countryCode
            currencyCode = it.currencyCode
            val myCurrency = database.currencyDao().findOneByCurrencyCode(currencyCode)
            currencyName = myCurrency?.currencyName ?: ""
        }

        processMaizePrices()
    }

    fun setProduceType(produceType: EnumMaizeProduceType) {
        _produceType.postValue(produceType.name.lowercase())
    }

    fun setUnitOfSale(unit: String, enum: EnumUnitOfSale) {
        _unitOfSale.postValue(unit)
        _unitWeight.postValue(enum.unitWeight())
    }

    fun updateUnitPrice(price: Double) {
        _unitPrice.postValue(price)
    }

    private fun processMaizePrices() = launchWithState {
        val prices = database.maizePriceDao().findAll()
        if (prices.isEmpty()) {
            fetchMaizePrices()
        } else {
            _maizePriceList.postValue(prices)
        }
    }

    private suspend fun fetchMaizePrices() {
        if (!canMakeNetworkCall()) {
            //@TODO add offline prices
            return;
        }
        val response = akilimoService.getMaizePrices(countryCode)
        val prices = response.data
        if (prices.isNotEmpty()) {
            database.maizePriceDao().insertAll(prices)
            _maizePriceList.postValue(prices)
        }
    }

    fun validateAndSave(backPressed: Boolean) = launchWithState {

        val produceType = _produceType.value
        val unitOfSale = _unitOfSale.value
        val unitPrice = _unitPrice.value ?: 0.0
        val unitWeight = _unitWeight.value ?: 0.0

        if (produceType.isNullOrEmpty()) {
            showSnackBar(R.string.lbl_maize_produce_prompt)
            return@launchWithState
        }

        if (unitOfSale.isNullOrEmpty() && produceType == EnumMaizeProduceType.GRAIN.name.lowercase()) {
            showSnackBar(R.string.lbl_maize_sale_unit_prompt)
            return@launchWithState
        }

        if (unitPrice <= 0.0 && produceType == EnumMaizeProduceType.FRESH_COB.name.lowercase()) {
            showSnackBar(R.string.lbl_cob_price_prompt)
            return@launchWithState
        }

        val market = _maizeMarket.value ?: MaizeMarket()
        market.produceType = produceType
        market.unitOfSale = unitOfSale
        market.unitPrice = unitPrice
        market.unitWeight = unitWeight

        database.maizeMarketDao().insert(market)
        _closeScreen.postValue(true)

    }


    fun requestPriceDialog(produceType: String, unitEnum: EnumUnitOfSale) {
        val args = Bundle().apply {
            putString(MaizePriceDialogFragment.CURRENCY_CODE, currencyCode)
            putString(MaizePriceDialogFragment.CURRENCY_NAME, currencyName)
            putString(MaizePriceDialogFragment.COUNTRY_CODE, countryCode)
            putDouble(MaizePriceDialogFragment.SELECTED_PRICE, _unitPrice.value ?: 0.0)
            putDouble(MaizePriceDialogFragment.AVERAGE_PRICE, 0.0) // can adjust if needed
            putString(MaizePriceDialogFragment.UNIT_OF_SALE, unitEnum.unitOfSale(getApplication()))
            putString(MaizePriceDialogFragment.PRODUCE_TYPE, produceType)
            putParcelable(MaizePriceDialogFragment.ENUM_UNIT_OF_SALE, unitEnum)
        }
        _showPriceDialog.postValue(args)
    }

    fun onDialogPriceSelected(selectedPrice: Double, isExact: Boolean) {
        val weight = _unitWeight.value ?: 1.0
        val price = if (isExact) selectedPrice else mathHelper.convertToUnitWeightPrice(
            selectedPrice,
            weight
        )
        _unitPrice.postValue(price)
    }
}
