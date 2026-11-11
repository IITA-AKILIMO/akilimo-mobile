package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.RadioButtonRow
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.CassavaMarketViewModel
import com.akilimo.mobile.ui.viewmodels.CassavaMarketViewModel.MarketChoice
import com.akilimo.mobile.utils.AmountInput
import com.akilimo.mobile.utils.MathHelper
import kotlinx.coroutines.launch

/** The localised name of a cassava unit ("1 tonne"), falling back to the raw DB label. */
@Composable
private fun CassavaUnit.displayLabel(): String =
    EnumUnitOfSale.entries.find { it.name.equals(label, ignoreCase = true) }
        ?.let { stringResource(it.labelRes) } ?: label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CassavaMarketScreen(
    navController: NavHostController,
    viewModel: CassavaMarketViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var marketChoice by rememberSaveable { mutableStateOf(MarketChoice.NONE) }
    // The open sheet is remembered by unit id so it survives rotation
    var sheetUnitId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Apply the initial market choice once
    LaunchedEffect(state.initialMarketChoice) {
        if (marketChoice == MarketChoice.NONE && state.initialMarketChoice != MarketChoice.NONE) {
            marketChoice = state.initialMarketChoice
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val canSave = when (marketChoice) {
        MarketChoice.FACTORY -> state.selectedFactoryId != null
        MarketChoice.MARKET -> state.selectedUnitId != null
        MarketChoice.NONE -> false
    }
    val disabledReason = stringResource(
        when (marketChoice) {
            MarketChoice.FACTORY -> R.string.msg_select_starch_factory
            MarketChoice.MARKET -> R.string.msg_select_unit_and_price
            MarketChoice.NONE -> R.string.msg_select_where_you_sell
        }
    )

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_cassava_market_outlet),
                collapsedTitle = stringResource(R.string.lbl_cassava),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStackSafely() },
            )
        },
        bottomBar = {
            // Choices are already written when made; Save marks the step complete
            FormSaveBar(
                enabled = canSave,
                isSaving = state.isSaving,
                disabledReason = disabledReason,
                onSave = { navController.completeTask(EnumAdviceTask.CASSAVA_MARKET_OUTLET) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Market choice radio buttons
            Column(
                modifier = Modifier
                    .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs)
                    .selectableGroup()
            ) {
                RadioButtonRow(
                    label = stringResource(R.string.lbl_starch_factory),
                    selected = marketChoice == MarketChoice.FACTORY,
                    onClick = { marketChoice = MarketChoice.FACTORY },
                )
                RadioButtonRow(
                    label = stringResource(R.string.lbl_regular_market),
                    selected = marketChoice == MarketChoice.MARKET,
                    onClick = { marketChoice = MarketChoice.MARKET },
                )
            }

            val listModifier = Modifier
                .weight(1f)
                .padding(horizontal = AkilimoSpacing.md)
            when (marketChoice) {
                MarketChoice.FACTORY -> when {
                    state.factoriesLoading -> ListLoading(listModifier)
                    state.factories.isEmpty() ->
                        ListEmpty(stringResource(R.string.msg_no_starch_factories), listModifier)
                    else -> LazyColumn(modifier = listModifier.selectableGroup()) {
                        items(state.factories, key = { it.id ?: 0 }) { factory ->
                            RadioButtonRow(
                                label = factory.name.orEmpty(),
                                selected = state.selectedFactoryId == factory.id,
                                enabled = !state.isSaving,
                                onClick = { viewModel.selectFactory(factory) },
                            )
                        }
                    }
                }

                MarketChoice.MARKET -> when {
                    state.unitsLoading -> ListLoading(listModifier)
                    state.cassavaUnits.isEmpty() ->
                        ListEmpty(stringResource(R.string.msg_no_units_of_sale), listModifier)
                    else -> LazyColumn(modifier = listModifier.selectableGroup()) {
                        items(state.cassavaUnits, key = { it.id }) { unit ->
                            RadioButtonRow(
                                label = unit.displayLabel(),
                                selected = state.selectedUnitId == unit.id,
                                onClick = { sheetUnitId = unit.id },
                            )
                        }
                    }
                }

                MarketChoice.NONE -> Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    val sheetUnit = state.cassavaUnits.find { it.id == sheetUnitId }
    if (sheetUnit != null) {
        CassavaPriceSheet(
            unit = sheetUnit,
            isCurrentSelection = state.selectedUnitId == sheetUnit.id,
            isSaving = state.isSaving,
            loadOptions = viewModel::loadPriceOptions,
            onSave = { uos, price -> viewModel.saveSelectedPrice(sheetUnit, uos, price) },
            onDismiss = { sheetUnitId = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CassavaPriceSheet(
    unit: CassavaUnit,
    isCurrentSelection: Boolean,
    isSaving: Boolean,
    loadOptions: suspend () -> CassavaMarketViewModel.PriceOptions,
    onSave: (EnumUnitOfSale, CassavaMarketPrice?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val uos = remember(unit.label) {
        EnumUnitOfSale.entries.find { it.name.equals(unit.label, ignoreCase = true) }
            ?: EnumUnitOfSale.THOUSAND_KG
    }

    var prices by remember(unit.id) { mutableStateOf<List<CassavaMarketPrice>?>(null) }
    var selectedPriceId by rememberSaveable(unit.id) { mutableStateOf<Int?>(null) }
    var exactPriceInput by rememberSaveable(unit.id) { mutableStateOf("") }
    // Pre-fill from the saved answer once; after rotation keep the user's pick
    var prefilled by rememberSaveable(unit.id) { mutableStateOf(false) }

    LaunchedEffect(unit.id) {
        val options = loadOptions()
        prices = options.prices
        if (!prefilled) {
            prefilled = true
            selectedPriceId = options.savedPriceId
            exactPriceInput = options.savedExactPrice?.let(AmountInput::format) ?: ""
        }
    }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    val loaded = prices
    val selectedPriceItem = loaded?.find { it.id == selectedPriceId }
    val isExactSelected = selectedPriceItem?.exactPrice == true
    // averagePrice == 0.0 (and not exact) is the "I don't know" option: valid, saved as price 0
    val isDoNotKnowSelected = selectedPriceItem != null && !isExactSelected &&
            selectedPriceItem.averagePrice == 0.0
    val exactPriceValue = AmountInput.parse(exactPriceInput)?.takeIf { it > 0.0 }
    val isConfirmEnabled = !isSaving && selectedPriceItem != null &&
            ((selectedPriceItem.averagePrice > 0.0 && !isExactSelected) ||
                    isDoNotKnowSelected ||
                    (isExactSelected && exactPriceValue != null))
    val unitLabel = unit.displayLabel()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AkilimoSpacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = unitLabel, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            when {
                loaded == null -> ListLoading()
                loaded.isEmpty() -> ListEmpty(stringResource(R.string.msg_no_prices_available))
                else -> Column(modifier = Modifier.selectableGroup()) {
                    loaded.forEach { price ->
                        val computedPrice = MathHelper.computeUnitPrice(price.averagePrice, uos)
                        val label = when {
                            price.averagePrice == 0.0 -> stringResource(R.string.lbl_do_not_know)
                            price.exactPrice -> stringResource(R.string.lbl_exact_price)
                            else -> "${MathHelper.format(computedPrice)} ${price.currencySymbol}"
                        }
                        RadioButtonRow(
                            label = label,
                            selected = selectedPriceId == price.id,
                            onClick = { selectedPriceId = price.id }
                        )
                        if (price.exactPrice && selectedPriceId == price.id) {
                            AkilimoTextField(
                                value = exactPriceInput,
                                onValueChange = { exactPriceInput = it },
                                label = pricePerUnitLabel(unitLabel, price.currencySymbol),
                                error = stringResource(R.string.error_amount_invalid)
                                    .takeIf { AmountInput.isInvalid(exactPriceInput, requirePositive = true) },
                                keyboardOptions = amountKeyboard(isLast = true),
                                // Indent under the radio button
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = AkilimoSpacing.xxxl)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            Button(
                onClick = {
                    val priceToSave: CassavaMarketPrice? = when {
                        isExactSelected -> {
                            val amount = exactPriceValue ?: return@Button
                            selectedPriceItem.copy(averagePrice = amount)
                        }

                        selectedPriceItem != null &&
                                (selectedPriceItem.averagePrice > 0.0 || isDoNotKnowSelected) ->
                            selectedPriceItem

                        else -> return@Button
                    }
                    onSave(uos, priceToSave)
                    close()
                },
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_confirm))
            }

            if (isCurrentSelection) {
                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
                OutlinedButton(
                    onClick = {
                        onSave(uos, null)
                        close()
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.lbl_remove))
                }
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.md))
        }
    }
}
