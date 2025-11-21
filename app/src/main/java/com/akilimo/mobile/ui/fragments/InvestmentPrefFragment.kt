package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding
import com.akilimo.mobile.dto.InvestmentPrefOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumInvestmentPref
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [InvestmentPrefFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InvestmentPrefFragment : BaseStepFragment<FragmentInvestmentPrefBinding>() {
    companion object {
        fun newInstance() = InvestmentPrefFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo

    private var investmentPrefs: List<InvestmentPrefOption> = emptyList()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInvestmentPrefBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
        investmentPrefs = listOf(
            InvestmentPrefOption(
                getString(R.string.lbl_investment_pref_prompt),
                EnumInvestmentPref.Prompt
            ),
            InvestmentPrefOption(
                EnumInvestmentPref.Rarely.label(requireContext()),
                EnumInvestmentPref.Rarely
            ),
            InvestmentPrefOption(
                EnumInvestmentPref.Sometimes.label(requireContext()),
                EnumInvestmentPref.Sometimes
            ),
            InvestmentPrefOption(
                EnumInvestmentPref.Often.label(requireContext()),
                EnumInvestmentPref.Often
            ),
        )

        val investmentPrefAdapter = ValueOptionAdapter(requireContext(), investmentPrefs)
        binding.dropInvestmentPref.setAdapter(investmentPrefAdapter)
        binding.dropInvestmentPref.setOnItemClickListener { _, _, position, _ ->
            val selected = investmentPrefAdapter.getItem(position) ?: return@setOnItemClickListener
            binding.dropInvestmentPref.setText(selected.displayLabel, false)
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            user?.let {
                binding.dropInvestmentPref.setText(
                    investmentPrefs.find { opt -> opt.valueOption == it.investmentPref }?.displayLabel,
                    false
                )
            }
        }
    }

    override fun verifyStep(): VerificationError? = with(binding) {
        val selectedLabel = binding.dropInvestmentPref.text.toString()
        val investmentPref = investmentPrefs
            .firstOrNull { it.displayLabel.equals(selectedLabel, ignoreCase = true) }?.valueOption
            ?: EnumInvestmentPref.Prompt

        if (investmentPref.riskLevel() < 0) {
            val message = getString(R.string.lbl_investment_pref_prompt)
            binding.lytInvestmentPref.error = message
            return VerificationError(message)
        }

        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser(
                userName = sessionManager.akilimoUser
            )
            userRepository.saveOrUpdateUser(
                user.copy(
                    investmentPref = investmentPref
                ), sessionManager.akilimoUser
            )
        }

        return null
    }


}