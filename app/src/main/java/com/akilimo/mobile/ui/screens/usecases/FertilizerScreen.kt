package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import com.akilimo.mobile.ui.components.compose.SegmentedTabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import com.akilimo.mobile.ui.components.compose.SectionHeader
import com.akilimo.mobile.ui.components.compose.StatItem
import com.akilimo.mobile.ui.components.compose.StatSummaryCard
import com.akilimo.mobile.ui.components.compose.StatusTone
import com.akilimo.mobile.ui.components.compose.SelectionCard
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.FertilizerViewModel
import com.akilimo.mobile.utils.AmountInput
import kotlinx.coroutines.launch

/** The list needs more than this many fertilizers before the search box and category bar appear. */
private const val FILTER_MIN_ITEMS = 10

/**
 * Fertilizer picker. Tapping any card opens the price sheet (pre-filled when the fertilizer
 * is already chosen), where the price can be changed or the fertilizer removed. Choices are
 * written when confirmed in the sheet; Save marks the step complete.
 */
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
    val snackbarHostState = remember { SnackbarHostState() }

    // The open sheet is remembered by fertilizer id so it survives rotation
    var sheetFertilizerId by rememberSaveable { mutableStateOf<Int?>(null) }

    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Search and the category bar only earn their space on a long list; a short one stays plain
    val showFilters = state.fertilizers.size > FILTER_MIN_ITEMS
    val shownFertilizers = if (showFilters) state.visibleFertilizers else state.fertilizers

    @Composable
    fun FertilizerCard(fertilizer: Fertilizer, grid: Boolean, modifier: Modifier = Modifier) {
        val isSelected = state.selectedIds.contains(fertilizer.id)
        SelectionCard(
            imageRes = R.drawable.ic_fertilizer_bag,
            title = fertilizer.name.orEmpty(),
            subtitle = if (isSelected && !fertilizer.displayPrice.isNullOrEmpty())
                fertilizer.displayPrice
            else
                stringResource(R.string.under_score),
            description = categoryLabelRes(fertilizer.category)?.let { stringResource(it) },
            isSelected = isSelected,
            isGridLayout = grid,
            onClick = { sheetFertilizerId = fertilizer.id },
            imageColorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = modifier,
            // Several fertilizers can be selected at once
            role = Role.Checkbox
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_available_fertilizers),
                subtitle = stringResource(R.string.lbl_available_fertilizers_subtitle),
                collapsedTitle = stringResource(R.string.lbl_fertilizers),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStackSafely() },
                actions = {
                    LayoutToggleButton(isGridLayout = state.isGridLayout, onToggle = viewModel::toggleLayout)
                }
            )
        },
        bottomBar = {
            FormSaveBar(
                enabled = state.selectedIds.isNotEmpty(),
                isSaving = state.isSaving,
                disabledReason = stringResource(R.string.msg_select_fertilizer)
                    .takeIf { state.fertilizers.isNotEmpty() },
                onSave = { navController.completeTask(adviceTask) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (state.fertilizers.isNotEmpty()) {
                StatSummaryCard(
                    stats = listOf(
                        StatItem(stringResource(R.string.lbl_available), state.fertilizers.size.toString()),
                        StatItem(
                            stringResource(R.string.lbl_selected),
                            state.selectedIds.size.toString(),
                            if (state.selectedIds.isEmpty()) StatusTone.Neutral else StatusTone.Success,
                        ),
                    ),
                    modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
                )
            }
            SectionHeader(
                title = stringResource(R.string.lbl_select_fertilizers),
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md)
            )

            if (showFilters) {
                FertilizerFilters(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    categories = state.categories,
                    selectedCategory = state.category,
                    onCategoryChange = viewModel::onCategoryChange,
                    modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
                )
            }

            RefreshableList(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh) {
                when {
                    state.isLoading -> PullableState { ListLoading() }
                    state.fertilizers.isEmpty() -> PullableState {
                        ListEmpty(
                            message = stringResource(R.string.msg_no_fertilizers),
                            modifier = Modifier.padding(horizontal = AkilimoSpacing.md),
                        )
                    }

                    shownFertilizers.isEmpty() -> PullableState {
                        ListEmpty(
                            message = stringResource(R.string.msg_no_matching_fertilizers),
                            modifier = Modifier.padding(horizontal = AkilimoSpacing.md),
                        )
                    }

                    state.isGridLayout -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AkilimoSpacing.xs)
                    ) {
                        items(shownFertilizers, key = { it.id ?: 0 }) { fertilizer ->
                            FertilizerCard(fertilizer, grid = true)
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs)
                    ) {
                        items(shownFertilizers, key = { it.id ?: 0 }) { fertilizer ->
                            FertilizerCard(
                                fertilizer,
                                grid = false,
                                modifier = Modifier.padding(horizontal = AkilimoSpacing.xs)
                            )
                        }
                    }
                }
            }
        }
    }

    val sheetFertilizer = state.fertilizers.find { it.id == sheetFertilizerId }
    if (sheetFertilizer != null) {
        FertilizerPriceSheet(
            fertilizer = sheetFertilizer,
            isSelected = state.selectedIds.contains(sheetFertilizer.id),
            isSaving = state.isSaving,
            loadOptions = { viewModel.loadPriceOptions(sheetFertilizer) },
            onConfirm = { priceId, price, displayPrice, isExact ->
                sheetFertilizer.id?.let { id ->
                    viewModel.selectFertilizer(
                        fertilizerId = id,
                        fertilizerPriceId = priceId,
                        price = price,
                        displayPrice = displayPrice,
                        isExactPrice = isExact
                    )
                }
            },
            onRemove = { sheetFertilizer.id?.let { viewModel.deselectFertilizer(it) } },
            onDismiss = { sheetFertilizerId = null },
        )
    }
}

