package com.akilimo.mobile.views.fragments.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
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
        const val PLANTING_RESULT_KEY = "planting_result"
        const val HARVEST_RESULT_KEY = "harvest_result"
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
                // Allow selecting planting dates from 16 months ago...
                datePicker.minDate = DateHelper.getMinDate(-16).timeInMillis
                // ...up to 12 months in the future
                datePicker.maxDate = DateHelper.getMinDate(12).timeInMillis
            }

            isHarvestDate && plantingDateStr.isNotEmpty() -> {
                val plantingCal = DateHelper.simpleDateFormatter.parse(plantingDateStr)?.let {
                    Calendar.getInstance().apply { time = it }
                }
                plantingCal?.let {
                    // Harvest allowed from 6 months after planting...
                    val min = it.clone() as Calendar
                    min.add(Calendar.MONTH, 6)
                    datePicker.minDate = min.timeInMillis

                    // ...up to 18 months after planting
                    val max = it.clone() as Calendar
                    max.add(Calendar.MONTH, 18)
                    datePicker.maxDate = max.timeInMillis
                }
            }
        }

        return dialog
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

        selectedDate = DateHelper.simpleDateFormatter.format(calendar.time)
        val resultBundle = Bundle().apply {
            putString("selectedDate", selectedDate)
        }

        when {
            isPlantingDate -> {
                setFragmentResult(PLANTING_RESULT_KEY, resultBundle)
            }

            isHarvestDate -> {
                setFragmentResult(HARVEST_RESULT_KEY, resultBundle)
            }
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