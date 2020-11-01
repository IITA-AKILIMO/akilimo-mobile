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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.common.util.Strings;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentCurrentPracticeBinding;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.ScheduledDate;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.utils.DateHelper;
import com.iita.akilimo.utils.enums.EnumOperationType;
import com.iita.akilimo.views.fragments.dialog.DateDialogPickerFragment;
import com.iita.akilimo.views.fragments.dialog.OperationTypeDialogFragment;
import com.stepstone.stepper.VerificationError;

;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class CurrentPracticeFragment extends BaseStepFragment {


    CheckBox chkPloughing;
    CheckBox chkRidging;
    TextView lblSelectedPlantingDate;
    TextView lblSelectedHarvestDate;
    AppCompatButton btnPickPlantingDate;
    AppCompatButton btnPickHarvestDate;

    FragmentCurrentPracticeBinding binding;


    private String selectedPlantingDate;
    private String selectedHarvestDate;
    private int plantingWindow = 0;
    private int harvestWindow = 0;


    private CurrentPractice currentPractice;
    private ScheduledDate scheduledDate;

    private String ploughingMethod;
    private String ridgingMethod;
    private String harrowingMethod;
    private String operation;

    private boolean performPloughing;
    private boolean performRidging;
    private boolean performHarrowing;
    private boolean isDataRefreshing = false;
    private boolean alreadyPlanted = false;

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
        binding = FragmentCurrentPracticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chkPloughing = binding.chkPloughing;
        chkRidging = binding.chkRidging;
        lblSelectedPlantingDate = binding.lblSelectedPlantingDate;
        lblSelectedHarvestDate = binding.lblSelectedHarvestDate;
        btnPickPlantingDate = binding.btnPickPlantingDate;
        btnPickHarvestDate = binding.btnPickHarvestDate;

        final FragmentManager fm = getActivity().getSupportFragmentManager();

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
            AppCompatDialogFragment newFragment = new DateDialogPickerFragment(true);
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(CurrentPracticeFragment.this, DateDialogPickerFragment.PLANTING_REQUEST_CODE);
            // show the datePicker
            newFragment.show(fm, "PlantingDatePicker");
        });

        btnPickHarvestDate.setOnClickListener(v -> {
            // create the datePickerFragment
            AppCompatDialogFragment newFragment = new DateDialogPickerFragment(true, selectedPlantingDate);
            // set the targetFragment to receive the results, specifying the request code
            newFragment.setTargetFragment(CurrentPracticeFragment.this, DateDialogPickerFragment.HARVEST_REQUEST_CODE);
            // show the datePicker
            newFragment.show(fm, "HarvestDatePicker");
        });


    }

    public void refreshData() {
        try {

            currentPractice = database.currentPracticeDao().findOne();
            scheduledDate = database.scheduleDateDao().findOne();
            if (currentPractice != null) {
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

            if (scheduledDate != null) {
                selectedPlantingDate = scheduledDate.getPlantingDate();
                selectedHarvestDate = scheduledDate.getHarvestDate();
                lblSelectedPlantingDate.setText(selectedPlantingDate);
                lblSelectedHarvestDate.setText(selectedHarvestDate);
            }

        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

    private void onCheckboxClicked(boolean checked) {
        if (!checked) {
            saveEntities();
            return;
        }
        Bundle arguments = new Bundle();
        arguments.putString(OperationTypeDialogFragment.OPERATION_TYPE, operation);

        OperationTypeDialogFragment operationTypeDialogFragment = new OperationTypeDialogFragment(context);
        operationTypeDialogFragment.setArguments(arguments);
        FragmentTransaction fragmentTransaction;


        fragmentTransaction = getParentFragmentManager().beginTransaction();
        Fragment prev = getParentFragmentManager().findFragmentByTag(OperationTypeDialogFragment.ARG_ITEM_ID);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        operationTypeDialogFragment.show(getParentFragmentManager(), OperationTypeDialogFragment.ARG_ITEM_ID);


        operationTypeDialogFragment.setOnDismissListener((operation, enumOperationType, cancelled) -> {
            switch (operation) {
                case "Plough":
                    performPloughing = !cancelled;
                    chkPloughing.setChecked(!cancelled);
                    ploughingMethod = enumOperationType.operationName();
                    break;
                case "Ridge":
                    performRidging = !cancelled;
                    chkRidging.setChecked(!cancelled);
                    ridgingMethod = enumOperationType.operationName();
                    break;
                default:
                    ridgingMethod = EnumOperationType.NONE.operationName();
                    performPloughing = false;
                    performRidging = false;
                    break;
            }
            saveEntities();
        });

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
        if (!performPloughing) {
            ploughingMethod = EnumOperationType.NONE.operationName();
        }
        if (!performRidging) {
            ridgingMethod = EnumOperationType.NONE.operationName();
        }
        if (!performHarrowing) {
            harrowingMethod = EnumOperationType.NONE.operationName();
        }

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
            if (currentPractice == null) {
                currentPractice = new CurrentPractice();
            }
            currentPractice.setRidgingMethod(ridgingMethod);
            currentPractice.setPloughingMethod(ploughingMethod);
            currentPractice.setPerformRidging(performRidging);
            currentPractice.setPerformPloughing(performPloughing);
            currentPractice.setPerformHarrowing(performHarrowing);

            if (currentPractice.getId() != null) {
                database.currentPracticeDao().update(currentPractice);
            } else {
                database.currentPracticeDao().insert(currentPractice);
            }

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
            currentPractice = database.currentPracticeDao().findOne();
            scheduledDate = database.scheduleDateDao().findOne();
            dataIsValid = true;
        } catch (Exception ex) {
            dataIsValid = false;
            errorMessage = ex.getMessage();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
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
