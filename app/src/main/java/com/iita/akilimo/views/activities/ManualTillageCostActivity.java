package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.toolbox.Volley;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.inherit.CostBaseActivity;
import com.iita.akilimo.models.OperationCost;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumOperation;
import com.iita.akilimo.utils.enums.EnumOperationType;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.OperationCostsDialogFragment;

import java.util.ArrayList;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ManualTillageCostActivity extends CostBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_manual_tillage_cost)
    String activityTitle;

    @BindView(R.id.manualPloughCostTitle)
    TextView manualPloughCostTitle;

    @BindView(R.id.manualRidgeCostTitle)
    TextView manualRidgeCostTitle;

    @BindView(R.id.manualPloughCostText)
    TextView manualPloughCostText;

    @BindView(R.id.manualRidgingCostText)
    TextView manualRidgingCostText;


    @BindView(R.id.btnPloughCost)
    AppCompatButton btnPloughCost;
    @BindView(R.id.btnRidgeCost)
    AppCompatButton btnRidgeCost;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;
    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

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
        queue = Volley.newRequestQueue(this);
        mathHelper = new MathHelper();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            currency = mandatoryInfo.getCurrency();
            areaUnit = mandatoryInfo.getAreaUnit();
            fieldSize = mandatoryInfo.getAreaSize();
            enumCountry = mandatoryInfo.getCountryEnum();
        }
        initToolbar();
        initComponent();

        operationCosts = objectBoxEntityProcessor.getOperationCosts();
        if (operationCosts != null) {
            manualPloughCost = operationCosts.getManualPloughCost();
            manualRidgeCost = operationCosts.getManualRidgeCost();

            manualPloughCostText.setText(getString(R.string.lbl_ploughing_cost_text, fieldSize, areaUnit, manualPloughCost, enumCountry.currency()));
            manualRidgingCostText.setText(getString(R.string.lbl_ridging_cost_text, fieldSize, areaUnit, manualRidgeCost, enumCountry.currency()));

        }
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
        String ploughTitle = context.getString(R.string.lbl_manual_tillage_cost, fieldSize, areaUnit);
        String ridgeTitle = context.getString(R.string.lbl_manual_ridge_cost, fieldSize, areaUnit);

        btnPloughCost.setOnClickListener(view -> loadOperationCost(EnumOperation.TILLAGE, EnumOperationType.MANUAL, ploughTitle));

        btnRidgeCost.setOnClickListener(view -> loadOperationCost(EnumOperation.RIDGING, EnumOperationType.MANUAL, ridgeTitle));

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

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

    @Override
    protected void showDialogFullscreen(ArrayList<OperationCost> operationCostList, EnumOperation operation, EnumCountry enumCountry, String dialogTitle) {
        Bundle arguments = new Bundle();

        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList);
        arguments.putParcelable(OperationCostsDialogFragment.OPERATION_NAME, operation);
        arguments.putParcelable(OperationCostsDialogFragment.SELECTED_COUNTRY, enumCountry);

        OperationCostsDialogFragment dialogFragment = new OperationCostsDialogFragment();
        dialogFragment.setArguments(arguments);

        dialogFragment.setOnDismissListener((operationCost, enumOperation, selectedCost, cancelled,isExactCost) -> {
            if (!cancelled && enumOperation != null) {
                switch (enumOperation) {
                    case TILLAGE:
                        manualPloughCost = selectedCost;
                        manualPloughCostText.setText(getString(R.string.lbl_ploughing_cost_text, fieldSize, areaUnit, selectedCost, enumCountry.currency()));
                        break;
                    case RIDGING:
                        manualRidgeCost = selectedCost;
                        manualRidgingCostText.setText(getString(R.string.lbl_ridging_cost_text, fieldSize, areaUnit, selectedCost, enumCountry.currency()));
                        break;
                }
            }
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
            dialogFragment.show(getSupportFragmentManager(), OperationCostsDialogFragment.ARG_ITEM_ID);
        }
    }
}
