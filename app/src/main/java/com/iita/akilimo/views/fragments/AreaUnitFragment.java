package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentAreaUnitBinding;
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


    RadioGroup rdgAreaUnit;

    FragmentAreaUnitBinding binding;

    private String selectedAreaUnit;
    private MandatoryInfo mandatoryInfo;
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
        binding = FragmentAreaUnitBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void refreshData() {
        try {
            mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
            if (mandatoryInfo != null) {
                areaUnitsEnum = mandatoryInfo.getAreaUnitsEnum();
                switch (areaUnitsEnum) {
                    default:
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
        } catch (Exception ex) {
            mandatoryInfo = new MandatoryInfo();
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving are info");
            Crashlytics.logException(ex);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rdgAreaUnit = binding.rdgAreaUnit;

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
            mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
            if (mandatoryInfo == null) {
                mandatoryInfo = new MandatoryInfo();
            }
            mandatoryInfo.setAreaUnitsEnum(areaUnitsEnum);
            mandatoryInfo.setAreaUnit(areaUnitsEnum.unitString());
            objectBoxEntityProcessor.saveMandatoryInfo(mandatoryInfo);
        });
    }
}
