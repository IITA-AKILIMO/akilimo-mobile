package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FertilizerViewModel(
    private val fertilizerRepo: FertilizerRepo,
    val selectedRepo: SelectedFertilizerRepo,
    private val userRepo: AkilimoUserRepo,
    val priceRepo: FertilizerPriceRepo,
    private val userName: String,
    private val fetchFlow: (EnumCountry) -> Flow<List<Fertilizer>>
) : ViewModel() {

    data class UiState(
        val fertilizers: List<Fertilizer> = emptyList(),
        val selectedIds: Set<Int> = emptySet(),
        val isEmpty: Boolean = false,
        val userId: Int = 0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val userId = user.id ?: return@launch
        val country = user.enumCountry

        _uiState.update { it.copy(userId = userId) }

        launch {
            fetchFlow(country).collectLatest { fertilizers ->
                val selectedList = selectedRepo.getSelectedSync(userId)
                val selectedIds = selectedList.map { it.fertilizerId }.toSet()
                val selectedMap = selectedList.associateBy { it.fertilizerId }

                val mapped = fertilizers.map { f ->
                    f.apply {
                        val sel = selectedMap[id]
                        isSelected = selectedIds.contains(id)
                        displayPrice = sel?.displayPrice.orEmpty()
                        selectedPrice = if (isSelected) sel?.fertilizerPrice ?: 0.0 else 0.0
                    }
                }
                _uiState.update {
                    it.copy(fertilizers = mapped, selectedIds = selectedIds, isEmpty = mapped.isEmpty())
                }
            }
        }

        launch {
            selectedRepo.observeSelected(userId).collectLatest { selectedList ->
                val selectedIds = selectedList.map { it.fertilizerId }.toSet()
                _uiState.update { it.copy(selectedIds = selectedIds) }
            }
        }
    }

    fun selectFertilizer(
        fertilizerId: Int,
        fertilizerPriceId: Int?,
        price: Double?,
        displayPrice: String?,
        isExactPrice: Boolean
    ) = viewModelScope.launch {
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

    fun deselectFertilizer(fertilizerId: Int) = viewModelScope.launch {
        val userId = _uiState.value.userId
        selectedRepo.deselect(userId, fertilizerId)
    }

    suspend fun getStepStatus(): EnumStepStatus {
        val userId = _uiState.value.userId
        val selected = selectedRepo.getSelectedSync(userId)
        return if (selected.isNotEmpty()) EnumStepStatus.COMPLETED else EnumStepStatus.IN_PROGRESS
    }

    companion object {
        fun defaultFlow(db: AppDatabase, country: EnumCountry): Flow<List<Fertilizer>> =
            FertilizerRepo(db.fertilizerDao()).observeByCountry(country)

        fun factory(
            db: AppDatabase,
            userName: String,
            fetchFlow: (EnumCountry) -> Flow<List<Fertilizer>> = { country ->
                defaultFlow(db, country)
            }
        ) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FertilizerViewModel(
                    fertilizerRepo = FertilizerRepo(db.fertilizerDao()),
                    selectedRepo = SelectedFertilizerRepo(db.selectedFertilizerDao()),
                    userRepo = AkilimoUserRepo(db.akilimoUserDao()),
                    priceRepo = FertilizerPriceRepo(db.fertilizerPriceDao()),
                    userName = userName,
                    fetchFlow = fetchFlow
                ) as T
            }
        }
    }
}
