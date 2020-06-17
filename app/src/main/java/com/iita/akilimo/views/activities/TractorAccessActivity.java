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
import com.iita.akilimo.databinding.ActivityTractorAccessBinding;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.inherit.CostBaseActivity;
import com.iita.akilimo.models.OperationCost;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumOperation;
import com.iita.akilimo.utils.enums.EnumOperationType;

import com.iita.akilimo.views.fragments.dialog.OperationCostsDialogFragment;

import java.util.ArrayList;

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
    OperationCosts operationCosts;
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
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(this);
        mathHelper = new MathHelper();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            currency = mandatoryInfo.getCurrency();
            areaUnit = mandatoryInfo.getAreaUnit();
            fieldSize = mandatoryInfo.getAreaSize();
            countryCode = mandatoryInfo.getCountryCode();
        }

        toolbar = binding.toolbar;
        implementTitle = binding.tractorAccess.implementTitle;
        rdgTractor = binding.tractorAccess.rdgTractor;
        implementCard = binding.tractorAccess.implementCard;
        chkPlough = binding.tractorAccess.chkPlough;
        chkRidger = binding.tractorAccess.chkRidger;

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

        chkPlough.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasPlough = isChecked;
            if (buttonView.isPressed() && isChecked && !dialogOpen) {
                String title = (getString(R.string.lbl_tractor_plough_cost, fieldSize, areaUnit));
                loadOperationCost(EnumOperation.TILLAGE.name(), EnumOperationType.MECHANICAL.name(), title);
            }
        });
        chkRidger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasRidger = isChecked;
            if (buttonView.isPressed() && isChecked && !dialogOpen) {
                String title = (getString(R.string.lbl_tractor_ridge_cost, fieldSize, areaUnit));
                loadOperationCost(EnumOperation.RIDGING.name(), EnumOperationType.MECHANICAL.name(), title);
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
    protected void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operation, String enumCountry, String dialogTitle) {
        Bundle arguments = new Bundle();

        if (dialogOpen) {
            return;
        }

        showCustomNotificationDialog();
        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList);
        arguments.putString(OperationCostsDialogFragment.OPERATION_NAME, operation);
        arguments.putString(OperationCostsDialogFragment.SELECTED_COUNTRY, enumCountry);
        arguments.putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle);

        OperationCostsDialogFragment dialogFragment = new OperationCostsDialogFragment();
        dialogFragment.setArguments(arguments);

        dialogFragment.setOnDismissListener((operationCost, enumOperation, selectedCost, cancelled, isExactCost) -> {
            if (!cancelled && enumOperation != null) {
                switch (enumOperation) {
                    case "TILLAGE":
                        tractorPloughCost = selectedCost;
                        exactPloughCost = isExactCost;
                        break;
                    case "RIDGING":
                        tractorRidgeCost = selectedCost;
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
