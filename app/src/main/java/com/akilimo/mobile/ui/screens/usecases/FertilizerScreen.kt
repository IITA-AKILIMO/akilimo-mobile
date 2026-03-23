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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
    var priceInput by remember { mutableStateOf("") }
    var isExactPrice by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val titleRes = when (adviceTask) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM,
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS -> R.string.lbl_available_fertilizers
        else -> R.string.lbl_available_fertilizers
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
                        .clickable {
                            if (isSelected) {
                                fertilizer.id?.let { viewModel.deselectFertilizer(it) }
                            } else {
                                selectedFertilizer = fertilizer
                                priceInput = fertilizer.selectedPrice
                                    .takeIf { it > 0 }
                                    ?.toString() ?: ""
                                isExactPrice = false
                                showPriceSheet = true
                            }
                        },
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
                                if (checked) {
                                    selectedFertilizer = fertilizer
                                    priceInput = fertilizer.selectedPrice
                                        .takeIf { it > 0 }
                                        ?.toString() ?: ""
                                    isExactPrice = false
                                    showPriceSheet = true
                                } else {
                                    fertilizer.id?.let { viewModel.deselectFertilizer(it) }
                                }
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
                    text = selectedFertilizer!!.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = stringResource(R.string.lbl_fertilizer_price),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.lbl_exact_price),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isExactPrice,
                        onCheckedChange = { isExactPrice = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val fert = selectedFertilizer ?: return@Button
                        val price = priceInput.toDoubleOrNull()
                        val displayPrice = if (price != null) priceInput else ""
                        viewModel.selectFertilizer(
                            fertilizerId = fert.id ?: return@Button,
                            fertilizerPriceId = null,
                            price = price,
                            displayPrice = displayPrice,
                            isExactPrice = isExactPrice
                        )
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
