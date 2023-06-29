package com.akilimo.mobile.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.toolbox.Volley;

import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.entities.Currency;
import com.akilimo.mobile.entities.FieldOperationCost;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.CostBaseActivity;
import com.akilimo.mobile.models.OperationCost;
import com.akilimo.mobile.utils.MathHelper;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.utils.enums.EnumOperation;
import com.akilimo.mobile.utils.enums.EnumOperationType;
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment;

import java.util.ArrayList;
import java.util.Locale;

;import io.sentry.Sentry;


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

    private String hintText;


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
            currencyCode = profileInfo.getCurrency();
            Currency myCurrency = database.currencyDao().findOneByCurrencyCode(currencyCode);
            currencySymbol = myCurrency.getCurrencySymbol();
        }

        initToolbar();
        initComponent();

        fieldOperationCost = database.fieldOperationCostDao().findOne();
        if (fieldOperationCost != null) {
            manualPloughCost = fieldOperationCost.getManualPloughCost();
            manualRidgeCost = fieldOperationCost.getManualRidgeCost();

            manualPloughCostText.setText(getString(R.string.lbl_ploughing_cost_text, mathHelper.removeLeadingZero(fieldSize), areaUnit, mathHelper.removeLeadingZero(manualPloughCost), currencySymbol));
            manualRidgingCostText.setText(getString(R.string.lbl_ridging_cost_text, mathHelper.removeLeadingZero(fieldSize), areaUnit, mathHelper.removeLeadingZero(manualRidgeCost), currencySymbol));

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
        Locale myLocale = getCurrentLocale();
        String translatedUnit = context.getString(R.string.lbl_acre);
        if (areaUnit.equals("ha")) {
            translatedUnit = context.getString(R.string.lbl_ha);
        }
        String finalTranslatedUnit = translatedUnit.toLowerCase(myLocale);

        String ploughTitle = context.getString(R.string.lbl_manual_tillage_cost, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit);
        String ridgeTitle = context.getString(R.string.lbl_manual_ridge_cost, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit);
        if (myLocale.getLanguage().equals("sw")) {
            ploughTitle = context.getString(R.string.lbl_manual_tillage_cost, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize));
            ridgeTitle = context.getString(R.string.lbl_manual_ridge_cost, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize));
        }
        String finalPloughTitle = ploughTitle;
        btnPloughCost.setOnClickListener(view -> {
            hintText = context.getString(R.string.lbl_manual_tillage_cost_hint, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit);
            if (!dialogOpen) {
                loadOperationCost(EnumOperation.TILLAGE.name(), EnumOperationType.MANUAL.name(), finalPloughTitle, hintText);
            }
        });

        String finalRidgeTitle = ridgeTitle;
        btnRidgeCost.setOnClickListener(view -> {
            hintText = context.getString(R.string.lbl_manual_ridge_cost_hint, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit);
            if (!dialogOpen) {
                loadOperationCost(EnumOperation.RIDGING.name(), EnumOperationType.MANUAL.name(), finalRidgeTitle, hintText);
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
        database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.MANUAL_TILLAGE_COST.name(), dataValid));
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
            Sentry.captureException(ex);
        }

    }


    @Override
    protected void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operation, String countryCode, String dialogTitle, String hintText) {
        Bundle arguments = new Bundle();

        if (dialogOpen) {
            return;
        }

        Locale myLocale = getCurrentLocale();
        String translatedUnit = context.getString(R.string.lbl_acre);
        if (areaUnit.equals("ha")) {
            translatedUnit = context.getString(R.string.lbl_ha);
        }
        String finalTranslatedUnit = translatedUnit.toLowerCase(myLocale);


        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList);
        arguments.putString(OperationCostsDialogFragment.OPERATION_NAME, operation);
        arguments.putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle);
        arguments.putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, hintText);
        arguments.putString(OperationCostsDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(OperationCostsDialogFragment.CURRENCY_SYMBOL, currencySymbol);
        arguments.putString(OperationCostsDialogFragment.COUNTRY_CODE, countryCode);

        OperationCostsDialogFragment dialogFragment = new OperationCostsDialogFragment(context);
        dialogFragment.setArguments(arguments);

        dialogFragment.setOnDismissListener((operationCost, enumOperation, selectedCost, cancelled, isExactCost) -> {

            if (!cancelled && enumOperation != null) {
                double roundedCost = mathHelper.roundToNearestSpecifiedValue(selectedCost, 1000);
                switch (enumOperation) {
                    case "TILLAGE":
                        manualPloughCost = roundedCost;
                        String manualTillageText = getString(R.string.lbl_ploughing_cost_text, mathHelper.removeLeadingZero(fieldSize),
                                finalTranslatedUnit, mathHelper.formatNumber(roundedCost, null), currencySymbol);
                        if (myLocale.getLanguage().equals("sw")) {
                            manualTillageText = getString(R.string.lbl_ploughing_cost_text,
                                    finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize), mathHelper.formatNumber(roundedCost, null), currencySymbol);
                        }
                        manualPloughCostText.setText(manualTillageText);
                        break;
                    case "RIDGING":
                        manualRidgeCost = roundedCost;
                        String manualRidgeText = getString(R.string.lbl_ridging_cost_text, mathHelper.removeLeadingZero(fieldSize),
                                finalTranslatedUnit, mathHelper.formatNumber(roundedCost, null), currencySymbol);
                        if (myLocale.getLanguage().equals("sw")) {
                            manualRidgeText = getString(R.string.lbl_ridging_cost_text,
                                    finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize), mathHelper.formatNumber(roundedCost, null), currencySymbol);
                        }
                        manualRidgingCostText.setText(manualRidgeText);
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
