package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.ManualTillageCostViewModel
import com.akilimo.mobile.utils.AmountInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManualTillageCostScreen(
    navController: NavHostController,
    viewModel: ManualTillageCostViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val numberFormat = rememberNumberFormat()

    var usePlough by rememberSaveable { mutableStateOf(true) }
    var useRidge by rememberSaveable { mutableStateOf(true) }
    var ploughingCost by rememberSaveable { mutableStateOf("") }
    var ridgingCost by rememberSaveable { mutableStateOf("") }

    // Prefill once from the DB; after rotation keep the user's unsaved edits
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.userId) {
        if (state.userId != 0 && !prefilled) {
            prefilled = true
            usePlough = state.performPloughing
            useRidge = state.performRidging
            ploughingCost = state.manualPloughCost?.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
            ridgingCost = state.manualRidgeCost?.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
        }
    }

    // Each selected operation needs a positive cost; previously a non-blank but
    // unparseable value (e.g. "12,000") was saved as 0.
    fun costOk(enabled: Boolean, text: String) =
        !enabled || (AmountInput.parse(text) ?: 0.0) > 0.0
    val anySelected = usePlough || useRidge
    val canSave = anySelected && costOk(usePlough, ploughingCost) && costOk(useRidge, ridgingCost)
    val disabledReason = if (!anySelected) {
        stringResource(R.string.msg_select_tillage_operation)
    } else {
        stringResource(R.string.msg_enter_cost_for_each)
    }
    val invalidAmount = stringResource(R.string.error_amount_invalid)
    fun errorFor(text: String) =
        invalidAmount.takeIf { AmountInput.isInvalid(text, requirePositive = true) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.MANUAL_TILLAGE_COST)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val areaUnitLabel = state.enumAreaUnit.label(context)
    fun fill(template: String) = fillFarmSizeTemplate(template, state.farmSize, areaUnitLabel, numberFormat)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_cost_of_manual_tillage),
                collapsedTitle = stringResource(R.string.lbl_tillage),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStackSafely() },
            )
        },
        bottomBar = {
            FormSaveBar(
                enabled = canSave,
                isSaving = state.isSaving,
                disabledReason = disabledReason,
                onSave = {
                    viewModel.saveCosts(
                        ridingCost = if (useRidge) AmountInput.parse(ridgingCost) else 0.0,
                        ploughingCost = if (usePlough) AmountInput.parse(ploughingCost) else 0.0,
                        performPloughing = usePlough,
                        performRidging = useRidge
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            Text(
                text = fill(stringResource(R.string.lbl_manual_tillage_cost)),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(AkilimoSpacing.sm))

            Text(
                text = stringResource(R.string.lbl_plough_op_type),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(AkilimoSpacing.xxs))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
                FilterChip(
                    selected = usePlough,
                    onClick = {
                        usePlough = !usePlough
                        if (!usePlough) ploughingCost = ""
                    },
                    label = { Text(stringResource(R.string.lbl_ploughing)) },
                )
                FilterChip(
                    selected = useRidge,
                    onClick = {
                        useRidge = !useRidge
                        if (!useRidge) ridgingCost = ""
                    },
                    label = { Text(stringResource(R.string.lbl_ridging)) }
                )
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.sm))

            if (usePlough) {
                AkilimoTextField(
                    value = ploughingCost,
                    onValueChange = { ploughingCost = it },
                    label = fill(stringResource(R.string.lbl_manual_tillage_cost_hint)),
                    error = errorFor(ploughingCost),
                    trailingIcon = currencySuffix(state.currencyCode),
                    keyboardOptions = amountKeyboard(isLast = !useRidge)
                )
                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
            }

            if (useRidge) {
                AkilimoTextField(
                    value = ridgingCost,
                    onValueChange = { ridgingCost = it },
                    label = fill(stringResource(R.string.lbl_manual_ridge_cost_hint)),
                    error = errorFor(ridgingCost),
                    trailingIcon = currencySuffix(state.currencyCode),
                    keyboardOptions = amountKeyboard(isLast = true)
                )
                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.md))
        }
    }
}
