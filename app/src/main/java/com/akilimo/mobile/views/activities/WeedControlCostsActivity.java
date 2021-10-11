package com.akilimo.mobile.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityWeedControlCostBinding;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.entities.Currency;
import com.akilimo.mobile.entities.CurrentPractice;
import com.akilimo.mobile.entities.FieldOperationCost;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.utils.MathHelper;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.utils.enums.EnumCountry;

;import java.util.Locale;


public class WeedControlCostsActivity extends BaseActivity {


    Toolbar toolbar;
    AppCompatTextView firstWeedingOpCostTitle;
    AppCompatTextView secondWeedingOpCostTitle;
    AppCompatTextView herbicideUseTitle;
    CardView herbicideUseCard;
    AppCompatButton btnFinish;
    AppCompatButton btnCancel;
    RadioGroup rdgWeedControl;
    EditText editFirstWeedingOpCost;
    EditText editSecondWeedingOpCost;

    ActivityWeedControlCostBinding binding;

    private MathHelper mathHelper;
    private CurrentPractice currentPractice;
    private FieldOperationCost fieldOperationCost;
    private boolean usesHerbicide;
    private String weedControlTechnique;
    private double firstOperationCost;
    private double secondOperationCost;
    private int weedRadioIndex;

    private double minCost = 1.0;
    private double maxCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeedControlCostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
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
            currencyName = myCurrency.getCurrencyName();
        }

        fieldOperationCost = database.fieldOperationCostDao().findOne();
        currentPractice = database.currentPracticeDao().findOne();

        toolbar = binding.toolbar;
        firstWeedingOpCostTitle = binding.weedControlCosts.firstWeedingOpCostTitle;
        secondWeedingOpCostTitle = binding.weedControlCosts.secondWeedingOpCostTitle;
        herbicideUseTitle = binding.weedControlCosts.herbicideUseTitle;
        herbicideUseCard = binding.weedControlCosts.herbicideUseCard;
        btnFinish = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;
        rdgWeedControl = binding.weedControlCosts.rdgWeedControl;
        editFirstWeedingOpCost = binding.weedControlCosts.editFirstWeedingOpCost;
        editSecondWeedingOpCost = binding.weedControlCosts.editSecondWeedingOpCost;


        initToolbar();
        initComponent();
    }

    @Override
    public void onBackPressed() {
        validate(true);
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_weed_control));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {

        Locale myLocale = getCurrentLocale();
        String translatedUnit = context.getString(R.string.lbl_acre);
        if (areaUnit.equals("ha")) {
            translatedUnit = context.getString(R.string.lbl_ha);
        }
        String finalTranslatedUnit = translatedUnit.toLowerCase(myLocale);
        switch (countryCode) {
            case "TZ":
                currencyName = EnumCountry.Tanzania.currencyName(context);
                break;
            case "NG":
                currencyName = EnumCountry.Nigeria.currencyName(context);
                break;
        }

        if (currentPractice != null) {
            weedRadioIndex = currentPractice.getWeedRadioIndex();
            weedControlTechnique = currentPractice.getWeedControlTechnique();
            rdgWeedControl.check(weedRadioIndex);
        }
        if (fieldOperationCost != null) {
            firstOperationCost = fieldOperationCost.getFirstWeedingOperationCost();
            if (firstOperationCost > 0) {
                editFirstWeedingOpCost.setText(String.valueOf(firstOperationCost));
            }

            secondOperationCost = fieldOperationCost.getSecondWeedingOperationCost();
            if (secondOperationCost > 0) {
                editSecondWeedingOpCost.setText(String.valueOf(secondOperationCost));
            }
        }
        String firstWeedCostTitle = getString(R.string.lbl_cost_of_first_weeding_operation, currencyName, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit);
        String secondWeedCostTitle = getString(R.string.lbl_cost_of_second_weeding_operation, currencyName, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit);
        if (myLocale.getLanguage().equals("sw")) {
            firstWeedCostTitle = getString(R.string.lbl_cost_of_first_weeding_operation, currencyCode, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize));
            secondWeedCostTitle = getString(R.string.lbl_cost_of_second_weeding_operation, currencyCode, finalTranslatedUnit, mathHelper.removeLeadingZero(fieldSize));
        }
        firstWeedingOpCostTitle.setText(firstWeedCostTitle);
        secondWeedingOpCostTitle.setText(secondWeedCostTitle);

        rdgWeedControl.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdManualOnlyControl:
                    usesHerbicide = false;
                    weedControlTechnique = "manual";
                    break;
                case R.id.rdHerbicideControl:
                    usesHerbicide = true;
                    weedControlTechnique = "herbicide";
                    break;
                case R.id.rdManualHerbicideControl:
                    usesHerbicide = true;
                    weedControlTechnique = "manual_herbicide";
                    break;
            }
        });


        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
        showCustomNotificationDialog();
    }

    @Override
    protected void validate(boolean backPressed) {
        if (Strings.isEmptyOrWhitespace(weedControlTechnique)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_selection), getString(R.string.lbl_weed_control_prompt));
            return;
        }

        //get the user input
        weedRadioIndex = rdgWeedControl.getCheckedRadioButtonId();
        firstOperationCost = mathHelper.convertToDouble(editFirstWeedingOpCost.getText().toString());
        secondOperationCost = mathHelper.convertToDouble(editSecondWeedingOpCost.getText().toString());

        if (firstOperationCost <= minCost) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_cost), getString(R.string.lbl_first_weeding_costs_prompt));
            return;
        }
        if (secondOperationCost <= minCost) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_cost), getString(R.string.lbl_second_weeding_cost_prompt));
            return;
        }

        try {
            if (currentPractice == null) {
                currentPractice = new CurrentPractice();
            }
            if (fieldOperationCost == null) {
                fieldOperationCost = new FieldOperationCost();
            }

            currentPractice.setWeedControlTechnique(weedControlTechnique);
            currentPractice.setUsesHerbicide(usesHerbicide);
            currentPractice.setWeedRadioIndex(weedRadioIndex);

            database.currentPracticeDao().insert(currentPractice);

            fieldOperationCost.setFirstWeedingOperationCost(firstOperationCost);
            fieldOperationCost.setSecondWeedingOperationCost(secondOperationCost);

            database.fieldOperationCostDao().insert(fieldOperationCost);
            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.COST_OF_WEED_CONTROL.name(), true));

            closeActivity(backPressed);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }

    }
}
