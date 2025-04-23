package com.akilimo.mobile.views.fragments.dialog

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import com.akilimo.mobile.interfaces.IDatePickerDismissListener
import com.akilimo.mobile.utils.DateHelper
import java.util.Calendar

class DateDialogPickerFragment : AppCompatDialogFragment, OnDateSetListener {
    private val myCalendar: Calendar = Calendar.getInstance()
    private var pickPlantingDate = false
    private var pickHarvestDate = false
    private var selectedPlantingDate: String? = null
    private var selectedDate: String? = null

    private var onDismissListener: IDatePickerDismissListener? = null

    companion object {
        const val PLANTING_REQUEST_CODE: Int = 11 // Used to identify the result
        const val HARVEST_REQUEST_CODE: Int = 12 // Used to identify the result
        const val TAG: String = "DatePickerFragment"
    }


    constructor(pickPlantingDate: Boolean) {
        this.pickPlantingDate = pickPlantingDate
    }

    /**
     * @param pickHarvestDate      Indicate if harvest date is being picked
     * @param selectedPlantingDate pass the planting date parameter
     */
    constructor(pickHarvestDate: Boolean, selectedPlantingDate: String) {
        this.pickHarvestDate = pickHarvestDate
        this.selectedPlantingDate = selectedPlantingDate
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Set the current date as the default date
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        // Return a new instance of DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            this@DateDialogPickerFragment, year, month, day
        )

        val datePicker = datePickerDialog.datePicker

        if (pickPlantingDate) {
            val minDate = DateHelper.getMinDate(-16)
            val maxDate = DateHelper.getMinDate(12)
            datePicker.minDate = minDate.timeInMillis
            datePicker.maxDate = maxDate.timeInMillis
        } else if (pickHarvestDate && !selectedPlantingDate.isNullOrEmpty()) {
            val minDate = DateHelper.getFutureOrPastMonth(selectedPlantingDate, 8)
            val maxDate = DateHelper.getFutureOrPastMonth(selectedPlantingDate, 16)
            datePicker.minDate = minDate.timeInMillis
            datePicker.maxDate = maxDate.timeInMillis
        }
        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        myCalendar[Calendar.YEAR] = year
        myCalendar[Calendar.MONTH] = month
        myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        selectedDate = DateHelper.getSimpleDateFormatter().format(myCalendar.time)

        //TODO use the Fragment Result API, which is cleaner and lifecycle-aware.
        val target = targetFragment
        if (target != null) {
            val intent = Intent()
            intent.putExtra("selectedDate", selectedDate)
            intent.putExtra("selectedDateObject", myCalendar.time)
            target.onActivityResult(
                targetRequestCode,
                Activity.RESULT_OK,
                intent
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss(
                myCalendar,
                selectedDate!!, pickPlantingDate, pickHarvestDate
            )
        }
    }

    fun setOnDismissListener(dismissListener: IDatePickerDismissListener?) {
        this.onDismissListener = dismissListener
    }
}
