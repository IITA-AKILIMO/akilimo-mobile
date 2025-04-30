package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityDatesBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.IDatePickerDismissListener
import com.akilimo.mobile.utils.DateHelper.formatLongToDateString
import com.akilimo.mobile.utils.DateHelper.formatToLocalDate
import com.akilimo.mobile.utils.DateHelper.olderThanCurrent
import com.akilimo.mobile.utils.DateHelper.simpleDateFormatter
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.views.fragments.dialog.DateDialogPickerFragment
import io.sentry.Sentry
import java.util.Calendar

class DatesActivity : BaseActivity() {
    private var _binding: ActivityDatesBinding? = null
    private val binding get() = _binding!!

    private var selectedPlantingDate: String = ""
    private var selectedHarvestDate: String = ""
    private var plantingWindow: Int = 0
    private var harvestWindow: Int = 0
    private var alternativeDate: Boolean = false
    private var alreadyPlanted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null  // Prevent memory leak
    }

    override fun initToolbar() {
        val myToolbar = binding.toolbar
        myToolbar.apply {
            setNavigationIcon(R.drawable.ic_left_arrow)
            setSupportActionBar(this)
            supportActionBar?.apply {
                title = getString(R.string.lbl_planting_harvest_dates)
                setDisplayHomeAsUpEnabled(true)
            }

            setNavigationOnClickListener {
                validate(true)
            }
        }
    }

    override fun initComponent() = binding.run {
        cardPlanting.run {
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

            btnPickPlantingDate.setOnClickListener {
                dialogDatePickerLight(true, false)
            }
        }

        cardHarvest.run {
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

            btnPickHarvestDate.setOnClickListener {
                dialogDatePickerLight(false, true)
            }
        }

        rdgAlternativeDate.setOnCheckedChangeListener { _, checkedId ->
            alternativeDate = checkedId == R.id.rdYes
            datesGroup.visibility = if (alternativeDate) View.VISIBLE else View.GONE
        }

        twoButtons.run {
            btnFinish.setOnClickListener { validate(false) }
            btnCancel.setOnClickListener { closeActivity(false) }
        }
    }

    override fun onResume() {
        super.onResume()
        val scheduledDate = database.scheduleDateDao().findOne()
        val dateFormat = "dd-MM-yyyy"
        if (scheduledDate != null) {
            alternativeDate = scheduledDate.alternativeDate
            selectedPlantingDate = scheduledDate.plantingDate
            selectedHarvestDate = scheduledDate.harvestDate
            plantingWindow = scheduledDate.plantingWindow
            harvestWindow = scheduledDate.harvestWindow
            alreadyPlanted = scheduledDate.alreadyPlanted
        }

        binding.run {
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
    }

    override fun validate(backPressed: Boolean) {
        if (selectedPlantingDate.isEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_planting_date),
                getString(R.string.lbl_planting_date_prompt)
            )
            return
        }

        if (selectedHarvestDate.isEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_harvest_date),
                getString(R.string.lbl_harvest_date_prompt)
            )
            return
        }

        try {
            val cropSchedule = database.scheduleDateDao().findOne() ?: CropSchedule()
            alreadyPlanted = olderThanCurrent(selectedPlantingDate)

            cropSchedule.apply {
                harvestDate = selectedHarvestDate
                harvestWindow = this@DatesActivity.harvestWindow
                plantingDate = selectedPlantingDate
                plantingWindow = this@DatesActivity.plantingWindow
                alternativeDate = this@DatesActivity.alternativeDate
                alreadyPlanted = this@DatesActivity.alreadyPlanted
            }

            database.scheduleDateDao().insert(cropSchedule)
            database.adviceStatusDao()
                .insert(AdviceStatus(EnumAdviceTasks.PLANTING_AND_HARVEST.name, true))

            closeActivity(backPressed)
        } catch (ex: Exception) {
            Toast.makeText(this@DatesActivity, ex.message ?: "Unknown error", Toast.LENGTH_SHORT)
                .show()
            Sentry.captureException(ex)
        }
    }

    private fun dialogDatePickerLight(pickPlantingDate: Boolean, pickHarvestDate: Boolean) {
        val pickerFragment = if (pickHarvestDate) {
            DateDialogPickerFragment(true, selectedPlantingDate)
        } else {
            DateDialogPickerFragment(true)
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