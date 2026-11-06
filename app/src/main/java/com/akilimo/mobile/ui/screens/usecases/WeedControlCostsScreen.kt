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
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
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

    var selectedMethod by remember { mutableStateOf<EnumWeedControlMethod?>(null) }
    var firstCost by remember { mutableStateOf("") }
    var secondCost by remember { mutableStateOf("") }

    LaunchedEffect(state.userId) {
        if (state.userId != 0) {
            selectedMethod = state.weedControlMethod
            firstCost = state.firstWeedingCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
            secondCost = state.secondWeedingCost.takeIf { it > 0 }?.let(AmountInput::format) ?: ""
        }
    }

    // Both costs are optional; a blank field clears the cost, anything typed must parse
    val invalidAmount = stringResource(R.string.error_amount_invalid)
    val firstError = invalidAmount.takeIf { AmountInput.isInvalid(firstCost, requirePositive = false) }
    val secondError = invalidAmount.takeIf { AmountInput.isInvalid(secondCost, requirePositive = false) }
    val canSave = firstError == null && secondError == null

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.COST_OF_WEED_CONTROL)
            viewModel.onSaveHandled()
        }
    }

    val weedMethods = EnumWeedControlMethod.entries

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_cost_of_weed_control),
                collapsedTitle = stringResource(R.string.lbl_weeding),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStack() },
            )
        }
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(8.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_cost_of_weed_control),
                options = weedMethods,
                selectedOption = selectedMethod,
                onOptionSelected = { selectedMethod = it },
                displayText = { it.label(context) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AkilimoTextField(
                value = firstCost,
                onValueChange = { firstCost = it },
                label = stringResource(R.string.lbl_first_weeding_costs_prompt),
                error = firstError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AkilimoTextField(
                value = secondCost,
                onValueChange = { secondCost = it },
                label = stringResource(R.string.lbl_second_weeding_cost_prompt),
                error = secondError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveCosts(
                        // Blank -> 0.0 so clearing a field actually clears the saved cost
                        firstCost = AmountInput.parse(firstCost) ?: 0.0,
                        secondCost = AmountInput.parse(secondCost) ?: 0.0,
                        method = selectedMethod
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
