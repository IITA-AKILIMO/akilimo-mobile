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
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.utils.enums.EnumCountry
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry

class AreaUnitFragment : BindBaseStepFragment<FragmentAreaUnitBinding>() {

    private var _areaUnit: String = EnumAreaUnit.ACRE.name
    private var oldAreaUnit: String? = ""
    private var areaUnitDisplay = ""

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
                _areaUnit = EnumAreaUnit.ACRE.name
                areaUnitDisplay = getString(R.string.lbl_acre)
            }

            binding.rbUnitHa -> {
                _areaUnit = EnumAreaUnit.HA.name
                areaUnitDisplay = getString(R.string.lbl_ha)
            }

            binding.rbUnitAre -> {
                _areaUnit = EnumAreaUnit.ARE.name
                areaUnitDisplay = getString(R.string.lbl_are)
            }

            else -> {
                _areaUnit = EnumAreaUnit.ACRE.name
            }
        }

        try {
            val dao = database.mandatoryInfoDao()
            val mandatoryInfo = dao.findOne() ?: MandatoryInfo()
            mandatoryInfo.apply {
                areaUnit = _areaUnit
                displayAreaUnit = areaUnitDisplay
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
            _areaUnit = EnumAreaUnit.ACRE.name
            if (mandatoryInfo != null) {
                _areaUnit = mandatoryInfo.areaUnit
                oldAreaUnit = mandatoryInfo.oldAreaUnit
            }

            val matchedOption = when (_areaUnit) {
                EnumAreaUnit.ACRE.name -> binding.rbUnitAcre.id
                EnumAreaUnit.ARE.name -> binding.rbUnitAre.id
                EnumAreaUnit.HA.name -> binding.rbUnitHa.id
                EnumAreaUnit.M2.name -> binding.rbUnitSqm.id
                else -> -1
            }
            binding.rdgAreaUnit.check(matchedOption)

            profileInfo?.countryCode?.let { code ->
                if (code == EnumCountry.Rwanda.countryCode()) {
                    binding.rbUnitAre.visibility = View.VISIBLE
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
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
