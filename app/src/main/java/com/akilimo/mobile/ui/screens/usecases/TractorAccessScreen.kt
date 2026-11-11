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
import com.akilimo.mobile.ui.components.compose.BinaryToggleChips
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.TractorAccessViewModel
import com.akilimo.mobile.ui.viewmodels.TractorAccessViewModel.SaveBlocker
import com.akilimo.mobile.utils.AmountInput

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TractorAccessScreen(
    navController: NavHostController,
    viewModel: TractorAccessViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val numberFormat = rememberNumberFormat()

    var tractorAvailable by rememberSaveable { mutableStateOf(false) }
    var usePlough by rememberSaveable { mutableStateOf(false) }
    var useRidge by rememberSaveable { mutableStateOf(false) }
    var useHarrow by rememberSaveable { mutableStateOf(false) }
    var ploughCost by rememberSaveable { mutableStateOf("") }
    var ridgeCost by rememberSaveable { mutableStateOf("") }
    var harrowCost by rememberSaveable { mutableStateOf("") }

    // Prefill once from the DB; after rotation keep the user's unsaved edits
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.userId) {
        if (state.userId != 0 && !prefilled) {
            prefilled = true
            tractorAvailable = state.tractorAvailable
            usePlough = state.tractorPloughCost > 0.0
            useRidge = state.tractorRidgeCost > 0.0
            useHarrow = state.tractorHarrowCost > 0.0
            ploughCost = state.tractorPloughCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
            ridgeCost = state.tractorRidgeCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
            harrowCost = state.tractorHarrowCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
        }
    }

    val blocker = TractorAccessViewModel.saveBlocker(
        tractorAvailable,
        listOf(usePlough to ploughCost, useRidge to ridgeCost, useHarrow to harrowCost)
    )
    val canSave = blocker == SaveBlocker.NONE
    val disabledReason = when (blocker) {
        SaveBlocker.NO_IMPLEMENT -> stringResource(R.string.msg_select_tractor_operation)
        SaveBlocker.MISSING_COST -> stringResource(R.string.msg_enter_cost_for_each)
        SaveBlocker.NONE -> null
    }
    val invalidAmount = stringResource(R.string.error_amount_invalid)
    fun errorFor(text: String) =
        invalidAmount.takeIf { AmountInput.isInvalid(text, requirePositive = true) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.TRACTOR_ACCESS)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val areaUnitLabel = state.enumAreaUnit.label(context)
    fun costLabel(template: String) =
        fillFarmSizeTemplate(template, state.farmSize, areaUnitLabel, numberFormat)

    // The last visible cost field gets the Done key; the others move to the next field
    val lastField = when {
        useHarrow -> "harrow"
        useRidge -> "ridge"
        else -> "plough"
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_tractor_access),
                collapsedTitle = stringResource(R.string.lbl_tractor_access_title),
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
                        tractorAvailable = tractorAvailable,
                        ploughingCost = if (usePlough) AmountInput.parse(ploughCost) else 0.0,
                        ridingCost = if (useRidge) AmountInput.parse(ridgeCost) else 0.0,
                        harrowingCost = if (useHarrow) AmountInput.parse(harrowCost) else 0.0
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            // Tractor available Yes / No
            Text(
                text = stringResource(R.string.lbl_tractor_available_question),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
            BinaryToggleChips(
                labelA = stringResource(R.string.lbl_yes),
                labelB = stringResource(R.string.lbl_no),
                selectedA = tractorAvailable,
                onSelectA = { tractorAvailable = true },
                onSelectB = {
                    tractorAvailable = false
                    usePlough = false; useRidge = false; useHarrow = false
                    ploughCost = ""; ridgeCost = ""; harrowCost = ""
                }
            )

            if (tractorAvailable) {
                Spacer(modifier = Modifier.height(AkilimoSpacing.sm))

                // Implement selector
                Text(
                    text = stringResource(R.string.lbl_select_tractor_operations),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(AkilimoSpacing.xxs))
                // FlowRow wraps on narrow phones and with longer sw/rw labels
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
                    FilterChip(
                        selected = usePlough,
                        onClick = {
                            usePlough = !usePlough
                            if (!usePlough) ploughCost = ""
                        },
                        label = { Text(stringResource(R.string.lbl_plough)) },
                    )
                    FilterChip(
                        selected = useRidge,
                        onClick = {
                            useRidge = !useRidge
                            if (!useRidge) ridgeCost = ""
                        },
                        label = { Text(stringResource(R.string.lbl_ridger)) },
                    )
                    FilterChip(
                        selected = useHarrow,
                        onClick = {
                            useHarrow = !useHarrow
                            if (!useHarrow) harrowCost = ""
                        },
                        label = { Text(stringResource(R.string.lbl_harrow)) }
                    )
                }

                if (usePlough) {
                    Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
                    AkilimoTextField(
                        value = ploughCost,
                        onValueChange = { ploughCost = it },
                        label = costLabel(stringResource(R.string.lbl_tractor_plough_cost_hint)),
                        error = errorFor(ploughCost),
                        trailingIcon = currencySuffix(state.currencyCode),
                        keyboardOptions = amountKeyboard(isLast = lastField == "plough")
                    )
                }
                if (useRidge) {
                    Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
                    AkilimoTextField(
                        value = ridgeCost,
                        onValueChange = { ridgeCost = it },
                        label = costLabel(stringResource(R.string.lbl_tractor_ridge_cost_hint)),
                        error = errorFor(ridgeCost),
                        trailingIcon = currencySuffix(state.currencyCode),
                        keyboardOptions = amountKeyboard(isLast = lastField == "ridge")
                    )
                }
                if (useHarrow) {
                    Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
                    AkilimoTextField(
                        value = harrowCost,
                        onValueChange = { harrowCost = it },
                        label = costLabel(stringResource(R.string.lbl_tractor_harrow_cost_hint)),
                        error = errorFor(harrowCost),
                        trailingIcon = currencySuffix(state.currencyCode),
                        keyboardOptions = amountKeyboard(isLast = lastField == "harrow")
                    )
                }
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.md))
        }
    }
}
