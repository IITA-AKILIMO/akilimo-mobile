package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentAreaUnitBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.viewmodels.AreaUnitViewModel
import com.akilimo.mobile.viewmodels.factory.AreaUnitViewModelFactory
import com.stepstone.stepper.VerificationError

class AreaUnitFragment : BindBaseStepFragment<FragmentAreaUnitBinding>() {

    private val viewModel: AreaUnitViewModel by viewModels {
        AreaUnitViewModelFactory(requireActivity().application)
    }

    companion object {
        fun newInstance() = AreaUnitFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAreaUnitBinding = FragmentAreaUnitBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {

        setupObservers()
        errorMessage = getString(R.string.lbl_area_unit_prompt)
        with(binding) {
            rdgAreaUnit.setOnCheckedChangeListener { _, _ -> handleUnitSelection() }
        }
    }

    override fun setupObservers() {
        viewModel.areaUnit.observe(viewLifecycleOwner) { unit ->
            val matchedOption = when (unit) {
                EnumAreaUnit.ACRE.name -> binding.rbUnitAcre.id
                EnumAreaUnit.ARE.name -> binding.rbUnitAre.id
                EnumAreaUnit.HA.name -> binding.rbUnitHa.id
                EnumAreaUnit.SQM.name -> binding.rbUnitSqm.id
                else -> -1
            }
            if (matchedOption != -1) {
                binding.rdgAreaUnit.check(matchedOption)
            }
        }

        viewModel.showAreUnit.observe(viewLifecycleOwner) { visible ->
            binding.rbUnitAre.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }


    private fun handleUnitSelection() {
        val selectedId = binding.rdgAreaUnit.checkedRadioButtonId
        val (unit, displayName) = when (binding.root.findViewById<View>(selectedId)) {
            binding.rbUnitAcre -> EnumAreaUnit.ACRE to getString(R.string.lbl_acre)
            binding.rbUnitHa -> EnumAreaUnit.HA to getString(R.string.lbl_ha)
            binding.rbUnitAre -> EnumAreaUnit.ARE to getString(R.string.lbl_are)
            binding.rbUnitSqm -> EnumAreaUnit.SQM to getString(R.string.lbl_sqm)
            else -> EnumAreaUnit.ACRE to getString(R.string.lbl_acre)
        }

        viewModel.updateUnit(unit, displayName)
    }

    override fun onSelected() {
        viewModel.loadData()
    }

    override fun verifyStep(): VerificationError? {
        return if (viewModel.areaUnit.value.isNullOrEmpty()) {
            VerificationError(errorMessage)
        } else null
    }


}
