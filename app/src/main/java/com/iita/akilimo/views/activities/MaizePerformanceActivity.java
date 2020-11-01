package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;


import com.google.android.gms.common.util.Strings;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityMaizePerformanceActivityBinding;
import com.iita.akilimo.entities.MaizePerformance;
import com.iita.akilimo.inherit.BaseActivity;

;


public class MaizePerformanceActivity extends BaseActivity {
    String activityTitle;
    String poorSoil;
    String richSoil;

    Toolbar toolbar;
    RadioGroup rdgMaizePerformance;
    AppCompatButton btnFinish;
    AppCompatButton btnCancel;
    TextView exceptionTitle;

    ActivityMaizePerformanceActivityBinding binding;


    private MaizePerformance maizePerformance;

    private String selectedMaizePerformance;
    private String maizePerformanceValue;
    private int performanceRadioIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaizePerformanceActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        database = AppDatabase.getDatabase(context);

        toolbar = binding.toolbar;
        rdgMaizePerformance = binding.rdgMaizePerformance;
        exceptionTitle = binding.exceptionTitle;
        btnFinish = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;

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
        getSupportActionBar().setTitle(getString(R.string.title_activity_maize_performance));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {

        poorSoil = getString(R.string.lbl_maize_performance_poor);
        richSoil = getString(R.string.lbl_maize_performance_rich);


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
        maizePerformance = database.maizePerformanceDao().findOne();
        if (maizePerformance != null) {
            performanceRadioIndex = maizePerformance.getPerformanceRadioIndex();
            rdgMaizePerformance.check(performanceRadioIndex);
        }

    }

    @Override
    protected void validate(boolean backPressed) {

        if (Strings.isEmptyOrWhitespace(selectedMaizePerformance) || Strings.isEmptyOrWhitespace(maizePerformanceValue)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_selection), getString(R.string.lbl_maize_performance_prompt));
            return;
        }

        performanceRadioIndex = rdgMaizePerformance.getCheckedRadioButtonId();
        try {

            if (maizePerformance == null) {
                maizePerformance = new MaizePerformance();
            }
            maizePerformance.setPerformanceRadioIndex(performanceRadioIndex);
            maizePerformance.setMaizePerformance(selectedMaizePerformance);
            maizePerformance.setPerformanceValue(maizePerformanceValue);

            database.maizePerformanceDao().insert(maizePerformance);
            closeActivity(backPressed);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

}
