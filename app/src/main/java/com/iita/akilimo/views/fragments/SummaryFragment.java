package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.MyTimeLineAdapter;
import com.iita.akilimo.databinding.FragmentSummaryBinding;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.entities.ScheduledDate;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.models.TimeLineModel;
import com.iita.akilimo.models.TimelineAttributes;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.StepStatus;
import com.stepstone.stepper.VerificationError;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends BaseStepFragment {

    RecyclerView recyclerView;
    FragmentSummaryBinding binding;

    private List<TimeLineModel> mDataList = new ArrayList<>();
    private TimelineAttributes mAttributes;
    private LocationInfo location;
    private MandatoryInfo mandatoryInfo;
    private CurrentPractice currentPractice;
    private ScheduledDate scheduledDate;
    private ProfileInfo profileInfo;

    private MyTimeLineAdapter adapter;


    private boolean countrySelected = false;
    private boolean areaUnitSelected = false;
    private boolean fieldSizeSelected = false;
    private boolean locationPicked = false;
    private boolean plantingDateProvided = false;
    private boolean harvestDateProvided = false;
    private boolean currentPracticeSelected = false;
    private boolean performPloughing = false;
    private boolean performRidging = false;

    private String areaUnit = "";
    private double fieldSize;
    private String pickedLocation = "";

    public SummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static SummaryFragment newInstance() {
        return new SummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAttributes = new TimelineAttributes(
                48,
                ContextCompat.getColor(context, R.color.akilimoLightGreen),
                ContextCompat.getColor(context, R.color.red_A400),
                true,
                0,
                0,
                0,
                0,
                0,
                0,
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorAccent),
                TimelineView.LineStyle.DASHED,
                4,
                2
        );
    }

    @Override
    protected View loadFragmentLayout(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = binding.timelineRecycler;

        initRecyclerView();
    }

    private void setDataListItems() {
        String plantingDate = "";
        String harvestDate = "";
        String fieldInfo = "";
        String placeName = "";
        StringBuilder ploughStr = new StringBuilder();
        StringBuilder ridgeStr = new StringBuilder();
        StepStatus stepStatus = StepStatus.WARNING;

        if (database == null) {
            return;
        }
        location = database.locationInfoDao().findOne();
        mandatoryInfo = database.mandatoryInfoDao().findOne();
        currentPractice = database.currentPracticeDao().findOne();
        scheduledDate = database.scheduleDateDao().findOne();

        profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryName = profileInfo.getCountryName();
            countrySelected = !Strings.isEmptyOrWhitespace(countryName);
        }
        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.getDisplayAreaUnit();
            areaUnitSelected = !Strings.isEmptyOrWhitespace(areaUnit);
            if (areaUnitSelected) {
                fieldSize = mandatoryInfo.getAreaSize();
                fieldSizeSelected = fieldSize > 0.0;

                try {
                    Locale locale = getCurrentLocale();
                    if (locale.getLanguage().equalsIgnoreCase("sw")) {
                        fieldInfo = String.format("%s %s", areaUnit, fieldSize);
                    } else {
                        fieldInfo = String.format("%s %s", fieldSize, areaUnit);
                    }
                } catch (Exception ex) {
                    fieldInfo = String.format("%s %s", fieldSize, areaUnit);
                    Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                    Crashlytics.logException(ex);
                }
            }
        }

        if (location != null) {
            pickedLocation = loadLocationInfo(location).toString();
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            String farmCountryCode = location.getLocationCountry();
            placeName = location.getPlaceName();
            locationPicked = lat != 0 || lon != 0;

            if (!Strings.isEmptyOrWhitespace(farmCountryCode)) {
                if (farmCountryCode.equalsIgnoreCase("ng") || farmCountryCode.equalsIgnoreCase("tz")) {
                    stepStatus = StepStatus.COMPLETED;
                } else {
                    Toast.makeText(context, "Farm location is outside supported countries, recommendations might not work", Toast.LENGTH_LONG).show();
                }
            }
        }

        if (scheduledDate != null) {
            plantingDate = scheduledDate.getPlantingDate();
            harvestDate = scheduledDate.getHarvestDate();
            plantingDateProvided = !Strings.isEmptyOrWhitespace(plantingDate);
            harvestDateProvided = !Strings.isEmptyOrWhitespace(plantingDate);
        }

        if (currentPractice != null) {
            currentPracticeSelected = true;
            performPloughing = currentPractice.getPerformPloughing();
            performRidging = currentPractice.getPerformRidging();

            if (performPloughing) {
                String ploughMethod = context.getString(R.string.lbl_manual);
                if (currentPractice.getPloughingMethod().equals("tractor")) {
                    ploughMethod = context.getString(R.string.lbl_tractor);
                }
                ploughStr.append(ploughMethod);
            } else {
                ploughStr.append(context.getString(R.string.lbl_no_ploughing));
            }


            if (performRidging) {
                String ridgeMethod = context.getString(R.string.lbl_manual);
                if (currentPractice.getRidgingMethod().equals("tractor")) {
                    ridgeMethod = context.getString(R.string.lbl_tractor);
                }
                ridgeStr.append(ridgeMethod);
            } else {
                ridgeStr.append(context.getString(R.string.lbl_no_ridging));
            }
        }


        mDataList = new ArrayList<>();
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_country), countryName, countrySelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_field), fieldInfo, fieldSizeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_location), pickedLocation, locationPicked ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_farm_place), placeName, stepStatus));

        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_planting_date), plantingDate, plantingDateProvided ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_harvesting_date), harvestDate, harvestDateProvided ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_ploughing), ploughStr.toString(), currentPracticeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_ridging), ridgeStr.toString(), currentPracticeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));

        initAdapter();
    }

    private void initRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void initAdapter() {
        adapter = new MyTimeLineAdapter(mDataList, mAttributes, context, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(adapter);
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
        setDataListItems();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
