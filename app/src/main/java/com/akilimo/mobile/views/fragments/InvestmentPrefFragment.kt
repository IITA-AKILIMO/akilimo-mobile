package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.data.RiskOption
import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.akilimo.mobile.viewmodels.InvestmentPrefViewModel
import com.akilimo.mobile.viewmodels.factory.InvestmentPrefViewModelFactory
import com.stepstone.stepper.VerificationError

class InvestmentPrefFragment : BindBaseStepFragment<FragmentInvestmentPrefBinding>() {

    private lateinit var radioButtons: List<RadioButton>
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

    private val viewModel: InvestmentPrefViewModel by viewModels {
        InvestmentPrefViewModelFactory(requireActivity().application, riskOptions)
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
        viewModel.loadInitialSelection()
        setupObservers()
    }

    private fun buildRiskOptionsUI(selectedValue: String) {
        binding.rdgRiskAttitude.removeAllViews()
        radioButtons = emptyList()
        radioButtons = riskOptions.map { option ->
            RadioButton(requireContext()).apply {
                tag = option.value
                text = option.displayLabel
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                isChecked = option.value == selectedValue
                setOnClickListener { viewModel.selectRisk(option) }
                binding.rdgRiskAttitude.addView(this)
            }
        }
    }

    override fun setupObservers() {
        viewModel.selectedRiskValue.observe(viewLifecycleOwner) { selectedValue ->
            if (!::radioButtons.isInitialized) {
                buildRiskOptionsUI(selectedValue)
            } else {
                radioButtons.forEach { it.isChecked = it.tag == selectedValue }
            }
        }
    }


    override fun verifyStep(): VerificationError? =
        if (viewModel.selectedRiskValue.value.isNullOrEmpty()) VerificationError(getString(R.string.please_select_an_option)) else null
}