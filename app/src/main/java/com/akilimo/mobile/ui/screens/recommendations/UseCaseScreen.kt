package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.dto.AdviceCompletionDto
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
import com.akilimo.mobile.ui.components.compose.InfoCard
import com.akilimo.mobile.ui.components.compose.InfoCardType
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.AdviceCompletionViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UseCaseScreen(
    useCase: EnumUseCase,
    tasks: List<EnumAdviceTask>,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val completionVm = hiltViewModel<AdviceCompletionViewModel>()
    val completions by completionVm.completions.collectAsStateWithLifecycle(initialValue = emptyMap())
    var showIncompleteDialog by rememberSaveable { mutableStateOf(false) }

    // Build visible task list with completion statuses
    val taskItems = tasks.map { task ->
        val status = completions[task.name]?.stepStatus ?: EnumStepStatus.NOT_STARTED
        task to status
    }
    // The recommendation request falls back to defaults for any step not answered, so
    // no step is strictly required. Instead of blocking, name the unfinished steps.
    val incompleteLabels = AdviceCompletionViewModel.incompleteTasks(taskItems)
        .map { it.label(context) }
        .distinct()

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
        val route: Any = when (task) {
            EnumAdviceTask.AVAILABLE_FERTILIZERS -> FertilizersRoute
            EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM -> InterCropFertilizersRoute
            EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS -> SweetPotatoInterCropFertilizersRoute
            EnumAdviceTask.INVESTMENT_AMOUNT -> InvestmentAmountRoute
            EnumAdviceTask.CURRENT_CASSAVA_YIELD -> CassavaYieldRoute
            EnumAdviceTask.CASSAVA_MARKET_OUTLET -> CassavaMarketRoute
            EnumAdviceTask.MAIZE_MARKET_OUTLET -> MaizeMarketRoute
            EnumAdviceTask.MAIZE_PERFORMANCE -> MaizePerformanceRoute
            EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET -> SweetPotatoMarketRoute
            EnumAdviceTask.PLANTING_AND_HARVEST -> DatesRoute
            EnumAdviceTask.MANUAL_TILLAGE_COST -> ManualTillageCostRoute
            EnumAdviceTask.TRACTOR_ACCESS -> TractorAccessRoute
            EnumAdviceTask.COST_OF_WEED_CONTROL -> WeedControlCostsRoute
            else -> return
        }
        navController.navigateIfResumed(route)
    }

    fun openRecommendation() = navController.navigateIfResumed(GetRecommendationRoute(useCase.name))

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                onBack = { navController.popBackStackSafely() }
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = stringResource(R.string.lbl_get_recommendations),
                onClick = {
                    if (incompleteLabels.isEmpty()) openRecommendation() else showIncompleteDialog = true
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            if (incompleteLabels.isNotEmpty()) {
                item(key = "incomplete_hint") {
                    InfoCard(
                        title = stringResource(R.string.lbl_steps_remaining_title),
                        message = stringResource(
                            R.string.msg_steps_remaining,
                            incompleteLabels.size,
                            taskItems.size
                        ),
                        modifier = Modifier.padding(
                            horizontal = AkilimoSpacing.md,
                            vertical = AkilimoSpacing.xs
                        ),
                    )
                }
            }
            itemsIndexed(taskItems, key = { _, item -> item.first.name }) { index, (task, status) ->
                TaskItemCard(
                    label = task.label(context),
                    status = status,
                    stepNumber = index + 1,
                    onClick = { navigateToTask(task) }
                )
            }
        }
    }

    if (showIncompleteDialog) {
        AlertDialog(
            onDismissRequest = { showIncompleteDialog = false },
            title = { Text(stringResource(R.string.lbl_incomplete_steps_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.msg_incomplete_steps,
                        incompleteLabels.joinToString(separator = "\n• ", prefix = "• ")
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showIncompleteDialog = false
                    openRecommendation()
                }) { Text(stringResource(R.string.lbl_continue_anyway)) }
            },
            dismissButton = {
                TextButton(onClick = { showIncompleteDialog = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            }
        )
    }
}
