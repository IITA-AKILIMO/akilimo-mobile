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
import com.iita.akilimo.adapters.TimeLineAdapter;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.TimeLineModel;
import com.iita.akilimo.entities.TimelineAttributes;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.StepStatus;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends BaseFragment {

    @BindView(R.id.timelineRecycler)
    RecyclerView recyclerView;

    private List<TimeLineModel> mDataList = new ArrayList<>();
    private TimelineAttributes mAttributes;
    private MandatoryInfo location;
    private IFragmentCallBack fragmentCallBack;
    private TimeLineAdapter adapter;


    private boolean countrySelected = false;
    private boolean areaUnitSelected = false;
    private boolean fieldSizeSelected = false;
    private boolean locationPicked = false;

    private String areaUnit;
    private double fieldSize = 0.0;
    private String pickedLocation;

    public SummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
//        if (context instanceof IFragmentCallBack) {
//            fragmentCallBack = (IFragmentCallBack) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
//        }
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
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
//        setDataListItems(false);
    }

    public void refreshData() {
        setDataListItems();
        initFragmentCallback();
    }

    private void setDataListItems() {

        if (objectBoxEntityProcessor == null) {
            return;
        }

        location = objectBoxEntityProcessor.getMandatoryInfo();
        countryName = "";
        if (location != null) {

            if (!Strings.isEmptyOrWhitespace(location.getCountryName())) {
                countrySelected = true;
                countryName = location.getCountryName();
            }

            if (!Strings.isEmptyOrWhitespace(location.getAreaUnit())) {
                areaUnitSelected = true;
                areaUnit = location.getAreaUnit();
            }

            fieldSize = location.getAreaSize();
            if (fieldSize > 0.0) {
                fieldSizeSelected = true;
            }

            pickedLocation = loadLocationInfo(location).toString();
            locationPicked = true;
        }

        mDataList = new ArrayList<>();
        mDataList.add(new TimeLineModel("Your country", countryName, countrySelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel("Area unit", areaUnit, areaUnitSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel("Field size", String.valueOf(fieldSize), fieldSizeSelected ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));
        mDataList.add(new TimeLineModel("Farm location", pickedLocation, locationPicked ? StepStatus.COMPLETED : StepStatus.INCOMPLETE));

//        mDataList.add(buildModel(StepStatus.COMPLETED));
//        mDataList.add(buildModel(StepStatus.INCOMPLETE));


        initAdapter();
    }

    private TimeLineModel buildModel(StepStatus stepStatus) {
        return new TimeLineModel(
                "Field size",
                String.valueOf(fieldSize),
                stepStatus);
    }

    private void initFragmentCallback() {
        if (countrySelected && areaUnitSelected && fieldSizeSelected && locationPicked) {
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
        adapter = new TimeLineAdapter(mDataList, mAttributes, context, ItemAnimation.FADE_IN);
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
