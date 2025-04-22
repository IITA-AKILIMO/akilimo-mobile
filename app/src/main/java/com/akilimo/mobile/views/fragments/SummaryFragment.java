package com.akilimo.mobile.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.MyTimeLineAdapter;
import com.akilimo.mobile.databinding.FragmentSummaryBinding;
import com.akilimo.mobile.entities.CurrentPractice;
import com.akilimo.mobile.entities.LocationInfo;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.entities.ScheduledDate;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.models.TimeLineModel;
import com.akilimo.mobile.models.TimelineAttributes;
import com.akilimo.mobile.utils.TheItemAnimation;
import com.akilimo.mobile.utils.enums.EnumCountry;
import com.akilimo.mobile.utils.enums.EnumInvestmentPref;
import com.akilimo.mobile.utils.enums.StepStatus;
import com.github.vipulasri.timelineview.TimelineView;
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
    private String[] risks = null;

    public SummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        risks = new String[]{EnumInvestmentPref.Rarely.prefName(context), EnumInvestmentPref.Sometimes.prefName(context), EnumInvestmentPref.Often.prefName(context)};
    }

    public static SummaryFragment newInstance() {
        return new SummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAttributes = new TimelineAttributes(48, ContextCompat.getColor(context, R.color.akilimoLightGreen), ContextCompat.getColor(context, R.color.red_A400), true, 0, 0, 0, 0, 0, 0, ContextCompat.getColor(context, R.color.colorAccent), ContextCompat.getColor(context, R.color.colorAccent), TimelineView.LineStyle.DASHED, 4, 2);
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
        String fieldName = "";
        String summaryTitle = context.getString(R.string.lbl_summary_title);
        int riskAttitude = 0;
        String riskAttitudeName = "";
        double lat = 0.0;
        double lon = 0.0;
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
            countryCode = profileInfo.getCountryCode();
            countrySelected = !countryName.isEmpty();
            riskAttitude = profileInfo.getRiskAtt();
        }
        riskAttitudeName = risks[riskAttitude];

        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.getDisplayAreaUnit();
            areaUnitSelected = areaUnit != null && !areaUnit.isEmpty();

            summaryTitle = String.format(context.getString(R.string.lbl_summary_text), profileInfo.names(), profileInfo.getFarmName());
            if (areaUnitSelected) {
                fieldSize = mandatoryInfo.getAreaSize();
                fieldSizeSelected = fieldSize > 0.0;
                fieldInfo = String.format("%s %s", fieldSize, areaUnit);
                try {
                    Locale locale = getCurrentLocale();
                    if (locale.getLanguage().equalsIgnoreCase("sw")) {
                        fieldInfo = String.format("%s %s", areaUnit, fieldSize);
                    } else {
                        fieldInfo = String.format("%s %s", fieldSize, areaUnit);
                    }
                } catch (Exception ignored) {

                }
            }
        }

        binding.txtSummaryTitle.setText(summaryTitle);
        if (location != null) {
            pickedLocation = loadLocationInfo(location).toString();
            lat = location.getLatitude();
            lon = location.getLongitude();
            locationPicked = lat != 0 || lon != 0;
        }

        if (scheduledDate != null) {
            plantingDate = scheduledDate.getPlantingDate();
            harvestDate = scheduledDate.getHarvestDate();
            plantingDateProvided = (plantingDate != null && !plantingDate.isEmpty());
            harvestDateProvided = (harvestDate != null && !harvestDate.isEmpty());
        }

        if (currentPractice != null) {
            currentPracticeSelected = true;
            performPloughing = currentPractice.getPerformPloughing();
            performRidging = currentPractice.getPerformRidging();

            String _ploughMethod = currentPractice.getPloughingMethod();
            String _ridgeMethod = currentPractice.getRidgingMethod();
            if (performPloughing && _ploughMethod != null) {
                String ploughMethod = context.getString(R.string.lbl_manual);
                if (_ploughMethod.equals("tractor")) {
                    ploughMethod = context.getString(R.string.lbl_tractor);
                }
                ploughStr.append(ploughMethod);
            } else {
                ploughStr.append(context.getString(R.string.lbl_no_ploughing));
            }


            if (performRidging && _ridgeMethod != null) {
                String ridgeMethod = context.getString(R.string.lbl_manual);
                if (_ridgeMethod.equals("tractor")) {
                    ridgeMethod = context.getString(R.string.lbl_tractor);
                }
                ridgeStr.append(ridgeMethod);
            } else {
                ridgeStr.append(context.getString(R.string.lbl_no_ridging));
            }
        }

        String location = mathHelper.removeLeadingZero(lat, "#.####") + "," + mathHelper.removeLeadingZero(lon, "#.####");

        mDataList = new ArrayList<>();
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_country), countryName, countrySelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_farm_place), location, locationPicked ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_farm_size), fieldInfo, fieldSizeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));

        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_planting_date), plantingDate, plantingDateProvided ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_harvesting_date), harvestDate, harvestDateProvided ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));

        if (!countryCode.equals(EnumCountry.Ghana.countryCode())) {
            mDataList.add(new TimeLineModel(context.getString(R.string.lbl_ploughing), ploughStr.toString(), currentPracticeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
            mDataList.add(new TimeLineModel(context.getString(R.string.lbl_ridging), ridgeStr.toString(), currentPracticeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        }

        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_risk_attitude), riskAttitudeName, StepStatus.COMPLETED));
        initAdapter();
    }

    private void initRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void initAdapter() {
        adapter = new MyTimeLineAdapter(mDataList, mAttributes, context, TheItemAnimation.FADE_IN);
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
