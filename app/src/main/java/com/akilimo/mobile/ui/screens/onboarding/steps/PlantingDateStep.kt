package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.utils.DateHelper
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantingDateStep(
    plantingDate: LocalDate?,
    harvestDate: LocalDate?,
    plantingFlex: Long,
    harvestFlex: Long,
    showFlexOptions: Boolean,
    errors: Map<String, String>,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val flexOptions = remember {
        listOf(
            context.getString(R.string.lbl_no_flexibility) to 0L,
            context.getString(R.string.lbl_one_month_window) to 1L,
            context.getString(R.string.lbl_two_month_window) to 2L,
        )
    }

    var showPlantingPicker by remember { mutableStateOf(false) }
    var showHarvestPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.lbl_planting_harvest_dates), style = MaterialTheme.typography.headlineMedium)

        AkilimoTextField(
            value = DateHelper.formatToString(plantingDate),
            onValueChange = {},
            label = stringResource(R.string.lbl_planting_date),
            readOnly = true,
            error = errors["plantingDate"],
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { showPlantingPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.lbl_pick_planting_date)) }

        if (showPlantingPicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = plantingDate
                    ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showPlantingPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            onEvent(OnboardingViewModel.Event.PlantingDateSelected(date))
                        }
                        showPlantingPicker = false
                    }) { Text(stringResource(R.string.lbl_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showPlantingPicker = false }) {
                        Text(stringResource(R.string.lbl_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        AkilimoTextField(
            value = DateHelper.formatToString(harvestDate),
            onValueChange = {},
            label = stringResource(R.string.lbl_harvesting_date),
            readOnly = true,
            error = errors["harvestDate"],
        )
        Button(
            onClick = { showHarvestPicker = true },
            enabled = plantingDate != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.lbl_pick_harvest_date)) }

        if (showHarvestPicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = harvestDate
                    ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showHarvestPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            onEvent(OnboardingViewModel.Event.HarvestDateSelected(date))
                        }
                        showHarvestPicker = false
                    }) { Text(stringResource(R.string.lbl_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showHarvestPicker = false }) {
                        Text(stringResource(R.string.lbl_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = showFlexOptions,
                onCheckedChange = { onEvent(OnboardingViewModel.Event.ShowFlexOptionsChanged(it)) },
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.lbl_flexible_dates))
        }

        if (showFlexOptions) {
            AkilimoDropdown(
                label = stringResource(R.string.lbl_planting_flex),
                options = flexOptions,
                selectedOption = flexOptions.find { it.second == plantingFlex },
                onOptionSelected = { onEvent(OnboardingViewModel.Event.PlantingFlexSelected(it.second)) },
                displayText = { it.first },
            )
            AkilimoDropdown(
                label = stringResource(R.string.lbl_harvest_flex),
                options = flexOptions,
                selectedOption = flexOptions.find { it.second == harvestFlex },
                onOptionSelected = { onEvent(OnboardingViewModel.Event.HarvestFlexSelected(it.second)) },
                displayText = { it.first },
            )
        }
    }
}
