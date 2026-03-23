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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.CassavaMarketViewModel
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
    var customPrice by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Apply initial market choice once
    LaunchedEffect(state.initialMarketChoice) {
        if (marketChoice == CassavaMarketViewModel.MarketChoice.NONE &&
            state.initialMarketChoice != CassavaMarketViewModel.MarketChoice.NONE
        ) {
            marketChoice = state.initialMarketChoice
        }
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
                                AdviceCompletionDto(EnumAdviceTask.CASSAVA_MARKET_OUTLET, EnumStepStatus.COMPLETED)
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

            // Content based on choice
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
                                    .clickable {
                                        selectedUnit = unit
                                        customPrice = ""
                                        showPriceSheet = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.selectedUnitId == unit.id)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = unit.label,
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
        ModalBottomSheet(
            onDismissRequest = { showPriceSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = selectedUnit!!.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoTextField(
                    value = customPrice,
                    onValueChange = { customPrice = it },
                    label = stringResource(R.string.lbl_unit_price),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val unit = selectedUnit ?: return@Button
                        val uos = EnumUnitOfSale.entries.find {
                            it.name.equals(unit.label, ignoreCase = true)
                        } ?: EnumUnitOfSale.ONE_KG
                        viewModel.saveSelectedPrice(unit, uos, null)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showPriceSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
