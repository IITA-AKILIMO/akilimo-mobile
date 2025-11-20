package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentCountryBinding
import com.akilimo.mobile.dto.CountryOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.blongho.country_data.World
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch

class CountryFragment : BaseStepFragment<FragmentCountryBinding>() {

    companion object {
        fun newInstance() = CountryFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo
    private lateinit var selectedFertilizerRepo: SelectedFertilizerRepo

    private val countries: List<CountryOption> by lazy {
        EnumCountry.entries.map {
            CountryOption(
                displayLabel = it.countryName,
                valueOption = it.name,
                currencyCode = it.currencyCode
            )
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCountryBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
        selectedFertilizerRepo = SelectedFertilizerRepo(database.selectedFertilizerDao())
        binding.countryBtnPickCountry.setOnClickListener { showCountryPicker() }

    }

    override fun prefillFromEntity() {
        safeScope.launch {
            userRepository.getUser(sessionManager.akilimoUser)?.let { user ->
                binding.greetingTitle.visibility = View.VISIBLE
                binding.greetingTitle.text = getString(
                    R.string.lbl_greetings_text
                ).replace("{full_name}", user.getNames())

                val countryCode = user.farmCountry.orEmpty()
                val label = countries.find { it.valueOption == countryCode }?.displayLabel.orEmpty()

                if (countryCode.isNotBlank()) {
                    binding.countryName.text = label
                    binding.countryImage.setImageResource(World.getFlagOf(countryCode))
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
                binding.countryImage.setImageResource(World.getFlagOf(countryCode))

                safeScope.launch {
                    val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser()
                    user.farmCountry = countryCode
                    userRepository.saveOrUpdateUser(user, sessionManager.akilimoUser)
                    if (user.id != null) {
                        selectedFertilizerRepo.deleteByUserId(user.id ?: 0)
                    }
                }
            }
            .show()
    }

    override fun verifyStep(): VerificationError? {
        val selectedLabel = binding.countryName.text.toString()
        val selectedCountry = countries.find { it.displayLabel == selectedLabel }?.valueOption

        if (selectedCountry.isNullOrBlank()) {
            val message = getString(R.string.lbl_pick_your_country)
            return VerificationError(message)
        }

        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser()
            user.farmCountry = selectedCountry
            userRepository.saveOrUpdateUser(user, sessionManager.akilimoUser)
        }

        return null
    }
}
