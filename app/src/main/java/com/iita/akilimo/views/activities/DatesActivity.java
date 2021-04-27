package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityDatesBinding;
import com.iita.akilimo.entities.AdviceStatus;
import com.iita.akilimo.entities.ScheduledDate;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.DateHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.views.fragments.dialog.DateDialogPickerFragment;

import org.joda.time.LocalDate;

public class DatesActivity extends BaseActivity {
    Toolbar toolbar;

    RelativeLayout lytPlantingHarvest;

    AppCompatButton btnPickPlantingDate;
    AppCompatButton btnPickHarvestDate;
    AppCompatButton btnCancel;
    AppCompatButton btnFinish;

    TextView lblSelectedPlantingDate;

    TextView lblSelectedHarvestDate;


    RadioGroup rdgAlternativeDate;

    RadioGroup rdgPlantingWindow;

    RadioGroup rdgHarvestWindow;

    SwitchCompat flexiblePlanting;
    SwitchCompat flexibleHarvest;

    ActivityDatesBinding binding;

    String selectedPlantingDate;
    String selectedHarvestDate;
    int plantingWindow = 0;
    int harvestWindow = 0;
    boolean alternativeDate;
    boolean alreadyPlanted;

    ScheduledDate scheduledDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDatesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        context = this;
        database = AppDatabase.getDatabase(context);

