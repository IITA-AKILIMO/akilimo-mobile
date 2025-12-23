package com.akilimo.mobile.ui.components

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.akilimo.mobile.utils.DateHelper
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneId

class CustomDatePicker(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val title: String= "AKILIMO Date picker",
    private val minDate: LocalDate? = null,
    private val maxDate: LocalDate? = null,
    private val initialDate: LocalDate? = null,
    private val onDateSelected: (LocalDate) -> Unit
) {
    fun show() {
        val minMillis = minDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val maxMillis = maxDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        val initialMillis =
            initialDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()

        val constraintsBuilder = CalendarConstraints.Builder()
        minMillis?.let { constraintsBuilder.setStart(it) }
        maxMillis?.let { constraintsBuilder.setEnd(it) }

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(initialMillis)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        picker.addOnPositiveButtonClickListener { epochMillis ->
            val selectedDate = DateHelper.unixTimeStampToDate(epochMillis) ?: LocalDate.now()
            onDateSelected(selectedDate)
        }

        picker.show(fragmentManager, "CustomDatePicker")
    }
}