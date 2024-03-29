package com.akilimo.mobile.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.toolbox.Volley;

import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.entities.Currency;
import com.akilimo.mobile.entities.CurrentPractice;
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

public class TractorAccessActivity extends CostBaseActivity {


    Toolbar toolbar;
    TextView implementTitle;
    RadioGroup rdgTractor;
    CardView implementCard;
    CheckBox chkPlough;
    CheckBox chkRidger;

    AppCompatButton btnFinish;
    AppCompatButton btnCancel;

    ActivityTractorAccessBinding binding;
    MathHelper mathHelper;
    FieldOperationCost fieldOperationCost;
    CurrentPractice currentPractice;


    private boolean hasTractor;
    private boolean hasPlough;
    private boolean hasRidger;
    private boolean hasHarrow;
    private boolean isDialogOpen;

    private boolean exactPloughCost;
    private boolean exactRidgeCost;

    private String ploughCostText;
    private String ridgingCostText;

    private boolean dataValid;
    private double tractorPloughCost;
    private double tractorRidgeCost;
    private boolean dialogOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTractorAccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);

        queue = Volley.newRequestQueue(this);
        mathHelper = new MathHelper();

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

        toolbar = binding.toolbar;
        implementTitle = binding.tractorAccess.implementTitle;
        rdgTractor = binding.tractorAccess.rdgTractor;
        implementCard = binding.tractorAccess.implementCard;
        chkPlough = binding.tractorAccess.chkPlough;
        chkRidger = binding.tractorAccess.chkRidger;
        btnFinish = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;

        initToolbar();
        initComponent();
        fieldOperationCost = database.fieldOperationCostDao().findOne();
        currentPractice = database.currentPracticeDao().findOne();
        if (fieldOperationCost != null) {
            tractorPloughCost = fieldOperationCost.getTractorPloughCost();
            tractorRidgeCost = fieldOperationCost.getTractorRidgeCost();
        }
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_tillage_operations));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        rdgTractor.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdYesTractor:
                    hasTractor = true;
                    implementTitle.setVisibility(View.VISIBLE);
                    implementCard.setVisibility(View.VISIBLE);
                    break;
                default:
                case R.id.rdNoTractor:
                    hasTractor = false;
                    implementTitle.setVisibility(View.GONE);
                    implementCard.setVisibility(View.GONE);
                    chkRidger.setChecked(false);
                    chkPlough.setChecked(false);
                    break;
            }
        });

        Locale myLocale = getCurrentLocale();
        String translatedUnit = context.getString(R.string.lbl_acre);
        if (areaUnit.equals("ha")) {
            translatedUnit = context.getString(R.string.lbl_ha);
        }
        String finalTranslatedUnit = translatedUnit.toLowerCase(myLocale);


        chkPlough.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasPlough = isChecked;
            if (buttonView.isPressed() && isChecked && !dialogOpen) {
                String title = (getString(R.string.lbl_tractor_plough_cost, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit));
                String hintText = (getString(R.string.lbl_tractor_plough_cost_hint, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit));
                if (myLocale.getLanguage().equals("sw")) {
                    title = (getString(R.string.lbl_tractor_plough_cost, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize)));
                    hintText = (getString(R.string.lbl_tractor_plough_cost_hint, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize)));
                }
                loadOperationCost(EnumOperation.TILLAGE.name(), EnumOperationType.MECHANICAL.operationName(), title, hintText);
            }
        });
        chkRidger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasRidger = isChecked;
            if (buttonView.isPressed() && isChecked && !dialogOpen) {
                String title = (getString(R.string.lbl_tractor_ridge_cost, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit));
                String hintText = (getString(R.string.lbl_tractor_ridge_cost_hint, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit));
                if (myLocale.getLanguage().equals("sw")) {
                    title = (getString(R.string.lbl_tractor_ridge_cost, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize)));
                    hintText = (getString(R.string.lbl_tractor_ridge_cost_hint, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize)));
                }
                loadOperationCost(EnumOperation.RIDGING.name(), EnumOperationType.MECHANICAL.operationName(), title, hintText);
            }
        });
        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void validate(boolean backPressed) {
        setData();
        if (dataValid) {
            closeActivity(backPressed);
        }
    }

    private void setData() {
        try {
            if (fieldOperationCost == null) {
                fieldOperationCost = new FieldOperationCost();
            }
            if (currentPractice == null) {
                currentPractice = new CurrentPractice();
            }

            dataValid = true;
            currentPractice.setTractorAvailable(hasTractor);
            currentPractice.setTractorPlough(hasPlough);
            currentPractice.setTractorHarrow(hasHarrow);
            currentPractice.setTractorRidger(hasRidger);

            database.currentPracticeDao().insert(currentPractice);

            fieldOperationCost.setTractorPloughCost(tractorPloughCost);
            fieldOperationCost.setTractorRidgeCost(tractorRidgeCost);
            fieldOperationCost.setExactTractorPloughPrice(exactPloughCost);
            fieldOperationCost.setExactTractorRidgePrice(exactRidgeCost);

            database.fieldOperationCostDao().insert(fieldOperationCost);
            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.TRACTOR_ACCESS.name(), true));
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Sentry.captureException(ex);
        }
    }

    @Override
    protected void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operation, String countrycode, String dialogTitle, String hintText) {
        Bundle arguments = new Bundle();

        if (dialogOpen) {
            return;
        }

        showCustomNotificationDialog();
        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList);
        arguments.putString(OperationCostsDialogFragment.OPERATION_NAME, operation);
        arguments.putString(OperationCostsDialogFragment.CURRENCY_CODE, currency);
        arguments.putString(OperationCostsDialogFragment.CURRENCY_SYMBOL, currencySymbol);
        arguments.putString(OperationCostsDialogFragment.COUNTRY_CODE, countrycode);
        arguments.putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle);
        arguments.putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, hintText);

        OperationCostsDialogFragment dialogFragment = new OperationCostsDialogFragment(context);
        dialogFragment.setArguments(arguments);

        dialogFragment.setOnDismissListener((operationCost, enumOperation, selectedCost, cancelled, isExactCost) -> {
            if (!cancelled && enumOperation != null) {
                double roundedCost = mathHelper.roundToNearestSpecifiedValue(selectedCost, 1000);
                switch (enumOperation) {
                    case "TILLAGE":
                        tractorPloughCost = roundedCost;
                        exactPloughCost = isExactCost;
                        break;
                    case "RIDGING":
                        tractorRidgeCost = roundedCost;
                        exactRidgeCost = isExactCost;
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
