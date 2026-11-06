package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.akilimo.mobile.ui.viewmodels.ProduceMarketViewModel
import com.akilimo.mobile.utils.AmountInput
import com.akilimo.mobile.ui.components.compose.popBackStackSafely

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

    var selectedUnit by rememberSaveable { mutableStateOf<EnumUnitOfSale?>(null) }
    var price by rememberSaveable { mutableStateOf("") }

    // Pre-populate from last entry
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

    val unitOptions = EnumUnitOfSale.entries.filter { it.isUniversal }

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
        }
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(8.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_unit_price),
                options = unitOptions,
                selectedOption = selectedUnit,
                onOptionSelected = { selectedUnit = it },
                displayText = { context.getString(it.labelRes) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AkilimoTextField(
                value = price,
                onValueChange = { price = it },
                label = if (state.currencyCode.isNotEmpty())
                    stringResource(R.string.lbl_unit_price) + " (${state.currencyCode})"
                else
                    stringResource(R.string.lbl_unit_price),
                error = stringResource(R.string.error_amount_invalid)
                    .takeIf { AmountInput.isInvalid(price, requirePositive = true) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val unit = selectedUnit ?: return@Button
                    val priceValue = AmountInput.parse(price)?.takeIf { it > 0.0 } ?: return@Button
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
                enabled = selectedUnit != null && (AmountInput.parse(price) ?: 0.0) > 0.0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
