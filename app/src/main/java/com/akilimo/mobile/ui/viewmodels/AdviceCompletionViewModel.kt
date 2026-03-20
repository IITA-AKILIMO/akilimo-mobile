package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.repos.AdviceCompletionRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdviceCompletionViewModel @Inject constructor(
    private val repo: AdviceCompletionRepo
) : ViewModel() {

    val completions = repo.getAllCompletions()

    fun updateStatus(dto: AdviceCompletionDto) = viewModelScope.launch {
        repo.updateStatus(dto)
    }
}
