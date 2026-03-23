package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.viewmodels.ManualTillageCostViewModel

@Composable
fun ManualTillageCostScreen(
    navController: NavHostController,
    viewModel: ManualTillageCostViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var usePlough by remember { mutableStateOf(true) }
    var useRidge by remember { mutableStateOf(true) }
    var ploughingCost by remember { mutableStateOf("") }
    var ridgingCost by remember { mutableStateOf("") }

    LaunchedEffect(state.userId) {
        if (state.userId != 0) {
            usePlough = state.performPloughing
            useRidge = state.performRidging
            ploughingCost = state.manualPloughCost?.takeIf { it > 0 }?.toString() ?: ""
            ridgingCost = state.manualRidgeCost?.takeIf { it > 0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.MANUAL_TILLAGE_COST)
            viewModel.onSaveHandled()
        }
    }

    val areaUnitLabel = state.enumAreaUnit.label(context)
    val farmSize = state.farmSize

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_cost_of_manual_tillage),
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.lbl_manual_tillage_cost)
                    .replace("{farm_size}", farmSize.toString())
                    .replace("{size_unit}", areaUnitLabel),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.lbl_plough_op_type),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                FilterChip(
                    selected = usePlough,
                    onClick = {
                        usePlough = !usePlough
                        if (!usePlough) ploughingCost = ""
                    },
                    label = { Text(stringResource(R.string.lbl_ploughing)) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = useRidge,
                    onClick = {
                        useRidge = !useRidge
                        if (!useRidge) ridgingCost = ""
                    },
                    label = { Text(stringResource(R.string.lbl_ridging)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (usePlough) {
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

            if (useRidge) {
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
                        ridingCost = if (useRidge) ridgingCost.toDoubleOrNull() else 0.0,
                        ploughingCost = if (usePlough) ploughingCost.toDoubleOrNull() else 0.0,
                        performPloughing = usePlough,
                        performRidging = useRidge
                    )
                },
                enabled = (usePlough && ploughingCost.isNotBlank()) ||
                        (useRidge && ridgingCost.isNotBlank()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
