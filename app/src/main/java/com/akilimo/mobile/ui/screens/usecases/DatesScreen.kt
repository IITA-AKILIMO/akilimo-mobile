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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.akilimo.mobile.ui.viewmodels.DatesViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatesScreen(
    navController: NavHostController,
    viewModel: DatesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var plantingDate by remember { mutableStateOf<LocalDate?>(null) }
    var harvestDate by remember { mutableStateOf<LocalDate?>(null) }
    var plantingFlex by remember { mutableStateOf(0L) }
    var harvestFlex by remember { mutableStateOf(0L) }
    var alternativeDate by remember { mutableStateOf(false) }
    var useFlexDates by remember { mutableStateOf(false) }

    var showPlantingDatePicker by remember { mutableStateOf(false) }
    var showHarvestDatePicker by remember { mutableStateOf(false) }

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

    if (showPlantingDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = plantingDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPlantingDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        plantingDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showPlantingDatePicker = false
                }) { Text(stringResource(R.string.lbl_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPlantingDatePicker = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHarvestDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = harvestDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showHarvestDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        harvestDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showHarvestDatePicker = false
                }) { Text(stringResource(R.string.lbl_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showHarvestDatePicker = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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

            OutlinedButton(
                onClick = { showPlantingDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (plantingDate != null)
                        stringResource(R.string.lbl_planting_date) + ": " + plantingDate!!.format(dateFormatter)
                    else
                        stringResource(R.string.lbl_pick_planting_date)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showHarvestDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (harvestDate != null)
                        stringResource(R.string.lbl_harvesting_date) + ": " + harvestDate!!.format(dateFormatter)
                    else
                        stringResource(R.string.lbl_pick_harvest_date)
                )
            }

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
                    onOptionSelected = { plantingFlex = it },
                    displayText = { months ->
                        if (months == 1L) "$months month" else "$months months"
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_harvest_flex),
                    options = flexOptions,
                    selectedOption = harvestFlex,
                    onOptionSelected = { harvestFlex = it },
                    displayText = { months ->
                        if (months == 1L) "$months month" else "$months months"
                    }
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
