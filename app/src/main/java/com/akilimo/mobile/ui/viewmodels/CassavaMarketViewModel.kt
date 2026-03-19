package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.CassavaMarketPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import com.akilimo.mobile.repos.CassavaUnitRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.StarchFactoryRepo
import com.akilimo.mobile.utils.MathHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CassavaMarketViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val factoryRepo: StarchFactoryRepo,
    val selectedRepo: SelectedCassavaMarketRepo,
    val priceRepo: CassavaMarketPriceRepo,
    private val cassavaUnitRepo: CassavaUnitRepo
) : ViewModel() {

    data class UiState(
        val factories: List<StarchFactory> = emptyList(),
        val cassavaUnits: List<CassavaUnit> = emptyList(),
        val factoriesRefreshing: Boolean = false,
        val unitsRefreshing: Boolean = false,
        val userId: Int = 0,
        val userCountry: EnumCountry = EnumCountry.Unsupported,
        val initialMarketChoice: MarketChoice = MarketChoice.NONE
    )

    enum class MarketChoice { NONE, FACTORY, MARKET }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val userId = user.id ?: return@launch

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
            factoryRepo.observeAll().collectLatest { factories ->
                val details = selectedRepo.getSelectedByUser(userId)
                val mapped = factories.map {
                    StarchFactory(id = it.id, name = it.name, label = it.label).apply {
                        isSelected = it.id == details?.selectedCassavaMarket?.starchFactoryId
                    }
                }
                _uiState.update { it.copy(factories = mapped, factoriesRefreshing = false) }
            }
        }

        launch {
            cassavaUnitRepo.observeAll().collectLatest { units ->
                val details = selectedRepo.getSelectedByUser(userId)
                val mapped = units.map { unit ->
                    CassavaUnit(id = unit.id, label = unit.label, description = unit.description).apply {
                        isSelected = unit.id == details?.selectedCassavaMarket?.cassavaUnitId
                    }
                }
                _uiState.update { it.copy(cassavaUnits = mapped, unitsRefreshing = false) }
            }
        }
    }

    fun selectFactory(factory: StarchFactory) = viewModelScope.launch {
        val userId = _uiState.value.userId
        selectedRepo.select(SelectedCassavaMarket(userId = userId, starchFactoryId = factory.id))
    }

    fun saveSelectedPrice(
        userName: String,
        unit: CassavaUnit,
        uos: EnumUnitOfSale,
        selectedPrice: CassavaMarketPrice?
    ) = viewModelScope.launch {
        val userId = userRepo.getUser(userName)?.id ?: return@launch

        val exactPrice = selectedPrice?.exactPrice ?: false
        val unitPrice = if (exactPrice) {
            selectedPrice!!.averagePrice
        } else {
            MathHelper.computeUnitPrice(selectedPrice?.averagePrice ?: 0.0, uos)
        }

        selectedRepo.select(
            SelectedCassavaMarket(
                userId = userId,
                cassavaUnitId = unit.id,
                unitOfSale = uos,
                unitPrice = unitPrice,
                marketPriceId = selectedPrice?.id
            )
        )
    }

}
