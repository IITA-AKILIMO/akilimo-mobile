package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.TillageOperations;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class HerbicideUseActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.title_herbicide_use)
    String activityTitle;


    @BindView(R.id.herbicideUseTitle)
    AppCompatTextView herbicideUseTitle;

    @BindView(R.id.herbicideUseCard)
    CardView herbicideUseCard;

    @BindView(R.id.btnFinish)
    MaterialButton btnFinish;

    @BindView(R.id.btnCancel)
    MaterialButton btnCancel;

    @BindView(R.id.rdgWeedControl)
    RadioGroup rdgWeedControl;

    TillageOperations tillageOperationsModel;

    private boolean usesHerbicide;
    private String weedControlTechnique;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herbicide_use);
        ButterKnife.bind(this);
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);
        context = this;

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

        tillageOperationsModel = objectBoxEntityProcessor.getTillageOperation();
        if (tillageOperationsModel == null) {
            tillageOperationsModel = new TillageOperations();
        }

        tillageOperationsModel.setUsesHerbicide(usesHerbicide);
        tillageOperationsModel.setWeedControlTechnique(weedControlTechnique);
        //proceed to save
        objectBoxEntityProcessor.saveTillageOperation(tillageOperationsModel);
        closeActivity(backPressed);
    }
}
