package com.akilimo.mobile.ui.components.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.dto.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus

/**
 * True only while the current destination is fully on screen. A second tap that lands
 * during the pop animation sees the entry already leaving (not RESUMED) and is ignored,
 * so double-taps no longer pop two screens.
 */
private fun NavHostController.isCurrentEntryResumed(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

/**
 * Back navigation that is safe against double-taps and never empties the back stack:
 * on the root destination it finishes the activity instead of leaving a blank screen.
 */
fun NavHostController.popBackStackSafely(): Boolean {
    if (!isCurrentEntryResumed()) return false
    if (previousBackStackEntry == null) {
        context.findActivity()?.finish()
        return false
    }
    return popBackStack()
}

/**
 * Sets "completed_task" on the previous back stack entry's savedStateHandle
 * and pops the back stack. Use this in use-case screens to signal completion
 * back to the parent UseCaseScreen. Ignored if the screen is already leaving,
 * so a double-tap cannot pop the parent as well.
 */
fun NavHostController.completeTask(task: EnumAdviceTask) {
    if (!isCurrentEntryResumed()) return
    val previous = previousBackStackEntry ?: return
    previous.savedStateHandle["completed_task"] = AdviceCompletionDto(task, EnumStepStatus.COMPLETED)
    popBackStack()
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
