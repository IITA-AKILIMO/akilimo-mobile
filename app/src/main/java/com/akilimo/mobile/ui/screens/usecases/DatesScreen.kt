package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.DateInputField
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.SwitchRow
import com.akilimo.mobile.ui.components.compose.completeTask
import com.akilimo.mobile.ui.viewmodels.DatesViewModel
import com.akilimo.mobile.utils.PlantingSchedule
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.akilimo.mobile.ui.components.compose.popBackStackSafely

private const val PLANTING_MONTHS_BEFORE = 4L
private const val PLANTING_MONTHS_AHEAD = 12L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesScreen(
    navController: NavHostController,
    viewModel: DatesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var plantingDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var harvestDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var plantingFlex by rememberSaveable { mutableStateOf(0L) }
    var harvestFlex by rememberSaveable { mutableStateOf(0L) }
    var alternativeDate by rememberSaveable { mutableStateOf(false) }
    var useFlexDates by rememberSaveable { mutableStateOf(false) }

    var showPlantingDatePicker by rememberSaveable { mutableStateOf(false) }
    var showHarvestDatePicker by rememberSaveable { mutableStateOf(false) }

    // True when the user's planting date is already in the past
    val alreadyPlanted by remember(plantingDate) {
        derivedStateOf { plantingDate?.isBefore(LocalDate.now()) == true }
    }

    val locale = currentLocale()
    val dateFormatter = remember(locale) { DateTimeFormatter.ofPattern("dd MMM yyyy", locale) }
    val flexOptions = listOf(0L, 1L, 2L)
    val noFlexLabel = stringResource(R.string.lbl_no_flexibility)
    val oneMonthLabel = stringResource(R.string.lbl_one_month_window)
    val twoMonthLabel = stringResource(R.string.lbl_two_month_window)
    fun flexLabel(months: Long) = when (months) {
        0L -> noFlexLabel
        1L -> oneMonthLabel
        else -> twoMonthLabel
    }

    // Initialize local state from loaded state
    // Prefill once from the DB; after rotation keep the user's unsaved edits
    var prefilled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.user) {
        if (state.user != null && !prefilled) {
            prefilled = true
            plantingDate = state.plantingDate
            // A date saved under the older, wider range may be refused by the engine: pick it again
            harvestDate = state.harvestDate?.takeIf { h -> state.plantingDate?.let { PlantingSchedule.isValid(it, h) } == true }
            plantingFlex = state.plantingFlex
            harvestFlex = state.harvestFlex
            alternativeDate = state.alternativeDate
            useFlexDates = (state.plantingFlex > 0L || state.harvestFlex > 0L)
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.completeTask(EnumAdviceTask.PLANTING_AND_HARVEST)
            viewModel.onSaveHandled()
        }
    }
    SaveErrorEffect(state.saveFailed, snackbarHostState, viewModel::onSaveErrorShown)

    // Planting date: [now − (4+flex), now + (12+flex)]
    if (showPlantingDatePicker) {
        val flex = if (useFlexDates) plantingFlex else 0L
        val pMin = LocalDate.now().minusMonths(PLANTING_MONTHS_BEFORE + flex)
        val pMax = LocalDate.now().plusMonths(PLANTING_MONTHS_AHEAD + flex)
        val plantingSelectableDates = remember(pMin, pMax) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val d = Instant.ofEpochMilli(utcTimeMillis).atOffset(ZoneOffset.UTC).toLocalDate()
                    return !d.isBefore(pMin) && !d.isAfter(pMax)
                }
                override fun isSelectableYear(year: Int) = year in pMin.year..pMax.year
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = plantingDate
                ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = plantingSelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showPlantingDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atOffset(ZoneOffset.UTC).toLocalDate()
                        val isNowPast = selected.isBefore(LocalDate.now())
                        if (isNowPast) plantingFlex = 0L   // can't have flex on a past date
                        harvestDate = null                   // harvest window shifted; force re-pick
                        plantingDate = selected
                    }
                    showPlantingDatePicker = false
                }) { Text(stringResource(R.string.lbl_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPlantingDatePicker = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            }
        ) { DatePicker(state = datePickerState, modifier = Modifier.weight(1f, fill = false)) }
    }

    // Harvest date: 235 to 452 days after planting, what the engine accepts (flexibility does not widen it)
    if (showHarvestDatePicker) {
        val pDate = plantingDate!! // only shown when plantingDate != null
        val hMin = PlantingSchedule.earliestHarvest(pDate)
        val hMax = PlantingSchedule.latestHarvest(pDate)
        val harvestSelectableDates = remember(hMin, hMax) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val d = Instant.ofEpochMilli(utcTimeMillis).atOffset(ZoneOffset.UTC).toLocalDate()
                    return !d.isBefore(hMin) && !d.isAfter(hMax)
                }
                override fun isSelectableYear(year: Int) = year in hMin.year..hMax.year
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (harvestDate ?: hMin)
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = harvestSelectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showHarvestDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        harvestDate = Instant.ofEpochMilli(millis)
                            .atOffset(ZoneOffset.UTC).toLocalDate()
                    }
                    showHarvestDatePicker = false
                }) { Text(stringResource(R.string.lbl_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showHarvestDatePicker = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            }
        ) { DatePicker(state = datePickerState, modifier = Modifier.weight(1f, fill = false)) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_consider_alternative_planting),
                collapsedTitle = stringResource(R.string.lbl_dates),
                scrollBehavior = scrollBehavior,
                onBack = { navController.popBackStackSafely() },
            )
        },
        bottomBar = {
            FormSaveBar(
                enabled = plantingDate != null && harvestDate != null,
                isSaving = state.isSaving,
                disabledReason = stringResource(
                    if (plantingDate == null) R.string.error_planting_date_required
                    else R.string.error_harvest_date_required
                ),
                onSave = {
                    val pDate = plantingDate ?: return@FormSaveBar
                    val hDate = harvestDate ?: return@FormSaveBar
                    viewModel.saveSchedule(
                        plantingDate = pDate,
                        harvestDate = hDate,
                        plantingFlex = if (useFlexDates) plantingFlex else 0L,
                        harvestFlex = if (useFlexDates) harvestFlex else 0L,
                        alternativeDate = alternativeDate
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            SwitchRow(
                label = stringResource(R.string.lbl_open_to_alternative_dates),
                checked = alternativeDate,
                onCheckedChange = { alternativeDate = it }
            )

            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            DateInputField(
                value = plantingDate?.format(dateFormatter) ?: "",
                label = stringResource(R.string.lbl_planting_date),
                placeholder = stringResource(R.string.lbl_pick_planting_date),
                onClick = { showPlantingDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            val noPlantingMsg = stringResource(R.string.lbl_planting_date_prompt)
            DateInputField(
                value = harvestDate?.format(dateFormatter) ?: "",
                label = stringResource(R.string.lbl_harvesting_date),
                placeholder = stringResource(R.string.lbl_pick_harvest_date),
                onClick = {
                    if (plantingDate == null) {
                        scope.launch { snackbarHostState.showSnackbar(noPlantingMsg) }
                    } else {
                        showHarvestDatePicker = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(AkilimoSpacing.xs))

            SwitchRow(
                label = stringResource(R.string.lbl_flexible_dates),
                checked = useFlexDates,
                onCheckedChange = { useFlexDates = it }
            )

            if (useFlexDates) {
                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_planting_flex),
                    options = flexOptions,
                    selectedOption = plantingFlex,
                    onOptionSelected = { plantingFlex = it; harvestDate = null },
                    displayText = { months -> flexLabel(months) },
                    enabled = !alreadyPlanted,
                )
                Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_harvest_flex),
                    options = flexOptions,
                    selectedOption = harvestFlex,
                    onOptionSelected = { harvestFlex = it; harvestDate = null },
                    displayText = { months -> flexLabel(months) },
                )
            }

            Spacer(modifier = Modifier.height(AkilimoSpacing.md))
        }
    }
}
