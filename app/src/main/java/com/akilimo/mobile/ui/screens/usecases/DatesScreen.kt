package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.DateInputField
import com.akilimo.mobile.ui.viewmodels.DatesViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesScreen(
    navController: NavHostController,
    viewModel: DatesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var plantingDate by remember { mutableStateOf<LocalDate?>(null) }
    var harvestDate by remember { mutableStateOf<LocalDate?>(null) }
    var plantingFlex by remember { mutableStateOf(0L) }
    var harvestFlex by remember { mutableStateOf(0L) }
    var alternativeDate by remember { mutableStateOf(false) }
    var useFlexDates by remember { mutableStateOf(false) }

    var showPlantingDatePicker by remember { mutableStateOf(false) }
    var showHarvestDatePicker by remember { mutableStateOf(false) }

    // True when the user's planting date is already in the past
    val alreadyPlanted by remember(plantingDate) {
        derivedStateOf { plantingDate?.isBefore(LocalDate.now()) == true }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val flexOptions = listOf(0L, 1L, 2L)

    // Initialize local state from loaded state
    LaunchedEffect(state.user) {
        if (state.user != null) {
            plantingDate = state.plantingDate
            harvestDate = state.harvestDate
            plantingFlex = state.plantingFlex
            harvestFlex = state.harvestFlex
            alternativeDate = state.alternativeDate
            useFlexDates = (state.plantingFlex > 0L || state.harvestFlex > 0L)
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    "completed_task",
                    AdviceCompletionDto(EnumAdviceTask.PLANTING_AND_HARVEST, EnumStepStatus.COMPLETED)
                )
            navController.popBackStack()
            viewModel.onSaveHandled()
        }
    }

    // Planting date: [now − (4+flex), now + (12+flex)]
    if (showPlantingDatePicker) {
        val flex = if (useFlexDates) plantingFlex else 0L
        val pMin = LocalDate.now().minusMonths(4 + flex)
        val pMax = LocalDate.now().plusMonths(12 + flex)
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

    // Harvest date: [planting + (8−flex), planting + (16+flex)]
    if (showHarvestDatePicker) {
        val pDate = plantingDate!! // only shown when plantingDate != null
        val flex = if (useFlexDates) harvestFlex else 0L
        val hMin = pDate.plusMonths(8L - flex)
        val hMax = pDate.plusMonths(16L + flex)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_consider_alternative_planting)) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.lbl_consider_alternative_planting),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = alternativeDate,
                    onCheckedChange = { alternativeDate = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DateInputField(
                value = plantingDate?.format(dateFormatter) ?: "",
                label = stringResource(R.string.lbl_planting_date),
                placeholder = stringResource(R.string.lbl_pick_planting_date),
                onClick = { showPlantingDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.lbl_flexible_dates),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = useFlexDates,
                    onCheckedChange = { useFlexDates = it }
                )
            }

            if (useFlexDates) {
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_planting_flex),
                    options = flexOptions,
                    selectedOption = plantingFlex,
                    onOptionSelected = { plantingFlex = it; harvestDate = null },
                    displayText = { months -> if (months == 1L) "$months month" else "$months months" },
                    enabled = !alreadyPlanted,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_harvest_flex),
                    options = flexOptions,
                    selectedOption = harvestFlex,
                    onOptionSelected = { harvestFlex = it; harvestDate = null },
                    displayText = { months -> if (months == 1L) "$months month" else "$months months" },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val pDate = plantingDate ?: return@Button
                    val hDate = harvestDate ?: return@Button
                    viewModel.saveSchedule(
                        plantingDate = pDate,
                        harvestDate = hDate,
                        plantingFlex = if (useFlexDates) plantingFlex else 0L,
                        harvestFlex = if (useFlexDates) harvestFlex else 0L,
                        alternativeDate = alternativeDate
                    )
                },
                enabled = plantingDate != null && harvestDate != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.lbl_save))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
