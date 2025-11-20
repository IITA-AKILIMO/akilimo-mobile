package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentPlantingDateBinding
import com.akilimo.mobile.dto.PlantingFlexOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.components.CustomDatePicker
import com.akilimo.mobile.utils.DateHelper
import com.akilimo.mobile.utils.DateHelper.olderThanCurrent
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch
import java.time.LocalDate

class PlantingDateFragment : BaseStepFragment<FragmentPlantingDateBinding>() {

    companion object {
        fun newInstance() = PlantingDateFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo
    private var plantingDate: LocalDate? = null
    private var harvestDate: LocalDate? = null


    private var flexOptions: List<PlantingFlexOption> = emptyList()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPlantingDateBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())

        flexOptions = listOf(
            PlantingFlexOption(getString(R.string.lbl_no_flexibility), 0),
            PlantingFlexOption(getString(R.string.lbl_one_month_window), 1),
            PlantingFlexOption(getString(R.string.lbl_two_month_window), 2),
        )

        val plantingAdapter = ValueOptionAdapter(requireContext(), flexOptions)
        val harvestAdapter = ValueOptionAdapter(requireContext(), flexOptions)

        binding.lytPlantingSectionFragment.apply {
            dropPlantingFlex.setAdapter(plantingAdapter)
            dropPlantingFlex.setOnItemClickListener { _, _, position, _ ->
                val selected = plantingAdapter.getItem(position) ?: return@setOnItemClickListener
                dropPlantingFlex.setText(selected.displayLabel, false)
            }

            dropHarvestFlex.setAdapter(harvestAdapter)
            dropHarvestFlex.setOnItemClickListener { _, _, position, _ ->
                val selected = harvestAdapter.getItem(position) ?: return@setOnItemClickListener
                dropHarvestFlex.setText(selected.displayLabel, false)
            }

            switchFlexibleDates.setOnCheckedChangeListener { _, isChecked ->
                cardFlexOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            inputPlantingDate.setOnClickListener { showPlantingDatePicker() }
            inputHarvestDate.setOnClickListener { showHarvestDatePicker() }
        }
    }

    private fun showPlantingDatePicker() {
        val selectedFlexLabel = binding.lytPlantingSectionFragment.dropPlantingFlex.text.toString()
        val flex = flexOptions.find { it.displayLabel == selectedFlexLabel }?.valueOption ?: 0

        val minDate = LocalDate.now().minusMonths(4 + flex)
        val maxDate = LocalDate.now().plusMonths(12 + flex)

        CustomDatePicker(
            context = requireContext(),
            fragmentManager = parentFragmentManager,
            title = getString(R.string.lbl_planting_date),
            minDate = minDate,
            maxDate = maxDate,
            initialDate = plantingDate
        ) { selected ->
            plantingDate = selected
            val alreadyPlanted = olderThanCurrent(selected)

            binding.lytPlantingSectionFragment.apply {
                inputPlantingDate.setText(DateHelper.formatToString(selected))
                dropPlantingFlex.isEnabled = !alreadyPlanted
                if (alreadyPlanted) {
                    dropPlantingFlex.setText(
                        flexOptions.first { it.valueOption == 0L }.displayLabel,
                        false
                    )
                }
                inputHarvestDate.text = null
                harvestDate = null
            }
        }.show()
    }
    private fun showHarvestDatePicker() {
        val planting = plantingDate ?: run {
            Toast.makeText(
                requireContext(),
                getString(R.string.lbl_planting_date_prompt),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val selectedFlexLabel = binding.lytPlantingSectionFragment.dropHarvestFlex.text.toString()
        val flex = flexOptions.find { it.displayLabel == selectedFlexLabel }?.valueOption ?: 0

        val minDate = planting.plusMonths((8 - flex))
        val maxDate = planting.plusMonths((16 + flex))

        CustomDatePicker(
            context = requireContext(),
            fragmentManager = parentFragmentManager,
            title = getString(R.string.lbl_harvesting_date),
            minDate = minDate,
            maxDate = maxDate,
            initialDate = harvestDate
        ) { selected ->
            harvestDate = selected
            binding.lytPlantingSectionFragment.inputHarvestDate.setText(
                DateHelper.formatToString(selected)
            )
        }.show()
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            userRepository.getUser(sessionManager.akilimoUser)?.let { user ->
                binding.lytPlantingSectionFragment.apply {
                    user.plantingDate?.let {
                        plantingDate = it
                        inputPlantingDate.setText(DateHelper.formatToString(plantingDate))
                    }
                    user.harvestDate?.let {
                        harvestDate = it
                        inputHarvestDate.setText(DateHelper.formatToString(harvestDate))
                    }

                    if (user.plantingFlex > 0 || user.harvestFlex > 0) {
                        switchFlexibleDates.isChecked = true
                        layoutFlexOptions.visibility = View.VISIBLE
                    }
                    val plantingFlexLabel =
                        flexOptions.find { it.valueOption == user.plantingFlex }?.displayLabel.orEmpty()
                    val harvestFlexLabel =
                        flexOptions.find { it.valueOption == user.harvestFlex }?.displayLabel.orEmpty()

                    dropPlantingFlex.setText(plantingFlexLabel, false)
                    dropHarvestFlex.setText(harvestFlexLabel, false)

                }
            }
        }
    }

    override fun verifyStep(): VerificationError? = with(binding.lytPlantingSectionFragment) {
        lytPlantingDate.error = null
        lytHarvestDate.error = null

        if (plantingDate == null) {
            val message = getString(R.string.lbl_planting_date_prompt)
            lytPlantingDate.error = message
            return VerificationError(message)
        }
        if (harvestDate == null) {
            val message = getString(R.string.lbl_harvest_date_prompt)
            lytHarvestDate.error = message
            return VerificationError(message)
        }

        val harvestFlexLabel = dropHarvestFlex.text.toString()
        val harvestFlex = flexOptions.find { it.displayLabel == harvestFlexLabel }?.valueOption ?: 0

        val plantingFlexLabel = dropPlantingFlex.text.toString()
        val plantingFlex =
            flexOptions.find { it.displayLabel == plantingFlexLabel }?.valueOption ?: 0


        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser()
            user.plantingDate = plantingDate
            user.harvestDate = harvestDate
            user.plantingFlex = plantingFlex
            user.harvestFlex = harvestFlex
            userRepository.saveOrUpdateUser(user, sessionManager.akilimoUser)
        }

        return null
    }
}