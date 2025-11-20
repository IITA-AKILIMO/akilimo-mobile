package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentDisclaimerBinding
import com.stepstone.stepper.VerificationError

/**
 * A simple [Fragment] subclass.
 * Use the [DisclaimerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DisclaimerFragment : BaseStepFragment<FragmentDisclaimerBinding>() {
    companion object {
        fun newInstance() = DisclaimerFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDisclaimerBinding {
        return FragmentDisclaimerBinding.inflate(inflater, container, false)
    }

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.chkAgreeTerms.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
            sessionManager.disclaimerRead = checked
        }
    }

    override fun verifyStep(): VerificationError? {
        if (!sessionManager.disclaimerRead) {
            return VerificationError(getString(R.string.lbl_agree_to_disclaimer))
        }
        return null
    }
}