package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun BioDataStep(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    gender: String,
    interest: String,
    errors: Map<String, String>,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val genderOptions = remember {
        listOf(
            "F" to context.getString(R.string.lbl_female),
            "M" to context.getString(R.string.lbl_male),
            "NA" to context.getString(R.string.lbl_prefer_not_to_say),
        )
    }
    val interestOptions = remember {
        listOf(
            "farmer" to context.getString(R.string.lbl_interest_farmer),
            "extension_agent" to context.getString(R.string.lbl_interest_extension_agent),
            "agronomist" to context.getString(R.string.lbl_interest_agronomist),
            "curious" to context.getString(R.string.lbl_interest_curious),
        )
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.lbl_self_intro), style = MaterialTheme.typography.headlineMedium)
        AkilimoTextField(
            value = firstName,
            onValueChange = { onEvent(OnboardingViewModel.Event.FirstNameChanged(it)) },
            label = stringResource(R.string.lbl_first_name),
            error = errors["firstName"],
        )
        AkilimoTextField(
            value = lastName,
            onValueChange = { onEvent(OnboardingViewModel.Event.LastNameChanged(it)) },
            label = stringResource(R.string.lbl_last_name),
            error = errors["lastName"],
        )
        AkilimoTextField(
            value = email,
            onValueChange = { onEvent(OnboardingViewModel.Event.EmailChanged(it)) },
            label = stringResource(R.string.lbl_email_address),
            error = errors["email"],
        )
        AkilimoTextField(
            value = phone,
            onValueChange = { onEvent(OnboardingViewModel.Event.PhoneChanged(it)) },
            label = stringResource(R.string.lbl_phone_number),
            error = errors["phone"],
        )
        AkilimoDropdown(
            label = stringResource(R.string.lbl_gender),
            options = genderOptions,
            selectedOption = genderOptions.find { it.first == gender },
            onOptionSelected = { onEvent(OnboardingViewModel.Event.GenderSelected(it.first)) },
            displayText = { it.second },
            error = errors["gender"],
        )
        AkilimoDropdown(
            label = stringResource(R.string.lbl_akilimo_interest),
            options = interestOptions,
            selectedOption = interestOptions.find { it.first == interest },
            onOptionSelected = { onEvent(OnboardingViewModel.Event.InterestSelected(it.first)) },
            displayText = { it.second },
            error = errors["interest"],
        )
    }
}
