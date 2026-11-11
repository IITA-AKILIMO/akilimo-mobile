package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.InvestmentAmountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentAmountScreen(
    navController: NavHostController,
    viewModel: InvestmentAmountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    // Amounts are whole currency units; group digits the way the farmer's language does
    val amountFormat = rememberNumberFormat(maxFractionDigits = 0)

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.INVESTMENT_AMOUNT)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val fieldLabel = stringResource(
        R.string.lbl_investment_for_field,
        rememberNumberFormat().format(state.farmSize),
        state.enumAreaUnit.label(context)
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_investment_amount),
                collapsedTitle = stringResource(R.string.lbl_investment),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStackSafely() },
            )
        },
        bottomBar = {
            FormSaveBar(
                enabled = state.selectedItem != null,
                isSaving = state.isSaving,
                disabledReason = stringResource(R.string.msg_select_investment_amount)
                    .takeIf { state.investments.isNotEmpty() },
                onSave = viewModel::saveSelected,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        RefreshableList(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().selectableGroup()) {
                when {
                    state.isLoading -> item(key = "loading") { ListLoading() }
                    state.investments.isEmpty() -> item(key = "empty") {
                        ListEmpty(
                            message = stringResource(R.string.msg_no_investment_options),
                            modifier = Modifier.padding(horizontal = AkilimoSpacing.md),
                        )
                    }
                }
                items(state.investments, key = { it.id }) { item ->
                    val isSelected = state.selectedId == item.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xxs)
                    ) {
                        // One tap target for the whole row; the RadioButton is display-only
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    role = Role.RadioButton,
                                    onClick = { viewModel.select(item) }
                                )
                                .padding(AkilimoSpacing.xs)
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Column(modifier = Modifier.padding(start = AkilimoSpacing.xs)) {
                                Text(
                                    text = stringResource(
                                        R.string.lbl_currency_amount,
                                        item.currencySymbol,
                                        amountFormat.format(item.investmentAmount)
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = fieldLabel,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(AkilimoSpacing.xs)) }
            }
        }
    }
}
