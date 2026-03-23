package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.navigation.CassavaMarketRoute
import com.akilimo.mobile.navigation.CassavaYieldRoute
import com.akilimo.mobile.navigation.DatesRoute
import com.akilimo.mobile.navigation.FertilizersRoute
import com.akilimo.mobile.navigation.GetRecommendationRoute
import com.akilimo.mobile.navigation.InterCropFertilizersRoute
import com.akilimo.mobile.navigation.InvestmentAmountRoute
import com.akilimo.mobile.navigation.MaizeMarketRoute
import com.akilimo.mobile.navigation.MaizePerformanceRoute
import com.akilimo.mobile.navigation.ManualTillageCostRoute
import com.akilimo.mobile.navigation.SweetPotatoInterCropFertilizersRoute
import com.akilimo.mobile.navigation.SweetPotatoMarketRoute
import com.akilimo.mobile.navigation.TractorAccessRoute
import com.akilimo.mobile.navigation.WeedControlCostsRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
import com.akilimo.mobile.ui.viewmodels.AdviceCompletionViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UseCaseScreen(
    useCase: EnumUseCase,
    tasks: List<EnumAdviceTask>,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val completionVm = hiltViewModel<AdviceCompletionViewModel>()
    val completions by completionVm.completions.collectAsStateWithLifecycle(initialValue = emptyMap())

    // Build visible task list with completion statuses
    val taskItems = tasks.map { task ->
        val status = completions[task.name]?.stepStatus ?: EnumStepStatus.NOT_STARTED
        task to status
    }

    // Observe completed_task results from Compose screens
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<AdviceCompletionDto?>("completed_task", null)
            ?.collectLatest { dto ->
                if (dto != null) {
                    completionVm.updateStatus(dto)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<AdviceCompletionDto>("completed_task")
                }
            }
    }

    fun navigateToTask(task: EnumAdviceTask) {
        when (task) {
            EnumAdviceTask.AVAILABLE_FERTILIZERS -> navController.navigate(FertilizersRoute)
            EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM -> navController.navigate(InterCropFertilizersRoute)
            EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS -> navController.navigate(SweetPotatoInterCropFertilizersRoute)
            EnumAdviceTask.INVESTMENT_AMOUNT -> navController.navigate(InvestmentAmountRoute)
            EnumAdviceTask.CURRENT_CASSAVA_YIELD -> navController.navigate(CassavaYieldRoute)
            EnumAdviceTask.CASSAVA_MARKET_OUTLET -> navController.navigate(CassavaMarketRoute)
            EnumAdviceTask.MAIZE_MARKET_OUTLET -> navController.navigate(MaizeMarketRoute)
            EnumAdviceTask.MAIZE_PERFORMANCE -> navController.navigate(MaizePerformanceRoute)
            EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET -> navController.navigate(SweetPotatoMarketRoute)
            EnumAdviceTask.PLANTING_AND_HARVEST -> navController.navigate(DatesRoute)
            EnumAdviceTask.MANUAL_TILLAGE_COST -> navController.navigate(ManualTillageCostRoute)
            EnumAdviceTask.TRACTOR_ACCESS -> navController.navigate(TractorAccessRoute)
            EnumAdviceTask.COST_OF_WEED_CONTROL -> navController.navigate(WeedControlCostsRoute)
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = stringResource(R.string.lbl_get_recommendations),
                onClick = { navController.navigate(GetRecommendationRoute(useCase.name)) }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(taskItems, key = { it.first.name }) { (task, status) ->
                TaskItemCard(
                    label = task.label(context),
                    status = status,
                    onClick = { navigateToTask(task) }
                )
            }
        }
    }
}
