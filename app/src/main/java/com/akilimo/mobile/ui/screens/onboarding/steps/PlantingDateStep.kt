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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.CustomDatePicker
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.utils.DateHelper
import java.time.LocalDate

@Composable
fun PlantingDateStep(
    plantingDate: LocalDate?,
    harvestDate: LocalDate?,
    plantingFlex: Long,
    harvestFlex: Long,
    showFlexOptions: Boolean,
    errors: Map<String, String>,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    fragmentManager: FragmentManager,
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
            onClick = {
                val minDate = LocalDate.now().minusMonths(4 + plantingFlex)
                val maxDate = LocalDate.now().plusMonths(12 + plantingFlex)
                CustomDatePicker(
                    context = context,
                    fragmentManager = fragmentManager,
                    title = context.getString(R.string.lbl_planting_date),
                    minDate = minDate,
                    maxDate = maxDate,
                    initialDate = plantingDate,
                ) { selected ->
                    onEvent(OnboardingViewModel.Event.PlantingDateSelected(selected))
                }.show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.lbl_pick_planting_date)) }

        AkilimoTextField(
            value = DateHelper.formatToString(harvestDate),
            onValueChange = {},
            label = stringResource(R.string.lbl_harvesting_date),
            readOnly = true,
            error = errors["harvestDate"],
        )
        Button(
            onClick = {
                if (plantingDate == null) return@Button
                val flex = harvestFlex
                val minDate = plantingDate.plusMonths(8 - flex)
                val maxDate = plantingDate.plusMonths(16 + flex)
                CustomDatePicker(
                    context = context,
                    fragmentManager = fragmentManager,
                    title = context.getString(R.string.lbl_harvesting_date),
                    minDate = minDate,
                    maxDate = maxDate,
                    initialDate = harvestDate,
                ) { selected ->
                    onEvent(OnboardingViewModel.Event.HarvestDateSelected(selected))
                }.show()
            },
            enabled = plantingDate != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.lbl_pick_harvest_date)) }

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
