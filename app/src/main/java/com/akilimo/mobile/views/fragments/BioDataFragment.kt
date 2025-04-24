package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentBioDataBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.ValidationHelper
import com.google.android.material.textfield.TextInputEditText
import com.hbb20.CountryCodePicker
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry


class BioDataFragment : BaseStepFragment() {
    private var userProfile: UserProfile? = null

    private val validationHelper: ValidationHelper by lazy { ValidationHelper() }

    private var phoneIsValid = true

    private var _binding: FragmentBioDataBinding? = null
    private val binding get() = _binding!!

    var genderSpinner: Spinner? = null
    var interestSpinner: Spinner? = null
    var edtFirstName: TextInputEditText? = null
    var edtLastName: TextInputEditText? = null

    //    TextInputEditText edtFamName;
    var edtEmail: TextInputEditText? = null
    var edtPhone: TextInputEditText? = null
    var ccp: CountryCodePicker? = null

    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var mobileCode: String? = null
    private var fullMobileNumber: String? = null
    private var userEnteredNumber: String? = null
    private var gender: String? = null
    private var akilimoInterest: String? = null
    private var selectedGenderIndex = -1
    private var selectedInterestIndex = -1

    private var rememberUserInfo = false

    companion object {
        fun newInstance(): BioDataFragment {
            return BioDataFragment()
        }
    }
    
    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBioDataBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        genderSpinner = binding.genderSpinner
        interestSpinner = binding.interestSpinner
        edtFirstName = binding.edtFirstName
        edtLastName = binding.edtLastName
        edtEmail = binding.edtEmail
        edtPhone = binding.edtPhone
        ccp = binding.ccp

        val genderStrings: MutableList<String> = ArrayList()
        genderStrings.add(0, this.getString(R.string.lbl_gender_prompt))
        genderStrings.add(this.getString(R.string.lbl_female))
        genderStrings.add(this.getString(R.string.lbl_male))
        genderStrings.add(this.getString(R.string.lbl_prefer_not_to_say))

        val genderAdapter: SpinnerAdapter =
            ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, genderStrings)
        genderSpinner!!.adapter = genderAdapter

        val interestStrings: MutableList<String> = ArrayList()
        interestStrings.add(0, this.getString(R.string.lbl_akilimo_interest_prompt))
        interestStrings.add(this.getString(R.string.lbl_interest_farmer))
        interestStrings.add(this.getString(R.string.lbl_interest_extension_agent))
        interestStrings.add(this.getString(R.string.lbl_interest_agronomist))
        interestStrings.add(this.getString(R.string.lbl_interest_curious))
        val interestAdapter: SpinnerAdapter =
            ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, interestStrings)
        interestSpinner!!.adapter = interestAdapter


        genderSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedGenderIndex = position
                gender = null
                if (position > 0) {
                    gender = genderStrings[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        interestSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedInterestIndex = position
                akilimoInterest = null
                if (position > 0) {
                    akilimoInterest = interestStrings[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        ccp!!.setPhoneNumberValidityChangeListener { isValidNumber: Boolean ->
            phoneIsValid = isValidNumber
        }

        ccp!!.setOnCountryChangeListener { mobileCode = ccp!!.selectedCountryCodeWithPlus }
        ccp!!.registerCarrierNumberEditText(edtPhone)

        binding.chkRememberDetails.setOnCheckedChangeListener { _: CompoundButton?, rememberInfo: Boolean ->
            rememberUserInfo = rememberInfo
            sessionManager.setRememberUserInfo(rememberUserInfo)
        }
    }

    private fun refreshData() {
        try {
            userProfile = database.profileInfoDao().findOne()
            rememberUserInfo = sessionManager.getRememberUserInfo()
            if (userProfile != null) {
                firstName = userProfile!!.firstName
                lastName = userProfile!!.lastName
                email = userProfile!!.email
                mobileCode = userProfile!!.mobileCode
                fullMobileNumber = userProfile!!.fullMobileNumber
                gender = userProfile!!.gender
                akilimoInterest = userProfile!!.akilimoInterest

                selectedGenderIndex = userProfile!!.selectedGenderIndex
                selectedInterestIndex = userProfile!!.selectedInterestIndex

                edtFirstName!!.setText(firstName)
                edtLastName!!.setText(lastName)
                edtEmail!!.setText(email)
                if (!TextUtils.isEmpty(fullMobileNumber)) {
                    ccp!!.fullNumber = fullMobileNumber
                }

                genderSpinner!!.setSelection(selectedGenderIndex)
                binding.chkRememberDetails.isChecked = rememberUserInfo
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    private fun saveBioData() {
        edtFirstName!!.error = null
        edtLastName!!.error = null
        edtEmail!!.error = null
        errorMessage = ""

        firstName = edtFirstName!!.text.toString()
        lastName = edtLastName!!.text.toString()
        email = edtEmail!!.text.toString().trim { it <= ' ' }
        userEnteredNumber = edtPhone!!.text.toString()
        fullMobileNumber = ccp!!.fullNumber
        mobileCode = ccp!!.selectedCountryCodeWithPlus

        if (TextUtils.isEmpty(firstName)) {
            errorMessage = this.getString(R.string.lbl_first_name_req)
            edtFirstName!!.error = errorMessage
            return
        }

        if (TextUtils.isEmpty(lastName)) {
            errorMessage = this.getString(R.string.lbl_last_name_req)
            edtLastName!!.error = errorMessage
            return
        }


        if (!TextUtils.isEmpty(fullMobileNumber) && !TextUtils.isEmpty(userEnteredNumber)) {
            if (!phoneIsValid) {
                errorMessage = this.getString(R.string.lbl_valid_number_req)
                edtPhone!!.error = errorMessage
                return
            } else {
                edtPhone!!.error = null
            }
        }

        if (!validationHelper.isValidEmail(email!!) && !TextUtils.isEmpty(email)) {
            errorMessage = this.getString(R.string.lbl_valid_email_req)
            edtEmail!!.error = errorMessage
            return
        }

        if (TextUtils.isEmpty(gender)) {
            errorMessage = this.getString(R.string.lbl_gender_prompt)
            return
        }


        if (TextUtils.isEmpty(akilimoInterest)) {
            errorMessage = this.getString(R.string.lbl_akilimo_interest_prompt)
            return
        }


        try {
            if (userProfile == null) {
                userProfile = UserProfile()
            }
            userProfile!!.firstName = firstName
            userProfile!!.lastName = lastName
            userProfile!!.gender = gender
            userProfile!!.akilimoInterest = akilimoInterest
            userProfile!!.email = email
            userProfile!!.mobileCode = mobileCode
            userProfile!!.fullMobileNumber = fullMobileNumber
            userProfile!!.selectedGenderIndex = selectedGenderIndex
            userProfile!!.selectedInterestIndex = selectedInterestIndex

            userProfile!!.deviceToken = sessionManager.getDeviceToken()

            userProfile!!.userName = userProfile!!.names()

            if (userProfile!!.profileId != null) {
                database.profileInfoDao().update(userProfile!!)
            } else {
                database.profileInfoDao().insert(userProfile!!)
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
