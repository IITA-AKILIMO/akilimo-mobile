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
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.ProduceMarketViewModel
import com.akilimo.mobile.utils.AmountInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SweetPotatoMarketScreen(
    navController: NavHostController,
    viewModel: ProduceMarketViewModel = hiltViewModel<
        ProduceMarketViewModel,
        ProduceMarketViewModel.Factory
    > { factory -> factory.create(EnumMarketType.SWEET_POTATO_MARKET) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedUnit by rememberSaveable { mutableStateOf<EnumUnitOfSale?>(null) }
    var price by rememberSaveable { mutableStateOf("") }

    // Prefill once from the DB; after rotation keep the user's unsaved edits
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.lastEntry) {
        state.lastEntry?.takeIf { !prefilled }?.let { entry ->
            prefilled = true
            selectedUnit = entry.unitOfSale
            price = entry.unitPrice.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val unitOptions = EnumUnitOfSale.entries.filter { it.isUniversal }
    val priceValue = AmountInput.parse(price)?.takeIf { it > 0.0 }
    val canSave = selectedUnit != null && priceValue != null
    val disabledReason = if (selectedUnit == null) {
        stringResource(R.string.msg_select_unit_of_sale)
    } else {
        stringResource(R.string.msg_enter_price)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_market_outlet_sweet_potato),
                collapsedTitle = stringResource(R.string.lbl_sweet_potato),
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
                    val unit = selectedUnit ?: return@FormSaveBar
                    val value = priceValue ?: return@FormSaveBar
                    viewModel.saveMarketEntry(
                        ProduceMarket(
                            userId = state.userId,
                            unitPrice = value,
                            marketType = EnumMarketType.SWEET_POTATO_MARKET,
                            produceType = EnumProduceType.SWEET_POTATO_TUBERS,
                            unitOfSale = unit
                        )
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_unit_of_sale),
                options = unitOptions,
                selectedOption = selectedUnit,
                onOptionSelected = { selectedUnit = it },
                displayText = { context.getString(it.labelRes) }
            )

            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            AkilimoTextField(
                value = price,
                onValueChange = { price = it },
                label = pricePerUnitLabel(
                    unitLabel = selectedUnit?.let { stringResource(it.labelRes) },
                    currencyCode = state.currencyCode
                ),
                error = stringResource(R.string.error_amount_invalid)
                    .takeIf { AmountInput.isInvalid(price, requirePositive = true) },
                keyboardOptions = amountKeyboard(isLast = true)
            )

            Spacer(modifier = Modifier.height(AkilimoSpacing.md))
        }
    }
}
