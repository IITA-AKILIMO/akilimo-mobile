package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.CassavaMarketPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.enums.EnumCassavaProduceType
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import com.akilimo.mobile.repos.CassavaUnitRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.workers.DataRefresher
import com.akilimo.mobile.repos.StarchFactoryRepo
import com.akilimo.mobile.utils.MathHelper
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Cassava market outlet. Unlike the other forms, a choice here is written as soon as it is
 * made (tapping a factory, or confirming a price in the unit sheet); the screen's Save only
 * marks the task complete. The price sheet is effectively its own small form with a confirm.
 */
@HiltViewModel
class CassavaMarketViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val factoryRepo: StarchFactoryRepo,
    private val selectedRepo: SelectedCassavaMarketRepo,
    private val priceRepo: CassavaMarketPriceRepo,
    private val cassavaUnitRepo: CassavaUnitRepo,
    private val appSettings: AppSettingsDataStore,
    private val dataRefresher: DataRefresher
) : ViewModel() {

    data class UiState(
        val factories: List<StarchFactory> = emptyList(),
        val selectedFactoryId: Int? = null,
        val cassavaUnits: List<CassavaUnit> = emptyList(),
        val selectedUnitId: Int? = null,
        val factoriesLoading: Boolean = true,
        val unitsLoading: Boolean = true,
        val userId: Int = 0,
        val userCountry: EnumCountry = EnumCountry.Unsupported,
        val initialMarketChoice: MarketChoice = MarketChoice.NONE,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val isRefreshing: Boolean = false
    )

    enum class MarketChoice { NONE, FACTORY, MARKET }

    /** What the unit price sheet needs: the options and the farmer's previous answer. */
    data class PriceOptions(
        val prices: List<CassavaMarketPrice>,
        val savedPriceId: Int?,
        /** Pre-fill for the exact-price field when the saved answer was an exact price. */
        val savedExactPrice: Double?
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadData(appSettings.akilimoUser) }
    }

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName)
        val userId = user?.id
        if (user == null || userId == null) {
            _uiState.update { it.copy(factoriesLoading = false, unitsLoading = false) }
            return@launch
        }

        val marketDetails = selectedRepo.getSelectedByUser(userId)
        val selectedMarket = marketDetails?.selectedCassavaMarket
        val initialChoice = when {
            selectedMarket?.starchFactoryId != null -> MarketChoice.FACTORY
            selectedMarket?.cassavaUnitId != null -> MarketChoice.MARKET
            else -> MarketChoice.NONE
        }

        _uiState.update {
            it.copy(
                userId = userId,
                userCountry = user.enumCountry,
                initialMarketChoice = initialChoice
            )
        }

        launch {
            combine(
                factoryRepo.observeAll(),
                selectedRepo.observeSelected(userId)
            ) { factories, details ->
                Pair(factories, details?.selectedCassavaMarket?.starchFactoryId)
            }.collectLatest { (factories, selectedId) ->
                _uiState.update {
                    it.copy(
                        factories = factories,
                        selectedFactoryId = selectedId,
                        factoriesLoading = false
                    )
                }
            }
        }

        launch {
            combine(
                cassavaUnitRepo.observeAll(),
                selectedRepo.observeSelected(userId)
            ) { units, details ->
                Pair(units, details?.selectedCassavaMarket?.cassavaUnitId)
            }.collectLatest { (units, selectedId) ->
                _uiState.update {
                    it.copy(
                        cassavaUnits = units,
                        selectedUnitId = selectedId,
                        unitsLoading = false
                    )
                }
            }
        }
    }

    /** Loads the price options for the unit sheet together with the saved answer. */
    suspend fun loadPriceOptions(): PriceOptions {
        val state = _uiState.value
        val prices = priceRepo.getPricesByCountry(state.userCountry)
        val saved = selectedRepo.getSelectedByUser(state.userId)
        val savedPriceId = saved?.marketPrice?.id
        val savedMarket = saved?.selectedCassavaMarket
        val savedExact = savedMarket
            ?.takeIf { it.marketPriceId != null && it.marketPriceId == savedPriceId }
            ?.takeIf { prices.find { p -> p.id == savedPriceId }?.exactPrice == true }
            ?.unitPrice
            ?.takeIf { it > 0 }
        return PriceOptions(prices, savedPriceId, savedExact)
    }

    fun selectFactory(factory: StarchFactory) = runSave {
        val userId = _uiState.value.userId
        val current = selectedRepo.getSelectedByUser(userId)?.selectedCassavaMarket
        selectedRepo.select(
            SelectedCassavaMarket(
                userId = userId,
                starchFactoryId = factory.id,
                // Keep choices made on other screens (yield) when switching market type
                yieldId = current?.yieldId,
                produceType = current?.produceType ?: EnumCassavaProduceType.ROOTS
            )
        )
        _uiState.update { it.copy(selectedFactoryId = factory.id) }
    }

    fun saveSelectedPrice(
        unit: CassavaUnit,
        uos: EnumUnitOfSale,
        selectedPrice: CassavaMarketPrice?
    ) = runSave {
        val userId = userRepo.getUser(appSettings.akilimoUser)?.id ?: error("No profile for current user")

        val exactPrice = selectedPrice?.exactPrice ?: false
        val unitPrice = if (exactPrice) {
            selectedPrice!!.averagePrice
        } else {
            MathHelper.computeUnitPrice(selectedPrice?.averagePrice ?: 0.0, uos)
        }

        val current = selectedRepo.getSelectedByUser(userId)?.selectedCassavaMarket
        selectedRepo.select(
            SelectedCassavaMarket(
                userId = userId,
                cassavaUnitId = unit.id,
                unitOfSale = uos,
                unitPrice = unitPrice,
                marketPriceId = selectedPrice?.id,
                yieldId = current?.yieldId,
                produceType = current?.produceType ?: EnumCassavaProduceType.ROOTS
            )
        )
    }

    /** Pull to refresh: fetch starch factories, units of sale and prices again. */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        launchRefresh({ r -> _uiState.update { it.copy(isRefreshing = r) } }) { dataRefresher.cassavaMarket() }
    }

    fun onSaveErrorShown() = _uiState.update { it.copy(saveFailed = false) }

    /** Runs a DB write with [UiState.isSaving] set, reporting failure via [UiState.saveFailed]. */
    private fun runSave(block: suspend () -> Unit) = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        try {
            block()
            _uiState.update { it.copy(isSaving = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Sentry.captureException(e)
            _uiState.update { it.copy(isSaving = false, saveFailed = true) }
        }
    }
}
