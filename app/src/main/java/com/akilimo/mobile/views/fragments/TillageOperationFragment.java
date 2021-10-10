package com.akilimo.mobile.views.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.akilimo.mobile.databinding.FragmentTillageOperationBinding;
import com.akilimo.mobile.entities.CurrentPractice;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.enums.EnumOperationType;
import com.akilimo.mobile.views.fragments.dialog.OperationTypeDialogFragment;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TillageOperationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TillageOperationFragment extends BaseStepFragment {

    private CurrentPractice currentPractice;
    FragmentTillageOperationBinding binding;
    private boolean performPloughing;
    private boolean performRidging;
    private boolean performHarrowing;
    private boolean isDataRefreshing = false;

    private CheckBox chkPloughing;
    private CheckBox chkRidging;

    private String ploughingMethod;
    private String ridgingMethod;
    private String harrowingMethod;
    private String operation;


    public TillageOperationFragment() {
        // Required empty public constructor
    }

    public static TillageOperationFragment newInstance() {
        return new TillageOperationFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTillageOperationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chkPloughing = binding.chkPloughing;
        chkRidging = binding.chkRidging;

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
    }

    public void refreshData() {
        try {
            currentPractice = database.currentPracticeDao().findOne();
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
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
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

            currentPractice = database.currentPracticeDao().findOne();
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
