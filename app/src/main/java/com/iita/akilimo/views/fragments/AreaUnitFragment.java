package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentAreaUnitBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.utils.enums.EnumAreaUnits;

;

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
    private String areaUnit = "acre";
    private int areaUnitRadioIndex = 0;

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
            mandatoryInfo = database.mandatoryInfoDao().findOne();
            if (mandatoryInfo != null) {
                areaUnit = mandatoryInfo.getAreaUnit();
                areaUnitRadioIndex = mandatoryInfo.getAreaUnitRadioIndex();
                rdgAreaUnit.check(areaUnitRadioIndex);
            }
        } catch (Exception ex) {
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
                    areaUnit = EnumAreaUnits.ACRE.unitName(context);
                    break;
                case R.id.rdHa:
                    areaUnit = EnumAreaUnits.HA.unitName(context);
                    break;
            }

            areaUnitRadioIndex = rdgAreaUnit.getCheckedRadioButtonId();
            try {
                if (mandatoryInfo == null) {
                    mandatoryInfo = new MandatoryInfo();
                }
                mandatoryInfo.setAreaUnitRadioIndex(areaUnitRadioIndex);
                mandatoryInfo.setAreaUnit(areaUnit);
                mandatoryInfo.setFieldSizeRadioIndex(0);
                mandatoryInfo.setAreaSize(0);

                database.mandatoryInfoDao().insert(mandatoryInfo);
                mandatoryInfo = database.mandatoryInfoDao().findOne();

            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }
        });
    }
}
