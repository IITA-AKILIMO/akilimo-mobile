package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentCountryBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.enums.EnumCountry
import com.blongho.country_data.World
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import java.util.Locale

class CountryFragment : BaseStepFragment() {

    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!

    private var mySelectedCountryIndex = -1

    private val allowedCountries = setOf(
        EnumCountry.Nigeria,
        EnumCountry.Tanzania
    )

    private val countries = EnumCountry.entries
        .filter { it in allowedCountries }
        .map(EnumCountry::name)
        .toTypedArray()

    private val countryMap: Map<String, EnumCountry> = EnumCountry.entries
        .associateBy { it.name.lowercase(Locale.getDefault()) }

    companion object {
        fun newInstance(): CountryFragment = CountryFragment()
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                mySelectedCountryIndex = profile.selectedCountryIndex
                val countryCode = profile.countryCode
                val countryName = profile.countryName

                if (countryCode.isNotBlank() && countryName.isNotBlank()) {
                    dataIsValid = true
                    binding.countryImage.setImageResource(World.getFlagOf(countryCode))
                    binding.countryName.text = countryName
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(requireContext(), ex.localizedMessage, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun showCountryPickerDialog() {
        val context = requireContext()

        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.lbl_pick_your_country))
            setSingleChoiceItems(countries, mySelectedCountryIndex) { _, index ->
                mySelectedCountryIndex = index
            }
            setPositiveButton(getString(R.string.lbl_ok)) { dialog, _ ->
                handleCountrySelection()
                dialog.dismiss()
            }
            setNegativeButton(getString(R.string.lbl_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create().apply {
                setCanceledOnTouchOutside(false)
                show()
            }
        }
    }

    private fun handleCountrySelection() {
        if (mySelectedCountryIndex < 0 || mySelectedCountryIndex >= countries.size) return

        val selectedName = countries[mySelectedCountryIndex]
        val selectedEnum =
            countryMap[selectedName.lowercase(Locale.getDefault())] ?: EnumCountry.Other

        val countryName = selectedEnum.name
        val countryCode = selectedEnum.countryCode()
        val currencyCode = selectedEnum.currencyCode()

        binding.countryImage.setImageResource(World.getFlagOf(countryCode))
        binding.countryName.text = countryName

        updateSelectedCountry(countryCode, countryName, currencyCode)
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
                selectedCountryIndex = mySelectedCountryIndex
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

    override fun onError(error: VerificationError) {
        // Not implemented
    }
}