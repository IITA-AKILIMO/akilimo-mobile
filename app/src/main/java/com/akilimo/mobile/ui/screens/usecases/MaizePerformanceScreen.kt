package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.dto.MaizePerfOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.SelectionCard
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.MaizePerformanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaizePerformanceScreen(
    navController: NavHostController,
    viewModel: MaizePerformanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.MAIZE_PERFORMANCE)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    @Composable
    fun OptionCard(option: MaizePerfOption, grid: Boolean) {
        SelectionCard(
            imageRes = option.valueOption.imageRes,
            title = context.getString(option.valueOption.label),
            subtitle = option.valueOption.performanceDesc?.let { context.getString(it) },
            isSelected = state.selected == option.valueOption,
            isGridLayout = grid,
            onClick = { viewModel.select(option.valueOption) }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_maize_performance),
                collapsedTitle = stringResource(R.string.lbl_maize),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStackSafely() },
                actions = {
                    LayoutToggleButton(isGridLayout = state.isGridLayout, onToggle = viewModel::toggleLayout)
                }
            )
        },
        bottomBar = {
            FormSaveBar(
                enabled = state.selected != null,
                isSaving = state.isSaving,
                disabledReason = stringResource(R.string.msg_select_an_option),
                onSave = viewModel::saveSelection,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isGridLayout) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = padding,
                horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AkilimoSpacing.xs)
            ) {
                items(state.options, key = { it.valueOption.name }) { option ->
                    OptionCard(option, grid = true)
                }
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AkilimoSpacing.xs)
            ) {
                items(state.options, key = { it.valueOption.name }) { option ->
                    OptionCard(option, grid = false)
                }
            }
        }
    }
}
