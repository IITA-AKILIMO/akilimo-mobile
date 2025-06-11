package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.data.CountryOption
import com.akilimo.mobile.data.indexOfValue
import com.akilimo.mobile.databinding.FragmentCountryBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.views.fragments.dialog.CountryPickerDialogFragment
import com.blongho.country_data.World
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class CountryFragment : BindBaseStepFragment<FragmentCountryBinding>() {

    private val allowedCountries = setOf(EnumCountry.Nigeria, EnumCountry.Tanzania)

    private val countries: List<CountryOption> by lazy {
        EnumCountry.entries
            .filter { it in allowedCountries }
            .map {
                CountryOption(
                    displayLabel = it.name,
                    value = it.countryCode(),
                    currencyCode = it.currencyName(requireContext())
                )
            }
    }

    private var selectedCountry = ""

    companion object {
        fun newInstance(): CountryFragment = CountryFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentCountryBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.countryBtnPickCountry.setOnClickListener {
            showCountryPickerDialog()
        }
    }

    override fun onSelected() {
        refreshData()
    }

    private fun refreshData() {
        try {
            val profile = database.profileInfoDao().findOne()
            if (profile != null) {
                binding.countryTitle.text =
                    getString(R.string.lbl_country_location, profile.firstName)
                if (profile.countryCode.isNotBlank() && profile.countryName.isNotBlank()) {
                    dataIsValid = true
                    binding.countryImage.setImageResource(World.getFlagOf(profile.countryCode))
                    binding.countryName.text = profile.countryName
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(requireContext(), ex.localizedMessage, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun showCountryPickerDialog() {
        val selectedCountryIndex = countries.indexOfValue(selectedCountry)

        CountryPickerDialogFragment(countries, selectedCountryIndex) { selectedIndex ->
            val selected = countries[selectedIndex]
            val countryCode = selected.value
            val countryName = selected.displayLabel
            val currencyCode = selected.currencyCode

            binding.countryImage.setImageResource(World.getFlagOf(countryCode))
            binding.countryName.text = countryName

            updateSelectedCountry(countryCode, countryName, currencyCode)
        }.show(parentFragmentManager, "countryPickerDialog")
    }


    private fun updateSelectedCountry(
        selectedCountryCode: String,
        selectedCountryName: String,
        selectedCurrencyCode: String
    ) {
        try {
            dataIsValid = selectedCountryCode.isNotBlank()
            val profile = database.profileInfoDao().findOne() ?: UserProfile()
            profile.apply {
                countryCode = selectedCountryCode
                countryName = selectedCountryName
                currencyCode = selectedCurrencyCode
            }

            database.profileInfoDao().insert(profile)
            sessionManager.setCountry(selectedCountryCode)
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? {
        return if (dataIsValid) null else VerificationError("Please select a country")
    }
}