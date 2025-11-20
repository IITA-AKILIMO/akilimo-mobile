package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentBioDataBinding
import com.akilimo.mobile.dto.InterestOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [BioDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BioDataFragment : BaseStepFragment<FragmentBioDataBinding>() {


    companion object {
        fun newInstance() = BioDataFragment()
    }

    private var genderOptions: List<InterestOption> = emptyList()
    private var interestOptions: List<InterestOption> = emptyList()

    private lateinit var userRepository: AkilimoUserRepo

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBioDataBinding = FragmentBioDataBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
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
            val selected = genderAdapter.getItem(position)?: return@setOnItemClickListener
            binding.dropGender.setText(selected.displayLabel, false)
        }

        binding.dropInterest.setAdapter(interestAdapter)
        binding.dropInterest.setOnItemClickListener { _, _, position, _ ->
            val selected = interestAdapter.getItem(position)?: return@setOnItemClickListener
            binding.dropInterest.setText(selected.displayLabel, false)
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            user?.let {
                binding.edtFirstName.setText(it.firstName)
                binding.edtLastName.setText(it.lastName)
                binding.edtEmail.setText(it.email)
                binding.ccpCountry.fullNumber = it.mobileNumber
                binding.edtPhone.setText(it.mobileNumber)
                binding.dropGender.setText(
                    genderOptions.find { opt -> opt.valueOption == it.gender }?.displayLabel,
                    false
                )
                binding.dropInterest.setText(
                    interestOptions.find { opt -> opt.valueOption == it.akilimoInterest }?.displayLabel,
                    false
                )
            }
        }
    }

    override fun verifyStep(): VerificationError? = with(binding) {
        val genderLabel = dropGender.text.toString()
        val interestLabel = dropInterest.text.toString()

        val gender = genderOptions.find { it.displayLabel == genderLabel }?.valueOption.orEmpty()
        val interest = interestOptions.find { it.displayLabel == interestLabel }?.valueOption.orEmpty()

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
            return VerificationError(message)
        }

        safeScope.launch {
            val existingUser = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser()

            existingUser.firstName = firstName
            existingUser.lastName = lastName
            existingUser.email = email
            existingUser.mobileNumber = phone
            existingUser.mobileCountryCode = phoneCountryCode
            existingUser.gender = gender
            existingUser.akilimoInterest = interest
            existingUser.deviceToken = sessionManager.deviceToken

            userRepository.saveOrUpdateUser(existingUser, sessionManager.akilimoUser)
        }

        return null
    }
}
