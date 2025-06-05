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
    private val myDispatchers: IDispatcherProvider = DefaultDispatcherProvider()
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

    private val LAST_SYNC_KEY = "last_fertilizer_sync"
    private val oneDayMillis = 24 * 60 * 60 * 1000L

    private val lastSync: Long
        get() = prefs.getLong(LAST_SYNC_KEY, 0L)

    private val now: Long
        get() = System.currentTimeMillis()

    init {
        database.profileInfoDao().findOne()?.let {
            countryCode = it.countryCode
            currencyCode = it.currencyCode
        }
    }

    fun loadFertilizers() {
        viewModelScope.launch(myDispatchers.io) {
            _loading.postValue(true)
            _error.postValue(false)

            try {
                val localFertilizers = loadLocalFertilizers()

                if (localFertilizers.isNotEmpty()) {
                    _fertilizers.postValue(localFertilizers)
                }

                if (shouldSync(localFertilizers)) {
                    fetchAndSyncFertilizers()
                } else if (localFertilizers.isEmpty()) {
                    _error.postValue(true)
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun refreshFertilizers() {
        viewModelScope.launch(myDispatchers.io) {
            _loading.postValue(true)
            _error.postValue(false)

            try {
                fetchAndSyncFertilizers()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private suspend fun loadLocalFertilizers(): List<Fertilizer> {
        return if (!useCase.isNullOrBlank()) {
            database.fertilizerDao().findAllSelectedByCountryAndUseCase(countryCode, useCase)
        } else {
            database.fertilizerDao().findAllSelectedByCountry(countryCode)
        }
    }

    private fun shouldSync(localFertilizers: List<Fertilizer>): Boolean {
        return now - lastSync > oneDayMillis || localFertilizers.isEmpty()
    }

    private suspend fun fetchAndSyncFertilizers() {
        val response = akilimoService.getFertilizers(countryCode, useCase)
        val remoteFertilizers = response.data

        if (remoteFertilizers.isNotEmpty()) {
            syncFertilizers(remoteFertilizers)
            _fertilizers.postValue(remoteFertilizers)
            prefs.edit().putLong(LAST_SYNC_KEY, now).apply()
        } else {
            _error.postValue(true)
        }
    }

    private fun handleError(e: Exception) {
        _error.postValue(true)
        Sentry.captureException(e)
    }

    private suspend fun syncFertilizers(fertilizers: List<Fertilizer>) {
        val savedList = if (useCase.isNullOrBlank()) {
            database.fertilizerDao().findAllByCountry(countryCode)
        } else {
            database.fertilizerDao().findAllByCountryAndUseCase(countryCode, useCase)
        }

        val toDelete = mutableListOf<Fertilizer>()

        if (savedList.isNotEmpty()) {
            for (savedFertilizer in savedList) {
                var found = false
                for (newFertilizer in fertilizers) {
                    if (savedFertilizer.fertilizerType == newFertilizer.fertilizerType) {
                        found = true
                        val existing =
                            database.fertilizerDao().findByType(newFertilizer.fertilizerType)
                        if (existing != null) {
                            existing.available = newFertilizer.available
                            database.fertilizerDao().update(existing)
                        } else {
                            database.fertilizerDao().insert(newFertilizer)
                        }
                        break
                    }
                }
                if (!found) toDelete.add(savedFertilizer)
            }
        } else {
            database.fertilizerDao().insertAll(fertilizers)
        }
        database.fertilizerDao().deleteFertilizerByList(toDelete)

        fertilizers.forEach { fertilizer ->
            loadFertilizerPrices(fertilizer.fertilizerKey ?: "")
        }
    }

    fun loadFertilizerPrices(fertilizerKey: String) {
        viewModelScope.launch(myDispatchers.io) {
            try {
                val response = akilimoService.getFertilizerPrices(fertilizerKey)
                response.data.let { prices ->
                    database.fertilizerPriceDao().insertAll(prices)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun saveSelectedFertilizer(selectedFertilizer: Fertilizer) {
        viewModelScope.launch(myDispatchers.io) {
            database.fertilizerDao().update(selectedFertilizer)
            _fertilizerUpdated.postValue(selectedFertilizer)
        }
    }

    fun isMinSelected(): Boolean {
        val count = if (useCase.isNullOrBlank()) {
            database.fertilizerDao().findAllSelectedByCountry(countryCode).size
        } else {
            database.fertilizerDao().findAllSelectedByCountryAndUseCase(countryCode, useCase).size
        }

        return if (count < minSelection) {
            _showSnackBarEvent.postValue(
                "Please select at least $minSelection fertilizers"
            )
            false
        } else true
    }

    fun clearSnackBarEvent() {
        _showSnackBarEvent.postValue(null)
    }
}
