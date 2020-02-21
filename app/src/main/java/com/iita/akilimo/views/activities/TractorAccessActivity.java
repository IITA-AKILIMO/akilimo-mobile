package com.iita.akilimo.views.activities;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
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

public class TractorAccessActivity extends BaseActivity {

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
    Button btnFinish;
    @BindView(R.id.btnCancel)
    Button btnCancel;

    private boolean hasTractor;
    private boolean hasPlough;
    private boolean hasRidger;
    private boolean hasHarrow;
    private boolean isDialogOpen;
    private boolean exactPloughCost;
    private boolean exactRidgeCost;

    private double ploughCost;
    private double ridgeCost;
    private String ploughCostText;
    private String ridgingCostText;


    private boolean dataValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tractor_access);
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
            if (buttonView.isPressed() && isChecked) {
                showOperationCostDialog(hasPlough, false);
            }
        });
        chkRidger.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasRidger = isChecked;
            if (buttonView.isPressed() && isChecked) {
                showOperationCostDialog(false, hasRidger);
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

        operationCosts.setTractorPloughCost(ploughCost);
        operationCosts.setTractorRidgeCost(ridgeCost);
        operationCosts.setExactTractorPloughPrice(exactPloughCost);
        operationCosts.setExactTractorRidgePrice(exactRidgeCost);

        objectBoxEntityProcessor.saveOperationCosts(operationCosts);

        dataValid = true;
    }

    private void showOperationCostDialog(final boolean isPloughCost, final boolean isRidgeCost) {
        if (isDialogOpen) {
            //do not open multiple dialogs
            return;
        }

        exactRidgeCost = false;
        exactPloughCost = false;
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_plough_ridge_costs);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final TextView ploughCostTitle = dialog.findViewById(R.id.ploughCostTitle);
        final TextView ridgeCostTitle = dialog.findViewById(R.id.ridgeCostTitle);

        final EditText etExactPloughCost = dialog.findViewById(R.id.etExactPloughCost);
        final EditText etExactRidgeCost = dialog.findViewById(R.id.etExactRidgeCost);

        final RadioGroup rdgPloughCost = dialog.findViewById(R.id.rdgPloughCost);
        final RadioGroup rdgRidgeCost = dialog.findViewById(R.id.rdgRidgeCost);

        ploughCostTitle.setText(getString(R.string.lbl_tractor_plough_cost, fieldSize, areaUnit));
        ridgeCostTitle.setText(getString(R.string.lbl_tractor_ridge_cost, fieldSize, areaUnit));
        etExactRidgeCost.setText(ridgingCostText);
        etExactPloughCost.setText(ploughCostText);

        if (isPloughCost) {
            rdgPloughCost.setVisibility(View.VISIBLE);
            ploughCostTitle.setVisibility(View.VISIBLE);
            rdgRidgeCost.setVisibility(View.GONE);
            ridgeCostTitle.setVisibility(View.GONE);
        }

        if (isRidgeCost) {
            rdgPloughCost.setVisibility(View.GONE);
            ploughCostTitle.setVisibility(View.GONE);
            rdgRidgeCost.setVisibility(View.VISIBLE);
            ridgeCostTitle.setVisibility(View.VISIBLE);
        }

        rdgPloughCost.setOnCheckedChangeListener((group, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rd_exact_plough_cost:
                    exactPloughCost = true;
                    dialog.findViewById(R.id.lytExactPlough).setVisibility(View.VISIBLE);
                    break;
            }
        });

        rdgRidgeCost.setOnCheckedChangeListener((group, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rd_exact_ridge_cost:
                    exactRidgeCost = true;
                    dialog.findViewById(R.id.lytExactRidge).setVisibility(View.VISIBLE);
                    break;
            }
        });

        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> {
            isDialogOpen = false;
            if (isPloughCost) {
                chkPlough.setChecked(false);
            } else if (isRidgeCost) {
                chkRidger.setChecked(false);
            }
            dialog.dismiss();
        });

        dialog.findViewById(R.id.bt_submit).setOnClickListener(view -> {
            if (isPloughCost) {
                ploughCostText = etExactPloughCost.getText().toString().trim();
                ploughCost = mathHelper.convertToDouble(ploughCostText);
                if (ploughCost <= 0) {
                    Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                } else {
                    dialog.dismiss();
                    isDialogOpen = false;
                }
            }

            if (isRidgeCost) {
                ridgingCostText = etExactRidgeCost.getText().toString().trim();
                ridgeCost = mathHelper.convertToDouble(ridgingCostText);
                if (ridgeCost <= 0) {
                    Snackbar.make(view, "Please enter a valid amount", Snackbar.LENGTH_LONG).show();
                } else {
                    dialog.dismiss();
                    isDialogOpen = false;
                }
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
        isDialogOpen = true;
    }
}
