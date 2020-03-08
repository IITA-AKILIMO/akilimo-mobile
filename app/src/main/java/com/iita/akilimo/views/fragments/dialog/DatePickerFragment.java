package com.iita.akilimo.views.fragments.dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.utils.DateHelper;
import com.iita.akilimo.utils.Tools;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "DatePickerFragment";
    private final Calendar myCalendar = Calendar.getInstance();
    private boolean pickPlantingDate;
    private boolean pickHarvestDate;
    private String selectedPlantingDate;


    public DatePickerFragment(boolean pickPlantingDate) {
        this.pickPlantingDate = pickPlantingDate;
    }

    /**
     * @param pickHarvestDate      Indicate if harvest date is being picked
     * @param selectedPlantingDate pass the planting date parameter
     */
    public DatePickerFragment(boolean pickHarvestDate, @NonNull String selectedPlantingDate) {
        this.pickHarvestDate = pickHarvestDate;
        this.selectedPlantingDate = selectedPlantingDate;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Set the current date as the default date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Return a new instance of DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), DatePickerFragment.this, year, month, day);

        DatePicker datePicker = datePickerDialog.getDatePicker();

        if (pickPlantingDate) {
            Calendar minDate = DateHelper.getMinDate(-16);
            Calendar maxDate = DateHelper.getMinDate(12);
            datePicker.setMinDate(minDate.getTimeInMillis());
            datePicker.setMaxDate(maxDate.getTimeInMillis());
        } else if (pickHarvestDate && !Strings.isEmptyOrWhitespace(selectedPlantingDate)) {
            Calendar minDate = DateHelper.getFutureOrPastMonth(selectedPlantingDate, 8);
            Calendar maxDate = DateHelper.getFutureOrPastMonth(selectedPlantingDate, 16);
            datePicker.setMinDate(minDate.getTimeInMillis());
            datePicker.setMaxDate(maxDate.getTimeInMillis());
        }
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        myCalendar.set(Calendar.YEAR, year);
        myCalendar.set(Calendar.MONTH, month);
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String selectedDate = DateHelper.getSimpleDateFormatter().format(myCalendar.getTime());

        // send date back to the target fragment
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                new Intent().putExtra("selectedDate", selectedDate)
        );
    }
}
