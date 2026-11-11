package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumFieldArea
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.utils.MathHelper

/** Accepts "1,5" as well as "1.5" (many keyboards in the region type a comma). */
internal fun parseFarmSize(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

@Composable
fun AreaUnitStep(
    areaUnit: EnumAreaUnit,
    farmSize: Double,
    customFarmSize: Boolean,
    rememberAreaUnit: Boolean,
    country: EnumCountry,
    errors: Map<String, String>,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
    /** False when the unit was remembered earlier: only the farm size is asked */
    showUnitPicker: Boolean = true,
) {
    val context = LocalContext.current
    val areaUnitOptions = remember(country) {
        buildList {
            add(EnumAreaUnit.ACRE)
            add(EnumAreaUnit.HA)
            add(EnumAreaUnit.M2)
            if (country == EnumCountry.RW) add(EnumAreaUnit.ARE)
        }
    }

    fun fieldSizesForUnit(unit: EnumAreaUnit): List<Pair<String, Double>> {
        val bases = listOf(
            EnumFieldArea.QUARTER_ACRE,
            EnumFieldArea.HALF_ACRE,
            EnumFieldArea.ONE_ACRE,
            EnumFieldArea.ONE_HALF_ACRE,
            EnumFieldArea.TWO_HALF_ACRE,
        )
        val converted = bases.map { area ->
            val convertedVal = MathHelper.convertFromAcres(area.areaValue(), unit)
            "%.2f %s".format(convertedVal, unit.label(context)) to convertedVal
        }
        return converted + (context.getString(R.string.exact_field_area) to EnumFieldArea.EXACT_AREA.areaValue())
    }

    val fieldSizeOptions = remember(areaUnit) { fieldSizesForUnit(areaUnit) }
    val selectedSizeOption = if (customFarmSize) null else fieldSizeOptions.find { it.second == farmSize }
    // Keyed only on customFarmSize: keying on farmSize would reset the text on every
    // keystroke (each edit updates farmSize), turning "2" into "2.0" mid-typing.
    var customSizeText by rememberSaveable(customFarmSize) {
        mutableStateOf(if (customFarmSize) (if (farmSize > 0) farmSize.toString() else "") else "")
    }
    val notANumber = customSizeText.isNotBlank() && parseFarmSize(customSizeText) == null
    val numberError = stringResource(R.string.error_farm_size_number)

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.lbl_area_unit), style = MaterialTheme.typography.headlineMedium)

        if (showUnitPicker) {
            AkilimoDropdown(
                label = stringResource(R.string.lbl_select_area_unit),
                options = areaUnitOptions,
                selectedOption = areaUnit,
                onOptionSelected = { onEvent(OnboardingViewModel.Event.AreaUnitSelected(it)) },
                displayText = { it.label(context) },
                error = errors["areaUnit"],
            )
        }

        AkilimoDropdown(
            label = stringResource(R.string.lbl_farm_size),
            options = fieldSizeOptions,
            selectedOption = selectedSizeOption,
            onOptionSelected = { (_, size) ->
                if (size == EnumFieldArea.EXACT_AREA.areaValue()) {
                    onEvent(OnboardingViewModel.Event.FarmSizeSelected(0.0, true))
                } else {
                    onEvent(OnboardingViewModel.Event.FarmSizeSelected(size, false))
                }
            },
            displayText = { it.first },
            error = if (customFarmSize) null else errors["farmSize"],
        )

        if (customFarmSize) {
            AkilimoTextField(
                value = customSizeText,
                onValueChange = { text ->
                    customSizeText = text
                    // Invalid text keeps size at 0 so Submit still refuses it; the inline error explains why
                    val parsed = parseFarmSize(text) ?: 0.0
                    onEvent(OnboardingViewModel.Event.FarmSizeSelected(parsed, true))
                },
                label = stringResource(R.string.lbl_cassava_field_size, areaUnit.label(context)),
                error = if (notANumber) numberError else errors["farmSize"],
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
            )
        }

        if (showUnitPicker) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = rememberAreaUnit,
                        role = Role.Checkbox,
                        onValueChange = { onEvent(OnboardingViewModel.Event.RememberAreaUnitChanged(it)) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = rememberAreaUnit, onCheckedChange = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.lbl_remember_pref))
            }
        }
    }
}
