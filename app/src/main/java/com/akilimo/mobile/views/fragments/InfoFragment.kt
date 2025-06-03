package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentInfoBinding
import com.akilimo.mobile.inherit.BaseStepFragment
import com.stepstone.stepper.VerificationError

class InfoFragment : BaseStepFragment() {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): InfoFragment {
            return InfoFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chkAgreeTerms.setOnCheckedChangeListener { compoundButton: CompoundButton?, checked: Boolean ->
            sessionManager.setDisclaimerRead(checked)
        }
    }

    override fun verifyStep(): VerificationError? {
        if (!sessionManager.getDisclaimerRead()) {
            return VerificationError(getString(R.string.lbl_agree_to_disclaimer))
        }
        return null
    }

    override fun onSelected() {
    }

    override fun onError(error: VerificationError) {
    }
}
