package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WeedControlCostsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_weed_control)
    String activityTitle;


    @BindView(R.id.firstWeedingOpCostTitle)
    AppCompatTextView firstWeedingOpCostTitle;
    @BindView(R.id.secondWeedingOpCostTitle)
    AppCompatTextView secondWeedingOpCostTitle;

    @BindView(R.id.herbicideUseTitle)
    AppCompatTextView herbicideUseTitle;

    @BindView(R.id.herbicideUseCard)
    CardView herbicideUseCard;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;

    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

    @BindView(R.id.rdgWeedControl)
    RadioGroup rdgWeedControl;

    @BindView(R.id.editFirstWeedingOpCost)
    EditText editFirstWeedingOpCost;
    @BindView(R.id.editSecondWeedingOpCost)
    EditText editSecondWeedingOpCost;

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
        setContentView(R.layout.activity_weed_control_cost);
        ButterKnife.bind(this);
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);
        context = this;
        mathHelper = new MathHelper();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        operationCosts = objectBoxEntityProcessor.getOperationCosts();
        currentPractice = objectBoxEntityProcessor.getCurrentPractice();
        if (mandatoryInfo != null) {
            currency = mandatoryInfo.countryEnum.currency();
        }
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
        getSupportActionBar().setTitle(activityTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {

        if (currentPractice != null) {
            weedRadioIndex = currentPractice.getWeedRadioIndex();
            rdgWeedControl.check(weedRadioIndex);
        }
        if (operationCosts != null) {
            firstOperationCost = operationCosts.getFirstWeedingOperationCost();
            secondOperationCost = operationCosts.getSecondWeedingOperationCost();
            editFirstWeedingOpCost.setText(String.valueOf(firstOperationCost));
            editSecondWeedingOpCost.setText(String.valueOf(secondOperationCost));
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
    }

    @Override
    protected void validate(boolean backPressed) {
        if (Strings.isEmptyOrWhitespace(weedControlTechnique)) {
            showCustomWarningDialog("Invalid selection", "Please specify how you control weeds on your farm");
            return;
        }

        //get the user input
        weedRadioIndex = rdgWeedControl.getCheckedRadioButtonId();
        firstOperationCost = mathHelper.convertToDouble(editFirstWeedingOpCost.getText().toString());
        secondOperationCost = mathHelper.convertToDouble(editSecondWeedingOpCost.getText().toString());

        if (firstOperationCost <= 0) {
            showCustomWarningDialog("Invalid cost amount", "Please specify cost for first weeding operation");
            return;
        }
        if (secondOperationCost <= 0) {
            showCustomWarningDialog("Invalid cost amount", "Please specify cost for second weeding operation");
            return;
        }

        currentPractice = objectBoxEntityProcessor.getCurrentPractice();
        if (currentPractice == null) {
            currentPractice = new CurrentPractice();
        }
        operationCosts = objectBoxEntityProcessor.getOperationCosts();
        if (operationCosts == null) {
            operationCosts = new OperationCosts();
        }

        currentPractice.setWeedControlTechnique(weedControlTechnique);
        currentPractice.setUsesHerbicide(usesHerbicide);
        currentPractice.setWeedRadioIndex(weedRadioIndex);

        operationCosts.setFirstWeedingOperationCost(firstOperationCost);
        operationCosts.setSecondWeedingOperationCost(secondOperationCost);
        //proceed to save
        objectBoxEntityProcessor.saveCurrentPractice(currentPractice);
        objectBoxEntityProcessor.saveOperationCosts(operationCosts);

        closeActivity(backPressed);
    }
}