        //set widgets
        toolbar = binding.toolbar;
        lytPlantingHarvest = binding.lytPlantingHarvest;
        btnPickPlantingDate = binding.btnPickPlantingDate;
        btnPickHarvestDate = binding.btnPickHarvestDate;
        btnCancel = binding.twoButtons.btnCancel;
        btnFinish = binding.twoButtons.btnFinish;
        lblSelectedPlantingDate = binding.lblSelectedPlantingDate;
        lblSelectedHarvestDate = binding.lblSelectedHarvestDate;
        rdgAlternativeDate = binding.rdgAlternativeDate;
        rdgPlantingWindow = binding.rdgPlantingWindow;
        rdgHarvestWindow = binding.rdgHarvestWindow;
        flexiblePlanting = binding.flexiblePlanting;
        flexibleHarvest = binding.flexibleHarvest;


        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.lbl_planting_harvest_dates));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> validate(true));
    }

    @Override
    protected void initComponent() {
        //now for title pickers
        btnPickPlantingDate.setOnClickListener(view -> dialogDatePickerLight(true, false));
        btnPickHarvestDate.setOnClickListener(view -> dialogDatePickerLight(false, true));
        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));

        flexiblePlanting.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            rdgPlantingWindow.clearCheck();
            plantingWindow = 0;
            if (isChecked) {
                if (alreadyPlanted) {
                    showCustomWarningDialog(getString(R.string.lbl_already_planted_title), getString(R.string.lbl_already_planted_text));
                    flexiblePlanting.setChecked(false);
                } else {
                    rdgPlantingWindow.setVisibility(View.VISIBLE);
                }
            } else {
                rdgPlantingWindow.setVisibility(View.GONE);
            }
        });

        flexibleHarvest.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            rdgHarvestWindow.setVisibility(View.GONE);
            rdgHarvestWindow.clearCheck();
            harvestWindow = 0;
            if (isChecked) {
                rdgHarvestWindow.setVisibility(View.VISIBLE);
            } else {
                rdgHarvestWindow.setVisibility(View.GONE);
            }
        });

        rdgPlantingWindow.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdPlantingOneMonth:
                    plantingWindow = 1;
                    break;
                case R.id.rdPlantingTwoMonths:
                    plantingWindow = 2;
                    break;
                default:
                    plantingWindow = 0;
                    break;
            }
        });
        rdgHarvestWindow.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdHarvestOneMonth:
                    harvestWindow = 1;
                    break;
                case R.id.rdHarvestTwoMonths:
                    harvestWindow = 2;
                    break;
                default:
                    harvestWindow = 0;
                    break;
            }
        });

        rdgAlternativeDate.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdYes:
                    lytPlantingHarvest.setVisibility(View.VISIBLE);
                    alternativeDate = true;
                    break;
                case R.id.rdNo:
                    lytPlantingHarvest.setVisibility(View.GONE);
                    alternativeDate = false;
                    break;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduledDate = database.scheduleDateDao().findOne();

        if (scheduledDate != null) {
            alternativeDate = scheduledDate.getAlternativeDate();
            String pd = scheduledDate.getPlantingDate();
            String hd = scheduledDate.getHarvestDate();
            int pw = scheduledDate.getPlantingWindow();
            int hw = scheduledDate.getHarvestWindow();

            alreadyPlanted = scheduledDate.getAlreadyPlanted();

            DateHelper.dateTimeFormat = "dd/MM/yyyy";
            LocalDate pDate = DateHelper.formatToLocalDate(pd);
            LocalDate hDate = DateHelper.formatToLocalDate(hd);
            lblSelectedPlantingDate.setText(pDate.toString());
            lblSelectedHarvestDate.setText(hDate.toString());
            rdgAlternativeDate.check(alternativeDate ? R.id.rdYes : R.id.rdNo);

            if (pw > 0) {
                flexiblePlanting.setChecked(true);
                rdgPlantingWindow.check(pw == 1 ? R.id.rdPlantingOneMonth : R.id.rdPlantingTwoMonths);
            }
            if (hw > 0) {
                flexibleHarvest.setChecked(true);
                rdgHarvestWindow.check(hw == 1 ? R.id.rdHarvestOneMonth : R.id.rdHarvestTwoMonths);
            }
            //assign these values to the global parameters
            selectedHarvestDate = hd;
            selectedPlantingDate = pd;
        }
    }

    @Override
    protected void validate(boolean backPressed) {
        if (Strings.isEmptyOrWhitespace(selectedPlantingDate)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_planting_date), getString(R.string.lbl_planting_date_prompt));
            return;
        }

        if (Strings.isEmptyOrWhitespace(selectedHarvestDate)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_harvest_date), getString(R.string.lbl_harvest_date_prompt));
            return;
        }

        try {
            if (scheduledDate == null) {
                scheduledDate = new ScheduledDate();
            }

            alreadyPlanted = DateHelper.olderThanCurrent(selectedPlantingDate);
            scheduledDate.setHarvestDate(selectedHarvestDate);
            scheduledDate.setHarvestWindow(harvestWindow);
            scheduledDate.setPlantingDate(selectedPlantingDate);
            scheduledDate.setPlantingWindow(plantingWindow);
            scheduledDate.setAlternativeDate(alternativeDate);
            scheduledDate.setAlreadyPlanted(alreadyPlanted);

            database.scheduleDateDao().insert(scheduledDate);

            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.PLANTING_AND_HARVEST.name(), true));

            closeActivity(backPressed);

        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    private void dialogDatePickerLight(boolean pickPlantingDate, boolean pickHarvestDate) {
        final FragmentManager fm = getSupportFragmentManager();

        DateDialogPickerFragment pickerFragment = new DateDialogPickerFragment(true);
        if (pickHarvestDate) {
            pickerFragment = new DateDialogPickerFragment(true, selectedPlantingDate);
        }
        pickerFragment.show(fm, DateDialogPickerFragment.TAG);

        pickerFragment.setOnDismissListener((myCalendar, selectedDate, plantingDateSelected, harvestDateSelected) -> {
            long date_ship_millis = myCalendar.getTimeInMillis();
            if (pickPlantingDate) {
                selectedPlantingDate = DateHelper.getSimpleDateFormatter().format(myCalendar.getTime());
                lblSelectedPlantingDate.setText(Tools.formatLongToDateString(date_ship_millis));
                selectedHarvestDate = null;
                lblSelectedHarvestDate.setText(null);
                alreadyPlanted = DateHelper.olderThanCurrent(selectedPlantingDate);
                if (alreadyPlanted) {
                    flexiblePlanting.setChecked(false);
                }
            } else if (pickHarvestDate) {
                selectedHarvestDate = DateHelper.getSimpleDateFormatter().format(myCalendar.getTime());
                lblSelectedHarvestDate.setText(Tools.formatLongToDateString(date_ship_millis));
            }
        });

    }
}
