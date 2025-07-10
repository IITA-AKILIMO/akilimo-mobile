package com.akilimo.mobile.views.fragments.dialog

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.akilimo.mobile.interfaces.IDatePickerDismissListener
import com.akilimo.mobile.utils.DateHelper
import java.util.Calendar

class DateDialogPickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private val calendar: Calendar = Calendar.getInstance()

    private var isPlantingDate = false
    private var isHarvestDate = false
    private var plantingDateStr: String = "01-01-1990"
    private var selectedDate: String = "01-01-1990"

    private var dismissListener: IDatePickerDismissListener? = null

    companion object {
        const val PLANTING_REQUEST_CODE = 11
        const val HARVEST_REQUEST_CODE = 12
        const val TAG = "DatePickerFragment"

        fun newInstanceForPlanting(): DateDialogPickerFragment {
            return DateDialogPickerFragment().apply {
                isPlantingDate = true
            }
        }

        fun newInstanceForHarvest(plantingDate: String): DateDialogPickerFragment {
            return DateDialogPickerFragment().apply {
                isHarvestDate = true
                plantingDateStr = plantingDate
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentDate = Calendar.getInstance()
        val (year, month, day) = listOf(
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        val dialog = DatePickerDialog(requireContext(), this, year, month, day)
        val datePicker = dialog.datePicker

        when {
            isPlantingDate -> {
                datePicker.minDate = DateHelper.getMinDate(-16).timeInMillis
                datePicker.maxDate = DateHelper.getMinDate(12).timeInMillis
            }

            isHarvestDate && plantingDateStr.isNotEmpty() -> {
                datePicker.minDate =
                    DateHelper.getFutureOrPastMonth(plantingDateStr, 8).timeInMillis
                datePicker.maxDate =
                    DateHelper.getFutureOrPastMonth(plantingDateStr, 16).timeInMillis
            }
        }

        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

        selectedDate = DateHelper.simpleDateFormatter.format(calendar.time)
        val resultIntent = Intent().apply {
            putExtra("selectedDate", selectedDate)
            putExtra("selectedDateObject", calendar.time)
        }

        targetFragment?.onActivityResult(
            targetRequestCode,
            Activity.RESULT_OK,
            resultIntent
        ) ?: run {
            // TODO: Replace with FragmentResult API if targetFragment is deprecated in your app
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(
            calendar,
            selectedDate,
            isPlantingDate,
            isHarvestDate
        )
    }

    fun setOnDismissListener(listener: IDatePickerDismissListener?) {
        dismissListener = listener
    }
}