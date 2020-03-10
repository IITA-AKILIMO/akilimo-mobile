package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.toolbox.Volley;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.CurrentPractice;
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

public class TractorAccessActivity extends CostBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_tillage_operations)
    String activityTitle;
    @BindView(R.id.implementTitle)
    TextView implementTitle;

    @BindView(R.id.rdgTractor)
    RadioGroup rdgTractor;
    @BindView(R.id.implementCard)
    CardView implementCard;

    @BindView(R.id.chkPlough)
    CheckBox chkPlough;
    @BindView(R.id.chkRidger)
    CheckBox chkRidger;

    MathHelper mathHelper;
    OperationCosts operationCosts;
    CurrentPractice currentPractice;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;
    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

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
        setContentView(R.layout.activity_tractor_access);
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
            tractorPloughCost = operationCosts.getTractorPloughCost();
            tractorRidgeCost = operationCosts.getTractorRidgeCost();

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

        chkPlough.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasPlough = isChecked;
            if (buttonView.isPressed() && isChecked && !dialogOpen) {
                String title = (getString(R.string.lbl_tractor_plough_cost, fieldSize, areaUnit));
                loadOperationCost(EnumOperation.TILLAGE, EnumOperationType.MECHANICAL, title);
            }
        });
        chkRidger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasRidger = isChecked;
            if (buttonView.isPressed() && isChecked && !dialogOpen) {
                String title = (getString(R.string.lbl_tractor_ridge_cost, fieldSize, areaUnit));
                loadOperationCost(EnumOperation.RIDGING, EnumOperationType.MECHANICAL, title);
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
        operationCosts = objectBoxEntityProcessor.getOperationCosts();
        currentPractice = objectBoxEntityProcessor.getCurrentPractice();
        if (operationCosts == null) {
            operationCosts = new OperationCosts();
        }
        if (currentPractice == null) {
            currentPractice = new CurrentPractice();
        }

        currentPractice.setTractorAvailable(hasTractor);
        currentPractice.setTractorPlough(hasPlough);
        currentPractice.setTractorHarrow(hasHarrow);
        currentPractice.setTractorRidger(hasRidger);

        operationCosts.setTractorPloughCost(tractorPloughCost);
        operationCosts.setTractorRidgeCost(tractorRidgeCost);

        operationCosts.setExactTractorPloughPrice(exactPloughCost);
        operationCosts.setExactTractorRidgePrice(exactRidgeCost);

        objectBoxEntityProcessor.saveOperationCosts(operationCosts);

        dataValid = true;
    }

    @Override
    protected void showDialogFullscreen(ArrayList<OperationCost> operationCostList, EnumOperation operation, EnumCountry enumCountry, String dialogTitle) {
        Bundle arguments = new Bundle();

        if (dialogOpen) {
            return;
        }
        dialogOpen = true;
        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList);
        arguments.putParcelable(OperationCostsDialogFragment.OPERATION_NAME, operation);
        arguments.putParcelable(OperationCostsDialogFragment.SELECTED_COUNTRY, enumCountry);
        arguments.putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle);

        OperationCostsDialogFragment dialogFragment = new OperationCostsDialogFragment();
        dialogFragment.setArguments(arguments);

        dialogFragment.setOnDismissListener((operationCost, enumOperation, selectedCost, cancelled, isExactCost) -> {
            if (!cancelled && enumOperation != null) {
                switch (enumOperation) {
                    case TILLAGE:
                        tractorPloughCost = selectedCost;
                        exactPloughCost = isExactCost;
                        break;
                    case RIDGING:
                        tractorRidgeCost = selectedCost;
                        exactRidgeCost = isExactCost;
                        break;
                }
            }
            dialogOpen = true;
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
