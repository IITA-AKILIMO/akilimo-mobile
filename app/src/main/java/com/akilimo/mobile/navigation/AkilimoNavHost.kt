package com.akilimo.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

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
) {
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
