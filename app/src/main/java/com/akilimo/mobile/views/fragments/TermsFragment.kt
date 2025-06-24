package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentInfoBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.stepstone.stepper.VerificationError

class TermsFragment : BindBaseStepFragment<FragmentInfoBinding>() {

    companion object {
        fun newInstance(): TermsFragment {
            return TermsFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentInfoBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.chkAgreeTerms.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
            preferenceManager.termsRead = checked
        }
    }

    override fun setupObservers() {
        TODO("Not yet implemented")
    }
    override fun verifyStep(): VerificationError? {
        if (!preferenceManager.termsRead) {
            return VerificationError(getString(R.string.lbl_agree_to_disclaimer))
        }
        return null
    }
}
