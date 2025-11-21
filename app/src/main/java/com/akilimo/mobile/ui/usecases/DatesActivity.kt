package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityAlternativePlantingScheduleBinding
import com.akilimo.mobile.dto.PlantingFlexOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.components.CustomDatePicker
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.DateHelper
import com.akilimo.mobile.utils.DateHelper.olderThanCurrent
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import java.time.LocalDate

class DatesActivity : BaseActivity<ActivityAlternativePlantingScheduleBinding>() {

    private lateinit var userRepo: AkilimoUserRepo

    private var plantingDate: LocalDate? = null
    private var harvestDate: LocalDate? = null
    private var alternativeDate = false
    private var alreadyPlanted = false
    private var flexOptions: List<PlantingFlexOption> = emptyList()

    override fun inflateBinding() =
        ActivityAlternativePlantingScheduleBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        flexOptions = listOf(
            PlantingFlexOption(getString(R.string.lbl_no_flexibility), 0),
            PlantingFlexOption(getString(R.string.lbl_one_month_window), 1),
            PlantingFlexOption(getString(R.string.lbl_two_month_window), 2),
        )

        setupUI()
        binding.fabSave.hide()
    }

    private fun setupUI() = with(binding.lytPlantingSectionActivity) {
        binding.switchAlternativePlanting.setOnCheckedChangeListener { _, isChecked ->
            alternativeDate = isChecked
            root.visibility = if (isChecked) View.VISIBLE else View.GONE
            toggleFab()
        }

        binding.fabSave.setOnClickListener {
            savePlantingSchedule()
            toggleFab()
        }

        setupFlexSpinner(dropPlantingFlex)
        setupFlexSpinner(dropHarvestFlex)

        switchFlexibleDates.setOnCheckedChangeListener { _, isChecked ->
            cardFlexOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        inputPlantingDate.setOnClickListener { showPlantingDatePicker() }
        inputHarvestDate.setOnClickListener { showHarvestDatePicker() }

        inputPlantingDate.addTextChangedListener { toggleFab() }
        inputHarvestDate.addTextChangedListener { toggleFab() }
    }

    private fun setupFlexSpinner(spinnerInput: MaterialAutoCompleteTextView) {
        val adapter = ValueOptionAdapter(this, flexOptions)
        spinnerInput.setAdapter(adapter)
        spinnerInput.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener
            if (alreadyPlanted && spinnerInput == binding.lytPlantingSectionActivity.dropPlantingFlex) {
                val defaultOption = flexOptions.first { it.valueOption == 0L }
                spinnerInput.setText(defaultOption.displayLabel, false)
                val snackBar = Snackbar.make(
                    binding.root,
                    getString(R.string.lbl_already_planted_text),
                    Snackbar.LENGTH_INDEFINITE
                )
                snackBar.setAction(getString(R.string.lbl_ok)) {
                    snackBar.dismiss()
                }
                snackBar.show()
            } else {
                spinnerInput.setText(selected.displayLabel, false)
            }
            toggleFab()
        }
    }

    override fun observeSyncWorker() {
        safeScope.launch {
            userRepo.getUser(sessionManager.akilimoUser)?.let { user ->
                populateUserData(user)
            }
        }
    }

    private fun populateUserData(user: AkilimoUser) = with(binding.lytPlantingSectionActivity) {
        binding.switchAlternativePlanting.isChecked =
            user.providedAlterNativeDate.also { alternativeDate = it }
        plantingDate =
            user.plantingDate?.also { inputPlantingDate.setText(DateHelper.formatToString(it)) }
        harvestDate =
            user.harvestDate?.also { inputHarvestDate.setText(DateHelper.formatToString(it)) }
        alreadyPlanted = olderThanCurrent(plantingDate)

        if (user.plantingFlex > 0 || user.harvestFlex > 0) {
            switchFlexibleDates.isChecked = true
            layoutFlexOptions.visibility = View.VISIBLE
        }

        dropPlantingFlex.setText(
            flexOptions.find { it.valueOption == user.plantingFlex }?.displayLabel.orEmpty(),
            false
        )
        dropHarvestFlex.setText(
            flexOptions.find { it.valueOption == user.harvestFlex }?.displayLabel.orEmpty(),
            false
        )
    }

    private fun showPlantingDatePicker() {
        val selectedFlexLabel = binding.lytPlantingSectionActivity.dropPlantingFlex.text.toString()
        val flex = flexOptions.find { it.displayLabel == selectedFlexLabel }?.valueOption ?: 0

        val minDate = LocalDate.now().minusMonths(4 + flex)
        val maxDate = LocalDate.now().plusMonths(12 + flex)

        CustomDatePicker(
            context = this,
            fragmentManager = supportFragmentManager,
            title = getString(R.string.lbl_planting_date),
            minDate = minDate,
            maxDate = maxDate,
            initialDate = plantingDate
        ) { selected ->
            plantingDate = selected
            alreadyPlanted = olderThanCurrent(selected)

            binding.lytPlantingSectionActivity.apply {
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
            toggleFab()
        }.show()
    }

    private fun showHarvestDatePicker() {
        val planting = plantingDate ?: run {
            Toast.makeText(this, getString(R.string.lbl_planting_date_prompt), Toast.LENGTH_SHORT)
                .show()
            return
        }

        val selectedFlexLabel = binding.lytPlantingSectionActivity.dropHarvestFlex.text.toString()
        val flex = flexOptions.find { it.displayLabel == selectedFlexLabel }?.valueOption ?: 0

        val minDate = planting.plusMonths((8 - flex))
        val maxDate = planting.plusMonths((16 + flex))

        CustomDatePicker(
            context = this,
            fragmentManager = supportFragmentManager,
            title = getString(R.string.lbl_harvesting_date),
            minDate = minDate,
            maxDate = maxDate,
            initialDate = harvestDate
        ) { selected ->
            harvestDate = selected
            binding.lytPlantingSectionActivity.inputHarvestDate.setText(
                DateHelper.formatToString(
                    selected
                )
            )
            toggleFab()
        }.show()
    }


    private fun savePlantingSchedule() = with(binding.lytPlantingSectionActivity) {
        lytPlantingDate.error = null
        lytHarvestDate.error = null

        if (plantingDate == null) {
            lytPlantingDate.error = getString(R.string.lbl_planting_date_prompt); return@with
        }
        if (harvestDate == null) {
            lytHarvestDate.error = getString(R.string.lbl_harvest_date_prompt); return@with
        }

        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val plantingFlex =
                flexOptions.find { it.displayLabel == dropPlantingFlex.text.toString() }?.valueOption
                    ?: 0
            val harvestFlex =
                flexOptions.find { it.displayLabel == dropHarvestFlex.text.toString() }?.valueOption
                    ?: 0

            userRepo.saveOrUpdateUser(
                user.copy(
                    plantingDate = plantingDate,
                    harvestDate = harvestDate,
                    plantingFlex = plantingFlex,
                    harvestFlex = harvestFlex,
                    providedAlterNativeDate = alternativeDate
                ), sessionManager.akilimoUser
            )

            val completion =
                AdviceCompletionDto(EnumAdviceTask.PLANTING_AND_HARVEST, EnumStepStatus.COMPLETED)
            setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
            finish()
        }
    }

    private fun hasFormChanged(user: AkilimoUser) = with(binding.lytPlantingSectionActivity) {
        val plantingFlexLabel =
            flexOptions.find { it.valueOption == user.plantingFlex }?.displayLabel.orEmpty()
        val harvestFlexLabel =
            flexOptions.find { it.valueOption == user.harvestFlex }?.displayLabel.orEmpty()
        plantingDate != user.plantingDate ||
                harvestDate != user.harvestDate ||
                alternativeDate != user.providedAlterNativeDate ||
                dropPlantingFlex.text.toString() != plantingFlexLabel ||
                dropHarvestFlex.text.toString() != harvestFlexLabel
    }

    private fun toggleFab() = safeScope.launch {
        userRepo.getUser(sessionManager.akilimoUser)?.let { user ->
            if (hasFormChanged(user)) binding.fabSave.show() else binding.fabSave.hide()
        }
    }
}
