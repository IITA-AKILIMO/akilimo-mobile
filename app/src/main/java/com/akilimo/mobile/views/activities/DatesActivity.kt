package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityDatesBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.IDatePickerDismissListener
import com.akilimo.mobile.utils.DateHelper.formatLongToDateString
import com.akilimo.mobile.utils.DateHelper.formatToLocalDate
import com.akilimo.mobile.utils.DateHelper.olderThanCurrent
import com.akilimo.mobile.utils.DateHelper.simpleDateFormatter
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.views.fragments.dialog.DateDialogPickerFragment
import io.sentry.Sentry
import java.util.Calendar

class DatesActivity : BindBaseActivity<ActivityDatesBinding>() {

    private var selectedPlantingDate = ""
    private var selectedHarvestDate = ""
    private var plantingWindow = 0
    private var harvestWindow = 0
    private var alternativeDate = false
    private var alreadyPlanted = false

    override fun inflateBinding() = ActivityDatesBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(binding.toolbar, R.string.lbl_planting_harvest_dates) { validate(true) }
        setupPlantingUI()
        setupHarvestUI()
        setupAlternativeDateUI()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        loadSchedule()
        updateUIFromData()
    }

    override fun validate(backPressed: Boolean) {
        if (!validateDates()) return
        try {
            saveSchedule()
            closeActivity(backPressed)
        } catch (ex: Exception) {
            Toast.makeText(this@DatesActivity, ex.message ?: "Unknown error", Toast.LENGTH_SHORT)
                .show()
            Sentry.captureException(ex)
        }
    }

    private fun setupPlantingUI() = binding.cardPlanting.run {
        flexiblePlanting.setOnCheckedChangeListener { _, isChecked ->
            rdgPlantingWindow.clearCheck()
            plantingWindow = 0

            if (!isChecked) {
                rdgPlantingWindow.visibility = View.GONE
                return@setOnCheckedChangeListener
            }

            if (alreadyPlanted) {
                showCustomWarningDialog(
                    getString(R.string.lbl_already_planted_title),
                    getString(R.string.lbl_already_planted_text)
                )
                flexiblePlanting.isChecked = false
                return@setOnCheckedChangeListener
            }

            rdgPlantingWindow.visibility = View.VISIBLE
        }

        rdgPlantingWindow.setOnCheckedChangeListener { _, checkedId ->
            plantingWindow = when (checkedId) {
                R.id.rdPlantingOneMonth -> 1
                R.id.rdPlantingTwoMonths -> 2
                else -> 0
            }
        }

        btnPickPlantingDate.setOnClickListener { showDatePicker(pickPlantingDate = true) }
    }

    private fun setupHarvestUI() = binding.cardHarvest.run {
        flexibleHarvest.setOnCheckedChangeListener { _, isChecked ->
            rdgHarvestWindow.visibility = if (isChecked) View.VISIBLE else View.GONE
            rdgHarvestWindow.clearCheck()
            harvestWindow = 0
        }

        rdgHarvestWindow.setOnCheckedChangeListener { _, checkedId ->
            harvestWindow = when (checkedId) {
                R.id.rdHarvestOneMonth -> 1
                R.id.rdHarvestTwoMonths -> 2
                else -> 0
            }
        }

        btnPickHarvestDate.setOnClickListener { showDatePicker(pickHarvestDate = true) }
    }

    private fun setupAlternativeDateUI() =
        binding.rdgAlternativeDate.setOnCheckedChangeListener { _, checkedId ->
            alternativeDate = checkedId == R.id.rdYes
            binding.datesGroup.visibility = if (alternativeDate) View.VISIBLE else View.GONE
        }

    private fun setupButtons() = binding.twoButtons.run {
        btnFinish.setOnClickListener { validate(false) }
        btnCancel.setOnClickListener { closeActivity(false) }
    }

    private fun loadSchedule() {
        val schedule = database.scheduleDateDao().findOne() ?: return
        selectedPlantingDate = schedule.plantingDate
        selectedHarvestDate = schedule.harvestDate
        plantingWindow = schedule.plantingWindow
        harvestWindow = schedule.harvestWindow
        alternativeDate = schedule.alternativeDate
        alreadyPlanted = schedule.alreadyPlanted
    }

    private fun updateUIFromData() = binding.run {
        val dateFormat = "dd-MM-yyyy"
        cardPlanting.lblSelectedPlantingDate.text =
            formatToLocalDate(selectedPlantingDate, dateFormat).toString()
        cardHarvest.lblSelectedHarvestDate.text =
            formatToLocalDate(selectedHarvestDate, dateFormat).toString()
        rdgAlternativeDate.check(if (alternativeDate) R.id.rdYes else R.id.rdNo)

        if (plantingWindow > 0) {
            cardPlanting.flexiblePlanting.isChecked = true
            cardPlanting.rdgPlantingWindow.check(
                if (plantingWindow == 1) R.id.rdPlantingOneMonth else R.id.rdPlantingTwoMonths
            )
        }

        if (harvestWindow > 0) {
            cardHarvest.flexibleHarvest.isChecked = true
            cardHarvest.rdgHarvestWindow.check(
                if (harvestWindow == 1) R.id.rdHarvestOneMonth else R.id.rdHarvestTwoMonths
            )
        }
    }

    private fun validateDates(): Boolean {
        val isPlantingDateValid = selectedPlantingDate.isNotEmpty()
        val isHarvestDateValid = selectedHarvestDate.isNotEmpty()

        when {
            !isPlantingDateValid -> showCustomWarningDialog(
                getString(R.string.lbl_invalid_planting_date),
                getString(R.string.lbl_planting_date_prompt)
            )

            !isHarvestDateValid -> showCustomWarningDialog(
                getString(R.string.lbl_invalid_harvest_date),
                getString(R.string.lbl_harvest_date_prompt)
            )
        }

        return isPlantingDateValid && isHarvestDateValid
    }


    private fun saveSchedule() {
        val cropSchedule = database.scheduleDateDao().findOne() ?: CropSchedule()
        alreadyPlanted = olderThanCurrent(selectedPlantingDate)

        cropSchedule.apply {
            plantingDate = this@DatesActivity.selectedPlantingDate
            harvestDate = this@DatesActivity.selectedHarvestDate
            plantingWindow = this@DatesActivity.plantingWindow
            harvestWindow = this@DatesActivity.harvestWindow
            alternativeDate = this@DatesActivity.alternativeDate
            alreadyPlanted = this@DatesActivity.alreadyPlanted
        }

        database.scheduleDateDao().insert(cropSchedule)
        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTask.PLANTING_AND_HARVEST.name, true))
    }

    private fun showDatePicker(
        pickPlantingDate: Boolean = false,
        pickHarvestDate: Boolean = false
    ) {
        val pickerFragment = if (pickHarvestDate) {
            DateDialogPickerFragment.newInstanceForHarvest(selectedPlantingDate)
        } else {
            DateDialogPickerFragment.newInstanceForPlanting()
        }

        pickerFragment.setOnDismissListener(object : IDatePickerDismissListener {
            override fun onDismiss(
                myCalendar: Calendar,
                selectedDate: String,
                plantingDateSelected: Boolean,
                harvestDateSelected: Boolean
            ) {
                val dateMs = myCalendar.timeInMillis
                if (pickPlantingDate) {
                    selectedHarvestDate = ""
                    selectedPlantingDate = simpleDateFormatter.format(myCalendar.time)
                    binding.cardPlanting.lblSelectedPlantingDate.text =
                        formatLongToDateString(dateMs)
                    binding.cardHarvest.lblSelectedHarvestDate.text = null

                    alreadyPlanted = olderThanCurrent(selectedPlantingDate)
                    if (alreadyPlanted) {
                        binding.cardPlanting.flexiblePlanting.isChecked = false
                    }
                } else if (pickHarvestDate) {
                    selectedHarvestDate = simpleDateFormatter.format(myCalendar.time)
                    binding.cardHarvest.lblSelectedHarvestDate.text = formatLongToDateString(dateMs)
                }
            }
        })

        pickerFragment.show(supportFragmentManager, DateDialogPickerFragment.TAG)
    }
}
