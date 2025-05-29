package com.akilimo.mobile.interfaces

import java.util.Calendar

interface IDatePickerDismissListener {
    fun onDismiss(
        myCalendar: Calendar,
        selectedDate: String,
        plantingDateSelected: Boolean,
        harvestDateSelected: Boolean
    )
}
