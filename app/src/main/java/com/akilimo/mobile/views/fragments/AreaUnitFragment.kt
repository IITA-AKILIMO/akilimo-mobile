package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentAreaUnitBinding
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumAreaUnits
import com.akilimo.mobile.utils.enums.EnumCountry
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class AreaUnitFragment : BindBaseStepFragment<FragmentAreaUnitBinding>() {

    private var _areaUnit: String = EnumAreaUnits.ACRE.name
    private var oldAreaUnit: String? = ""
    private var areaUnitDisplay = ""
    private var _areaUnitRadioIndex: Int = -1

    companion object {
        fun newInstance() = AreaUnitFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAreaUnitBinding = FragmentAreaUnitBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        errorMessage = getString(R.string.lbl_area_unit_prompt)

        with(binding) {
            rdgAreaUnit.setOnCheckedChangeListener { _, _ -> handleUnitSelection() }
        }
    }

    private fun handleUnitSelection() {
        val context = requireContext()
        val selectedId = binding.rdgAreaUnit.checkedRadioButtonId

        when (binding.root.findViewById<View>(selectedId)) {
            binding.rbUnitAcre -> {
                _areaUnit = EnumAreaUnits.ACRE.name
                areaUnitDisplay = getString(R.string.lbl_acre)
                _areaUnitRadioIndex = selectedId
            }

            binding.rbUnitHa -> {
                _areaUnit = EnumAreaUnits.HA.name
                areaUnitDisplay = getString(R.string.lbl_ha)
                _areaUnitRadioIndex = selectedId
            }

            binding.rbUnitAre -> {
                _areaUnit = EnumAreaUnits.ARE.name
                areaUnitDisplay = getString(R.string.lbl_are)
                _areaUnitRadioIndex = selectedId
            }

            else -> {
                _areaUnit = EnumAreaUnits.ACRE.name
                _areaUnitRadioIndex = -1
            }
        }

        try {
            val dao = database.mandatoryInfoDao()
            val mandatoryInfo = dao.findOne() ?: MandatoryInfo()
            mandatoryInfo.apply {
                areaUnit = _areaUnit
                displayAreaUnit = areaUnitDisplay
                areaUnitRadioIndex = _areaUnitRadioIndex
                if (id != null) dao.update(this) else dao.insert(this)
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message.orEmpty(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshData() {
        try {
            val mandatoryInfo = database.mandatoryInfoDao().findOne()
            val profileInfo = database.profileInfoDao().findOne()

            if (mandatoryInfo != null) {
                _areaUnit = mandatoryInfo.areaUnit
                oldAreaUnit = mandatoryInfo.oldAreaUnit
                _areaUnitRadioIndex = mandatoryInfo.areaUnitRadioIndex
            } else {
                _areaUnit = EnumAreaUnits.ACRE.name
                _areaUnitRadioIndex = -1
            }

            binding.rdgAreaUnit.check(_areaUnitRadioIndex)

            profileInfo?.countryCode?.let { code ->
                if (code == EnumCountry.Rwanda.countryCode()) {
                    binding.rbUnitAre.visibility = View.VISIBLE
                    if (oldAreaUnit == EnumAreaUnits.ARE.unitName(requireContext())) {
                        binding.rbUnitAre.isChecked = true
                    }
                }
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }

    override fun onSelected() {
        refreshData()
    }

    override fun verifyStep(): VerificationError? {
        return if (_areaUnit.isEmpty()) {
            VerificationError(errorMessage)
        } else null
    }


}
