package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MySpinnerAdapter
import com.akilimo.mobile.data.InterestOption
import com.akilimo.mobile.databinding.FragmentBioDataBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.ValidationHelper
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry


class BioDataFragment : BindBaseStepFragment<FragmentBioDataBinding>() {

    private val validationHelper: ValidationHelper by lazy { ValidationHelper() }

    private var phoneIsValid = true

    private var myMobileCode: String = ""

    private var myGender: String? = null
    private var myAkilimoInterest: String? = null
    private var mySelectedGenderIndex = -1
    private var mySelectedInterestIndex = -1

    var genderOptions = listOf<InterestOption>()
    var interestOptions = listOf<InterestOption>()

    companion object {
        fun newInstance(): BioDataFragment {
            return BioDataFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentBioDataBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        genderOptions = listOf(
            InterestOption(getString(R.string.lbl_gender_prompt), ""),
            InterestOption(getString(R.string.lbl_female), "F"),
            InterestOption(getString(R.string.lbl_male), "M"),
            InterestOption(getString(R.string.lbl_prefer_not_to_say), "NA"),
        )
        interestOptions = listOf(
            InterestOption(getString(R.string.lbl_akilimo_interest_prompt), ""),
            InterestOption(getString(R.string.lbl_interest_farmer), "farmer"),
            InterestOption(getString(R.string.lbl_interest_extension_agent), "extension_agent"),
            InterestOption(getString(R.string.lbl_interest_agronomist), "agronomist"),
            InterestOption(getString(R.string.lbl_interest_curious), "curious")
        )

        val genderAdapter = MySpinnerAdapter(requireContext(), genderOptions.map { it.label })
        val interestAdapter = MySpinnerAdapter(requireContext(), interestOptions.map { it.label })


        binding.apply {

            spnGender.apply {
                adapter = genderAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        mySelectedGenderIndex = position
                        myGender = null
                        if (position > 0) {
                            myGender = genderOptions[position].value
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //do nothing
                    }
                }
            }

            spnInterest.apply {
                adapter = interestAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        mySelectedInterestIndex = position
                        myAkilimoInterest = null
                        if (position > 0) {
                            myAkilimoInterest = interestOptions[position].value
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        //do nothing
                    }
                }
            }

            ccpCountry.apply {
                setPhoneNumberValidityChangeListener { isValidNumber: Boolean ->
                    phoneIsValid = isValidNumber
                }
                setOnCountryChangeListener {
                    myMobileCode = binding.ccpCountry.selectedCountryCodeWithPlus
                }
                registerCarrierNumberEditText(binding.edtPhone)
            }
        }
    }

    private fun refreshData() {
        try {
            val userProfile = database.profileInfoDao().findOne()
            if (userProfile != null) {
                val myFirstName = userProfile.firstName
                val myLastName = userProfile.lastName
                val myEmail = userProfile.email
                myMobileCode = userProfile.mobileCode
                val myPhoneNumber = userProfile.phoneNumber
                myGender = userProfile.gender
                myAkilimoInterest = userProfile.akilimoInterest

                mySelectedGenderIndex = userProfile.selectedGenderIndex
                mySelectedInterestIndex = userProfile.selectedInterestIndex
                binding.apply {
                    edtFirstName.setText(myFirstName)
                    edtLastName.setText(myLastName)
                    edtEmail.setText(myEmail)

                    if (!myPhoneNumber.isNullOrEmpty()) {
                        ccpCountry.fullNumber = myPhoneNumber
                    }

                    if (mySelectedGenderIndex in genderOptions.indices) {
                        spnGender.setSelection(mySelectedGenderIndex)
                    }

                    if (mySelectedInterestIndex in interestOptions.indices) {
                        spnInterest.setSelection(mySelectedInterestIndex)
                    }
                }
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


        val myFirstName = binding.edtFirstName.text.toString()
        val myLastName = binding.edtLastName.text.toString()
        val myEmail = binding.edtEmail.text.toString().trim { it <= ' ' }
        val userEnteredNumber = binding.edtPhone.text.toString()
        val myPhoneNumber = binding.ccpCountry.fullNumber
        val myMobileCode = binding.ccpCountry.selectedCountryCodeWithPlus

        if (TextUtils.isEmpty(myFirstName)) {
            errorMessage = getString(R.string.lbl_first_name_req)
            binding.edtFirstName.error = errorMessage
            return
        }

        if (myLastName.isEmpty()) {
            errorMessage = getString(R.string.lbl_last_name_req)
            binding.edtLastName.error = errorMessage
            return
        }


        if (myPhoneNumber.isNullOrEmpty() && userEnteredNumber.isEmpty()) {
            if (!phoneIsValid) {
                errorMessage = getString(R.string.lbl_valid_number_req)
                binding.edtPhone.error = errorMessage
                return
            }
        }

        if (myEmail.isNotEmpty()) {
            if (!validationHelper.isValidEmail(myEmail)) {
                errorMessage = getString(R.string.lbl_valid_email_req)
                binding.edtEmail.error = errorMessage
                return
            }
        }

        if (myGender.isNullOrEmpty()) {
            errorMessage = getString(R.string.lbl_gender_prompt)
            return
        }


        if (myAkilimoInterest.isNullOrEmpty()) {
            errorMessage = getString(R.string.lbl_akilimo_interest_prompt)
            return
        }


        try {
            val userProfile = database.profileInfoDao().findOne() ?: UserProfile()

            userProfile.apply {
                firstName = myFirstName
                lastName = myLastName
                gender = myGender
                akilimoInterest = myAkilimoInterest
                email = myEmail
                mobileCode = myMobileCode
                phoneNumber = myPhoneNumber
                selectedGenderIndex = mySelectedGenderIndex
                selectedInterestIndex = mySelectedInterestIndex
                deviceToken = sessionManager.getDeviceToken()
            }
            val profileId = userProfile.profileId
            if (profileId != null) {
                database.profileInfoDao().update(userProfile)
            } else {
                database.profileInfoDao().insert(userProfile)
            }

        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }


    override fun verifyStep(): VerificationError? {
        saveBioData()
        if (!TextUtils.isEmpty(errorMessage)) {
            return VerificationError(errorMessage)
        }
        return null
    }

    override fun onSelected() {
        refreshData()
    }

    override fun onError(error: VerificationError) {
    }
}
