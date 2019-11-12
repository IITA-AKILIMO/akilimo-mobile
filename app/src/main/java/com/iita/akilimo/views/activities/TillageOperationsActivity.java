package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.TillageOperations;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.CurrencyHelper;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.OperationCostDialogFragment;

import org.jetbrains.annotations.NotNull;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TillageOperationsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_tillage_operations)
    String activityTitle;

    @BindView(R.id.implementTitle)
    TextView implementTitle;

    @BindView(R.id.tillageOperationsTitle)
    TextView tillageOperationsTitle;


    @BindView(R.id.ridgingTitle)
    TextView ridgingTitle;


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
    Button btnFinish;
    @BindView(R.id.btnCancel)
    Button btnCancel;

    @BindView(R.id.btnFragmentDialog)
    Button btnFragmentDialog;

    CurrencyHelper currencyHelper;
    TillageOperations tillageOperationsModel;

    private boolean hasTractor;
    private boolean hasPlough;
    private boolean hasRidger;
    private boolean dataValid;

    private String tillageOperation;
    public static final int DIALOG_QUEST_CODE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tillage_operations);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        currencyHelper = new CurrencyHelper();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            currency = mandatoryInfo.getCurrency();
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
        btnFragmentDialog.setOnClickListener(view -> showDialogFullscreen());
    }

    @Override
    protected void validate(boolean backPressed) {
        setData();
        if (dataValid) {
            closeActivity(backPressed);
        }
    }

    private void setData() {
        tillageOperationsModel = objectBoxEntityProcessor.getTillageOperation();
        if (tillageOperationsModel == null) {
            tillageOperationsModel = new TillageOperations();
        }

        dataValid = false;
        if (Strings.isEmptyOrWhitespace(tillageOperation)) {
            showCustomWarningDialog("Invalid selection", "Please specify the number of tillage operations on your farm");
            return;
        }

        dataValid = true;
        tillageOperationsModel.setTractorAvailable(hasTractor);
        tillageOperationsModel.setTractorHarrow(hasRidger);
        tillageOperationsModel.setTractorPlough(hasPlough);
        tillageOperationsModel.setTillageOperation(tillageOperation);
        //proceed to save
        objectBoxEntityProcessor.saveTillageOperation(tillageOperationsModel);
    }

    private void showDialogFullscreen() {
        setData();
        if (!dataValid) {
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.setCustomAnimations(R.anim.animate_slide_in_left, R.anim.animate_slide_out_right);

        OperationCostDialogFragment newFragment = new OperationCostDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(OperationCostDialogFragment.SELECTED_OPERATIONS, tillageOperationsModel);
        arguments.putString(OperationCostDialogFragment.CURRENCY_CODE, currency);
        newFragment.setArguments(arguments);
        newFragment.setRequestCode(DIALOG_QUEST_CODE);

        transaction.add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit();

        newFragment.setOnCallbackResult(new IFragmentCallBack() {
            @Override
            public void onFragmentClose(boolean hideButton) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onDataSaved() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendResult(int requestCode, @NotNull Object obj) {
                if (requestCode == DIALOG_QUEST_CODE) {
                    tillageOperationsModel = (TillageOperations) obj;
                    objectBoxEntityProcessor.saveTillageOperation(tillageOperationsModel);
                }
            }
        });
    }
}
