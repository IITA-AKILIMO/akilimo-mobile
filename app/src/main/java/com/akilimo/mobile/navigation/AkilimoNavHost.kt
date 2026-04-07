package com.akilimo.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.akilimo.mobile.data.AppSettingsEntryPoint
import dagger.hilt.android.EntryPointAccessors

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
    val context = LocalContext.current
    val appSettings = remember {
        EntryPointAccessors.fromApplication(context, AppSettingsEntryPoint::class.java)
            .appSettings()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        onboardingGraph(navController = navController, appSettings = appSettings)
        recommendationsGraph(navController = navController)
        useCaseGraph(navController = navController)
        settingsGraph(navController = navController)
    }
}
