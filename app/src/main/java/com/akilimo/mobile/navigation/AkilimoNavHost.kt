package com.akilimo.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.akilimo.mobile.notifications.NotificationTarget
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

/** Compose route a notification tap opens. */
fun NotificationTarget.toRoute(): Any = when (this) {
    NotificationTarget.HUB -> HubRoute
    NotificationTarget.RECOMMENDATIONS -> RecommendationsRoute
    NotificationTarget.SETTINGS -> SettingsRoute
    NotificationTarget.INBOX -> InboxRoute
}

/**
 * Root navigation host for the AKILIMO app.
 *
 * Route registration is split into feature graph functions so the root host
 * only wires the top-level graphs.
 */
@Composable
fun AkilimoNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = OnboardingRoute,
    /** Screen a tapped push notification asked for; opened once, then [onTargetHandled] is called. */
    notificationTarget: NotificationTarget? = null,
    onTargetHandled: () -> Unit = {},
) {
    LaunchedEffect(notificationTarget) {
        notificationTarget?.let {
            navController.navigate(it.toRoute()) { launchSingleTop = true }
            onTargetHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        onboardingGraph(navController = navController)
        recommendationsGraph(navController = navController)
        useCaseGraph(navController = navController)
        accountGraph(navController = navController)
        settingsGraph(navController = navController)
    }
}
