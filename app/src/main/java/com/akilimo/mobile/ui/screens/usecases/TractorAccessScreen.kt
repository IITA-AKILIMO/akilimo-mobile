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
import com.akilimo.mobile.ui.components.compose.BinaryToggleChips
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.viewmodels.TractorAccessViewModel

@Composable
fun TractorAccessScreen(
    navController: NavHostController,
    viewModel: TractorAccessViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tractorAvailable by remember { mutableStateOf(false) }
    var usePlough by remember { mutableStateOf(false) }
    var useRidge by remember { mutableStateOf(false) }
    var useHarrow by remember { mutableStateOf(false) }
    var ploughCost by remember { mutableStateOf("") }
    var ridgeCost by remember { mutableStateOf("") }
    var harrowCost by remember { mutableStateOf("") }

    LaunchedEffect(state.userId) {
        if (state.userId != 0) {
            tractorAvailable = state.tractorAvailable
            usePlough = state.tractorPloughCost > 0.0
            useRidge = state.tractorRidgeCost > 0.0
            useHarrow = state.tractorHarrowCost > 0.0
            ploughCost = state.tractorPloughCost.takeIf { it > 0 }?.toString() ?: ""
            ridgeCost = state.tractorRidgeCost.takeIf { it > 0 }?.toString() ?: ""
            harrowCost = state.tractorHarrowCost.takeIf { it > 0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.TRACTOR_ACCESS)
            viewModel.onSaveHandled()
        }
    }

    val areaUnitLabel = state.enumAreaUnit.label(context)
    val farmSize = state.farmSize

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_tractor_access),
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tractor available Yes / No
            Text(
                text = stringResource(R.string.lbl_tractor_access),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            BinaryToggleChips(
                labelA = stringResource(R.string.lbl_yes),
                labelB = stringResource(R.string.lbl_no),
                selectedA = tractorAvailable,
                onSelectA = { tractorAvailable = true },
                onSelectB = {
                    tractorAvailable = false
                    usePlough = false; useRidge = false; useHarrow = false
                    ploughCost = ""; ridgeCost = ""; harrowCost = ""
                }
            )

            if (tractorAvailable) {
                Spacer(modifier = Modifier.height(12.dp))

                // Implement selector
                Text(
                    text = stringResource(R.string.lbl_tractor_access),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    FilterChip(
                        selected = usePlough,
                        onClick = {
                            usePlough = !usePlough
                            if (!usePlough) ploughCost = ""
                        },
                        label = { Text(stringResource(R.string.lbl_plough)) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = useRidge,
                        onClick = {
                            useRidge = !useRidge
                            if (!useRidge) ridgeCost = ""
                        },
                        label = { Text(stringResource(R.string.lbl_ridger)) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = useHarrow,
                        onClick = {
                            useHarrow = !useHarrow
                            if (!useHarrow) harrowCost = ""
                        },
                        label = { Text(stringResource(R.string.lbl_harrow)) }
                    )
                }

                if (usePlough) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AkilimoTextField(
                        value = ploughCost,
                        onValueChange = { ploughCost = it },
                        label = stringResource(R.string.lbl_tractor_plough_cost_hint)
                            .replace("{farm_size}", farmSize.toString())
                            .replace("{size_unit}", areaUnitLabel),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                if (useRidge) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AkilimoTextField(
                        value = ridgeCost,
                        onValueChange = { ridgeCost = it },
                        label = stringResource(R.string.lbl_tractor_ridge_cost_hint)
                            .replace("{farm_size}", farmSize.toString())
                            .replace("{size_unit}", areaUnitLabel),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                if (useHarrow) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveCosts(
                        tractorAvailable = tractorAvailable,
                        ploughingCost = if (usePlough) ploughCost.toDoubleOrNull() else 0.0,
                        ridingCost = if (useRidge) ridgeCost.toDoubleOrNull() else 0.0,
                        harrowingCost = if (useHarrow) harrowCost.toDoubleOrNull() else 0.0
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
