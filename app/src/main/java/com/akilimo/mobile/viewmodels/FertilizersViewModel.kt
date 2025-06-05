package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import io.sentry.Sentry
import kotlinx.coroutines.launch

class FertilizersViewModel(
    private val application: Application,
    private val minSelection: Int,
    private val useCase: String?,
    private val akilimoService: AkilimoService = AkilimoApi.apiService,
    private val database: AppDatabase = AppDatabase.getDatabase(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val _fertilizers = MutableLiveData<List<Fertilizer>>()
    val fertilizers: LiveData<List<Fertilizer>> = _fertilizers

    private val _fertilizerUpdated = MutableLiveData<Fertilizer>()
    val fertilizerUpdated: LiveData<Fertilizer> = _fertilizerUpdated

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val _showSnackBarEvent = MutableLiveData<String?>()
    val showSnackBarEvent: LiveData<String?> = _showSnackBarEvent

    var countryCode: String = ""
    var currencyCode: String = ""

    private val prefs =
        application.getSharedPreferences("fertilizer_prefs", Application.MODE_PRIVATE)
    private val lastSyncKey = "last_fertilizer_sync"
    private val oneDayMillis = 24 * 60 * 60 * 1000L

    private val now: Long get() = System.currentTimeMillis()
    private val lastSync: Long get() = prefs.getLong(lastSyncKey, 0L)

    init {
        database.profileInfoDao().findOne()?.let {
            countryCode = it.countryCode
            currencyCode = it.currencyCode
        }
    }

    fun loadFertilizers() = launchWithState {
        val localFertilizers = getFertilizersFromDb()
        if (localFertilizers.isNotEmpty()) {
            _fertilizers.postValue(localFertilizers)
        }

        if (shouldSync(localFertilizers)) {
            fetchAndSyncFertilizers()
        } else if (localFertilizers.isEmpty()) {
            _error.postValue(true)
        }
    }

    fun refreshFertilizers() = launchWithState {
        fetchAndSyncFertilizers()
    }

    private fun getFertilizersFromDb(): List<Fertilizer> =
        if (useCase.isNullOrBlank()) {
            database.fertilizerDao().findAllByCountry(countryCode)
        } else {
            database.fertilizerDao().findAllByCountryAndUseCase(countryCode, useCase)
        }

    private fun shouldSync(local: List<Fertilizer>) =
        now - lastSync > oneDayMillis || local.isEmpty()

    private suspend fun fetchAndSyncFertilizers() {
        val response = akilimoService.getFertilizers(countryCode, useCase)
        val remoteFertilizers = response.data

        if (remoteFertilizers.isNotEmpty()) {
            syncFertilizers(remoteFertilizers)
            _fertilizers.postValue(remoteFertilizers)
            prefs.edit().putLong(lastSyncKey, now).apply()
        } else {
            _error.postValue(true)
        }
    }

    private fun syncFertilizers(newList: List<Fertilizer>) {
        val currentList = getFertilizersFromDb()
        val toDelete = currentList.filterNot { current ->
            newList.any { it.fertilizerType == current.fertilizerType }
        }

        newList.forEach { new ->
            val existing = database.fertilizerDao().findByType(new.fertilizerType)
            if (existing != null) {
                existing.available = new.available
                database.fertilizerDao().update(existing)
            } else {
                database.fertilizerDao().insert(new)
            }
            loadFertilizerPrices(new.fertilizerKey ?: "")
        }

        if (toDelete.isNotEmpty()) {
            database.fertilizerDao().deleteFertilizerByList(toDelete)
        }

        if (currentList.isEmpty()) {
            database.fertilizerDao().insertAll(newList)
        }
    }

    fun loadFertilizerPrices(fertilizerKey: String) = launchIgnoreErrors {
        val prices = akilimoService.getFertilizerPrices(fertilizerKey).data
        database.fertilizerPriceDao().insertAll(prices)
    }

    fun saveSelectedFertilizer(selected: Fertilizer) = viewModelScope.launch(dispatchers.io) {
        database.fertilizerDao().update(selected)
        _fertilizerUpdated.postValue(selected)
    }

    fun isMinSelected(): Boolean {
        val count = if (useCase.isNullOrBlank()) {
            database.fertilizerDao().findAllSelectedByCountry(countryCode).size
        } else {
            database.fertilizerDao().findAllSelectedByCountryAndUseCase(countryCode, useCase).size
        }

        return if (count < minSelection) {
            _showSnackBarEvent.postValue("Please select at least $minSelection fertilizers")
            false
        } else true
    }

    fun clearSnackBarEvent() {
        _showSnackBarEvent.postValue(null)
    }

    private fun launchWithState(block: suspend () -> Unit) = viewModelScope.launch(dispatchers.io) {
        _loading.postValue(true)
        _error.postValue(false)
        try {
            block()
        } catch (e: Exception) {
            handleError(e)
        } finally {
            _loading.postValue(false)
        }
    }

    private fun launchIgnoreErrors(block: suspend () -> Unit) =
        viewModelScope.launch(dispatchers.io) {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }

    private fun handleError(e: Exception) {
        _error.postValue(true)
        Sentry.captureException(e)
    }
}
