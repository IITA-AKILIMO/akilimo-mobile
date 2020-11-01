package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.toolbox.Volley;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityManualTillageCostBinding;
import com.iita.akilimo.entities.FieldOperationCost;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.CostBaseActivity;
import com.iita.akilimo.models.OperationCost;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumOperation;
import com.iita.akilimo.utils.enums.EnumOperationType;
import com.iita.akilimo.views.fragments.dialog.OperationCostsDialogFragment;

import java.util.ArrayList;

;


public class ManualTillageCostActivity extends CostBaseActivity {

    Toolbar toolbar;
    TextView manualPloughCostTitle;
    TextView manualRidgeCostTitle;
    TextView manualPloughCostText;
    TextView manualRidgingCostText;

    AppCompatButton btnPloughCost;
    AppCompatButton btnRidgeCost;
    AppCompatButton btnFinish;
    AppCompatButton btnCancel;

    ActivityManualTillageCostBinding binding;
    MathHelper mathHelper;
    FieldOperationCost fieldOperationCost;

    private double manualPloughCost = 0;
    private double manualRidgeCost = 0;
    private boolean dataValid;
    private boolean dialogOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualTillageCostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        database = AppDatabase.getDatabase(context);
        queue = Volley.newRequestQueue(this);
        mathHelper = new MathHelper();

        toolbar = binding.toolbar;
        manualPloughCostTitle = binding.manualTillage.manualPloughCostTitle;
        manualRidgeCostTitle = binding.manualTillage.manualRidgeCostTitle;
        manualPloughCostText = binding.manualTillage.manualPloughCostText;
        manualRidgingCostText = binding.manualTillage.manualRidgingCostText;
        btnPloughCost = binding.manualTillage.btnPloughCost;
        btnRidgeCost = binding.manualTillage.btnRidgeCost;
        btnFinish = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;

        MandatoryInfo mandatoryInfo = database.mandatoryInfoDao().findOne();
        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.getAreaUnit();
            fieldSize = mandatoryInfo.getAreaSize();
        }

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        initToolbar();
        initComponent();

        fieldOperationCost = database.fieldOperationCostDao().findOne();
        if (fieldOperationCost != null) {
            manualPloughCost = fieldOperationCost.getManualPloughCost();
            manualRidgeCost = fieldOperationCost.getManualRidgeCost();

            manualPloughCostText.setText(getString(R.string.lbl_ploughing_cost_text, fieldSize, areaUnit, manualPloughCost, currency));
            manualRidgingCostText.setText(getString(R.string.lbl_ridging_cost_text, fieldSize, areaUnit, manualRidgeCost, currency));

        }
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_manual_tillage_cost));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        String ploughTitle = context.getString(R.string.lbl_manual_tillage_cost, fieldSize, areaUnit);
        String ridgeTitle = context.getString(R.string.lbl_manual_ridge_cost, fieldSize, areaUnit);

        btnPloughCost.setOnClickListener(view -> {
            if (!dialogOpen) {
                loadOperationCost(EnumOperation.TILLAGE.name(), EnumOperationType.MANUAL.name(), ploughTitle);
            }
        });

        btnRidgeCost.setOnClickListener(view -> {
            if (!dialogOpen) {
                loadOperationCost(EnumOperation.RIDGING.name(), EnumOperationType.MANUAL.name(), ridgeTitle);
            }
        });

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

        manualPloughCostTitle.setText(ploughTitle);
        manualRidgeCostTitle.setText(ridgeTitle);

        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {
        setData();
        if (dataValid) {
            closeActivity(backPressed);
        }
    }

    private void setData() {
        if (fieldOperationCost == null) {
            fieldOperationCost = new FieldOperationCost();
        }

        dataValid = false;
        if (manualPloughCost <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_selection), getString(R.string.lbl_manual_plough_cost_prompt));
            return;
        }

        if (manualRidgeCost <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_selection), getString(R.string.lbl_manual_ridge_cost_prompt));
            return;
        }

        dataValid = true;
        try {

            fieldOperationCost.setManualPloughCost(manualPloughCost);
            fieldOperationCost.setManualRidgeCost(manualRidgeCost);
            database.fieldOperationCostDao().insert(fieldOperationCost);

        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }

    }

    @Override
    protected void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operation, String countryCode, String dialogTitle) {
        Bundle arguments = new Bundle();

        if (dialogOpen) {
            return;
        }
        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList);
        arguments.putString(OperationCostsDialogFragment.OPERATION_NAME, operation);
        arguments.putString(OperationCostsDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(OperationCostsDialogFragment.COUNTRY_CODE, countryCode);

        OperationCostsDialogFragment dialogFragment = new OperationCostsDialogFragment(context);
        dialogFragment.setArguments(arguments);

        dialogFragment.setOnDismissListener((operationCost, enumOperation, selectedCost, cancelled, isExactCost) -> {
            if (!cancelled && enumOperation != null) {
                switch (enumOperation) {
                    case "TILLAGE":
                        manualPloughCost = selectedCost;
                        manualPloughCostText.setText(getString(R.string.lbl_ploughing_cost_text, fieldSize, areaUnit, selectedCost, currency));
                        break;
                    case "RIDGING":
                        manualRidgeCost = selectedCost;
                        manualRidgingCostText.setText(getString(R.string.lbl_ridging_cost_text, fieldSize, areaUnit, selectedCost, currency));
                        break;
                }
            }
            dialogOpen = false;
        });


        FragmentTransaction fragmentTransaction;
        if (getFragmentManager() != null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.setCustomAnimations(R.anim.animate_slide_in_left, R.anim.animate_slide_out_right);
            Fragment prev = getSupportFragmentManager().findFragmentByTag(OperationCostsDialogFragment.ARG_ITEM_ID);
            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);
            dialogOpen = true;
            dialogFragment.show(getSupportFragmentManager(), OperationCostsDialogFragment.ARG_ITEM_ID);
        }
    }
}
