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
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.WeedControlCostsViewModel

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
            firstCost = state.firstWeedingCost.takeIf { it > 0 }?.toString() ?: ""
            secondCost = state.secondWeedingCost.takeIf { it > 0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    "completed_task",
                    AdviceCompletionDto(EnumAdviceTask.COST_OF_WEED_CONTROL, EnumStepStatus.COMPLETED)
                )
            navController.popBackStack()
            viewModel.onSaveHandled()
        }
    }

    val weedMethods = EnumWeedControlMethod.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_cost_of_weed_control)) },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AkilimoTextField(
                value = secondCost,
                onValueChange = { secondCost = it },
                label = stringResource(R.string.lbl_second_weeding_cost_prompt),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveCosts(
                        firstCost = firstCost.toDoubleOrNull(),
                        secondCost = secondCost.toDoubleOrNull(),
                        method = selectedMethod
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
