package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPriceResponse
import com.akilimo.mobile.entities.FertilizerResponse
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.AkilimoService
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import io.sentry.Sentry
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FertilizersViewModel(
    application: Application,
    private val myDispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val akilimoService: AkilimoService = AkilimoApi.apiService

    private val _fertilizers = MutableLiveData<List<Fertilizer>>()
    val fertilizers: LiveData<List<Fertilizer>> = _fertilizers

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val _showSnackBarEvent = MutableLiveData<String?>()
    val showSnackBarEvent: LiveData<String?> = _showSnackBarEvent

    var countryCode: String = ""
    var currencyCode: String = ""
    var useCase: String = "NA"

    private val minSelection = 2

    // For selected fertilizers tracking inside VM (optional)
    private val _selectedFertilizers = mutableListOf<Fertilizer>()
//    val selectedFertilizers: List<Fertilizer> get() = _selectedFertilizers

    init {
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }
    }

    fun loadFertilizers() {
        viewModelScope.launch(myDispatchers.io) {
            _loading.postValue(true)
            _error.postValue(false)

            val localFertilizers = database.fertilizerDao().findAllByCountry(countryCode)
            if (localFertilizers.isNotEmpty()) {
                _fertilizers.postValue(localFertilizers)
                _loading.postValue(false)
                return@launch
            }

            val call = akilimoService.getFertilizers(countryCode)
            call.enqueue(object : Callback<FertilizerResponse> {
                override fun onResponse(
                    call: Call<FertilizerResponse>, response: Response<FertilizerResponse>
                ) {
                    if (response.isSuccessful) {
                        val remoteFertilizers = response.body()?.data ?: emptyList()
                        viewModelScope.launch(myDispatchers.io) {
                            syncFertilizers(remoteFertilizers)
                            _fertilizers.postValue(remoteFertilizers)
                            _loading.postValue(false)
                        }
                    } else {
                        _loading.postValue(false)
                        _error.postValue(true)
                    }
                }

                override fun onFailure(call: Call<FertilizerResponse>, t: Throwable) {
                    _loading.postValue(false)
                    _error.postValue(true)
                    Sentry.captureException(t)
                }
            })
        }
    }

    private suspend fun syncFertilizers(fertilizers: List<Fertilizer>) {
        val savedList = database.fertilizerDao().findAllByCountry(countryCode)
        val toDelete = mutableListOf<Fertilizer>()

        if (savedList.isNotEmpty()) {
            for (fertilizer in savedList) {
                var found = false
                for (fertilizer in fertilizers) {
                    val existing = database.fertilizerDao().findByType(fertilizer.fertilizerType)
                    if (existing != null) {
                        existing.available = fertilizer.available
                        database.fertilizerDao().update(existing)
                    } else {
                        database.fertilizerDao().insert(fertilizer)
                    }
                    if (fertilizer.fertilizerType.equals(fertilizer.fertilizerType)) {
                        found = true
                    }
                }
                if (!found) toDelete.add(fertilizer)
            }
        } else {
            database.fertilizerDao().insertAll(fertilizers)
        }
        database.fertilizerDao().deleteFertilizerByList(toDelete)

        // Load prices for all fertilizers asynchronously
        fertilizers.forEach { fertilizer ->
            loadFertilizerPrices(fertilizer.fertilizerKey ?: "")
        }
    }

    fun loadFertilizerPrices(fertilizerKey: String) {
        val call = akilimoService.getFertilizerPrices(fertilizerKey)
        call.enqueue(object : Callback<FertilizerPriceResponse> {
            override fun onResponse(
                call: Call<FertilizerPriceResponse>, response: Response<FertilizerPriceResponse>
            ) {
                if (response.isSuccessful) {
                    viewModelScope.launch(myDispatchers.io) {
                        response.body()?.data?.let { prices ->
                            database.fertilizerPriceDao().insertAll(prices)
                        }
                    }
                } else {
                    _error.postValue(true)
                }
            }

            override fun onFailure(call: Call<FertilizerPriceResponse>, t: Throwable) {
                _error.postValue(true)
                Sentry.captureException(t)
            }
        })
    }

    fun saveSelectedFertilizers(selected: List<Fertilizer>) {
        viewModelScope.launch(myDispatchers.io) {
            _selectedFertilizers.clear()
            _selectedFertilizers.addAll(selected)
            database.fertilizerDao().updateSelected(selected)
            database.adviceStatusDao().insert(
                AdviceStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS.name, isMinSelected())
            )
        }
    }

    fun isMinSelected(): Boolean {
        val count = database.fertilizerDao().findAllSelectedByCountry(countryCode).size
        if (count < minSelection) {
            _showSnackBarEvent.postValue(
                String.format(
                    Locale.US, "Please select at least %d fertilizers", minSelection
                )
            )
            return false
        }
        return true
    }

    fun clearSnackBarEvent() {
        _showSnackBarEvent.postValue(null)
    }
}
