package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.ProduceMarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SweetPotatoMarketScreen(
    navController: NavHostController,
    viewModel: ProduceMarketViewModel = hiltViewModel<ProduceMarketViewModel, ProduceMarketViewModel.Factory> { factory ->
        factory.create(EnumMarketType.SWEET_POTATO_MARKET)
    }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedUnit by remember { mutableStateOf<EnumUnitOfSale?>(null) }
    var price by remember { mutableStateOf("") }

    // Pre-populate from last entry
    LaunchedEffect(state.lastEntry) {
        state.lastEntry?.let { entry ->
            selectedUnit = entry.unitOfSale
            price = entry.unitPrice.takeIf { it > 0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    "completed_task",
                    AdviceCompletionDto(EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET, EnumStepStatus.COMPLETED)
                )
            navController.popBackStack()
            viewModel.onSaveHandled()
        }
    }

    val unitOptions = EnumUnitOfSale.entries.filter { it.isUniversal }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_market_outlet_sweet_potato)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.lbl_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_unit_price),
                options = unitOptions,
                selectedOption = selectedUnit,
                onOptionSelected = { selectedUnit = it },
                displayText = { it.unitOfSale(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AkilimoTextField(
                value = price,
                onValueChange = { price = it },
                label = if (state.currencyCode.isNotEmpty())
                    stringResource(R.string.lbl_unit_price) + " (${state.currencyCode})"
                else
                    stringResource(R.string.lbl_unit_price),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val unit = selectedUnit ?: return@Button
                    val priceValue = price.toDoubleOrNull() ?: return@Button
                    viewModel.saveMarketEntry(
                        ProduceMarket(
                            userId = state.userId,
                            unitPrice = priceValue,
                            marketType = EnumMarketType.SWEET_POTATO_MARKET,
                            produceType = EnumProduceType.SWEET_POTATO_TUBERS,
                            unitOfSale = unit
                        )
                    )
                },
                enabled = selectedUnit != null && price.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
