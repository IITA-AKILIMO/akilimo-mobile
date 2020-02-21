package com.iita.akilimo.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.entities.TillageOperations;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ManualTillageCostActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_manual_tillage_cost)
    String activityTitle;

    @BindView(R.id.manualPloughCostTitle)
    TextView manualPloughCostTitle;
    @BindView(R.id.manualRidgeCostTitle)
    TextView manualRidgeCostTitle;

    @BindView(R.id.rdgManualTillageCost)
    RadioGroup rdgManualTillageCost;
    @BindView(R.id.rdgManualRidgeCost)
    RadioGroup rdgManualRidgeCost;

    @BindView(R.id.btnFinish)
    Button btnFinish;
    @BindView(R.id.btnCancel)
    Button btnCancel;

    MathHelper mathHelper;
    OperationCosts operationCosts;

    private double manualPloughCost = 0;
    private double manualRidgeCost = 0;
    private boolean dataValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_tillage_cost);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        mathHelper = new MathHelper();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            currency = mandatoryInfo.getCurrency();
            areaUnit = mandatoryInfo.getAreaUnit();
            fieldSize = mandatoryInfo.getAreaSize();
        }

        operationCosts = objectBoxEntityProcessor.getOperationCosts();
        if (operationCosts == null) {
            operationCosts = new OperationCosts();
        }
        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(activityTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        rdgManualTillageCost.setOnCheckedChangeListener((group, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdSpecifyTillageCost:
                    manualPloughCost = 0;
                    break;
                default:
                    break;
            }
        });

        rdgManualRidgeCost.setOnCheckedChangeListener((group, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdSpecifyRidgeCost:
                    manualRidgeCost = 0;
                    break;
                default:
                    break;
            }
        });


        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

        String ploughTitle = context.getString(R.string.lbl_manual_tillage_cost, fieldSize, areaUnit);
        String ridgeTitle = context.getString(R.string.lbl_manual_ridge_cost, fieldSize, areaUnit);
        manualPloughCostTitle.setText(ploughTitle);
        manualRidgeCostTitle.setText(ridgeTitle);
    }

    @Override
    protected void validate(boolean backPressed) {
        setData();
        if (dataValid) {
            closeActivity(backPressed);
        }
    }

    private void setData() {
        operationCosts = objectBoxEntityProcessor.getOperationCosts();
        if (operationCosts == null) {
            operationCosts = new OperationCosts();
        }

        dataValid = false;
        if (manualPloughCost <= 0) {
            showCustomWarningDialog("Invalid selection", "Please specify the cost of manual ploughing on your farm");
            return;
        }

        if (manualRidgeCost <= 0) {
            showCustomWarningDialog("Invalid selection", "Please specify the cost of manual ridging on your farm");
            return;
        }

        dataValid = true;
        operationCosts.setManualPloughCost(manualPloughCost);
        operationCosts.setManualRidgeCost(manualRidgeCost);
        //proceed to save
        objectBoxEntityProcessor.saveOperationCosts(operationCosts);
    }
}
