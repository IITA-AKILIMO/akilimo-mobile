package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.RadioButtonRow
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
import com.akilimo.mobile.ui.components.compose.SelectionCard
import com.akilimo.mobile.ui.components.compose.InfoCard
import com.akilimo.mobile.ui.components.compose.InfoCardType
import com.akilimo.mobile.ui.components.compose.SectionHeader
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.theme.AkilimoSpacing
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    fun openSheet(fertilizer: Fertilizer) {
        selectedFertilizer = fertilizer
        prices = emptyList()
        selectedPriceId = null
        exactPriceInput = ""
        showPriceSheet = true
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(titleRes),
                subtitle = stringResource(R.string.lbl_available_fertilizers_subtitle),
                collapsedTitle = stringResource(R.string.lbl_fertilizers),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { viewModel.toggleLayout() }) {
                        Icon(
                            painter = painterResource(
                                if (state.isGridLayout) R.drawable.ic_list else R.drawable.ic_grid
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = stringResource(R.string.lbl_finish),
                enabled = state.selectedIds.isNotEmpty(),
                onClick = { navController.completeTask(adviceTask) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SectionHeader(
                title = stringResource(R.string.lbl_select_fertilizers),
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md)
            )
            
            if (state.isGridLayout) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AkilimoSpacing.xs)
                ) {
                    items(state.fertilizers, key = { it.id ?: 0 }) { fertilizer ->
                        val isSelected = state.selectedIds.contains(fertilizer.id)
                        SelectionCard(
                            imageRes = R.drawable.ic_fertilizer_bag,
                            title = fertilizer.name.orEmpty(),
                            subtitle = if (isSelected && !fertilizer.displayPrice.isNullOrEmpty())
                                fertilizer.displayPrice
                            else
                                stringResource(R.string.under_score),
                            isSelected = isSelected,
                            isGridLayout = true,
                            onClick = {
                                if (isSelected) fertilizer.id?.let { viewModel.deselectFertilizer(it) }
                                else openSheet(fertilizer)
                            },
                            imageColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs)
                ) {
                    items(state.fertilizers, key = { it.id ?: 0 }) { fertilizer ->
                        val isSelected = state.selectedIds.contains(fertilizer.id)
                        SelectionCard(
                            imageRes = R.drawable.ic_fertilizer_bag,
                            title = fertilizer.name.orEmpty(),
                            subtitle = if (isSelected && !fertilizer.displayPrice.isNullOrEmpty())
                                fertilizer.displayPrice
                            else
                                stringResource(R.string.under_score),
                            isSelected = isSelected,
                            isGridLayout = false,
                            onClick = {
                                if (isSelected) fertilizer.id?.let { viewModel.deselectFertilizer(it) }
                                else openSheet(fertilizer)
                            },
                            imageColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(horizontal = AkilimoSpacing.xs)
                        )
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
                    .padding(horizontal = AkilimoSpacing.md)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = fert.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

                prices.forEach { price ->
                    val label = when {
                        price.pricePerBag == 0.0 -> stringResource(R.string.lbl_do_not_know)
                        price.pricePerBag < 0.0 -> stringResource(R.string.lbl_exact_price)
                        else -> price.priceRange
                    }
                    RadioButtonRow(
                        label = label,
                        selected = selectedPriceId == price.id,
                        onClick = { selectedPriceId = price.id }
                    )
                    if (price.pricePerBag < 0.0 && selectedPriceId == price.id) {
                        AkilimoTextField(
                            value = exactPriceInput,
                            onValueChange = { exactPriceInput = it },
                            label = stringResource(R.string.lbl_fertilizer_price),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = AkilimoSpacing.xxxl)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

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
                        .padding(vertical = AkilimoSpacing.xs),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }

                if (state.selectedIds.contains(fert.id)) {
                    OutlinedButton(
                        onClick = {
                            fert.id?.let { viewModel.deselectFertilizer(it) }
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                showPriceSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(stringResource(R.string.lbl_remove))
                    }
                }

                Spacer(modifier = Modifier.height(AkilimoSpacing.md))
            }
        }
    }
}

