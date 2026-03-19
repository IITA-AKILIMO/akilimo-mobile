package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.MaizePerformanceRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.dto.MaizePerfOption
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.enums.EnumMaizePerformance
import com.akilimo.mobile.repos.AkilimoUserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MaizePerformanceViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val maizeRepo: MaizePerformanceRepo
) : ViewModel() {

    data class UiState(val options: List<MaizePerfOption> = emptyList())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadOptions(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val allOptions = EnumMaizePerformance.entries.map { MaizePerfOption(valueOption = it) }
        maizeRepo.observeByUserId(user.id ?: 0).collect { savedPerf ->
            val options = allOptions.map { option ->
                option.copy(isSelected = option.valueOption == savedPerf?.maizePerformance)
            }
            _uiState.update { it.copy(options = options) }
        }
    }

    fun saveSelection(userName: String, selected: EnumMaizePerformance) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        maizeRepo.saveOrUpdatePerformance(
            MaizePerformance(userId = user.id ?: 0, maizePerformance = selected)
        )
    }

}
