package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.workers.DataRefresher
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Fertilizer categories as the API names them, in the order the filter bar shows them. */
private val CATEGORY_ORDER = listOf("STRAIGHT", "COMPOUND", "OTHER")

@HiltViewModel(assistedFactory = FertilizerViewModel.Factory::class)
class FertilizerViewModel @AssistedInject constructor(
    private val fertilizerRepo: FertilizerRepo,
    private val selectedRepo: SelectedFertilizerRepo,
    private val userRepo: AkilimoUserRepo,
    private val priceRepo: FertilizerPriceRepo,
    private val appSettings: AppSettingsDataStore,
    private val dataRefresher: DataRefresher,
    @Assisted private val fertilizerFlow: EnumFertilizerFlow
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fertilizerFlow: EnumFertilizerFlow): FertilizerViewModel
    }

    data class UiState(
        val fertilizers: List<Fertilizer> = emptyList(),
        val selectedIds: Set<Int> = emptySet(),
        val isEmpty: Boolean = false,
        val userId: Int = 0,
        val isGridLayout: Boolean = false,
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val query: String = "",
        /** Category to show (STRAIGHT, COMPOUND, OTHER), or null for all. */
        val category: String? = null,
    ) {
        /** Categories present in the list, in a fixed order, for the filter bar. */
        val categories: List<String>
            get() = CATEGORY_ORDER.filter { c -> fertilizers.any { it.category == c } }

        /** The list after the search text and category filter; an unknown category shows under All only. */
        val visibleFertilizers: List<Fertilizer>
            get() = fertilizers.filter { f ->
                (category == null || f.category == category) &&
                    (query.isBlank() || f.name.orEmpty().contains(query.trim(), ignoreCase = true))
            }
    }

    /** What the price sheet needs: the price options and the saved answer, if any. */
    data class PriceOptions(
        val prices: List<FertilizerPrice>,
        val savedPriceId: Int?,
        /** Pre-fill for the exact-price field when the saved answer was an exact price. */
        val savedExactPrice: Double?
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isGridLayout = appSettings.isFertilizerGrid) }
        viewModelScope.launch { loadData() }
    }

    private fun loadData() = viewModelScope.launch {
        val userName = appSettings.akilimoUser
        val user = userRepo.getUser(userName)
        val userId = user?.id
        if (user == null || userId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return@launch
        }
        val country = user.enumCountry

        _uiState.update { it.copy(userId = userId) }

        launch {
            val listFlow = when (fertilizerFlow) {
                EnumFertilizerFlow.DEFAULT -> fertilizerRepo.observeByCountry(country)
                EnumFertilizerFlow.CIM -> fertilizerRepo.observeByCimAvailable(country)
                EnumFertilizerFlow.CIS -> fertilizerRepo.observeByCisAvailable(country)
            }
            combine(listFlow, selectedRepo.observeSelected(userId)) { fertilizers, selectedList ->
                val selectedIds = selectedList.map { it.fertilizerId }.toSet()
                val selectedMap = selectedList.associateBy { it.fertilizerId }
                val mapped = fertilizers.map { f ->
                    f.copy().apply {
                        val sel = selectedMap[id]
                        isSelected = selectedIds.contains(id)
                        displayPrice = sel?.displayPrice.orEmpty()
                        selectedPrice = if (isSelected) sel?.fertilizerPrice ?: 0.0 else 0.0
                    }
                }
                Pair(mapped, selectedIds)
            }.collectLatest { (mapped, selectedIds) ->
                _uiState.update {
                    it.copy(
                        fertilizers = mapped,
                        selectedIds = selectedIds,
                        isEmpty = mapped.isEmpty(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectFertilizer(
        fertilizerId: Int,
        fertilizerPriceId: Int?,
        price: Double?,
        displayPrice: String?,
        isExactPrice: Boolean
    ) = runSave {
        val userId = _uiState.value.userId
        selectedRepo.select(
            SelectedFertilizer(
                userId = userId,
                fertilizerId = fertilizerId,
                fertilizerPriceId = fertilizerPriceId,
                fertilizerPrice = price ?: 0.0,
                displayPrice = displayPrice,
                isExactPrice = isExactPrice
            )
        )
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    fun onCategoryChange(category: String?) = _uiState.update { it.copy(category = category) }

    fun toggleLayout() {
        val newValue = !_uiState.value.isGridLayout
        appSettings.isFertilizerGrid = newValue
        _uiState.update { it.copy(isGridLayout = newValue) }
    }

    fun deselectFertilizer(fertilizerId: Int) = runSave {
        val userId = _uiState.value.userId
        selectedRepo.deselect(userId, fertilizerId)
    }

    /** Loads the price options for [fertilizer] together with the farmer's saved answer. */
    suspend fun loadPriceOptions(fertilizer: Fertilizer): PriceOptions {
        val prices = priceRepo.getByFertilizerKey(fertilizer.key.orEmpty())
        val saved = selectedRepo.getSelectedByFertilizer(fertilizer.id ?: 0)
        return PriceOptions(
            prices = prices,
            savedPriceId = saved?.fertilizerPriceId,
            savedExactPrice = saved?.takeIf { it.isExactPrice }?.fertilizerPrice?.takeIf { it > 0.0 }
        )
    }

    /** Pull to refresh: fetch the fertilizers and their prices again. */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        launchRefresh({ r -> _uiState.update { it.copy(isRefreshing = r) } }) { dataRefresher.fertilizers() }
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

    suspend fun getStepStatus(): EnumStepStatus {
        val userId = _uiState.value.userId
        val selected = selectedRepo.getSelectedSync(userId)
        return if (selected.isNotEmpty()) EnumStepStatus.COMPLETED else EnumStepStatus.IN_PROGRESS
    }
}
