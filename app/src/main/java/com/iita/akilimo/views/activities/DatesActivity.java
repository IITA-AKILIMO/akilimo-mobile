package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.PlantingHarvestDates;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.DateHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

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

    @BindView(R.id.btnPickPlantingDate)
    MaterialButton btnPickPlantingDate;
    @BindView(R.id.btnPickHarvestDate)
    MaterialButton btnPickHarvestDate;

    @BindView(R.id.btnCancel)
    MaterialButton btnCancel;
    @BindView(R.id.btnFinish)
    MaterialButton btnFinish;

    @BindView(R.id.flexiblePlanting)
    SwitchCompat flexiblePlanting;
    @BindView(R.id.flexibleHarvest)
    SwitchCompat flexibleHarvest;

    @BindView(R.id.lblSelectedPlantingDate)
    TextView lblSelectedPlantingDate;
    @BindView(R.id.lblSelectedHarvestDate)
    TextView lblSelectedHarvestDate;

    @BindView(R.id.rdgPlantingWindow)
    RadioGroup rdgPlantingWindow;
    @BindView(R.id.rdgHarvestWindow)
    RadioGroup rdgHarvestWindow;

    String selectedPlantingDate;
    String selectedHarvestDate;
    int plantingWindow = 0;
    int harvestWindow = 0;

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
        flexiblePlanting.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                rdgPlantingWindow.setVisibility(View.VISIBLE);
                rdgPlantingWindow.clearCheck();
                plantingWindow = 0;
            } else {
                rdgPlantingWindow.setVisibility(View.GONE);
            }
        });
        flexibleHarvest.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            rdgHarvestWindow.setVisibility(View.GONE);
            if (isChecked) {
                rdgHarvestWindow.setVisibility(View.VISIBLE);
                rdgHarvestWindow.clearCheck();
                harvestWindow = 0;
            }
        });
        //now for title pickers
        btnPickPlantingDate.setOnClickListener(view -> dialogDatePickerLight(true, false));
        btnPickHarvestDate.setOnClickListener(view -> dialogDatePickerLight(false, true));
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

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //fetch the presaved data
        plantingHarvestDates = objectBoxEntityProcessor.getPlantingHarvestDates();

        if (plantingHarvestDates != null) {
            String pd = plantingHarvestDates.getPlantingDate();
            String hd = plantingHarvestDates.getHarvestDate();
            int pw = plantingHarvestDates.getPlantingWindow();
            int hw = plantingHarvestDates.getHarvestWindow();

            DateHelper.dateTimeFormat = "dd/MM/yyyy";
            LocalDate pDate = DateHelper.formatToLocalDate(pd);
            LocalDate hDate = DateHelper.formatToLocalDate(hd);

            lblSelectedPlantingDate.setText(pDate.toString());
            lblSelectedHarvestDate.setText(hDate.toString());

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
            showCustomWarningDialog("Invalid planting title", "Please provide a valid planting title");
            return;
        }

        if (Strings.isEmptyOrWhitespace(selectedHarvestDate)) {
            showCustomWarningDialog("Invalid harvest title", "Please provide a valid harvest title");
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

        long id = objectBoxEntityProcessor.savePlantingHarvestDates(plantingHarvestDates);
        if (id <= 0) {
            showCustomWarningDialog("Unable to save", "Unable to save planting and harvest dates, please try again");
            return;
        }
        closeActivity(backPressed);
    }

    private void dialogDatePickerLight(boolean pickPlantingDate, boolean pickHarvestDate) {
        Calendar cur_calender = Calendar.getInstance();
        DatePickerDialog datePicker = DatePickerDialog.newInstance(
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    long date_ship_millis = calendar.getTimeInMillis();
                    if (pickPlantingDate) {
                        selectedPlantingDate = DateHelper.getSimpleDateFormatter().format(calendar.getTime());
                        lblSelectedPlantingDate.setText(Tools.formatLongToDateString(date_ship_millis));
                        selectedHarvestDate = null;
                        lblSelectedHarvestDate.setText(null);
                    } else if (pickHarvestDate) {
                        selectedHarvestDate = DateHelper.getSimpleDateFormatter().format(calendar.getTime());
                        lblSelectedHarvestDate.setText(Tools.formatLongToDateString(date_ship_millis));
                    }
                },
                cur_calender.get(Calendar.YEAR),
                cur_calender.get(Calendar.MONTH),
                cur_calender.get(Calendar.DAY_OF_MONTH)
        );
        //set dark light
        datePicker.setThemeDark(false);
        datePicker.setAccentColor(getResources().getColor(R.color.colorAccent));
        datePicker.setOkColor(getResources().getColor(R.color.grey_50));
        datePicker.setCancelColor(getResources().getColor(R.color.grey_50));
        datePicker.setMinDate(cur_calender);
        if (pickPlantingDate) {
            datePicker.setMinDate(DateHelper.getMinDate(-16));
            datePicker.setMaxDate(DateHelper.getMaxDate(12));
        } else if ((pickHarvestDate) && !Strings.isEmptyOrWhitespace(selectedPlantingDate)) {
            datePicker.setMinDate(DateHelper.getFutureOrPastMonth(selectedPlantingDate, 8));
            datePicker.setMaxDate(DateHelper.getFutureOrPastMonth(selectedPlantingDate, 16));
        }
        datePicker.show(getFragmentManager(), "DatePickerDialog");
    }
}
