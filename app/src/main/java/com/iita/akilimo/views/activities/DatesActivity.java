package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.PlantingHarvestDates;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IDatePickerDismissListener;
import com.iita.akilimo.utils.DateHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.DateDialogPickerFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import java.util.Calendar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DatesActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.lbl_planting_harvest_dates)
    String activityTitle;

    @BindView(R.id.lytPlantingHarvest)
    RelativeLayout lytPlantingHarvest;

    @BindView(R.id.btnPickPlantingDate)
    AppCompatButton btnPickPlantingDate;
    @BindView(R.id.btnPickHarvestDate)
    AppCompatButton btnPickHarvestDate;

    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;
    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;


    @BindView(R.id.lblSelectedPlantingDate)
    TextView lblSelectedPlantingDate;
    @BindView(R.id.lblSelectedHarvestDate)
    TextView lblSelectedHarvestDate;

    @BindView(R.id.rdgAlternativeDate)
    RadioGroup rdgAlternativeDate;
    @BindView(R.id.rdgPlantingWindow)
    RadioGroup rdgPlantingWindow;
    @BindView(R.id.rdgHarvestWindow)
    RadioGroup rdgHarvestWindow;

    @BindView(R.id.flexiblePlanting)
    SwitchCompat flexiblePlanting;
    @BindView(R.id.flexibleHarvest)
    SwitchCompat flexibleHarvest;

    String selectedPlantingDate;
    String selectedHarvestDate;
    int plantingWindow = 0;
    int harvestWindow = 0;
    boolean alternativeDate;

    PlantingHarvestDates plantingHarvestDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dates);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(activityTitle);
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
            if (isChecked) {
                rdgPlantingWindow.setVisibility(View.VISIBLE);
                rdgPlantingWindow.clearCheck();
                plantingWindow = 0;
            } else {
                rdgPlantingWindow.clearCheck();
                plantingWindow = 0;
                rdgPlantingWindow.setVisibility(View.GONE);
            }
        });

        flexibleHarvest.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            rdgHarvestWindow.setVisibility(View.GONE);
            if (isChecked) {
                rdgHarvestWindow.setVisibility(View.VISIBLE);
                rdgHarvestWindow.clearCheck();
                harvestWindow = 0;
            } else {
                rdgHarvestWindow.setVisibility(View.GONE);
                rdgHarvestWindow.clearCheck();
                harvestWindow = 0;
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
        plantingHarvestDates = objectBoxEntityProcessor.getPlantingHarvestDates();

        if (plantingHarvestDates != null) {
            alternativeDate = plantingHarvestDates.isAlternativeDate();
            String pd = plantingHarvestDates.getPlantingDate();
            String hd = plantingHarvestDates.getHarvestDate();
            int pw = plantingHarvestDates.getPlantingWindow();
            int hw = plantingHarvestDates.getHarvestWindow();

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

        plantingHarvestDates = objectBoxEntityProcessor.getPlantingHarvestDates();
        if (plantingHarvestDates == null) {
            plantingHarvestDates = new PlantingHarvestDates();
        }
        plantingHarvestDates.setHarvestDate(selectedHarvestDate);
        plantingHarvestDates.setHarvestWindow(harvestWindow);
        plantingHarvestDates.setPlantingDate(selectedPlantingDate);
        plantingHarvestDates.setPlantingWindow(plantingWindow);
        plantingHarvestDates.setAlternativeDate(alternativeDate);

        long id = objectBoxEntityProcessor.savePlantingHarvestDates(plantingHarvestDates);
        if (id <= 0) {
            showCustomWarningDialog(getString(R.string.lbl_unable_to_save), getString(R.string.lbl_date_save_error));
            return;
        }
        closeActivity(backPressed);
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
            } else if (pickHarvestDate) {
                selectedHarvestDate = DateHelper.getSimpleDateFormatter().format(myCalendar.getTime());
                lblSelectedHarvestDate.setText(Tools.formatLongToDateString(date_ship_millis));
            }
        });

    }
}