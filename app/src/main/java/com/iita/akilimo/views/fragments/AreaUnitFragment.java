package com.iita.akilimo.views.fragments;


import android.annotation.SuppressLint;
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
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentAreaUnitBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.utils.enums.EnumAreaUnits;
import com.stepstone.stepper.VerificationError;

;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AreaUnitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AreaUnitFragment extends BaseStepFragment {


    RadioGroup rdgAreaUnit;

    FragmentAreaUnitBinding binding;

    private String selectedAreaUnit;
    private MandatoryInfo mandatoryInfo;
    private String areaUnit = "acre";
    private String oldAreaUnit = "";
    private String areaUnitDisplay = "acre";
    private int areaUnitRadioIndex = 0;
    private boolean areaUnitChanged = false;

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

    public void refreshData() {
        try {
            mandatoryInfo = database.mandatoryInfoDao().findOne();
            if (mandatoryInfo != null) {
                areaUnit = mandatoryInfo.getAreaUnit();
                oldAreaUnit = mandatoryInfo.getOldAreaUnit();
                areaUnitRadioIndex = mandatoryInfo.getAreaUnitRadioIndex();
                rdgAreaUnit.check(areaUnitRadioIndex);
                dataIsValid = !Strings.isEmptyOrWhitespace(areaUnit);
            } else {
                areaUnitRadioIndex = -1;
                areaUnit = null;
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rdgAreaUnit = binding.rdgAreaUnit;

        errorMessage = context.getString(R.string.lbl_area_unit_prompt);
        rdgAreaUnit.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdAcre:
                    areaUnitRadioIndex = R.id.rdAcre;
                    areaUnitDisplay = context.getString(R.string.lbl_acre);
                    areaUnit = EnumAreaUnits.ACRE.unitName(context);
                    break;
                case R.id.rdHa:
                    areaUnitRadioIndex = R.id.rdHa;
                    areaUnitDisplay = context.getString(R.string.lbl_ha);
                    areaUnit = EnumAreaUnits.HA.unitName(context);
                    break;
                default:
                    areaUnitRadioIndex = -1;
                    areaUnit = null;
                    break;
            }

            dataIsValid = !Strings.isEmptyOrWhitespace(areaUnit);
            if (!dataIsValid) {
                return;
            }
            try {
                mandatoryInfo = database.mandatoryInfoDao().findOne();
                if (mandatoryInfo == null) {
                    mandatoryInfo = new MandatoryInfo();
                }
                mandatoryInfo.setAreaUnitRadioIndex(areaUnitRadioIndex);
                mandatoryInfo.setAreaUnit(areaUnit);
                mandatoryInfo.setDisplayAreaUnit(areaUnitDisplay);
                if (mandatoryInfo.getId() != null) {
                    database.mandatoryInfoDao().update(mandatoryInfo);
                } else {
                    database.mandatoryInfoDao().insert(mandatoryInfo);
                }

            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
                dataIsValid = false;
            }
        });
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (!dataIsValid) {
            return new VerificationError(errorMessage);
        }
        return null;
    }

    @Override
    public void onSelected() {
        refreshData();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
