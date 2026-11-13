package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.PhoneNumberField
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.utils.PhoneCountry

@Composable
fun BioDataStep(
    firstName: String,
    lastName: String,
    email: String,
    phone: String,
    phoneRegion: String,
    phoneDialCode: Int,
    phoneCountries: List<PhoneCountry>,
    suggestedPhoneRegions: List<String>,
    gender: String,
    interest: String,
    recLanguage: String,
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

    val recommendationLanguageOptions = remember {
        listOf(
            "en" to context.getString(R.string.lbl_english),
            "sw" to context.getString(R.string.lbl_swahili),
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

    val nameKeyboard = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.lbl_self_intro),
            style = MaterialTheme.typography.headlineMedium
        )
        AkilimoTextField(
            value = firstName,
            onValueChange = { onEvent(OnboardingViewModel.Event.FirstNameChanged(it)) },
            label = stringResource(R.string.lbl_first_name),
            error = errors["firstName"],
            keyboardOptions = nameKeyboard,
        )
        AkilimoTextField(
            value = lastName,
            onValueChange = { onEvent(OnboardingViewModel.Event.LastNameChanged(it)) },
            label = stringResource(R.string.lbl_last_name),
            error = errors["lastName"],
            keyboardOptions = nameKeyboard,
        )
        AkilimoTextField(
            value = email,
            onValueChange = { onEvent(OnboardingViewModel.Event.EmailChanged(it)) },
            label = stringResource(R.string.lbl_optional_field, stringResource(R.string.lbl_email_address)),
            error = errors["email"],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        // Any country in the world: the code button opens a searchable country list
        PhoneNumberField(
            number = phone,
            onNumberChange = { onEvent(OnboardingViewModel.Event.PhoneChanged(it)) },
            region = phoneRegion,
            dialCode = phoneDialCode,
            onRegionChange = { onEvent(OnboardingViewModel.Event.PhoneRegionSelected(it)) },
            countries = phoneCountries,
            suggestedRegions = suggestedPhoneRegions,
            label = stringResource(R.string.lbl_optional_field, stringResource(R.string.lbl_phone_number)),
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

        AkilimoDropdown(
            label = stringResource(R.string.lbl_recommendation_language),
            options = recommendationLanguageOptions,
            selectedOption = recommendationLanguageOptions.find { it.first == recLanguage },
            onOptionSelected = { onEvent(OnboardingViewModel.Event.RecLanguageSelected(it.first)) },
            displayText = { it.second },
            error = errors["rec_language"],
        )
    }
}
