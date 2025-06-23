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
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private var networkFailureCount = 0
    private var lastFailureTime: Long = 0
    private val failureCooldownMillis = 30 * 60 * 1000L // 30 minutes
    private val maxFailures = 3

    var countryCode: String = ""
    var currencyCode: String = ""

    private val lastSyncKey = "last_fertilizer_sync"

    @Suppress("MagicNumber")
    private val oneDayMillis = 24 * 60 * 60 * 1000L

    private val now: Long get() = System.currentTimeMillis()
    private val lastSync: Long get() = prefs.getLong(lastSyncKey, 0L)


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


    private val prefs =
        application.getSharedPreferences("fertilizer_prefs", Application.MODE_PRIVATE)


    init {
        database.profileInfoDao().findOne()?.let {
            countryCode = it.countryCode
            currencyCode = it.currencyCode
        }
    }

    fun loadFertilizers() = launchWithState {
        val localFertilizers = getFertilizersFromDb()
        if (shouldSync(localFertilizers)) {
            fetchAndSyncFertilizers(localFertilizers)
        } else {
            _fertilizers.postValue(localFertilizers)
        }
    }

    fun refreshFertilizers() = launchWithState {
        val fallback = getFertilizersFromDb()
        fetchAndSyncFertilizers(fallback)
    }

    private fun getFertilizersFromDb(): List<Fertilizer> = if (useCase.isNullOrBlank()) {
        database.fertilizerDao().findAllByCountry(countryCode)
    } else {
        database.fertilizerDao().findAllByCountryAndUseCase(countryCode, useCase)
    }

    private fun shouldSync(local: List<Fertilizer>) =
        now - lastSync > oneDayMillis || local.isEmpty()

    private suspend fun fetchAndSyncFertilizers(offlineFertilizers: List<Fertilizer>) {
        if (networkFailureCount >= maxFailures) {
            // Too many failures, use local DB only
            _showSnackBarEvent.postValue("Using offline data due to network issues.")
            _fertilizers.postValue(offlineFertilizers)
            return
        }

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

        newList.forEach { newFertilizer ->
            val existing = database.fertilizerDao().findByKey(newFertilizer.fertilizerKey)
            if (existing != null) {
                newFertilizer.selected = existing.selected
                database.fertilizerDao().update(newFertilizer)
            } else {
                database.fertilizerDao().insert(newFertilizer)
            }
            loadFertilizerPrices(newFertilizer.fertilizerKey ?: "")
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
            resetFailureCount()
        } catch (e: Exception) {
            trackFailure()
            handleError(e)
        } finally {
            _loading.postValue(false)
        }
    }

    private fun launchIgnoreErrors(block: suspend () -> Unit) =
        viewModelScope.launch(dispatchers.io) {
            try {
                block()
                resetFailureCount()
            } catch (e: Exception) {
                handleError(e)
            }
        }

    private fun handleError(e: Exception) {
        _error.postValue(true)
        Sentry.captureException(e)
    }

    private fun trackFailure() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFailureTime > failureCooldownMillis) {
            networkFailureCount = 1
        } else {
            networkFailureCount++
        }
        lastFailureTime = currentTime
    }

    private fun resetFailureCount() {
        networkFailureCount = 0
    }
}
