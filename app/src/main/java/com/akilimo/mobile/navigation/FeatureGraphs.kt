package com.akilimo.mobile.navigation

import OnboardingScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.screens.onboarding.LegalWizardScreen
import com.akilimo.mobile.ui.screens.recommendations.GetRecommendationScreen
import com.akilimo.mobile.ui.screens.recommendations.RecommendationsScreen
import com.akilimo.mobile.ui.screens.recommendations.UseCaseScreen
import com.akilimo.mobile.ui.screens.settings.LocationPickerScreen
import com.akilimo.mobile.ui.screens.settings.SettingsScreen
import com.akilimo.mobile.ui.screens.settings.WebViewScreen
import com.akilimo.mobile.ui.screens.usecases.CassavaMarketScreen
import com.akilimo.mobile.ui.screens.usecases.CassavaYieldScreen
import com.akilimo.mobile.ui.screens.usecases.DatesScreen
import com.akilimo.mobile.ui.screens.usecases.FertilizerScreen
import com.akilimo.mobile.ui.screens.usecases.InvestmentAmountScreen
import com.akilimo.mobile.ui.screens.usecases.MaizeMarketScreen
import com.akilimo.mobile.ui.screens.usecases.MaizePerformanceScreen
import com.akilimo.mobile.ui.screens.usecases.ManualTillageCostScreen
import com.akilimo.mobile.ui.screens.usecases.SweetPotatoMarketScreen
import com.akilimo.mobile.ui.screens.usecases.TractorAccessScreen
import com.akilimo.mobile.ui.screens.usecases.WeedControlCostsScreen

fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    appSettings: AppSettingsDataStore,
) {
    composable<LegalWizardRoute> {
        LegalWizardScreen(navController = navController)
    }
    composable<OnboardingRoute> {
        OnboardingScreen(
            navController = navController,
            appSettings = appSettings,
        )
    }
}

fun NavGraphBuilder.recommendationsGraph(navController: NavHostController) {
    composable<RecommendationsRoute> {
        RecommendationsScreen(navController = navController)
    }
    composable<FrRoute> {
        UseCaseScreen(
            useCase = EnumUseCase.FR,
            tasks = listOf(
                EnumAdviceTask.AVAILABLE_FERTILIZERS,
                EnumAdviceTask.INVESTMENT_AMOUNT,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
            ),
            navController = navController,
        )
    }
    composable<BppRoute> {
        UseCaseScreen(
            useCase = EnumUseCase.PP,
            tasks = listOf(
                EnumAdviceTask.PLANTING_AND_HARVEST,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
                EnumAdviceTask.MANUAL_TILLAGE_COST,
                EnumAdviceTask.TRACTOR_ACCESS,
                EnumAdviceTask.COST_OF_WEED_CONTROL,
            ),
            navController = navController,
        )
    }
    composable<SphRoute> {
        UseCaseScreen(
            useCase = EnumUseCase.SP,
            tasks = listOf(
                EnumAdviceTask.PLANTING_AND_HARVEST,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
            ),
            navController = navController,
        )
    }
    composable<IcMaizeRoute> {
        UseCaseScreen(
            useCase = EnumUseCase.CIM,
            tasks = listOf(
                EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM,
                EnumAdviceTask.PLANTING_AND_HARVEST,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.MAIZE_MARKET_OUTLET,
                EnumAdviceTask.MAIZE_PERFORMANCE,
            ),
            navController = navController,
        )
    }
    composable<IcSweetPotatoRoute> {
        UseCaseScreen(
            useCase = EnumUseCase.CIS,
            tasks = listOf(
                EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS,
                EnumAdviceTask.PLANTING_AND_HARVEST,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET,
            ),
            navController = navController,
        )
    }
    composable<GetRecommendationRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<GetRecommendationRoute>()
        GetRecommendationScreen(
            useCase = EnumUseCase.fromCode(route.useCaseCode),
            navController = navController,
        )
    }
}

fun NavGraphBuilder.useCaseGraph(navController: NavHostController) {
    composable<FertilizersRoute> {
        FertilizerScreen(
            fertilizerFlow = EnumFertilizerFlow.DEFAULT,
            adviceTask = EnumAdviceTask.AVAILABLE_FERTILIZERS,
            navController = navController,
        )
    }
    composable<InterCropFertilizersRoute> {
        FertilizerScreen(
            fertilizerFlow = EnumFertilizerFlow.CIM,
            adviceTask = EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM,
            navController = navController,
        )
    }
    composable<SweetPotatoInterCropFertilizersRoute> {
        FertilizerScreen(
            fertilizerFlow = EnumFertilizerFlow.CIS,
            adviceTask = EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS,
            navController = navController,
        )
    }
    composable<InvestmentAmountRoute> {
        InvestmentAmountScreen(navController = navController)
    }
    composable<CassavaMarketRoute> {
        CassavaMarketScreen(navController = navController)
    }
    composable<CassavaYieldRoute> {
        CassavaYieldScreen(navController = navController)
    }
    composable<DatesRoute> {
        DatesScreen(navController = navController)
    }
    composable<ManualTillageCostRoute> {
        ManualTillageCostScreen(navController = navController)
    }
    composable<TractorAccessRoute> {
        TractorAccessScreen(navController = navController)
    }
    composable<WeedControlCostsRoute> {
        WeedControlCostsScreen(navController = navController)
    }
    composable<MaizeMarketRoute> {
        MaizeMarketScreen(navController = navController)
    }
    composable<MaizePerformanceRoute> {
        MaizePerformanceScreen(navController = navController)
    }
    composable<SweetPotatoMarketRoute> {
        SweetPotatoMarketScreen(navController = navController)
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    composable<SettingsRoute> {
        SettingsScreen(navController = navController)
    }
    composable<WebViewRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<WebViewRoute>()
        WebViewScreen(route = route, navController = navController)
    }
    composable<LocationPickerRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<LocationPickerRoute>()
        LocationPickerScreen(route = route, navController = navController)
    }
}
