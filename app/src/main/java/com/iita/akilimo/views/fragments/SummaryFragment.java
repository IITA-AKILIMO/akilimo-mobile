package com.iita.akilimo.views.fragments;


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
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.models.TimeLineModel;
import com.iita.akilimo.models.TimelineAttributes;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.StepStatus;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends BaseFragment {

    RecyclerView recyclerView;
    FragmentSummaryBinding binding;

    private List<TimeLineModel> mDataList = new ArrayList<>();
    private TimelineAttributes mAttributes;
    private LocationInfo location;
    private MandatoryInfo mandatoryInfo;
    private CurrentPractice currentPractice;
    private ScheduledDate scheduledDate;
    private ProfileInfo profileInfo;

    private IFragmentCallBack fragmentCallBack;
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

    public void setOnFragmentCloseListener(IFragmentCallBack callBack) {
        this.fragmentCallBack = callBack;
    }

    public static SummaryFragment newInstance() {
        return new SummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAttributes = new TimelineAttributes(
                48,
                ContextCompat.getColor(context, R.color.green_500),
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
                2,
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

    public void refreshData() {
        setDataListItems();
        initFragmentCallback();
    }

    private void setDataListItems() {
        String plantingDate = "";
        String harvestDate = "";
        StringBuilder fieldInfo = new StringBuilder();
        StringBuilder ploughStr = new StringBuilder();
        StringBuilder ridgeStr = new StringBuilder();

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
            areaUnit = mandatoryInfo.getAreaUnit();
            if (!Strings.isEmptyOrWhitespace(areaUnit)) {
                areaUnitSelected = true;
            } else {
                areaUnit = context.getString(R.string.empty_text);
            }
            fieldSize = mandatoryInfo.getAreaSize();
            fieldSizeSelected = fieldSize > 0.0;
        }

        if (location != null) {
            pickedLocation = loadLocationInfo(location).toString();
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            locationPicked = lat != 0 || lon != 0;
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

        fieldInfo.append(fieldSize)
                .append(context.getString(R.string.empty_text))
                .append(areaUnit);

        mDataList = new ArrayList<>();
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_country), countryName, countrySelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_field), fieldInfo.toString(), fieldSizeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_location), pickedLocation, locationPicked ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_planting_date), plantingDate, plantingDateProvided ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_harvesting_date), harvestDate, harvestDateProvided ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_ploughing), ploughStr.toString(), currentPracticeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel(context.getString(R.string.lbl_ridging), ridgeStr.toString(), currentPracticeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));

        initAdapter();
    }

    private void initFragmentCallback() {
        if (countrySelected && areaUnitSelected && fieldSizeSelected && locationPicked && plantingDateProvided && harvestDateProvided && currentPracticeSelected) {
            if (fragmentCallBack == null) {
                fragmentCallBack = (IFragmentCallBack) context;
                setOnFragmentCloseListener(fragmentCallBack);
            }
            if (fragmentCallBack != null) {
                fragmentCallBack.onFragmentClose(false);
            }
        }
    }

    private void initRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                initFragmentCallback();
            }
        });

    }

    private void initAdapter() {
        adapter = new MyTimeLineAdapter(mDataList, mAttributes, context, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        fragmentCallBack = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentCallBack = null;
    }
}
