package com.akilimo.mobile.ui.activities

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityUserSettingsBinding
import com.akilimo.mobile.dto.AreaUnitOption
import com.akilimo.mobile.dto.CountryOption
import com.akilimo.mobile.dto.InterestOption
import com.akilimo.mobile.dto.findByValue
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.ui.viewmodels.UserSettingsViewModel
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.launch
import timber.log.Timber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserSettingsActivity : BaseActivity<ActivityUserSettingsBinding>() {

    private val viewModel: UserSettingsViewModel by viewModels()

    private lateinit var genderOptions: List<InterestOption>
    private lateinit var languageOptions: List<InterestOption>
    private lateinit var countryOptions: List<CountryOption>
    private lateinit var areaUnitOptions: List<AreaUnitOption>

    override fun inflateBinding(): ActivityUserSettingsBinding =
        ActivityUserSettingsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupToolbar()
        setupDropdownOptions()
        setupDropdownAdapters()
        setupSaveButton()
        observeViewModel()
        viewModel.loadPreferences()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdownOptions() {
        genderOptions = listOf(
            InterestOption(getString(R.string.lbl_gender_prompt), ""),
            InterestOption(getString(R.string.lbl_female), "F"),
            InterestOption(getString(R.string.lbl_male), "M"),
            InterestOption(getString(R.string.lbl_prefer_not_to_say), "NA")
        )

        languageOptions = Locales.supportedLocales.map {
            InterestOption(it.getDisplayLanguage(it), it.toLanguageTag())
        }

        countryOptions = EnumCountry.entries
            .filter { it != EnumCountry.Unsupported }
            .map { CountryOption(it.countryName, it, it.currencyCode) }

        areaUnitOptions = EnumAreaUnit.entries.map { unit ->
            AreaUnitOption(unit.label(this), unit)
        }
    }

    private fun setupDropdownAdapters() {
        val genderAdapter = ValueOptionAdapter(this, genderOptions)
        binding.dropGender.setAdapter(genderAdapter)
        binding.dropGender.setOnItemClickListener { _, _, position, _ ->
            genderAdapter.getItem(position)?.let { binding.dropGender.setText(it.displayLabel, false) }
        }

        val languageAdapter = ValueOptionAdapter(this, languageOptions)
        binding.dropLanguage.setAdapter(languageAdapter)
        binding.dropLanguage.setOnItemClickListener { _, _, position, _ ->
            languageAdapter.getItem(position)?.let { binding.dropLanguage.setText(it.displayLabel, false) }
        }

        val countryAdapter = ValueOptionAdapter(this, countryOptions)
        binding.dropCountry.setAdapter(countryAdapter)
        binding.dropCountry.setOnItemClickListener { _, _, position, _ ->
            countryAdapter.getItem(position)?.let { binding.dropCountry.setText(it.displayLabel, false) }
        }

        val areaUnitAdapter = ValueOptionAdapter(this, areaUnitOptions)
        binding.dropAreaUnit.setAdapter(areaUnitAdapter)
        binding.dropAreaUnit.setOnItemClickListener { _, _, position, _ ->
            areaUnitAdapter.getItem(position)?.let { binding.dropAreaUnit.setText(it.displayLabel, false) }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.preferences?.let { populateForm(it) }

                    if (state.saved) {
                        Snackbar.make(binding.root, R.string.lbl_settings_saved, Snackbar.LENGTH_SHORT).show()
                        Timber.d("User preferences saved")

                        // Sync side effects that need Android context
                        sessionManager.languageCode = state.newLanguageCode
                        sessionManager.darkMode = state.preferences?.darkMode ?: false

                        val selectedLocale = Locales.supportedLocales
                            .find { it.toLanguageTag() == state.newLanguageCode }
                            ?: Locales.english
                        AppLocale.desiredLocale = selectedLocale

                        AppCompatDelegate.setDefaultNightMode(
                            if (state.preferences?.darkMode == true) AppCompatDelegate.MODE_NIGHT_YES
                            else AppCompatDelegate.MODE_NIGHT_NO
                        )

                        if (state.languageChanged) {
                            AlertDialog.Builder(this@UserSettingsActivity)
                                .setTitle(R.string.lbl_restart_required)
                                .setMessage(R.string.lbl_restart_language_message)
                                .setPositiveButton(R.string.lbl_restart_now) { _, _ ->
                                    ProcessPhoenix.triggerRebirth(this@UserSettingsActivity)
                                }
                                .setNegativeButton(R.string.lbl_restart_later, null)
                                .show()
                        }

                        viewModel.onSaveHandled()
                    }
                }
            }
        }
    }

    private fun populateForm(prefs: UserPreferences) = with(binding) {
        edtFirstName.setText(prefs.firstName)
        edtLastName.setText(prefs.lastName)
        edtBio.setText(prefs.bio)
        edtEmail.setText(prefs.email)
        edtPhone.setText(prefs.phoneNumber)

        prefs.phoneCountryCode?.let { code ->
            if (code.isNotBlank()) {
                ccpCountry.setCountryForPhoneCode(
                    code.removePrefix("+").toIntOrNull() ?: return@let
                )
            }
        }
        ccpCountry.registerCarrierNumberEditText(edtPhone)

        dropGender.setText(
            genderOptions.find { it.valueOption == prefs.gender }?.displayLabel ?: "", false
        )
        dropLanguage.setText(
            languageOptions.find { it.valueOption == prefs.languageCode }?.displayLabel ?: "", false
        )
        dropCountry.setText(
            countryOptions.findByValue(prefs.country)?.displayLabel ?: "", false
        )
        dropAreaUnit.setText(
            areaUnitOptions.findByValue(prefs.preferredAreaUnit)?.displayLabel ?: "", false
        )

        switchNotifyEmail.isChecked = prefs.notifyByEmail
        switchNotifySms.isChecked = prefs.notifyBySms
        switchDarkMode.isChecked = prefs.darkMode
    }

    private fun setupSaveButton() {
        binding.btnSaveSettings.setOnClickListener { validateAndSave() }
    }

    private fun validateAndSave() = with(binding) {
        lytFirstName.error = null
        lytEmail.error = null
        lytPhone.error = null

        val firstName = edtFirstName.text.toString().trim()
        val lastName = edtLastName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val hasPhoneInput = edtPhone.text?.isNotBlank() == true
        val phone = if (hasPhoneInput) ccpCountry.fullNumber else ""
        val phoneCountryCode = if (hasPhoneInput) ccpCountry.selectedCountryCodeWithPlus else ""

        if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            lytEmail.error = getString(R.string.lbl_valid_email_req)
            return@with
        }
        if (hasPhoneInput && !Patterns.PHONE.matcher(phone).matches()) {
            lytPhone.error = getString(R.string.lbl_valid_number_req)
            return@with
        }

        val genderLabel = dropGender.text.toString()
        val gender = genderOptions.find { it.displayLabel == genderLabel }?.valueOption.orEmpty()

        val langLabel = dropLanguage.text.toString()
        val langCode = languageOptions.find { it.displayLabel == langLabel }?.valueOption
            ?: Locales.english.toLanguageTag()

        val countryLabel = dropCountry.text.toString()
        val country = countryOptions.find { it.displayLabel == countryLabel }?.valueOption
            ?: EnumCountry.Unsupported

        val areaLabel = dropAreaUnit.text.toString()
        val areaUnit = areaUnitOptions.find { it.displayLabel == areaLabel }?.valueOption
            ?: EnumAreaUnit.ACRE

        val preferences = UserPreferences(
            languageCode = langCode,
            firstName = firstName.ifBlank { null },
            lastName = lastName.ifBlank { null },
            email = email.ifBlank { null },
            phoneNumber = phone.ifBlank { null },
            phoneCountryCode = phoneCountryCode.ifBlank { null },
            gender = gender.ifBlank { null },
            country = country,
            bio = edtBio.text.toString().trim().ifBlank { null },
            notifyByEmail = switchNotifyEmail.isChecked,
            notifyBySms = switchNotifySms.isChecked,
            preferredAreaUnit = areaUnit,
            darkMode = switchDarkMode.isChecked
        )

        viewModel.savePreferences(preferences, sessionManager.akilimoUser, sessionManager.languageCode)
    }
}
