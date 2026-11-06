package com.akilimo.mobile.ui.screens.recommendations

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController

/**
 * Forward navigation that ignores a second tap landing while the first navigation is
 * already animating: the current entry is no longer RESUMED, so the duplicate is dropped
 * instead of pushing the same destination twice.
 */
internal fun NavHostController.navigateIfResumed(route: Any) {
    if (currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return
    navigate(route)
}
