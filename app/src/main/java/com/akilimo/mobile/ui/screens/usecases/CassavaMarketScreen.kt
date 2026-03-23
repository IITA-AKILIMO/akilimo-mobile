package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.CassavaMarketViewModel
import com.akilimo.mobile.utils.MathHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CassavaMarketScreen(
    navController: NavHostController,
    viewModel: CassavaMarketViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var marketChoice by remember { mutableStateOf(CassavaMarketViewModel.MarketChoice.NONE) }
    var showPriceSheet by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf<CassavaUnit?>(null) }
    var prices by remember { mutableStateOf<List<CassavaMarketPrice>>(emptyList()) }
    var selectedPriceId by remember { mutableStateOf<Int?>(null) }
    var exactPriceInput by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Apply the initial market choice once
    LaunchedEffect(state.initialMarketChoice) {
        if (marketChoice == CassavaMarketViewModel.MarketChoice.NONE &&
            state.initialMarketChoice != CassavaMarketViewModel.MarketChoice.NONE
        ) {
            marketChoice = state.initialMarketChoice
        }
    }

    fun openUnitSheet(unit: CassavaUnit) {
        selectedUnit = unit
        prices = emptyList()
        selectedPriceId = null
        exactPriceInput = ""
        showPriceSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_cassava_price)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.lbl_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            val canConfirm = when (marketChoice) {
                CassavaMarketViewModel.MarketChoice.FACTORY -> state.selectedFactoryId != null
                CassavaMarketViewModel.MarketChoice.MARKET -> state.selectedUnitId != null
                CassavaMarketViewModel.MarketChoice.NONE -> false
            }
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "completed_task",
                                AdviceCompletionDto(
                                    EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                                    EnumStepStatus.COMPLETED
                                )
                            )
                        navController.popBackStack()
                    },
                    enabled = canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Market choice radio buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { marketChoice = CassavaMarketViewModel.MarketChoice.FACTORY }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                RadioButton(
                    selected = marketChoice == CassavaMarketViewModel.MarketChoice.FACTORY,
                    onClick = { marketChoice = CassavaMarketViewModel.MarketChoice.FACTORY }
                )
                Text(
                    text = stringResource(R.string.lbl_starch_factory),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { marketChoice = CassavaMarketViewModel.MarketChoice.MARKET }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                RadioButton(
                    selected = marketChoice == CassavaMarketViewModel.MarketChoice.MARKET,
                    onClick = { marketChoice = CassavaMarketViewModel.MarketChoice.MARKET }
                )
                Text(
                    text = stringResource(R.string.lbl_regular_market),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            when (marketChoice) {
                CassavaMarketViewModel.MarketChoice.FACTORY -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(state.factories, key = { it.id ?: 0 }) { factory ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable { viewModel.selectFactory(factory) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.selectedFactoryId == factory.id)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = factory.name.orEmpty(),
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                CassavaMarketViewModel.MarketChoice.MARKET -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(state.cassavaUnits, key = { it.id }) { unit ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable { openUnitSheet(unit) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.selectedUnitId == unit.id)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = EnumUnitOfSale.entries
                                        .find { it.name == unit.label }
                                        ?.let { stringResource(it.labelRes) } ?: unit.label,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                CassavaMarketViewModel.MarketChoice.NONE -> {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showPriceSheet && selectedUnit != null) {
        val unit = selectedUnit!!
        val uos = remember(unit.label) {
            EnumUnitOfSale.entries.find { it.name.equals(unit.label, ignoreCase = true) }
                ?: EnumUnitOfSale.THOUSAND_KG
        }

        LaunchedEffect(unit.id) {
            prices = viewModel.priceRepo.getPricesByCountry(state.userCountry)
            val mkt = viewModel.selectedRepo.getSelectedByUser(state.userId)
            selectedPriceId = mkt?.marketPrice?.id
            // Pre-fill exact price input if the saved selection was an exact-price entry
            val saved = mkt?.selectedCassavaMarket
            if (saved?.marketPriceId != null && saved.marketPriceId == selectedPriceId) {
                val savedPrice = prices.find { it.id == selectedPriceId }
                if (savedPrice?.exactPrice == true) {
                    exactPriceInput = saved.unitPrice.takeIf { it > 0 }?.toString() ?: ""
                }
            }
        }

        val selectedPriceItem = prices.find { it.id == selectedPriceId }
        val isExactSelected = selectedPriceItem?.exactPrice == true
        val isConfirmEnabled = selectedPriceItem != null &&
                (selectedPriceItem.averagePrice > 0.0 && !isExactSelected ||
                        (isExactSelected && exactPriceInput.toDoubleOrNull()
                            ?.let { it > 0.0 } == true))

        ModalBottomSheet(
            onDismissRequest = { showPriceSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = unit.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                prices.forEach { price ->
                    val computedPrice = MathHelper.computeUnitPrice(price.averagePrice, uos)
                    val label = when {
                        price.averagePrice == 0.0 -> stringResource(R.string.lbl_do_not_know)
                        price.exactPrice -> stringResource(R.string.lbl_exact_price)
                        else -> "${MathHelper.format(computedPrice)} ${price.currencySymbol}"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPriceId = price.id }
                    ) {
                        RadioButton(
                            selected = selectedPriceId == price.id,
                            onClick = { selectedPriceId = price.id }
                        )
                        Text(label, modifier = Modifier.weight(1f))
                    }
                    if (price.exactPrice && selectedPriceId == price.id) {
                        AkilimoTextField(
                            value = exactPriceInput,
                            onValueChange = { exactPriceInput = it },
                            label = stringResource(R.string.lbl_unit_price) +
                                    if (price.currencySymbol.isNotEmpty()) " (${price.currencySymbol})" else "",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val priceToSave: CassavaMarketPrice? = when {
                            isExactSelected -> {
                                val amount = exactPriceInput.toDoubleOrNull() ?: return@Button
                                selectedPriceItem.copy(averagePrice = amount)
                            }

                            selectedPriceItem != null && selectedPriceItem.averagePrice > 0.0 ->
                                selectedPriceItem

                            else -> return@Button
                        }
                        viewModel.saveSelectedPrice(unit, uos, priceToSave)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showPriceSheet = false
                        }
                    },
                    enabled = isConfirmEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }

                if (state.selectedUnitId == unit.id) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.saveSelectedPrice(unit, uos, null)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showPriceSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.lbl_remove))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
