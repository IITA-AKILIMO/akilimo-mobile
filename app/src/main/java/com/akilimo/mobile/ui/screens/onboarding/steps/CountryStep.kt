package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun CountryStep(
    country: EnumCountry,
    error: String?,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countryOptions = remember { EnumCountry.entries.filter { it != EnumCountry.Unsupported } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.lbl_country_location_sub), style = MaterialTheme.typography.headlineMedium)
        AkilimoDropdown(
            label = stringResource(R.string.lbl_pick_your_country),
            options = countryOptions,
            selectedOption = country.takeIf { it != EnumCountry.Unsupported },
            onOptionSelected = { onEvent(OnboardingViewModel.Event.CountrySelected(it)) },
            displayText = { it.countryName },
            error = error,
        )
    }
}
