package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MySpinnerAdapter
import com.akilimo.mobile.data.InterestOption
import com.akilimo.mobile.data.indexOfValue
import com.akilimo.mobile.databinding.FragmentBioDataBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.ValidationHelper
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class BioDataFragment : BindBaseStepFragment<FragmentBioDataBinding>() {

    private val validationHelper by lazy { ValidationHelper() }

    private var isPhoneValid = true
    private var selectedMobileCode: String = ""

    private var selectedGenderValue: String? = null
    private var selectedInterestValue: String? = null

    private var genderOptions: List<InterestOption> = emptyList()
    private var interestOptions: List<InterestOption> = emptyList()

    companion object {
        fun newInstance(): BioDataFragment = BioDataFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentBioDataBinding.inflate(inflater, container, false)

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

        binding.spnGender.apply {
            adapter = MySpinnerAdapter(requireContext(), genderOptions.map { it.displayLabel })
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    selectedGenderValue = genderOptions.getOrNull(pos)?.value.takeIf { pos > 0 }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        binding.spnInterest.apply {
            adapter = MySpinnerAdapter(requireContext(), interestOptions.map { it.displayLabel })
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    selectedInterestValue = interestOptions.getOrNull(pos)?.value.takeIf { pos > 0 }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        binding.ccpCountry.apply {
            setPhoneNumberValidityChangeListener { isValid -> isPhoneValid = isValid }
            setOnCountryChangeListener {
                selectedMobileCode = selectedCountryCodeWithPlus
            }
            registerCarrierNumberEditText(binding.edtPhone)
        }
    }

    private fun refreshData() {
        try {
            val userProfile = database.profileInfoDao().findOne() ?: return

            selectedMobileCode = userProfile.mobileCode
            selectedGenderValue = userProfile.gender
            selectedInterestValue = userProfile.akilimoInterest

            binding.apply {
                edtFirstName.setText(userProfile.firstName)
                edtLastName.setText(userProfile.lastName)
                edtEmail.setText(userProfile.email)

                if (!userProfile.phoneNumber.isNullOrEmpty()) {
                    ccpCountry.fullNumber = userProfile.phoneNumber
                }

                spnGender.setSelection(genderOptions.indexOfValue(selectedGenderValue))
                spnInterest.setSelection(interestOptions.indexOfValue(selectedInterestValue))

            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun saveBioData() {
        errorMessage = ""

        binding.apply {
            edtFirstName.error = null
            edtLastName.error = null
            edtEmail.error = null
            edtPhone.error = null
        }

        val firstName = binding.edtFirstName.text.toString()
        val lastName = binding.edtLastName.text.toString()
        val email = binding.edtEmail.text.toString().trim()
        val rawPhoneNumber = binding.edtPhone.text.toString()
        val fullPhoneNumber = binding.ccpCountry.fullNumber
        val mobileCode = binding.ccpCountry.selectedCountryCodeWithPlus

        if (firstName.isBlank()) {
            errorMessage = getString(R.string.lbl_first_name_req)
            binding.edtFirstName.error = errorMessage
            return
        }

        if (lastName.isBlank()) {
            errorMessage = getString(R.string.lbl_last_name_req)
            binding.edtLastName.error = errorMessage
            return
        }

        if ((fullPhoneNumber.isNullOrEmpty() || rawPhoneNumber.isEmpty()) && !isPhoneValid) {
            errorMessage = getString(R.string.lbl_valid_number_req)
            binding.edtPhone.error = errorMessage
            return
        }

        if (validationHelper.isEmailValid(email)) {
            errorMessage = getString(R.string.lbl_valid_email_req)
            binding.edtEmail.error = errorMessage
            return
        }

        if (selectedGenderValue.isNullOrEmpty()) {
            errorMessage = getString(R.string.lbl_gender_prompt)
            return
        }

        if (selectedInterestValue.isNullOrEmpty()) {
            errorMessage = getString(R.string.lbl_akilimo_interest_prompt)
            return
        }

        try {
            val userProfile = database.profileInfoDao().findOne() ?: UserProfile()

            userProfile.apply {
                this.firstName = firstName
                this.lastName = lastName
                this.gender = selectedGenderValue
                this.akilimoInterest = selectedInterestValue
                this.email = email
                this.mobileCode = mobileCode
                this.phoneNumber = fullPhoneNumber
                this.deviceToken = sessionManager.getDeviceToken()
            }

            database.profileInfoDao().insert(userProfile)

        } catch (ex: Exception) {
            Toast.makeText(context, ex.message.orEmpty(), Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? {
        saveBioData()
        return if (errorMessage.isNotEmpty()) VerificationError(errorMessage) else null
    }

    override fun onSelected() {
        refreshData()
    }
}