package com.akilimo.mobile.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class InvestmentPrefFragment : BindBaseStepFragment<FragmentInvestmentPrefBinding>() {

    private var myRiskName: String = ""
    private var myRiskAtt = 0
    private var myRiskRadioIndex = -1
    private var riskAttitudes: Array<String> = arrayOf()

    companion object {
        fun newInstance() = InvestmentPrefFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        riskAttitudes = arrayOf(
            EnumInvestmentPref.Rarely.prefName(context),
            EnumInvestmentPref.Sometimes.prefName(context),
            EnumInvestmentPref.Often.prefName(context)
        )
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentInvestmentPrefBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addRiskRadioButtons(riskAttitudes)

        binding.apply {
            binding.rdgRiskAttitude.setOnCheckedChangeListener { radioGroup, _ ->
                val radioButton = root.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                    ?: return@setOnCheckedChangeListener
                val itemTagIndex = radioButton.tag as Int
                myRiskAtt = itemTagIndex.coerceIn(0, 2)
                myRiskName = riskAttitudes[myRiskAtt]
                updateInvestmentPref(myRiskAtt, radioButton.id)
            }

        }
    }

    private fun addRiskRadioButtons(risks: Array<String>) {
        binding.rdgRiskAttitude.removeAllViews()
        risks.forEachIndexed { index, riskText ->
            RadioButton(activity).apply {
                id = index
                tag = index
                text = riskText
                binding.rdgRiskAttitude.addView(this)
            }
        }
    }

    private fun updateInvestmentPref(investmentPref: Int, riskRadioIndex: Int) {
        try {
            val userProfile = database.profileInfoDao().findOne()
            userProfile?.let {
                it.selectedRiskIndex = riskRadioIndex
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
        try {
            val userProfile = database.profileInfoDao().findOne()
            userProfile?.let {
                myRiskAtt = it.riskAtt
                myRiskRadioIndex = it.selectedRiskIndex
                binding.rdgRiskAttitude.check(myRiskRadioIndex)
                myRiskName = riskAttitudes[myRiskAtt]
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }
}