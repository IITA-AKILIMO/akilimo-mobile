package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
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
import com.akilimo.mobile.ui.viewmodels.TractorAccessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TractorAccessScreen(
    navController: NavHostController,
    viewModel: TractorAccessViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var tractorAvailable by remember { mutableStateOf(false) }
    var ploughCost by remember { mutableStateOf("") }
    var ridgeCost by remember { mutableStateOf("") }
    var harrowCost by remember { mutableStateOf("") }

    LaunchedEffect(state.userId) {
        if (state.userId != 0) {
            tractorAvailable = state.tractorAvailable
            ploughCost = state.tractorPloughCost.takeIf { it > 0 }?.toString() ?: ""
            ridgeCost = state.tractorRidgeCost.takeIf { it > 0 }?.toString() ?: ""
            harrowCost = state.tractorHarrowCost.takeIf { it > 0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    "completed_task",
                    AdviceCompletionDto(EnumAdviceTask.TRACTOR_ACCESS, EnumStepStatus.COMPLETED)
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
                title = { Text(stringResource(R.string.lbl_tractor_access)) },
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
                text = stringResource(R.string.lbl_tractor_access),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                FilterChip(
                    selected = tractorAvailable,
                    onClick = { tractorAvailable = true },
                    label = { Text(stringResource(R.string.lbl_yes)) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = !tractorAvailable,
                    onClick = { tractorAvailable = false },
                    label = { Text(stringResource(R.string.lbl_no)) }
                )
            }

            if (tractorAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoTextField(
                    value = ploughCost,
                    onValueChange = { ploughCost = it },
                    label = stringResource(R.string.lbl_tractor_plough_cost_hint)
                        .replace("{farm_size}", farmSize.toString())
                        .replace("{size_unit}", areaUnitLabel),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoTextField(
                    value = ridgeCost,
                    onValueChange = { ridgeCost = it },
                    label = stringResource(R.string.lbl_tractor_ridge_cost_hint)
                        .replace("{farm_size}", farmSize.toString())
                        .replace("{size_unit}", areaUnitLabel),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoTextField(
                    value = harrowCost,
                    onValueChange = { harrowCost = it },
                    label = stringResource(R.string.lbl_tractor_harrow_cost_hint)
                        .replace("{farm_size}", farmSize.toString())
                        .replace("{size_unit}", areaUnitLabel),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveCosts(
                        tractorAvailable = tractorAvailable,
                        ridingCost = ridgeCost.toDoubleOrNull(),
                        ploughingCost = ploughCost.toDoubleOrNull(),
                        harrowingCost = harrowCost.toDoubleOrNull()
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
