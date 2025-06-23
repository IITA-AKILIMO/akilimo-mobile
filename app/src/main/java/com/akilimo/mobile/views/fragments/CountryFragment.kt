package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.data.indexOfValue
import com.akilimo.mobile.databinding.FragmentCountryBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.viewmodels.CountryViewModel
import com.akilimo.mobile.viewmodels.factory.CountryViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.CountryPickerDialogFragment
import com.blongho.country_data.World
import com.stepstone.stepper.VerificationError

class CountryFragment : BindBaseStepFragment<FragmentCountryBinding>() {

    private val allowedCountries: Set<EnumCountry> =
        setOf(EnumCountry.Nigeria, EnumCountry.Tanzania)

    private val viewModel: CountryViewModel by viewModels {
        CountryViewModelFactory(requireActivity().application, allowedCountries)
    }


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
        observeProfile()
    }

    override fun onSelected() {
        viewModel.loadProfile()
    }

    private fun observeProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                binding.countryTitle.text =
                    getString(R.string.lbl_country_location, profile.firstName)
                if (profile.countryCode.isNotBlank() && profile.countryName.isNotBlank()) {
                    dataIsValid = true
                    binding.countryImage.setImageResource(World.getFlagOf(profile.countryCode))
                    binding.countryName.text = profile.countryName
                }
            }
        }
    }


    private fun showCountryPickerDialog() {
        val countries = viewModel.countries
        val selectedCountry = viewModel.userProfile.value?.countryCode ?: ""
        val selectedCountryIndex = countries.indexOfValue(selectedCountry)

        CountryPickerDialogFragment(countries, selectedCountryIndex) { selectedIndex ->
            val selected = countries[selectedIndex]
            val countryCode = selected.value
            val countryName = selected.displayLabel
            val currencyCode = selected.currencyCode

            binding.countryImage.setImageResource(World.getFlagOf(countryCode))
            binding.countryName.text = countryName

            viewModel.updateCountrySelection(countryCode, countryName, currencyCode)
        }.show(parentFragmentManager, "countryPickerDialog")
    }


    override fun verifyStep(): VerificationError? {
        return if (dataIsValid) null else VerificationError("Please select a country")
    }
}