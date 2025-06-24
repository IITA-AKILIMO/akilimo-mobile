package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityDatesBinding
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.IDatePickerDismissListener
import com.akilimo.mobile.utils.DateHelper.formatToLocalDate
import com.akilimo.mobile.viewmodels.DatesViewModel
import com.akilimo.mobile.viewmodels.factory.DatesViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.DateDialogPickerFragment
import io.sentry.Sentry
import java.util.Calendar

class DatesActivity : BindBaseActivity<ActivityDatesBinding>() {

    private val viewModel: DatesViewModel by viewModels {
        DatesViewModelFactory(application = this.application)
    }

    override fun inflateBinding() = ActivityDatesBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar, R.string.lbl_planting_harvest_dates) { validate(true) }
        observeViewModel()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSchedule()
    }

    private fun observeViewModel() {
        viewModel.plantingDate.observe(this) { updatePlantingDate(it) }
        viewModel.harvestDate.observe(this) { updateHarvestDate(it) }
        viewModel.alternativeDate.observe(this) { updateAlternativeDate(it) }
        viewModel.plantingWindow.observe(this) { updatePlantingWindow(it) }
        viewModel.harvestWindow.observe(this) { updateHarvestWindow(it) }
    }

    private fun setupUI() {
        setupPlantingUI()
        setupHarvestUI()
        setupAlternativeDateUI()
        setupButtons()
    }

    private fun updatePlantingDate(dateStr: String?) {
        binding.cardPlanting.lblSelectedPlantingDate.text =
            formatToLocalDate(dateStr ?: "", "dd-MM-yyyy").toString()
    }

    private fun updateHarvestDate(dateStr: String?) {
        binding.cardHarvest.lblSelectedHarvestDate.text =
            formatToLocalDate(dateStr ?: "", "dd-MM-yyyy").toString()
    }

    private fun updateAlternativeDate(isAlternative: Boolean) {
        binding.rdgAlternativeDate.check(if (isAlternative) R.id.rdYes else R.id.rdNo)
        binding.datesGroup.visibility = if (isAlternative) View.VISIBLE else View.GONE
    }

    private fun updatePlantingWindow(window: Int) {
        val planting = binding.cardPlanting
        planting.flexiblePlanting.isChecked = window > 0
        if (window > 0) {
            planting.rdgPlantingWindow.check(
                if (window == 1) R.id.rdPlantingOneMonth else R.id.rdPlantingTwoMonths
            )
        } else {
            planting.rdgPlantingWindow.clearCheck()
        }
    }

    private fun updateHarvestWindow(window: Int) {
        val harvest = binding.cardHarvest
        harvest.flexibleHarvest.isChecked = window > 0
        if (window > 0) {
            harvest.rdgHarvestWindow.check(
                if (window == 1) R.id.rdHarvestOneMonth else R.id.rdHarvestTwoMonths
            )
        } else {
            harvest.rdgHarvestWindow.clearCheck()
        }
    }

    private fun setupPlantingUI() = binding.cardPlanting.run {
        flexiblePlanting.setOnCheckedChangeListener { _, isChecked ->
            if (viewModel.alreadyPlanted.value == true) {
                showCustomWarningDialog(
                    getString(R.string.lbl_already_planted_title),
                    getString(R.string.lbl_already_planted_text)
                )
                flexiblePlanting.isChecked = false
                return@setOnCheckedChangeListener
            }
            rdgPlantingWindow.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) viewModel.plantingWindow.value = 0
        }

        rdgPlantingWindow.setOnCheckedChangeListener { _, checkedId ->
            viewModel.plantingWindow.value = when (checkedId) {
                R.id.rdPlantingOneMonth -> 1
                R.id.rdPlantingTwoMonths -> 2
                else -> 0
            }
        }

        btnPickPlantingDate.setOnClickListener {
            showDatePicker(pickPlantingDate = true)
        }
    }

    private fun setupHarvestUI() = binding.cardHarvest.run {
        flexibleHarvest.setOnCheckedChangeListener { _, isChecked ->
            rdgHarvestWindow.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) viewModel.harvestWindow.value = 0
        }

        rdgHarvestWindow.setOnCheckedChangeListener { _, checkedId ->
            viewModel.harvestWindow.value = when (checkedId) {
                R.id.rdHarvestOneMonth -> 1
                R.id.rdHarvestTwoMonths -> 2
                else -> 0
            }
        }

        btnPickHarvestDate.setOnClickListener {
            showDatePicker(pickHarvestDate = true)
        }
    }

    private fun setupAlternativeDateUI() {
        binding.rdgAlternativeDate.setOnCheckedChangeListener { _, checkedId ->
            viewModel.alternativeDate.value = checkedId == R.id.rdYes
        }
    }

    private fun setupButtons() = binding.twoButtons.run {
        btnFinish.setOnClickListener { validate(false) }
        btnCancel.setOnClickListener { closeActivity(false) }
    }

    override fun validate(backPressed: Boolean) {
        val plantingValid = !viewModel.plantingDate.value.isNullOrBlank()
        val harvestValid = !viewModel.harvestDate.value.isNullOrBlank()

        if (!plantingValid) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_planting_date),
                getString(R.string.lbl_planting_date_prompt)
            )
            return
        }

        if (!harvestValid) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_harvest_date),
                getString(R.string.lbl_harvest_date_prompt)
            )
            return
        }

        viewModel.saveSchedule(
            onSuccess = { closeActivity(backPressed) },
            onError = {
                Toast.makeText(this, it.message ?: "Error", Toast.LENGTH_SHORT).show()
                Sentry.captureException(it)
            }
        )
    }

    private fun showDatePicker(
        pickPlantingDate: Boolean = false,
        pickHarvestDate: Boolean = false
    ) {
        val picker = if (pickHarvestDate) {
            DateDialogPickerFragment.newInstanceForHarvest(viewModel.plantingDate.value ?: "")
        } else {
            DateDialogPickerFragment.newInstanceForPlanting()
        }

        picker.setOnDismissListener(object : IDatePickerDismissListener {
            override fun onDismiss(
                myCalendar: Calendar,
                selectedDate: String,
                plantingDateSelected: Boolean,
                harvestDateSelected: Boolean
            ) {
                myCalendar.timeInMillis
                if (pickPlantingDate) {
                    viewModel.updatePlantingDate(selectedDate)
                    binding.cardHarvest.lblSelectedHarvestDate.text = null
                } else if (pickHarvestDate) {
                    viewModel.updateHarvestDate(selectedDate)
                }
            }
        })

        picker.show(supportFragmentManager, DateDialogPickerFragment.TAG)
    }
}
