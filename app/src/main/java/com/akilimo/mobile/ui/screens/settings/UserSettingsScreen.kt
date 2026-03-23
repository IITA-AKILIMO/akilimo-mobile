package com.akilimo.mobile.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.dto.AreaUnitOption
import com.akilimo.mobile.dto.CountryOption
import com.akilimo.mobile.dto.InterestOption
import com.akilimo.mobile.dto.findByValue
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.LabeledTextField
import com.akilimo.mobile.ui.components.compose.SwitchRow
import com.akilimo.mobile.ui.viewmodels.UserSettingsViewModel
import dev.b3nedikt.app_locale.AppLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<UserSettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.lbl_settings_saved)

    // Form state
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Dropdown options
    val genderOptions = remember {
        listOf(
            InterestOption(context.getString(R.string.lbl_gender_prompt), ""),
            InterestOption(context.getString(R.string.lbl_female), "F"),
            InterestOption(context.getString(R.string.lbl_male), "M"),
            InterestOption(context.getString(R.string.lbl_prefer_not_to_say), "NA"),
        )
    }
    val languageOptions = remember {
        Locales.supportedLocales.map { InterestOption(it.getDisplayLanguage(it), it.toLanguageTag()) }
    }
    val countryOptions = remember {
        EnumCountry.entries
            .filter { it != EnumCountry.Unsupported }
            .map { CountryOption(it.countryName, it, it.currencyCode) }
    }
    val areaUnitOptions = remember {
        EnumAreaUnit.entries.map { AreaUnitOption(it.label(context), it) }
    }

    var selectedGender by rememberSaveable { mutableStateOf<InterestOption?>(null) }
    var selectedLanguage by rememberSaveable { mutableStateOf<InterestOption?>(null) }
    var selectedCountry by rememberSaveable { mutableStateOf<CountryOption?>(null) }
    var selectedAreaUnit by rememberSaveable { mutableStateOf<AreaUnitOption?>(null) }
    var notifyEmail by rememberSaveable { mutableStateOf(false) }
    var notifySms by rememberSaveable { mutableStateOf(false) }
    var darkMode by rememberSaveable { mutableStateOf(false) }
    var formPopulated by rememberSaveable { mutableStateOf(false) }

    // Populate form once preferences load
    LaunchedEffect(state.preferences) {
        val prefs = state.preferences ?: return@LaunchedEffect
        if (formPopulated) return@LaunchedEffect
        formPopulated = true
        firstName = prefs.firstName.orEmpty()
        lastName = prefs.lastName.orEmpty()
        bio = prefs.bio.orEmpty()
        email = prefs.email.orEmpty()
        phone = prefs.phoneNumber.orEmpty()
        selectedGender = genderOptions.find { it.valueOption == prefs.gender }
        selectedLanguage = languageOptions.find { it.valueOption == prefs.languageCode }
        selectedCountry = countryOptions.findByValue(prefs.country) as? CountryOption
        selectedAreaUnit = areaUnitOptions.findByValue(prefs.preferredAreaUnit) as? AreaUnitOption
        notifyEmail = prefs.notifyByEmail
        notifySms = prefs.notifyBySms
        darkMode = prefs.darkMode
    }

    // Handle save side-effects
    LaunchedEffect(state.saved) {
        if (!state.saved) return@LaunchedEffect

        snackbarHostState.showSnackbar(savedMessage)

        val selectedLocale = Locales.supportedLocales
            .find { it.toLanguageTag() == state.newLanguageCode }
            ?: Locales.english
        AppLocale.desiredLocale = selectedLocale

        AppCompatDelegate.setDefaultNightMode(
            if (state.preferences?.darkMode == true) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        if (state.languageChanged) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(state.newLanguageCode)
            )
        }

        viewModel.onSaveHandled()
    }

    LaunchedEffect(Unit) { viewModel.loadPreferences() }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_settings),
                onBack = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            LabeledTextField(
                sectionLabel = stringResource(R.string.lbl_first_name),
                value = firstName,
                onValueChange = { firstName = it }
            )
            LabeledTextField(
                sectionLabel = stringResource(R.string.lbl_last_name),
                value = lastName,
                onValueChange = { lastName = it }
            )
            LabeledTextField(
                sectionLabel = stringResource(R.string.lbl_bio),
                value = bio,
                onValueChange = { bio = it },
                hint = stringResource(R.string.lbl_bio_hint)
            )
            LabeledTextField(
                sectionLabel = stringResource(R.string.lbl_email_address),
                value = email,
                onValueChange = { email = it; emailError = null },
                hint = stringResource(R.string.lbl_email_address_hint),
                error = emailError
            )
            LabeledTextField(
                sectionLabel = stringResource(R.string.lbl_phone_number),
                value = phone,
                onValueChange = { phone = it; phoneError = null },
                error = phoneError
            )
            Spacer(Modifier.height(12.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_gender),
                options = genderOptions,
                selectedOption = selectedGender,
                onOptionSelected = { selectedGender = it },
                displayText = { it.displayLabel },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_language),
                options = languageOptions,
                selectedOption = selectedLanguage,
                onOptionSelected = { selectedLanguage = it },
                displayText = { it.displayLabel },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_country),
                options = countryOptions,
                selectedOption = selectedCountry,
                onOptionSelected = { selectedCountry = it },
                displayText = { it.displayLabel },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            AkilimoDropdown(
                label = stringResource(R.string.lbl_area_unit),
                options = areaUnitOptions,
                selectedOption = selectedAreaUnit,
                onOptionSelected = { selectedAreaUnit = it },
                displayText = { it.displayLabel },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            SwitchRow(
                label = stringResource(R.string.lbl_notify_email),
                checked = notifyEmail,
                onCheckedChange = { notifyEmail = it },
            )
            SwitchRow(
                label = stringResource(R.string.lbl_notify_sms),
                checked = notifySms,
                onCheckedChange = { notifySms = it },
            )
            SwitchRow(
                label = stringResource(R.string.lbl_dark_mode),
                checked = darkMode,
                onCheckedChange = { darkMode = it },
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val validEmail = android.util.Patterns.EMAIL_ADDRESS
                    val validPhone = android.util.Patterns.PHONE

                    if (email.isNotBlank() && !validEmail.matcher(email).matches()) {
                        emailError = context.getString(R.string.lbl_valid_email_req)
                        return@Button
                    }
                    if (phone.isNotBlank() && !validPhone.matcher(phone).matches()) {
                        phoneError = context.getString(R.string.lbl_valid_number_req)
                        return@Button
                    }

                    val prefs = UserPreferences(
                        languageCode = selectedLanguage?.valueOption ?: Locales.english.toLanguageTag(),
                        firstName = firstName.trim().ifBlank { null },
                        lastName = lastName.trim().ifBlank { null },
                        email = email.trim().ifBlank { null },
                        phoneNumber = phone.trim().ifBlank { null },
                        gender = selectedGender?.valueOption?.ifBlank { null },
                        country = selectedCountry?.valueOption ?: EnumCountry.Unsupported,
                        bio = bio.trim().ifBlank { null },
                        notifyByEmail = notifyEmail,
                        notifyBySms = notifySms,
                        preferredAreaUnit = selectedAreaUnit?.valueOption ?: EnumAreaUnit.ACRE,
                        darkMode = darkMode,
                    )
                    viewModel.savePreferences(prefs)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.lbl_save))
            }
        }
    }
}

