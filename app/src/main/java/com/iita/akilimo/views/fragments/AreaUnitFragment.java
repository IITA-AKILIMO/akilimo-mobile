package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.utils.enums.EnumAreaUnits;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AreaUnitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AreaUnitFragment extends BaseFragment {


    @BindView(R.id.rdgAreaUnit)
    RadioGroup rdgAreaUnit;

    private String selectedAreaUnit;
    private MandatoryInfo location;
    private EnumAreaUnits areaUnitsEnum;

    public AreaUnitFragment() {
        // Required empty public constructor
    }

    public static AreaUnitFragment newInstance() {
        return new AreaUnitFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_unit, container, false);
    }

    @Override
    public void refreshData() {
        location = objectBoxEntityProcessor.getMandatoryInfo();
        if (location != null) {
            areaUnitsEnum = location.getAreaUnitsEnum();
            switch (areaUnitsEnum) {
                case ACRE:
                    rdgAreaUnit.check(R.id.rdAcre);
                    break;
                case HA:
                    rdgAreaUnit.check(R.id.rdHa);
                    break;
                case SQM:
                    rdgAreaUnit.check(R.id.rdSqm);
                    break;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //save this data
        rdgAreaUnit.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdAcre:
                    areaUnitsEnum = EnumAreaUnits.ACRE;
                    break;
                case R.id.rdHa:
                    areaUnitsEnum = EnumAreaUnits.HA;
                    break;
                case R.id.rdSqm:
                    areaUnitsEnum = EnumAreaUnits.SQM;
                    break;
            }
            location = objectBoxEntityProcessor.getMandatoryInfo();
            if (location == null) {
                location = new MandatoryInfo();
            }
            location.setAreaUnitsEnum(areaUnitsEnum);
            location.setAreaUnit(areaUnitsEnum.unitString());
            objectBoxEntityProcessor.saveMandatoryInfo(location);
        });
    }
}
