package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentCountryBinding
import com.akilimo.mobile.dto.CountryOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.blongho.country_data.World
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.akilimo.mobile.wizard.ValidationError
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryFragment : BaseStepFragment<FragmentCountryBinding>() {

    companion object {
        fun newInstance() = CountryFragment()
    }

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    private val countries: List<CountryOption> by lazy {
        EnumCountry.entries.map { country ->
            CountryOption(
                displayLabel = country.countryName,
                valueOption = country,
                currencyCode = country.currencyCode
            )
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCountryBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.countryBtnPickCountry.setOnClickListener { showCountryPicker() }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = onboardingViewModel.getUser(sessionManager.akilimoUser)
            val prefs = onboardingViewModel.getPreferences()

            if (user != null && user.enumCountry != EnumCountry.Unsupported) {
                binding.greetingTitle.visibility = View.VISIBLE
                binding.greetingTitle.text = getString(
                    R.string.lbl_greetings_text
                ).replace("{full_name}", user.getNames())

                val label =
                    countries.find { it.valueOption == user.enumCountry }?.displayLabel.orEmpty()

                user.enumCountry.let { countryCode ->
                    binding.countryName.text = label
                    binding.countryImage.setImageResource(World.getFlagOf(countryCode.name))
                }
            } else if (prefs.country != EnumCountry.Unsupported) {
                binding.greetingTitle.visibility = View.VISIBLE
                binding.greetingTitle.text = getString(
                    R.string.lbl_greetings_text
                ).replace("{full_name}", prefs.firstName ?: "User")

                val label =
                    countries.find { it.valueOption == prefs.country }?.displayLabel.orEmpty()

                prefs.country.let { countryCode ->
                    binding.countryName.text = label
                    binding.countryImage.setImageResource(World.getFlagOf(countryCode.name))
                }
            }
        }
    }

    private fun showCountryPicker() {
        val countryLabels = countries.map { it.displayLabel }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.lbl_pick_your_country)
            .setItems(countryLabels) { _, which ->
                val selected = countries[which]
                val countryCode = selected.valueOption

                binding.countryName.text = selected.displayLabel
                binding.countryImage.setImageResource(World.getFlagOf(countryCode.name))

                safeScope.launch {
                    val user = onboardingViewModel.getUser(sessionManager.akilimoUser)
                        ?: AkilimoUser(userName = sessionManager.akilimoUser)
                    onboardingViewModel.saveUser(
                        user.copy(enumCountry = countryCode), sessionManager.akilimoUser
                    )
                    if (user.id != null) {
                        onboardingViewModel.deleteSelectedFertilizersByUser(user.id)
                    }
                }
            }
            .show()
    }

    override fun verifyStep(): ValidationError? {
        val selectedLabel = binding.countryName.text.toString()
        val selectedCountry = countries.find { it.displayLabel == selectedLabel }?.valueOption

        if (selectedCountry == null || selectedCountry == EnumCountry.Unsupported) {
            val message = getString(R.string.lbl_pick_your_country)
            return ValidationError(message)
        }

        safeScope.launch {
            val user = onboardingViewModel.getUser(sessionManager.akilimoUser)
                ?: AkilimoUser(userName = sessionManager.akilimoUser)
            onboardingViewModel.saveUser(
                user.copy(enumCountry = selectedCountry), sessionManager.akilimoUser
            )
        }

        return null
    }
}
