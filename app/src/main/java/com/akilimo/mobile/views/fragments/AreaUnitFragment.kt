package com.akilimo.mobile.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentAreaUnitBinding
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.enums.EnumAreaUnits
import com.akilimo.mobile.utils.enums.EnumCountry
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import java.util.Locale


class AreaUnitFragment : BaseStepFragment() {
    private var _binding: FragmentAreaUnitBinding? = null
    private val binding get() = _binding!!

    private var mandatoryInfo: MandatoryInfo? = null
    private var areaUnit: String? = "acre"
    private var oldAreaUnit: String? = ""
    private var areaUnitDisplay = "acre"
    private var areaUnitRadioIndex = 0
    private var rememberPreference = false

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAreaUnitBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        errorMessage = context.getString(R.string.lbl_area_unit_prompt)
        binding.rdgAreaUnit.setOnCheckedChangeListener { _: RadioGroup?, radioIndex: Int ->
            when (radioIndex) {
                R.id.rb_unit_acre -> {
                    areaUnitRadioIndex = R.id.rb_unit_acre
                    areaUnitDisplay = context.getString(R.string.lbl_acre)
                    areaUnit = EnumAreaUnits.ACRE.name
                }

                R.id.rb_unit_ha -> {
                    areaUnitRadioIndex = R.id.rb_unit_ha
                    areaUnitDisplay = context.getString(R.string.lbl_ha)
                    areaUnit = EnumAreaUnits.HA.name
                }

                R.id.rb_unit_are -> {
                    areaUnitRadioIndex = R.id.rb_unit_are
                    areaUnitDisplay = context.getString(R.string.lbl_are)
                    areaUnit = EnumAreaUnits.ARE.name
                }

                else -> {
                    areaUnitRadioIndex = -1
                    areaUnit = null
                }
            }
            if (areaUnit == null) {
                return@setOnCheckedChangeListener
            }
            try {
                mandatoryInfo = database.mandatoryInfoDao().findOne()
                if (mandatoryInfo == null) {
                    mandatoryInfo = MandatoryInfo()
                }
                mandatoryInfo!!.areaUnitRadioIndex = areaUnitRadioIndex
                mandatoryInfo!!.areaUnit = areaUnit!!.lowercase(Locale.getDefault())
                mandatoryInfo!!.displayAreaUnit = areaUnitDisplay
                if (mandatoryInfo!!.id != null) {
                    database.mandatoryInfoDao().update(mandatoryInfo!!)
                } else {
                    database.mandatoryInfoDao().insert(mandatoryInfo!!)
                }
            } catch (ex: Exception) {
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun refreshData() {
        try {
            rememberPreference = sessionManager.getRememberAreaUnit()
            mandatoryInfo = database.mandatoryInfoDao().findOne()
            val rdAre = binding.rbUnitAre
            val profileInfo = database.profileInfoDao().findOne()
            if (mandatoryInfo != null) {
                areaUnit = mandatoryInfo!!.areaUnit
                oldAreaUnit = mandatoryInfo!!.oldAreaUnit
                areaUnitRadioIndex = mandatoryInfo!!.areaUnitRadioIndex
                binding.rdgAreaUnit.check(areaUnitRadioIndex)
            } else {
                binding.rdgAreaUnit.check(areaUnitRadioIndex)
                areaUnitRadioIndex = -1
                areaUnit = null
            }

            if (profileInfo != null) {
                val countryCode = profileInfo.countryCode
                if (countryCode == EnumCountry.Rwanda.countryCode()) {
                    //set the are unit radiobutton to visible
                    rdAre.visibility = View.VISIBLE
                    if (oldAreaUnit == EnumAreaUnits.ARE.unitName(requireContext())) {
                        rdAre.isChecked = true
                    }
                }
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? {
        if (areaUnit == null || areaUnit!!.isEmpty()) {
            return VerificationError(errorMessage)
        }
        return null
    }

    override fun onSelected() {
        refreshData()
    }


    override fun onError(error: VerificationError) {
    }

    companion object {
        fun newInstance(): AreaUnitFragment {
            return AreaUnitFragment()
        }
    }
}
