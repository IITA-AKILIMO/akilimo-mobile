package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

private val ScreenPadding = 16.dp
private val ItemSpacing = 12.dp
private val CardMinHeight = 148.dp

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
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleLayout() }) {
                        Icon(
                            imageVector = if (state.isGridLayout) {
                                Icons.Filled.ViewAgenda
                            } else {
                                Icons.Filled.ViewModule
                            },
                            contentDescription = if (state.isGridLayout) {
                                stringResource(R.string.lbl_list)
                            } else {
                                stringResource(R.string.lbl_grid)
                            }
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
                        .padding(ScreenPadding)
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.lbl_finish))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenPadding)
        ) {
            FertilizerSelectionHeader(
                selectedCount = state.selectedIds.size,
                isGridLayout = state.isGridLayout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ScreenPadding, bottom = ItemSpacing)
            )

            if (state.isGridLayout) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(ItemSpacing),
                    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.fertilizers, key = { it.id ?: 0 }) { fertilizer ->
                        FertilizerCard(
                            fertilizer = fertilizer,
                            isSelected = state.selectedIds.contains(fertilizer.id),
                            isGridLayout = true,
                            onClick = { openSheet(fertilizer) },
                            onDeselect = { fertilizer.id?.let { viewModel.deselectFertilizer(it) } }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.fertilizers, key = { it.id ?: 0 }) { fertilizer ->
                        FertilizerCard(
                            fertilizer = fertilizer,
                            isSelected = state.selectedIds.contains(fertilizer.id),
                            isGridLayout = false,
                            onClick = { openSheet(fertilizer) },
                            onDeselect = { fertilizer.id?.let { viewModel.deselectFertilizer(it) } }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(ScreenPadding))
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
            (
                selectedPriceItem.pricePerBag > 0.0 ||
                    (isExactSelected && exactPriceInput.toDoubleOrNull()?.let { it > 0.0 } == true)
                )

        ModalBottomSheet(
            onDismissRequest = { showPriceSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = fert.name.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.lbl_select_price),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(ScreenPadding))

                prices.forEach { price ->
                    val label = when {
                        price.pricePerBag == 0.0 -> stringResource(R.string.lbl_do_not_know)
                        price.pricePerBag < 0.0 -> stringResource(R.string.lbl_exact_price)
                        else -> price.priceRange
                    }
                    FertilizerPriceOption(
                        label = label,
                        isSelected = selectedPriceId == price.id,
                        showInput = price.pricePerBag < 0.0 && selectedPriceId == price.id,
                        exactPriceInput = exactPriceInput,
                        currencySymbol = price.currencySymbol.orEmpty(),
                        onSelect = { selectedPriceId = price.id },
                        onExactPriceChange = { exactPriceInput = it }
                    )
                }

                Spacer(modifier = Modifier.height(ItemSpacing))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }

                if (state.selectedIds.contains(fert.id)) {
                    Spacer(modifier = Modifier.height(ItemSpacing))
                    OutlinedButton(
                        onClick = {
                            fert.id?.let { viewModel.deselectFertilizer(it) }
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showPriceSheet = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.lbl_remove))
                    }
                }

                Spacer(modifier = Modifier.height(ScreenPadding))
            }
        }
    }
}

@Composable
private fun FertilizerSelectionHeader(
    selectedCount: Int,
    isGridLayout: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.lbl_choose_fertilizer),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = if (selectedCount > 0) {
                    "$selectedCount ${stringResource(R.string.lbl_selected)}"
                } else {
                    stringResource(R.string.lbl_tap_to_select_fertilizer)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = if (isGridLayout) {
                    stringResource(R.string.lbl_grid_layout_hint)
                } else {
                    stringResource(R.string.lbl_list_layout_hint)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun FertilizerPriceOption(
    label: String,
    isSelected: Boolean,
    showInput: Boolean,
    exactPriceInput: String,
    currencySymbol: String,
    onSelect: () -> Unit,
    onExactPriceChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }

            if (showInput) {
                AkilimoTextField(
                    value = exactPriceInput,
                    onValueChange = onExactPriceChange,
                    label = stringResource(R.string.lbl_fertilizer_price) +
                        if (currencySymbol.isNotEmpty()) " ($currencySymbol)" else "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 8.dp, bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FertilizerCard(
    fertilizer: Fertilizer,
    isSelected: Boolean,
    isGridLayout: Boolean,
    onClick: () -> Unit,
    onDeselect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (isSelected) onDeselect else onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        if (isGridLayout) {
            GridFertilizerCardContent(
                fertilizer = fertilizer,
                isSelected = isSelected
            )
        } else {
            ListFertilizerCardContent(
                fertilizer = fertilizer,
                isSelected = isSelected
            )
        }
    }
}

@Composable
private fun GridFertilizerCardContent(
    fertilizer: Fertilizer,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(CardMinHeight)
            .padding(12.dp)
    ) {
        FertilizerIconBadge(isSelected = isSelected)
        Text(
            text = fertilizer.name.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (isSelected && !fertilizer.displayPrice.isNullOrEmpty()) {
                fertilizer.displayPrice!!
            } else {
                stringResource(R.string.lbl_tap_to_add_price)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
        if (isSelected) {
            SelectedChip()
        }
    }
}

@Composable
private fun ListFertilizerCardContent(
    fertilizer: Fertilizer,
    isSelected: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        FertilizerIconBadge(isSelected = isSelected)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = fertilizer.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = if (isSelected && !fertilizer.displayPrice.isNullOrEmpty()) {
                    fertilizer.displayPrice!!
                } else {
                    stringResource(R.string.lbl_tap_to_select_and_set_price)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.lbl_selected),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FertilizerIconBadge(isSelected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
            .padding(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_fertilizer_bag),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ),
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun SelectedChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.lbl_selected),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.widthIn(min = 48.dp)
        )
    }
}
