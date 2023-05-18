package com.akilimo.mobile.views.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentManager;

import com.akilimo.mobile.databinding.FragmentPlantingHarvestDateBinding;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.R;
import com.akilimo.mobile.entities.ScheduledDate;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.DateHelper;
import com.akilimo.mobile.views.fragments.dialog.DateDialogPickerFragment;
import com.stepstone.stepper.VerificationError;

;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class PlantingDateFragment extends BaseStepFragment {


    TextView lblSelectedPlantingDate;
    TextView lblSelectedHarvestDate;
    AppCompatButton btnPickPlantingDate;
    AppCompatButton btnPickHarvestDate;

    FragmentPlantingHarvestDateBinding binding;


    private String selectedPlantingDate;
    private String selectedHarvestDate;
    private int plantingWindow = 0;
    private int harvestWindow = 0;


    private ScheduledDate scheduledDate;

    private boolean alreadyPlanted = false;

    public PlantingDateFragment() {
        // Required empty public constructor
    }

    public static PlantingDateFragment newInstance() {
        return new PlantingDateFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlantingHarvestDateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblSelectedPlantingDate = binding.lblSelectedPlantingDate;
        lblSelectedHarvestDate = binding.lblSelectedHarvestDate;
        btnPickPlantingDate = binding.btnPickPlantingDate;
        btnPickHarvestDate = binding.btnPickHarvestDate;

        final FragmentManager fm = getActivity().getSupportFragmentManager();

        btnPickPlantingDate.setOnClickListener(v -> {
            // create the datePickerFragment
            AppCompatDialogFragment newFragment = new DateDialogPickerFragment(true);
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(PlantingDateFragment.this, DateDialogPickerFragment.PLANTING_REQUEST_CODE);
            // show the datePicker
            newFragment.show(fm, "PlantingDatePicker");
        });

        btnPickHarvestDate.setOnClickListener(v -> {
            // create the datePickerFragment
            AppCompatDialogFragment newFragment = new DateDialogPickerFragment(true, selectedPlantingDate);
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(PlantingDateFragment.this, DateDialogPickerFragment.HARVEST_REQUEST_CODE);
            // show the datePicker
            newFragment.show(fm, "HarvestDatePicker");
        });


    }

    public void refreshData() {
        try {

            scheduledDate = database.scheduleDateDao().findOne();

            if (scheduledDate != null) {
                selectedPlantingDate = scheduledDate.getPlantingDate();
                selectedHarvestDate = scheduledDate.getHarvestDate();
                lblSelectedPlantingDate.setText(selectedPlantingDate);
                lblSelectedHarvestDate.setText(selectedHarvestDate);
            }

        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check for the results
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == DateDialogPickerFragment.PLANTING_REQUEST_CODE) {
                selectedPlantingDate = data.getStringExtra("selectedDate");
                selectedHarvestDate = null;
            } else if (requestCode == DateDialogPickerFragment.HARVEST_REQUEST_CODE) {
                selectedHarvestDate = data.getStringExtra("selectedDate");
            }
        }
        lblSelectedPlantingDate.setText(selectedPlantingDate);
        lblSelectedHarvestDate.setText(selectedHarvestDate);
    }

    private void saveEntities() {

        dataIsValid = !Strings.isEmptyOrWhitespace(selectedPlantingDate);
        if (!dataIsValid) {
            errorMessage = context.getString(R.string.lbl_planting_date_prompt);
            return;
        }
        dataIsValid = !Strings.isEmptyOrWhitespace(selectedHarvestDate);
        if (!dataIsValid) {
            errorMessage = context.getString(R.string.lbl_harvest_date_prompt);
            return;
        }
        try {


            if (scheduledDate == null) {
                scheduledDate = new ScheduledDate();
            }
            DateHelper.dateTimeFormat = "dd/MM/yyyy";
            alreadyPlanted = DateHelper.olderThanCurrent(selectedPlantingDate);

            scheduledDate.setPlantingDate(selectedPlantingDate);
            scheduledDate.setHarvestDate(selectedHarvestDate);
            scheduledDate.setAlreadyPlanted(alreadyPlanted);

            if (scheduledDate.getId() != null) {
                database.scheduleDateDao().update(scheduledDate);
            } else {
                database.scheduleDateDao().insert(scheduledDate);
            }
            scheduledDate = database.scheduleDateDao().findOne();
            dataIsValid = true;
        } catch (Exception ex) {
            dataIsValid = false;
            errorMessage = ex.getMessage();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        saveEntities();
        if (!dataIsValid) {
            showCustomWarningDialog(errorMessage);
            return new VerificationError(errorMessage);
        }
        return null;
    }

    @Override
    public void onSelected() {
        refreshData();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
