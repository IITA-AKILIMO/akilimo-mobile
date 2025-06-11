package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentInfoBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.stepstone.stepper.VerificationError

class InfoFragment : BindBaseStepFragment<FragmentInfoBinding>() {

    companion object {
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentInfoBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.chkAgreeTerms.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
            sessionManager.setDisclaimerRead(checked)
        }
    }

    override fun verifyStep(): VerificationError? {
        if (!sessionManager.getDisclaimerRead()) {
            return VerificationError(getString(R.string.lbl_agree_to_disclaimer))
        }
        return null
    }
}
