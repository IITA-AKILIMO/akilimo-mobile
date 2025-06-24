package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.adapters.MySpinnerAdapter
import com.akilimo.mobile.data.indexOfValue
import com.akilimo.mobile.databinding.FragmentBioDataBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.viewmodels.BioDataViewModel
import com.akilimo.mobile.viewmodels.factory.BioDataViewModelFactory
import com.stepstone.stepper.VerificationError

class BioDataFragment : BindBaseStepFragment<FragmentBioDataBinding>() {

    private val viewModel: BioDataViewModel by viewModels {
        BioDataViewModelFactory(
            requireActivity().application, preferenceManager
        )
    }


    private var isPhoneValid = true

    companion object {
        fun newInstance(): BioDataFragment = BioDataFragment()
    }


    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        FragmentBioDataBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupObservers()
        setupSpinners()
        setupPhoneValidation()

        viewModel.loadUserProfile()
    }

    override fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.apply {
                edtFirstName.setText(profile.firstName)
                edtLastName.setText(profile.lastName)
                edtEmail.setText(profile.email)
                ccpCountry.fullNumber = profile.phoneNumber.orEmpty()
                spnGender.setSelection(viewModel.genderOptions.indexOfValue(profile.gender))
                spnInterest.setSelection(viewModel.interestOptions.indexOfValue(profile.akilimoInterest))
            }
        }
    }

    private fun setupSpinners() {
        binding.spnGender.apply {
            adapter =
                MySpinnerAdapter(requireContext(), viewModel.genderOptions.map { it.displayLabel })
        }

        binding.spnInterest.apply {
            adapter = MySpinnerAdapter(
                requireContext(),
                viewModel.interestOptions.map { it.displayLabel })
        }
    }

    private fun setupPhoneValidation() {
        binding.ccpCountry.apply {
            setPhoneNumberValidityChangeListener { isValid -> isPhoneValid = isValid }
            setOnCountryChangeListener { }
            registerCarrierNumberEditText(binding.edtPhone)
        }
    }

    override fun verifyStep(): VerificationError? {
        val gender =
            viewModel.genderOptions.getOrNull(binding.spnGender.selectedItemPosition)?.value.orEmpty()
        val interest =
            viewModel.interestOptions.getOrNull(binding.spnInterest.selectedItemPosition)?.value.orEmpty()
        val phone = binding.ccpCountry.fullNumber
        val code = binding.ccpCountry.selectedCountryCodeWithPlus

        errorMessage = viewModel.saveUserProfile(
            binding.edtFirstName.text.toString(),
            binding.edtLastName.text.toString(),
            binding.edtEmail.text.toString().trim(),
            phone,
            code,
            gender,
            interest
        ) ?: ""

        return if (errorMessage.isNotEmpty()) VerificationError(errorMessage) else null
    }

    override fun onSelected() {
        viewModel.loadUserProfile()
    }
}
