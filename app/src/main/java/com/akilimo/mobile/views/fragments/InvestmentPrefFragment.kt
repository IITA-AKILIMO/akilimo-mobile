package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.akilimo.mobile.data.RiskOption
import com.akilimo.mobile.data.indexOfValue
import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class InvestmentPrefFragment : BindBaseStepFragment<FragmentInvestmentPrefBinding>() {

    private var selectedRiskValue: String = EnumInvestmentPref.RARELY.name

    private val riskOptions: List<RiskOption> by lazy {
        EnumInvestmentPref.entries
            .map {
                RiskOption(
                    value = it.name,
                    displayLabel = it.prefName(requireContext()),
                    riskAtt = it.riskAtt()
                )
            }
    }

    companion object {
        fun newInstance() = InvestmentPrefFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentInvestmentPrefBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.rdgRiskAttitude.setOnCheckedChangeListener { group, _ ->
            val selectedRadio = group.findViewById<RadioButton>(group.checkedRadioButtonId)
            val tagValue = selectedRadio?.tag as? String ?: return@setOnCheckedChangeListener

            selectedRiskValue = tagValue
            val index = riskOptions.indexOfValue(selectedRiskValue)
            updateInvestmentPref(index)
        }

        binding.rdgRiskAttitude.removeAllViews()
        riskOptions.forEach { option ->
            RadioButton(requireContext()).apply {
                tag = option.value
                text = option.displayLabel
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                binding.rdgRiskAttitude.addView(this)

                if (option.value == selectedRiskValue) {
                    isChecked = true
                }
            }
        }
    }

    private fun updateInvestmentPref(index: Int) {
        try {
            val profile = database.profileInfoDao().findOne() ?: return
            profile.riskAtt = index
            if (profile.profileId != null) {
                database.profileInfoDao().insert(profile)
            }
        } catch (ex: Exception) {
            Toast.makeText(requireContext(), ex.localizedMessage, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? =
        if (selectedRiskValue.isEmpty()) VerificationError("Please select an option") else null
}