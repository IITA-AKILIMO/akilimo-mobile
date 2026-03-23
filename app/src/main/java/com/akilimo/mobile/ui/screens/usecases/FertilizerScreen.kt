package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Checkbox
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
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.FertilizerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerScreen(
    fertilizerFlow: EnumFertilizerFlow,
    adviceTask: EnumAdviceTask,
    navController: NavHostController,
) {
    val viewModel = hiltViewModel<FertilizerViewModel, FertilizerViewModel.Factory> { factory ->
        factory.create(fertilizerFlow)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showPriceSheet by remember { mutableStateOf(false) }
    var selectedFertilizer by remember { mutableStateOf<Fertilizer?>(null) }
    var prices by remember { mutableStateOf<List<FertilizerPrice>>(emptyList()) }
    var selectedPriceId by remember { mutableStateOf<Int?>(null) }
    var exactPriceInput by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val titleRes = when (adviceTask) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM,
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS -> R.string.lbl_available_fertilizers
        else -> R.string.lbl_available_fertilizers
    }

    fun openSheet(fertilizer: Fertilizer) {
        selectedFertilizer = fertilizer
        prices = emptyList()
        selectedPriceId = null
        exactPriceInput = ""
        showPriceSheet = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
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
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "completed_task",
                                AdviceCompletionDto(adviceTask, EnumStepStatus.COMPLETED)
                            )
                        navController.popBackStack()
                    },
                    enabled = state.selectedIds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.lbl_finish))
                }
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(state.fertilizers, key = { it.id ?: 0 }) { fertilizer ->
                val isSelected = state.selectedIds.contains(fertilizer.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { openSheet(fertilizer) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked) openSheet(fertilizer)
                                else fertilizer.id?.let { viewModel.deselectFertilizer(it) }
                            }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = fertilizer.name.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isSelected && !fertilizer.displayPrice.isNullOrEmpty()) {
                                Text(
                                    text = fertilizer.displayPrice!!,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPriceSheet && selectedFertilizer != null) {
        val fert = selectedFertilizer!!

        LaunchedEffect(fert.id) {
            prices = viewModel.priceRepo.getByFertilizerKey(fert.key.orEmpty())
            val preSelected = viewModel.selectedRepo.getSelectedByFertilizer(fert.id ?: 0)
            selectedPriceId = preSelected?.fertilizerPriceId
        }

        val selectedPriceItem = prices.find { it.id == selectedPriceId }
        val isExactSelected = selectedPriceItem?.pricePerBag?.let { it < 0.0 } == true
        val isConfirmEnabled = selectedPriceItem != null &&
                (selectedPriceItem.pricePerBag > 0.0 ||
                        (isExactSelected && exactPriceInput.toDoubleOrNull()?.let { it > 0.0 } == true))

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
                    text = fert.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                prices.forEach { price ->
                    val label = when {
                        price.pricePerBag == 0.0 -> stringResource(R.string.lbl_do_not_know)
                        price.pricePerBag < 0.0 -> stringResource(R.string.lbl_exact_price)
                        else -> price.priceRange
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
                    if (price.pricePerBag < 0.0 && selectedPriceId == price.id) {
                        AkilimoTextField(
                            value = exactPriceInput,
                            onValueChange = { exactPriceInput = it },
                            label = stringResource(R.string.lbl_fertilizer_price),
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
                        val finalPrice: Double? = when {
                            isExactSelected -> exactPriceInput.toDoubleOrNull()
                            else -> selectedPriceItem?.pricePerBag
                        }
                        if (finalPrice != null && finalPrice > 0.0) {
                            val currencySymbol = selectedPriceItem?.currencySymbol.orEmpty()
                            val displayPrice: String = when {
                                isExactSelected -> "$exactPriceInput $currencySymbol"
                                else -> selectedPriceItem?.priceRange.orEmpty()
                            }
                            viewModel.selectFertilizer(
                                fertilizerId = fert.id ?: return@Button,
                                fertilizerPriceId = selectedPriceItem?.id,
                                price = finalPrice,
                                displayPrice = displayPrice,
                                isExactPrice = isExactSelected
                            )
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showPriceSheet = false
                            }
                        }
                    },
                    enabled = isConfirmEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }

                if (state.selectedIds.contains(fert.id)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            fert.id?.let { viewModel.deselectFertilizer(it) }
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
