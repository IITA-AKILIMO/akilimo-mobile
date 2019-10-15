package com.iita.akilimo.views.activities;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.TillageOperations;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.CurrencyHelper;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TillageOperationsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_tillage_operations)
    String activityTitle;

    @BindView(R.id.implementTitle)
    AppCompatTextView implementTitle;

    @BindView(R.id.tillageOperationsTitle)
    AppCompatTextView tillageOperationsTitle;


    @BindView(R.id.ridgingTitle)
    AppCompatTextView ridgingTitle;


    @BindView(R.id.implementCard)
    CardView implementCard;

    @BindView(R.id.tillageOperationsCard)
    CardView tillageOperationsCard;

    @BindView(R.id.ridgingCard)
    CardView ridgingCard;


    @BindView(R.id.rdgTractor)
    RadioGroup rdgTractor;

    @BindView(R.id.rdgTillageOperations)
    RadioGroup rdgTillageOperations;

    @BindView(R.id.rdgRidging)
    RadioGroup rdgRidging;


    @BindView(R.id.chkPlough)
    CheckBox chkPlough;

    @BindView(R.id.chkRidger)
    CheckBox chkRidger;

    @BindView(R.id.btnFinish)
    MaterialButton btnFinish;

    @BindView(R.id.btnCancel)
    MaterialButton btnCancel;


    CurrencyHelper currencyHelper;
    TillageOperations tillageOperationsModel;

    private boolean hasTractor;
    private boolean hasPlough;
    private boolean hasRidger;

    private String tillageOperation;

    private boolean dataIsValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tillage_operations);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        currencyHelper = new CurrencyHelper();

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

        rdgTillageOperations.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdNone:
                    tillageOperation = "NA";
                    break;
                case R.id.rdSinglePlough:
                    tillageOperation = "single";
                    break;
            }
        });

        chkPlough.setOnCheckedChangeListener((compoundButton, checked) -> hasPlough = checked);
        chkRidger.setOnCheckedChangeListener((compoundButton, checked) -> hasRidger = checked);

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void validate(boolean backPressed) {
        if (Strings.isEmptyOrWhitespace(tillageOperation)) {
            showCustomWarningDialog("Invalid selection", "Please specify the number of tillage operations on your farm");
            return;
        }

        tillageOperationsModel = objectBoxEntityProcessor.getTillageOperation();
        if (tillageOperationsModel == null) {
            tillageOperationsModel = new TillageOperations();
        }

        tillageOperationsModel.setTractorAvailable(hasTractor);
        tillageOperationsModel.setTractorHarrow(hasRidger);
        tillageOperationsModel.setTractorPlough(hasPlough);
        tillageOperationsModel.setTillageOperation(tillageOperation);
        //proceed to save
        objectBoxEntityProcessor.saveTillageOperation(tillageOperationsModel);
        closeActivity(backPressed);
    }
}
