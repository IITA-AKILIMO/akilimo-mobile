package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.ActivityWeedControlCostBinding;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;

;


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
    private OperationCosts operationCosts;
    private boolean usesHerbicide;
    private String weedControlTechnique;
    private double firstOperationCost;
    private double secondOperationCost;
    private int weedRadioIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWeedControlCostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ormProcessor = new OrmProcessor();
        context = this;
        mathHelper = new MathHelper();

        MandatoryInfo mandatoryInfo = ormProcessor.getMandatoryInfo();
        operationCosts = ormProcessor.getOperationCosts();
        currentPractice = ormProcessor.getCurrentPractice();
        if (mandatoryInfo != null) {
            currency = mandatoryInfo.getCurrency();
        }

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

        if (currentPractice != null) {
            weedRadioIndex = currentPractice.getWeedRadioIndex();
            weedControlTechnique = currentPractice.getWeedControlTechnique();
            rdgWeedControl.check(weedRadioIndex);
        }
        if (operationCosts != null) {
            firstOperationCost = operationCosts.getFirstWeedingOperationCost();
            if (firstOperationCost > 0) {
                editFirstWeedingOpCost.setText(String.valueOf(firstOperationCost));
            }

            secondOperationCost = operationCosts.getSecondWeedingOperationCost();
            if (secondOperationCost > 0) {
                editSecondWeedingOpCost.setText(String.valueOf(secondOperationCost));
            }
        }
        firstWeedingOpCostTitle.setText(getString(R.string.lbl_cost_of_first_weeding_operation, currency));
        secondWeedingOpCostTitle.setText(getString(R.string.lbl_cost_of_second_weeding_operation, currency));

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

        if (firstOperationCost <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_cost), getString(R.string.lbl_first_weeding_costs_prompt));
            return;
        }
        if (secondOperationCost <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_cost), getString(R.string.lbl_second_weeding_cost_prompt));
            return;
        }


        currentPractice = ormProcessor.getCurrentPractice();
        operationCosts = ormProcessor.getOperationCosts();

        try {
            if (currentPractice == null) {
                currentPractice = new CurrentPractice();
            }
            if (operationCosts == null) {
                operationCosts = new OperationCosts();
            }

            currentPractice.setWeedControlTechnique(weedControlTechnique);
            currentPractice.setUsesHerbicide(usesHerbicide);
            currentPractice.setWeedRadioIndex(weedRadioIndex);

            operationCosts.setFirstWeedingOperationCost(firstOperationCost);
            operationCosts.setSecondWeedingOperationCost(secondOperationCost);

            closeActivity(backPressed);
        } catch (
                Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }

    }
}
