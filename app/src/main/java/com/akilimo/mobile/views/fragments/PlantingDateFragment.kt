package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.akilimo.mobile.databinding.FragmentPlantingHarvestDateBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.viewmodels.PlantingDateViewModel
import com.akilimo.mobile.viewmodels.factory.PlantingDateViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.DateDialogPickerFragment
import com.stepstone.stepper.VerificationError


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 */
class PlantingDateFragment : BindBaseStepFragment<FragmentPlantingHarvestDateBinding>() {

    private val viewModel: PlantingDateViewModel by viewModels {
        PlantingDateViewModelFactory(requireActivity().application)
    }

    companion object {
        fun newInstance(): PlantingDateFragment {
            return PlantingDateFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPlantingHarvestDateBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {

        binding.plantingBtnPickDate.setOnClickListener { _: View? ->
            binding.harvestBtnPickDate.isEnabled = false
            DateDialogPickerFragment.newInstanceForPlanting()
                .show(parentFragmentManager, "PlantingDatePicker")
        }

        binding.harvestBtnPickDate.setOnClickListener { _: View? ->
            DateDialogPickerFragment.newInstanceForHarvest(viewModel.plantingDate.value ?: "")
                .show(parentFragmentManager, "HarvestDatePicker")
        }

        setupDateResultListeners()
        setupObservers()
    }

    private fun setupDateResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            DateDialogPickerFragment.PLANTING_RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val date = bundle.getString("selectedDate") ?: ""
            viewModel.setPlantingDate(date)
        }

        parentFragmentManager.setFragmentResultListener(
            DateDialogPickerFragment.HARVEST_RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val date = bundle.getString("selectedDate") ?: ""
            viewModel.setHarvestDate(date)
        }
    }

    override fun setupObservers() {
        viewModel.plantingDate.observe(viewLifecycleOwner) { plantingDate ->
            binding.plantingDateLabel.text = plantingDate
            binding.harvestBtnPickDate.isEnabled = plantingDate.isNotEmpty()
        }

        viewModel.harvestDate.observe(viewLifecycleOwner) { harvestDate ->
            binding.harvestDateLabel.text = harvestDate
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onSelected() {
        viewModel.loadInitialDates()
    }
    override fun verifyStep(): VerificationError? {
        viewModel.saveSchedule()

        val error = viewModel.errorMessage.value
        return if (error.isNullOrBlank()) {
            null
        } else {
            showCustomWarningDialog(error)
            VerificationError(error)
        }
    }
}