/** Label for a fertilizer category, or null when the API sent none we know. */
private fun categoryLabelRes(category: String?): Int? = when (category) {
    "STRAIGHT" -> R.string.lbl_category_straight
    "COMPOUND" -> R.string.lbl_category_compound
    "OTHER" -> R.string.lbl_category_other
    else -> null
}

/** Search box and category bar above the fertilizer list. */
@Composable
private fun FertilizerFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
        AkilimoTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "",
            accessibilityLabel = stringResource(R.string.lbl_search_fertilizers),
            placeholder = stringResource(R.string.lbl_search_fertilizers),
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        )
        // A bar with only "All" would be pointless: show it once the list has real categories
        if (categories.isNotEmpty()) {
            SegmentedTabRow(
                options = listOf(stringResource(R.string.lbl_all)) +
                    categories.map { stringResource(categoryLabelRes(it) ?: R.string.lbl_category_other) },
                selectedIndex = selectedCategory?.let { categories.indexOf(it) + 1 } ?: 0,
                onSelect = { index -> onCategoryChange(if (index == 0) null else categories[index - 1]) },
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FertilizerPriceSheet(
    fertilizer: Fertilizer,
    isSelected: Boolean,
    isSaving: Boolean,
    loadOptions: suspend () -> FertilizerViewModel.PriceOptions,
    onConfirm: (priceId: Int?, price: Double, displayPrice: String, isExact: Boolean) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val key = fertilizer.id ?: 0

    var prices by remember(key) { mutableStateOf<List<FertilizerPrice>?>(null) }
    var selectedPriceId by rememberSaveable(key) { mutableStateOf<Int?>(null) }
    var exactPriceInput by rememberSaveable(key) { mutableStateOf("") }
    // Pre-fill from the saved answer once; after rotation keep the user's pick
    var prefilled by rememberSaveable(key) { mutableStateOf(false) }

    LaunchedEffect(key) {
        val options = loadOptions()
        prices = options.prices
        if (!prefilled) {
            prefilled = true
            selectedPriceId = options.savedPriceId
            exactPriceInput = options.savedExactPrice?.let(AmountInput::format) ?: ""
        }
    }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    val loaded = prices
    val selectedPriceItem = loaded?.find { it.id == selectedPriceId }
    val isExactSelected = selectedPriceItem?.pricePerBag?.let { it < 0.0 } == true
    // pricePerBag == 0.0 is the "I don't know" option: a valid choice, sent as price 0
    val isDoNotKnowSelected = selectedPriceItem?.pricePerBag == 0.0
    val exactPriceValue = AmountInput.parse(exactPriceInput)?.takeIf { it > 0.0 }
    val isConfirmEnabled = !isSaving && selectedPriceItem != null &&
            (selectedPriceItem.pricePerBag > 0.0 || isDoNotKnowSelected ||
                    (isExactSelected && exactPriceValue != null))
    val doNotKnowLabel = stringResource(R.string.lbl_do_not_know)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AkilimoSpacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = fertilizer.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            when {
                loaded == null -> ListLoading()
                loaded.isEmpty() -> ListEmpty(stringResource(R.string.msg_no_prices_available))
                else -> Column(modifier = Modifier.selectableGroup()) {
                    loaded.forEach { price ->
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
                                error = stringResource(R.string.error_amount_invalid)
                                    .takeIf { AmountInput.isInvalid(exactPriceInput, requirePositive = true) },
                                trailingIcon = currencySuffix(price.currencySymbol),
                                keyboardOptions = amountKeyboard(isLast = true),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = AkilimoSpacing.xxxl)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            Button(
                onClick = {
                    val finalPrice: Double? = when {
                        isExactSelected -> exactPriceValue
                        isDoNotKnowSelected -> 0.0
                        else -> selectedPriceItem?.pricePerBag?.takeIf { it > 0.0 }
                    }
                    if (finalPrice != null) {
                        val currencySymbol = selectedPriceItem?.currencySymbol.orEmpty()
                        val displayPrice: String = when {
                            isExactSelected -> "${AmountInput.format(finalPrice)} $currencySymbol"
                            isDoNotKnowSelected -> doNotKnowLabel
                            else -> selectedPriceItem?.priceRange.orEmpty()
                        }
                        onConfirm(selectedPriceItem?.id, finalPrice, displayPrice, isExactSelected)
                        close()
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

            if (isSelected) {
                OutlinedButton(
                    onClick = {
                        onRemove()
                        close()
                    },
                    enabled = !isSaving,
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
