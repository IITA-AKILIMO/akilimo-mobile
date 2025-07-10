package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.viewmodels.base.BaseNetworkViewModel
import io.sentry.Sentry
import kotlinx.coroutines.launch

class CassavaMarketViewModel(
    private val application: Application,
    private val api: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseNetworkViewModel(application, dispatchers) {

    private val _starchFactories = MutableLiveData<List<StarchFactory>>()
    val starchFactories: LiveData<List<StarchFactory>> = _starchFactories

    private val _cassavaMarket = MutableLiveData<CassavaMarket?>()
    val cassavaMarket: LiveData<CassavaMarket?> = _cassavaMarket

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val unitPrice = MutableLiveData(0.0)
    val unitPriceP1 = MutableLiveData(0.0)
    val unitPriceP2 = MutableLiveData(0.0)
    val unitPriceM1 = MutableLiveData(0.0)
    val unitPriceM2 = MutableLiveData(0.0)

    val selectedFactory = MutableLiveData<String?>()
    val produceType = MutableLiveData<String?>()
    val unitOfSale = MutableLiveData("NA")
    val unitWeight = MutableLiveData(0.0)
    val harvestWindow = MutableLiveData(0)

    fun loadInitialData(countryCode: String) {
        viewModelScope.launch(dispatchers.io) {
            _cassavaMarket.value = database.cassavaMarketDao().findOne()
            _starchFactories.value =
                database.starchFactoryDao().findStarchFactoriesByCountry(countryCode)

            val scheduledDate = database.scheduleDateDao().findOne()
            harvestWindow.postValue(scheduledDate?.harvestWindow ?: 0)
        }
    }

    fun fetchStarchFactories(countryCode: String) {
        viewModelScope.launch(dispatchers.io) {

            val response = api.getStarchFactories(countryCode)
            database.starchFactoryDao().insertAll(response.data)
            _starchFactories.value = response.data
        }
    }

    fun fetchCassavaPrices(countryCode: String) {
        viewModelScope.launch(dispatchers.io) {
            val response = api.getCassavaPrices(countryCode)
            database.cassavaPriceDao().insertAll(response.data)
        }
    }

    fun saveCassavaMarket(factoryRequired: Boolean, produce: String) {
        viewModelScope.launch {
            try {
                val market = database.cassavaMarketDao().findOne() ?: CassavaMarket()
                market.apply {
                    starchFactory = selectedFactory.value ?: "NA"
                    isStarchFactoryRequired = factoryRequired
                    produceType = this@CassavaMarketViewModel.produceType.value ?: produce
                    unitPrice = this@CassavaMarketViewModel.unitPrice.value ?: 0.0
                    unitPriceP1 = this@CassavaMarketViewModel.unitPriceP1.value ?: 0.0
                    unitPriceP2 = this@CassavaMarketViewModel.unitPriceP2.value ?: 0.0
                    unitPriceM1 = this@CassavaMarketViewModel.unitPriceM1.value ?: 0.0
                    unitPriceM2 = this@CassavaMarketViewModel.unitPriceM2.value ?: 0.0
                }
                database.cassavaMarketDao().insert(market)
            } catch (ex: Exception) {
                _errorMessage.postValue(ex.localizedMessage)
                showSnackBar(ex.localizedMessage ?: "An unexpected error occurred")
                Sentry.captureException(ex)
            }
        }
    }
}