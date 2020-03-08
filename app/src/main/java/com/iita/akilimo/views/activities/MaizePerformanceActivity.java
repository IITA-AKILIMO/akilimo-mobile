package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MaizePerformance;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MaizePerformanceActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_maize_performance)
    String activityTitle;

    @BindString(R.string.lbl_maize_performance_poor)
    String poorSoil;
    @BindString(R.string.lbl_maize_performance_rich)
    String richSoil;

    @BindView(R.id.rdgMaizePerformance)
    RadioGroup rdgMaizePerformance;

    @BindView(R.id.btnFinish)
    MaterialButton btnFinish;

    @BindView(R.id.btnCancel)
    MaterialButton btnCancel;

    @BindView(R.id.exceptionTitle)
    TextView exceptionTitle;


    private MaizePerformance maizePerformance;

    private String selectedMaizePerformance;
    private String maizePerformanceValue;
    private int performanceRadioIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maize_performance_activity);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);

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

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

        rdgMaizePerformance.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            exceptionTitle.setVisibility(View.GONE);
            switch (radioIndex) {
                case R.id.rdMaizeKneeHigh:
                    selectedMaizePerformance = "50";
                    maizePerformanceValue = "1";
                    exceptionTitle.setVisibility(View.VISIBLE);
                    exceptionTitle.setText(poorSoil);
                    break;
                case R.id.rdMaizeChestHigh:
                    selectedMaizePerformance = "150";
                    maizePerformanceValue = "2";
                    break;
                case R.id.rdMaizeYellowLeaves:
                    selectedMaizePerformance = "yellow";
                    maizePerformanceValue = "3";
                    break;
                case R.id.rdMaizeGreenLeaves:
                    selectedMaizePerformance = "green";
                    maizePerformanceValue = "4";
                    break;
                case R.id.rdMaizeDarkGreenLeaves:
                    selectedMaizePerformance = "dark green";
                    maizePerformanceValue = "5";
                    exceptionTitle.setVisibility(View.VISIBLE);
                    exceptionTitle.setText(richSoil);
                    break;
            }
        });

        //preset saved data if any
        maizePerformance = objectBoxEntityProcessor.getMaizePerformance();
        if (maizePerformance != null) {
            performanceRadioIndex = maizePerformance.getPerformanceRadioIndex();
            rdgMaizePerformance.check(performanceRadioIndex);
        }

    }

    @Override
    protected void validate(boolean backPressed) {

        if (Strings.isEmptyOrWhitespace(selectedMaizePerformance) || Strings.isEmptyOrWhitespace(maizePerformanceValue)) {
            showCustomWarningDialog("Invalid selection", "Please provide a valid maize performance selection");
            return;
        }

        performanceRadioIndex = rdgMaizePerformance.getCheckedRadioButtonId();
        maizePerformance = objectBoxEntityProcessor.getMaizePerformance();
        if (maizePerformance == null) {
            maizePerformance = new MaizePerformance();
        }
        maizePerformance.setPerformanceRadioIndex(performanceRadioIndex);
        maizePerformance.setMaizePerformance(selectedMaizePerformance);
        maizePerformance.setPerformanceValue(maizePerformanceValue);

        objectBoxEntityProcessor.saveMaizePerformanceData(maizePerformance);
        closeActivity(backPressed);
    }
}
