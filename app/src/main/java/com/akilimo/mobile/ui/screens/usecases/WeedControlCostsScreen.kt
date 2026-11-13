package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.FormCard
import com.akilimo.mobile.ui.components.compose.ResultCard
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.WeedControlCostsViewModel
import com.akilimo.mobile.utils.AmountInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeedControlCostsScreen(
    navController: NavHostController,
    viewModel: WeedControlCostsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedMethod by rememberSaveable { mutableStateOf<EnumWeedControlMethod?>(null) }
    var firstCost by rememberSaveable { mutableStateOf("") }
    var secondCost by rememberSaveable { mutableStateOf("") }

    // Prefill once from the DB; after rotation keep the user's unsaved edits
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.userId) {
        if (state.userId != 0 && !prefilled) {
            prefilled = true
            selectedMethod = state.weedControlMethod
            firstCost = state.firstWeedingCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
            secondCost = state.secondWeedingCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
        }
    }

    // Method is required; both costs are optional (blank clears), anything typed must parse
    val invalidAmount = stringResource(R.string.error_amount_invalid)
    val firstError = invalidAmount.takeIf { AmountInput.isInvalid(firstCost, requirePositive = false) }
    val secondError = invalidAmount.takeIf { AmountInput.isInvalid(secondCost, requirePositive = false) }
    val canSave = WeedControlCostsViewModel.canSave(selectedMethod, firstCost, secondCost)
    val disabledReason = when {
        selectedMethod == null -> stringResource(R.string.error_weed_control_required)
        else -> stringResource(R.string.msg_fix_amounts_to_save)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.COST_OF_WEED_CONTROL)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val weedMethods = EnumWeedControlMethod.entries

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_cost_of_weed_control),
                collapsedTitle = stringResource(R.string.lbl_weeding),
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
                        // Blank -> 0.0 so clearing a field actually clears the saved cost
                        firstCost = AmountInput.parse(firstCost) ?: 0.0,
                        secondCost = AmountInput.parse(secondCost) ?: 0.0,
                        method = selectedMethod
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            FormCard {
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_weed_control_method),
                    options = weedMethods,
                    selectedOption = selectedMethod,
                    onOptionSelected = { selectedMethod = it },
                    displayText = { it.label(context) }
                )

                AkilimoTextField(
                    value = firstCost,
                    onValueChange = { firstCost = it },
                    label = stringResource(R.string.lbl_first_weeding_costs_prompt),
                    helperText = stringResource(R.string.lbl_optional),
                    error = firstError,
                    trailingIcon = currencySuffix(state.currencyCode),
                    keyboardOptions = amountKeyboard()
                )

                AkilimoTextField(
                    value = secondCost,
                    onValueChange = { secondCost = it },
                    label = stringResource(R.string.lbl_second_weeding_cost_prompt),
                    helperText = stringResource(R.string.lbl_optional),
                    error = secondError,
                    trailingIcon = currencySuffix(state.currencyCode),
                    keyboardOptions = amountKeyboard(isLast = true)
                )
            }

            // Sum of the weeding costs typed so far; blank or invalid entries count as zero
            val totalCost = (AmountInput.parse(firstCost) ?: 0.0) + (AmountInput.parse(secondCost) ?: 0.0)
            if (totalCost > 0.0) {
                Spacer(modifier = Modifier.height(AkilimoSpacing.sm))
                ResultCard(
                    title = stringResource(R.string.lbl_total_weeding_cost),
                    value = rememberNumberFormat(maxFractionDigits = 0).format(totalCost),
                    unit = state.currencyCode.takeIf { it.isNotBlank() },
                    highlight = true,
                )
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.md))
        }
    }
}
