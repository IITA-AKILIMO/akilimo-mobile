package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dto.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
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

    companion object {
        /** Tasks, in display order, whose status is anything other than COMPLETED. */
        fun incompleteTasks(taskItems: List<Pair<EnumAdviceTask, EnumStepStatus>>): List<EnumAdviceTask> =
            taskItems.filter { (_, status) -> status != EnumStepStatus.COMPLETED }.map { it.first }

        /** Share of tasks that are COMPLETED, 0..1; 0 when there are no tasks. */
        fun progress(taskItems: List<Pair<EnumAdviceTask, EnumStepStatus>>): Float =
            if (taskItems.isEmpty()) 0f else 1f - incompleteTasks(taskItems).size.toFloat() / taskItems.size
    }
}
