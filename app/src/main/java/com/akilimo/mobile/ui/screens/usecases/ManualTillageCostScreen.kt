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
import androidx.compose.material3.MaterialTheme
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
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.ManualTillageCostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualTillageCostScreen(
    navController: NavHostController,
    viewModel: ManualTillageCostViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var ploughingCost by remember { mutableStateOf("") }
    var ridgingCost by remember { mutableStateOf("") }

    LaunchedEffect(state.userId) {
        if (state.userId != 0) {
            ploughingCost = state.manualPloughCost?.takeIf { it > 0 }?.toString() ?: ""
            ridgingCost = state.manualRidgeCost?.takeIf { it > 0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    "completed_task",
                    AdviceCompletionDto(EnumAdviceTask.MANUAL_TILLAGE_COST, EnumStepStatus.COMPLETED)
                )
            navController.popBackStack()
            viewModel.onSaveHandled()
        }
    }

    val areaUnitLabel = state.enumAreaUnit.name.lowercase()
    val farmSize = state.farmSize

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_cost_of_manual_tillage)) },
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

            Text(
                text = stringResource(R.string.lbl_manual_tillage_cost)
                    .replace("{farm_size}", farmSize.toString())
                    .replace("{size_unit}", areaUnitLabel),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.performPloughing) {
                AkilimoTextField(
                    value = ploughingCost,
                    onValueChange = { ploughingCost = it },
                    label = stringResource(R.string.lbl_manual_tillage_cost_hint)
                        .replace("{farm_size}", farmSize.toString())
                        .replace("{size_unit}", areaUnitLabel),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.performRidging) {
                AkilimoTextField(
                    value = ridgingCost,
                    onValueChange = { ridgingCost = it },
                    label = stringResource(R.string.lbl_manual_ridge_cost_hint)
                        .replace("{farm_size}", farmSize.toString())
                        .replace("{size_unit}", areaUnitLabel),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveCosts(
                        ridingCost = ridgingCost.toDoubleOrNull(),
                        ploughingCost = ploughingCost.toDoubleOrNull()
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
