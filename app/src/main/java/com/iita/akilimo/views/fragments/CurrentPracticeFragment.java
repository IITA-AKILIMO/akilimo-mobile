package com.iita.akilimo.views.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.PlantingHarvestDates;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.views.fragments.dialog.DatePickerFragment;
import com.iita.akilimo.views.fragments.dialog.OperationTypeDialogFragment;

import butterknife.BindView;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class CurrentPracticeFragment extends BaseFragment {

    public static final int PLANTING_REQUEST_CODE = 11; // Used to identify the result
    public static final int HARVEST_REQUEST_CODE = 12; // Used to identify the result


    @BindView(R.id.chkPloughing)
    CheckBox chkPloughing;
    @BindView(R.id.chkRidging)
    CheckBox chkRidging;

    @BindView(R.id.lblSelectedPlantingDate)
    TextView lblSelectedPlantingDate;
    @BindView(R.id.lblSelectedHarvestDate)
    TextView lblSelectedHarvestDate;

    @BindView(R.id.btnPickPlantingDate)
    MaterialButton btnPickPlantingDate;
    @BindView(R.id.btnPickHarvestDate)
    MaterialButton btnPickHarvestDate;


    private String selectedPlantingDate;
    private String selectedHarvestDate;
    private int plantingWindow = 0;
    private int harvestWindow = 0;


    private CurrentPractice currentPractice;
    private PlantingHarvestDates plantingHarvestDates;

    private String ploughingMethod, ridgingMethod, operation;

    boolean performPloughing, performRidging, performHarrowing;
    boolean isDataRefreshing = false;

    public CurrentPracticeFragment() {
        // Required empty public constructor
    }

    public static CurrentPracticeFragment newInstance() {
        return new CurrentPracticeFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_practice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FragmentManager fm = getActivity().getSupportFragmentManager();


//        chkPloughing.setOnClickListener(this::onCheckboxClicked);
//        chkRidging.setOnClickListener(this::onCheckboxClicked);

        chkPloughing.setOnCheckedChangeListener((buttonView, checked) -> {
            operation = checked ? "Plough" : "NA";
            performPloughing = checked;
            if (buttonView.isPressed()) {
                onCheckboxClicked(checked);
            }
            isDataRefreshing = false;
        });
        chkRidging.setOnCheckedChangeListener((buttonView, checked) -> {
            performRidging = checked;
            operation = checked ? "Ridge" : "NA";
            if (buttonView.isPressed()) {
                onCheckboxClicked(checked);
            }
            isDataRefreshing = false;
        });

        btnPickPlantingDate.setOnClickListener(v -> {
            // create the datePickerFragment
            AppCompatDialogFragment newFragment = new DatePickerFragment(true);
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(CurrentPracticeFragment.this, PLANTING_REQUEST_CODE);
            // show the datePicker
            newFragment.show(fm, "PlantingDatePicker");
        });

        btnPickHarvestDate.setOnClickListener(v -> {
            // create the datePickerFragment
            AppCompatDialogFragment newFragment = new DatePickerFragment(true, selectedPlantingDate);
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(CurrentPracticeFragment.this, HARVEST_REQUEST_CODE);
            // show the datePicker
            newFragment.show(fm, "HarvestDatePicker");
        });


    }

    @Override
    public void refreshData() {
        try {

            currentPractice = objectBoxEntityProcessor.getCurrentPractice();
            plantingHarvestDates = objectBoxEntityProcessor.getPlantingHarvestDates();
            if (currentPractice == null) {
                currentPractice = new CurrentPractice();
            } else {
                isDataRefreshing = true;
                performPloughing = currentPractice.getPerformPloughing();
                performRidging = currentPractice.getPerformRidging();
                ploughingMethod = currentPractice.getPloughingMethod();
                ridgingMethod = currentPractice.getRidgingMethod();

                if (performPloughing) {
                    chkPloughing.setChecked(true);
                }
                if (performRidging) {
                    chkRidging.setChecked(true);
                }

            }

            if (plantingHarvestDates != null) {
                selectedPlantingDate = plantingHarvestDates.getPlantingDate();
                selectedHarvestDate = plantingHarvestDates.getHarvestDate();
                lblSelectedPlantingDate.setText(selectedPlantingDate);
                lblSelectedHarvestDate.setText(selectedHarvestDate);
            } else {
                plantingHarvestDates = new PlantingHarvestDates();
            }

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, TAG, "An error occurred fetching info");
            Crashlytics.logException(ex);
        }
    }

    private void onCheckboxClicked(boolean checked) {
        if (!checked) {
            saveEntities();
            return;
        }
        Bundle arguments = new Bundle();
        arguments.putString(OperationTypeDialogFragment.OPERATION_TYPE, operation);

        OperationTypeDialogFragment operationTypeDialogFragment = new OperationTypeDialogFragment();
        operationTypeDialogFragment.setArguments(arguments);
        FragmentTransaction fragmentTransaction;

        if (getFragmentManager() != null) {
            fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(OperationTypeDialogFragment.ARG_ITEM_ID);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);
            operationTypeDialogFragment.show(getFragmentManager(), OperationTypeDialogFragment.ARG_ITEM_ID);
        }

        operationTypeDialogFragment.setOnDismissListener((operation, enumOperationType, cancelled) -> {
            switch (operation) {
                case "Plough":
                    performPloughing = !cancelled;
                    if (cancelled) {
                        chkPloughing.setChecked(false);
                        ploughingMethod = null;
                    } else {
                        ploughingMethod = enumOperationType.operationName();
                    }
                    break;
                case "Ridge":
                    performRidging = !cancelled;
                    if (cancelled) {
                        chkRidging.setChecked(false);
                        ridgingMethod = null;
                    } else {
                        ridgingMethod = enumOperationType.operationName();
                    }
                    break;
            }
            saveEntities();
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check for the results
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PLANTING_REQUEST_CODE) {
                selectedPlantingDate = data.getStringExtra("selectedDate");
                selectedHarvestDate = null; //make this null so as to require reselection of harvest date
            } else if (requestCode == HARVEST_REQUEST_CODE) {
                selectedHarvestDate = data.getStringExtra("selectedDate");
            }
        }

        lblSelectedPlantingDate.setText(selectedPlantingDate);
        lblSelectedHarvestDate.setText(selectedHarvestDate);
        saveEntities();
    }

    private void saveEntities() {
        currentPractice.setRidgingMethod(ridgingMethod);
        currentPractice.setPloughingMethod(ploughingMethod);
        currentPractice.setPerformRidging(performRidging);
        currentPractice.setPerformPloughing(performPloughing);
        currentPractice.setPerformHarrowing(performHarrowing);


        plantingHarvestDates.setPlantingDate(selectedPlantingDate);
        plantingHarvestDates.setHarvestDate(selectedHarvestDate);

        objectBoxEntityProcessor.saveCurrentPractice(currentPractice);
        objectBoxEntityProcessor.savePlantingHarvestDates(plantingHarvestDates);
    }
}