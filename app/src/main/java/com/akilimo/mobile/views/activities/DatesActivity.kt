package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityDatesBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.ScheduledDate
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
    var myToolbar: Toolbar? = null

    var lytPlantingHarvest: RelativeLayout? = null

    var btnPickPlantingDate: AppCompatButton? = null
    var btnPickHarvestDate: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null
    var btnFinish: AppCompatButton? = null

    var lblSelectedPlantingDate: TextView? = null

    var lblSelectedHarvestDate: TextView? = null


    var rdgAlternativeDate: RadioGroup? = null

    var rdgPlantingWindow: RadioGroup? = null

    var rdgHarvestWindow: RadioGroup? = null

    var flexiblePlanting: SwitchCompat? = null
    var flexibleHarvest: SwitchCompat? = null

    private var _binding: ActivityDatesBinding? = null
    private val binding get() = _binding!!


    var selectedPlantingDate: String? = null
    var selectedHarvestDate: String? = null
    var plantingWindow: Int = 0
    var harvestWindow: Int = 0
    var alternativeDate: Boolean = false
    var alreadyPlanted: Boolean = false

//    var scheduledDate: ScheduledDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myToolbar = binding.toolbar
        lytPlantingHarvest = binding.lytPlantingHarvest
        btnPickPlantingDate = binding.btnPickPlantingDate
        btnPickHarvestDate = binding.btnPickHarvestDate
        btnCancel = binding.twoButtons.btnCancel
        btnFinish = binding.twoButtons.btnFinish
        lblSelectedPlantingDate = binding.lblSelectedPlantingDate
        lblSelectedHarvestDate = binding.lblSelectedHarvestDate
        rdgAlternativeDate = binding.rdgAlternativeDate
        rdgPlantingWindow = binding.rdgPlantingWindow
        rdgHarvestWindow = binding.rdgHarvestWindow
        flexiblePlanting = binding.flexiblePlanting
        flexibleHarvest = binding.flexibleHarvest


        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        myToolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(myToolbar)
        supportActionBar!!.title = getString(R.string.lbl_planting_harvest_dates)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        myToolbar!!.setNavigationOnClickListener { v: View? -> validate(true) }
    }

    override fun initComponent() {
        //now for title pickers
        btnPickPlantingDate!!.setOnClickListener { view: View? ->
            dialogDatePickerLight(
                pickPlantingDate = true,
                pickHarvestDate = false
            )
        }
        btnPickHarvestDate!!.setOnClickListener { view: View? ->
            dialogDatePickerLight(
                pickPlantingDate = false,
                pickHarvestDate = true
            )
        }
        btnFinish!!.setOnClickListener { view: View? -> validate(false) }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }

        flexiblePlanting!!.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->
            rdgPlantingWindow!!.clearCheck()
            plantingWindow = 0
            if (isChecked) {
                if (alreadyPlanted) {
                    showCustomWarningDialog(
                        getString(R.string.lbl_already_planted_title),
                        getString(R.string.lbl_already_planted_text)
                    )
                    flexiblePlanting!!.isChecked = false
                } else {
                    rdgPlantingWindow!!.visibility = View.VISIBLE
                }
            } else {
                rdgPlantingWindow!!.visibility = View.GONE
            }
        }

        flexibleHarvest!!.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->
            rdgHarvestWindow!!.visibility =
                View.GONE
            rdgHarvestWindow!!.clearCheck()
            harvestWindow = 0
            if (isChecked) {
                rdgHarvestWindow!!.visibility = View.VISIBLE
            } else {
                rdgHarvestWindow!!.visibility = View.GONE
            }
        }

        rdgPlantingWindow!!.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            plantingWindow = when (radioIndex) {
                R.id.rdPlantingOneMonth -> 1
                R.id.rdPlantingTwoMonths -> 2
                else -> 0
            }
        }
        rdgHarvestWindow!!.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            harvestWindow = when (radioIndex) {
                R.id.rdHarvestOneMonth -> 1
                R.id.rdHarvestTwoMonths -> 2
                else -> 0
            }
        }

        rdgAlternativeDate!!.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            when (radioIndex) {
                R.id.rdYes -> {
                    lytPlantingHarvest!!.visibility = View.VISIBLE
                    alternativeDate = true
                }

                R.id.rdNo -> {
                    lytPlantingHarvest!!.visibility = View.GONE
                    alternativeDate = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val database = getDatabase(this@DatesActivity)

        val scheduledDate = database.scheduleDateDao().findOne()

        val dateFormat = "dd-MM-yyyy"
        if (scheduledDate != null) {
            alternativeDate = scheduledDate.alternativeDate
            val pd = scheduledDate.plantingDate
            val hd = scheduledDate.harvestDate
            val pw = scheduledDate.plantingWindow
            val hw = scheduledDate.harvestWindow

            alreadyPlanted = scheduledDate.alreadyPlanted

            val pDate = formatToLocalDate(pd, dateFormat)
            val hDate = formatToLocalDate(hd, dateFormat)
            lblSelectedPlantingDate!!.text = pDate.toString()
            lblSelectedHarvestDate!!.text = hDate.toString()
            rdgAlternativeDate!!.check(if (alternativeDate) R.id.rdYes else R.id.rdNo)

            if (pw > 0) {
                flexiblePlanting!!.isChecked = true
                rdgPlantingWindow!!.check(if (pw == 1) R.id.rdPlantingOneMonth else R.id.rdPlantingTwoMonths)
            }
            if (hw > 0) {
                flexibleHarvest!!.isChecked = true
                rdgHarvestWindow!!.check(if (hw == 1) R.id.rdHarvestOneMonth else R.id.rdHarvestTwoMonths)
            }
            //assign these values to the global parameters
            selectedHarvestDate = hd
            selectedPlantingDate = pd
        }
    }

    override fun validate(backPressed: Boolean) {
        if (selectedPlantingDate.isNullOrEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_planting_date),
                getString(R.string.lbl_planting_date_prompt)
            )
            return
        }

        if (selectedHarvestDate.isNullOrEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_harvest_date),
                getString(R.string.lbl_harvest_date_prompt)
            )
            return
        }

        var scheduledDate = database.scheduleDateDao().findOne()
        if (scheduledDate == null) {
            scheduledDate = ScheduledDate()
        }
        try {

            alreadyPlanted = olderThanCurrent(selectedPlantingDate)
            scheduledDate.harvestDate = selectedHarvestDate
            scheduledDate.harvestWindow = harvestWindow
            scheduledDate.plantingDate = selectedPlantingDate
            scheduledDate.plantingWindow = plantingWindow
            scheduledDate.alternativeDate = alternativeDate
            scheduledDate.alreadyPlanted = alreadyPlanted

            val database = getDatabase(this@DatesActivity)
            database.scheduleDateDao().insert(scheduledDate)
            database.adviceStatusDao()
                .insert(AdviceStatus(EnumAdviceTasks.PLANTING_AND_HARVEST.name, true))

            closeActivity(backPressed)
        } catch (ex: Exception) {
            Toast.makeText(this@DatesActivity, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    private fun dialogDatePickerLight(pickPlantingDate: Boolean, pickHarvestDate: Boolean) {
        val fm = supportFragmentManager

        var pickerFragment = DateDialogPickerFragment(true)
        if (pickHarvestDate) {
            pickerFragment = DateDialogPickerFragment(true, selectedPlantingDate!!)
        }
        pickerFragment.show(fm, DateDialogPickerFragment.TAG)

        pickerFragment.setOnDismissListener(object : IDatePickerDismissListener {
            override fun onDismiss(
                myCalendar: Calendar,
                selectedDate: String,
                plantingDateSelected: Boolean,
                harvestDateSelected: Boolean
            ) {
                val dateMs = myCalendar.timeInMillis
                if (pickPlantingDate) {
                    selectedHarvestDate = null
                    selectedPlantingDate = simpleDateFormatter.format(myCalendar.time)
                    binding.lblSelectedPlantingDate.text = formatLongToDateString(dateMs)
                    binding.lblSelectedHarvestDate.text = null
                    alreadyPlanted = olderThanCurrent(selectedPlantingDate)
                    if (alreadyPlanted) {
                        binding.flexiblePlanting.isChecked = false
                    }
                } else if (pickHarvestDate) {
                    selectedHarvestDate = simpleDateFormatter.format(myCalendar.time)
                    lblSelectedHarvestDate!!.text = formatLongToDateString(dateMs)
                }
            }

        })
    }
}
