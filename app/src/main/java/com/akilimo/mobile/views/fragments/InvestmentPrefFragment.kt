package com.akilimo.mobile.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class InvestmentPrefFragment : BaseStepFragment() {
    private var _binding: FragmentInvestmentPrefBinding? = null
    private val binding get() = _binding!!

    private var userProfile: UserProfile? = null
    private var myRiskName: String = ""
    private var myRiskAtt = 0
    private var myRiskRadioIndex = -1
    private var investmentPreference: Array<String> = arrayOf()
    private var rememberInvestmentPref = false

    companion object {
        fun newInstance() = InvestmentPrefFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        investmentPreference = arrayOf(
            EnumInvestmentPref.Rarely.prefName(context),
            EnumInvestmentPref.Sometimes.prefName(context),
            EnumInvestmentPref.Often.prefName(context)
        )
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvestmentPrefBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addRiskRadioButtons(investmentPreference)

        binding.apply {
            rdgRiskGroup.setOnCheckedChangeListener { radioGroup, _ ->
                val radioButton = root.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                    ?: return@setOnCheckedChangeListener
                val itemTagIndex = radioButton.tag as Int
                myRiskAtt = itemTagIndex.coerceIn(0, 2)
                myRiskName = investmentPreference[myRiskAtt]
                updateInvestmentPref(myRiskAtt, radioButton.id)
            }

            chkRememberDetails.setOnCheckedChangeListener { _, rememberInfo ->
                sessionManager.setRememberInvestmentPref(rememberInfo)
            }
        }
    }

    private fun addRiskRadioButtons(risks: Array<String>) {
        binding.rdgRiskGroup.removeAllViews()
        risks.forEachIndexed { index, riskText ->
            RadioButton(activity).apply {
                id = index
                tag = index
                text = riskText
                binding.rdgRiskGroup.addView(this)
            }
        }
    }

    private fun updateInvestmentPref(investmentPref: Int, investmentRadioIndex: Int) {
        try {
            userProfile?.let {
                it.selectedRiskIndex = investmentRadioIndex
                it.riskAtt = investmentPref

                if (it.profileId != null) {
                    database.profileInfoDao().update(it)
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? =
        if (myRiskName.isEmpty() != false) VerificationError("Please select an option") else null

    override fun onSelected() {
        refreshData()
    }

    override fun onError(error: VerificationError) {
        // No implementation needed
    }

    private fun refreshData() {
        try {
            val userProfile = database.profileInfoDao().findOne()
            userProfile?.let {
                myRiskAtt = it.riskAtt
                myRiskRadioIndex = it.selectedRiskIndex
                binding.rdgRiskGroup.check(myRiskRadioIndex)
                myRiskName = investmentPreference[myRiskAtt]
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }
}