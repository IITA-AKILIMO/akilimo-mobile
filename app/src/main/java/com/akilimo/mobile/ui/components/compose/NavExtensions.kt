package com.akilimo.mobile.ui.components.compose

import androidx.navigation.NavHostController
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus

/**
 * Sets "completed_task" on the previous back stack entry's savedStateHandle
 * and pops the back stack. Use this in use-case screens to signal completion
 * back to the parent UseCaseScreen.
 */
fun NavHostController.completeTask(task: EnumAdviceTask) {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set("completed_task", AdviceCompletionDto(task, EnumStepStatus.COMPLETED))
    popBackStack()
}
