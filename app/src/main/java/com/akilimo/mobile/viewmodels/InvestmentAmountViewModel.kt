package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.viewmodels.base.BaseNetworkViewModel

class InvestmentAmountViewModel(
    private val application: Application,
    val mathHelper: MathHelper,
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseNetworkViewModel(application, dispatchers) {
    companion object {
        private const val MIN_INVESTMENT_USD = 1.0
        private const val INVALID_SELECTION = -1.0
    }

    private val _investmentAmounts = MutableLiveData<List<InvestmentAmount>>()
    val investmentAmounts: LiveData<List<InvestmentAmount>> = _investmentAmounts

    private val _selectedInvestmentAmount = MutableLiveData<Double>(INVALID_SELECTION)
    val selectedInvestmentAmount: LiveData<Double> = _selectedInvestmentAmount

    private val _isExactAmount = MutableLiveData<Boolean>(false)
    val isExactAmount: LiveData<Boolean> = _isExactAmount

    private val _minInvestmentLocal = MutableLiveData<Double>(0.0)
    val minInvestmentLocal: LiveData<Double> = _minInvestmentLocal

    var countryCode: String = ""
    var currencyCode: String = ""
    var currencySymbol: String? = null
    var currencyName: String? = null

    var fieldSize: Double = 0.0
    var fieldSizeAcre: Double = 0.0
    var areaUnit: String = ""
    var areaUnitText: String = ""

    var baseCurrency: String = "" // set this if needed for conversions

    // Current input investment amount local and USD
    private var investmentAmountLocal = 0.0
    private var investmentAmountUSD = 0.0
    private var minimumAmountUSD = 0.0

    fun initializeData() {
        // Load profile info
        database.profileInfoDao().findOne()?.let { profile ->
            countryCode = profile.countryCode
            currencyCode = profile.currencyCode
            CurrencyCode.getCurrencySymbol(currencyCode)?.let {
                currencySymbol = it.symbol
                currencyName = it.name
            }
        }

        // Load mandatory info
        database.mandatoryInfoDao().findOne()?.let { info ->
            fieldSize = info.areaSize
            fieldSizeAcre = info.areaSize
            areaUnit = info.areaUnit
            areaUnitText = info.displayAreaUnit
        }

        // Load saved investment amount if any
        database.investmentAmountDao().findOne()?.let {
            _selectedInvestmentAmount.value = it.investmentAmount
        }
    }


    fun loadInvestmentAmounts(selectedFieldArea: String) = launchWithState {
        val response = akilimoService.getInvestmentAmounts(countryCode)
        val investmentAmount = response.data
        if (investmentAmount.isNotEmpty()) {
            database.investmentAmountDao().insertAll(investmentAmount)
            _investmentAmounts.value = investmentAmount
        } else {
            showSnackBar("Investment amount data not available")
        }
    }

    fun onInvestmentOptionSelected(investment: InvestmentAmount) {
        _isExactAmount.postValue(investment.sortOrder == 0L)
        investmentAmountLocal = investment.investmentAmount
        _selectedInvestmentAmount.postValue(investmentAmountLocal)
    }

    fun validateInvestmentInput(amountText: String): Boolean {
        if (amountText.isBlank()) {
            return false
        }
        investmentAmountLocal = amountText.toDoubleOrNull() ?: 0.0
        investmentAmountUSD = mathHelper.convertToUSD(investmentAmountLocal, currencyCode)
        minimumAmountUSD =
            mathHelper.computeInvestmentAmount(MIN_INVESTMENT_USD, fieldSizeAcre, baseCurrency)
        val minLocal = mathHelper.convertToLocalCurrency(minimumAmountUSD, currencyCode)
        _minInvestmentLocal.postValue(minLocal)

        return investmentAmountLocal >= minLocal
    }

    fun getErrorMessage(): String? {
        return if (investmentAmountLocal < (_minInvestmentLocal.value ?: 0.0)) {
            "Investment must be at least ${_minInvestmentLocal.value} $currencyCode"
        } else null
    }

    fun saveInvestmentAmount(fieldSizeAcre: Double): Boolean {
        return try {
            val amountToInvestRaw = mathHelper.computeInvestmentForSpecifiedAreaUnit(
                investmentAmountLocal, fieldSize, areaUnit
            )
            val roundedAmount = mathHelper.roundToNDecimalPlaces(amountToInvestRaw, 2.0)

            val investment = database.investmentAmountDao().findOne() ?: InvestmentAmount()
            investment.investmentAmount = roundedAmount
            investment.minInvestmentAmount = _minInvestmentLocal.value ?: 0.0
            investment.fieldSize = fieldSizeAcre

            database.investmentAmountDao().insert(investment)
            true
        } catch (ex: Exception) {
            showSnackBar(ex.localizedMessage ?: "An unexpected error occurred")
            false
        }
    }
}