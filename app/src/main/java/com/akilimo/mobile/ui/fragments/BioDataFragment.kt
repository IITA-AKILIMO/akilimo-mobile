package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentBioDataBinding
import com.akilimo.mobile.dto.InterestOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.wizard.ValidationError
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [BioDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class BioDataFragment : BaseStepFragment<FragmentBioDataBinding>() {

    companion object {
        fun newInstance() = BioDataFragment()
    }

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    private var genderOptions: List<InterestOption> = emptyList()
    private var interestOptions: List<InterestOption> = emptyList()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBioDataBinding = FragmentBioDataBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        genderOptions = listOf(
            InterestOption(getString(R.string.lbl_gender_prompt), ""),
            InterestOption(getString(R.string.lbl_female), "F"),
            InterestOption(getString(R.string.lbl_male), "M"),
            InterestOption(getString(R.string.lbl_prefer_not_to_say), "NA")
        )

        interestOptions = listOf(
            InterestOption(getString(R.string.lbl_akilimo_interest_prompt), ""),
            InterestOption(getString(R.string.lbl_interest_farmer), "farmer"),
            InterestOption(getString(R.string.lbl_interest_extension_agent), "extension_agent"),
            InterestOption(getString(R.string.lbl_interest_agronomist), "agronomist"),
            InterestOption(getString(R.string.lbl_interest_curious), "curious")
        )

        binding.ccpCountry.apply {
            registerCarrierNumberEditText(binding.edtPhone)
        }
        val genderAdapter = ValueOptionAdapter(requireContext(), genderOptions)
        val interestAdapter = ValueOptionAdapter(requireContext(), interestOptions)

        binding.dropGender.setAdapter(genderAdapter)
        binding.dropGender.setOnItemClickListener { _, _, position, _ ->
            val selected = genderAdapter.getItem(position) ?: return@setOnItemClickListener
            binding.dropGender.setText(selected.displayLabel, false)
        }

        binding.dropInterest.setAdapter(interestAdapter)
        binding.dropInterest.setOnItemClickListener { _, _, position, _ ->
            val selected = interestAdapter.getItem(position) ?: return@setOnItemClickListener
            binding.dropInterest.setText(selected.displayLabel, false)
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = onboardingViewModel.getUser(sessionManager.akilimoUser)
            val prefs = onboardingViewModel.getPreferences()

            if (user != null) {
                binding.edtFirstName.setText(user.firstName)
                binding.edtLastName.setText(user.lastName)
                binding.edtEmail.setText(user.email)
                binding.ccpCountry.fullNumber = user.mobileNumber
                binding.edtPhone.setText(user.mobileNumber)
                binding.dropGender.setText(
                    genderOptions.find { opt -> opt.valueOption == user.gender }?.displayLabel,
                    false
                )
                binding.dropInterest.setText(
                    interestOptions.find { opt -> opt.valueOption == user.akilimoInterest }?.displayLabel,
                    false
                )
            } else {
                //Fall back to user preferences
                binding.edtFirstName.setText(prefs.firstName)
                binding.edtLastName.setText(prefs.lastName)
                binding.edtEmail.setText(prefs.email)
                prefs.phoneNumber?.let {
                    binding.ccpCountry.fullNumber = it
                    binding.edtPhone.setText(it)
                }
                binding.dropGender.setText(
                    genderOptions.find { opt -> opt.valueOption == prefs.gender }?.displayLabel,
                    false
                )
            }
        }
    }

    override fun verifyStep(): ValidationError? = with(binding) {
        val genderLabel = dropGender.text.toString()
        val interestLabel = dropInterest.text.toString()

        val gender = genderOptions.find { it.displayLabel == genderLabel }?.valueOption.orEmpty()
        val interest =
            interestOptions.find { it.displayLabel == interestLabel }?.valueOption.orEmpty()

        val firstName = edtFirstName.text.toString()
        val lastName = edtLastName.text.toString()
        val email = edtEmail.text.toString()
        val hasUserInput = edtPhone.text?.isNotBlank() == true
        val phone = if (hasUserInput) ccpCountry.fullNumber else ""
        val phoneCountryCode = if (hasUserInput) ccpCountry.selectedCountryCodeWithPlus else ""

        val validations = listOf(
            Triple(firstName.isBlank(), lytFirstName, R.string.lbl_first_name_req),
            Triple(lastName.isBlank(), lytLastName, R.string.lbl_last_name_req),
            Triple(gender.isBlank(), lytGender, R.string.lbl_gender_prompt),
            Triple(
                email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                lytEmail,
                R.string.lbl_valid_email_req
            ),
            Triple(
                hasUserInput && !Patterns.PHONE.matcher(phone).matches(),
                lytPhone,
                R.string.lbl_valid_number_req
            ),
            Triple(interest.isBlank(), lytInterest, R.string.lbl_akilimo_interest_prompt),
        )

        // Clear all errors
        validations.forEach { (_, layout, _) -> layout.error = null }

        // Return first error if any
        validations.firstOrNull { it.first }?.let { (_, layout, msgRes) ->
            val message = getString(msgRes)
            layout.error = message
            return ValidationError(message)
        }

        safeScope.launch {
            val existingUser = onboardingViewModel.getUser(sessionManager.akilimoUser)
                ?: AkilimoUser(userName = sessionManager.akilimoUser)
            onboardingViewModel.saveUser(
                existingUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    mobileNumber = phone,
                    mobileCountryCode = phoneCountryCode,
                    gender = gender,
                    akilimoInterest = interest,
                    deviceToken = sessionManager.deviceToken
                ), sessionManager.akilimoUser
            )
        }

        return null
    }
}
