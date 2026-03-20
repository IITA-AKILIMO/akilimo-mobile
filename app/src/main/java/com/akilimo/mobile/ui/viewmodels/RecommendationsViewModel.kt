package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dto.AdviceOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo
) : ViewModel() {

    data class UiState(val adviceOptions: List<AdviceOption> = emptyList())

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadAdviceOptions(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch

        val options = mutableListOf(
            AdviceOption(EnumAdvice.FERTILIZER_RECOMMENDATIONS),
            AdviceOption(EnumAdvice.BEST_PLANTING_PRACTICES),
            AdviceOption(EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH),
        )

        when (user.enumCountry) {
            EnumCountry.NG -> options.add(AdviceOption(EnumAdvice.INTERCROPPING_MAIZE))
            EnumCountry.TZ -> options.add(AdviceOption(EnumAdvice.INTERCROPPING_SWEET_POTATO))
            else -> Unit
        }

        _uiState.update { it.copy(adviceOptions = options) }
    }

    fun trackActiveAdvice(userName: String, selected: EnumAdvice) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        userRepo.saveOrUpdateUser(user.copy(activeAdvise = selected), userName)
    }

}
