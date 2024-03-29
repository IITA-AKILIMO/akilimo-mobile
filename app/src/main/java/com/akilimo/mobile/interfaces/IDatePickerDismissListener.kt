package com.akilimo.mobile.interfaces

import java.util.*

interface IDatePickerDismissListener {
    fun onDismiss(
        myCalendar: Calendar,
        selectedDate: String,
        plantingDateSelected: Boolean,
        harvestDateSelected: Boolean
    )
}
