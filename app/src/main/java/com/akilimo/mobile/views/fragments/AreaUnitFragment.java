package com.akilimo.mobile.views.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.utils.enums.EnumCountry;
import com.crashlytics.android.Crashlytics;
import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentAreaUnitBinding;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.enums.EnumAreaUnits;
import com.stepstone.stepper.VerificationError;

;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AreaUnitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AreaUnitFragment extends BaseStepFragment {

    FragmentAreaUnitBinding binding;
    private MandatoryInfo mandatoryInfo;
    private String areaUnit = "acre";
    private String oldAreaUnit = "";
    private String areaUnitDisplay = "acre";
    private int areaUnitRadioIndex = 0;
    private boolean rememberPreference = false;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        errorMessage = context.getString(R.string.lbl_area_unit_prompt);
        binding.rdgAreaUnit.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdAcre:
                    areaUnitRadioIndex = R.id.rdAcre;
                    areaUnitDisplay = context.getString(R.string.lbl_acre);
                    areaUnit = EnumAreaUnits.ACRE.name();
                    break;
                case R.id.rdHa:
                    areaUnitRadioIndex = R.id.rdHa;
                    areaUnitDisplay = context.getString(R.string.lbl_ha);
                    areaUnit = EnumAreaUnits.HA.name();
                    break;
                case R.id.rdAre:
                    areaUnitRadioIndex = R.id.rdHa;
                    areaUnitDisplay = context.getString(R.string.lbl_are);
                    areaUnit = EnumAreaUnits.ARE.name();
                    break;
                default:
                    areaUnitRadioIndex = -1;
                    areaUnit = null;
                    break;
            }

            if (areaUnit == null) {
                return;
            }

            try {
                mandatoryInfo = database.mandatoryInfoDao().findOne();
                if (mandatoryInfo == null) {
                    mandatoryInfo = new MandatoryInfo();
                }
                mandatoryInfo.setAreaUnitRadioIndex(areaUnitRadioIndex);
                mandatoryInfo.setAreaUnit(areaUnit.toLowerCase());
                mandatoryInfo.setDisplayAreaUnit(areaUnitDisplay);
                if (mandatoryInfo.getId() != null) {
                    database.mandatoryInfoDao().update(mandatoryInfo);
                } else {
                    database.mandatoryInfoDao().insert(mandatoryInfo);
                }

            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.chkRememberDetails.setOnCheckedChangeListener((compoundButton, rememberInfo) -> {
            rememberPreference = rememberInfo;
            sessionManager.setRememberAreUnit(rememberInfo);
        });
    }

    public void refreshData() {
        try {
            rememberPreference = sessionManager.getRememberAreUnit();
            mandatoryInfo = database.mandatoryInfoDao().findOne();
            RadioButton rdAre = binding.rdAre;
            ProfileInfo profileInfo = database.profileInfoDao().findOne();
            if (mandatoryInfo != null) {
                areaUnit = mandatoryInfo.getAreaUnit();
                oldAreaUnit = mandatoryInfo.getOldAreaUnit();
                areaUnitRadioIndex = mandatoryInfo.getAreaUnitRadioIndex();
                binding.rdgAreaUnit.check(areaUnitRadioIndex);
            } else {
                binding.rdgAreaUnit.check(areaUnitRadioIndex);
                areaUnitRadioIndex = -1;
                areaUnit = null;
            }

            if (profileInfo != null) {
                countryCode = profileInfo.getCountryCode();
                if (countryCode != null) {
                    if (countryCode.equals(EnumCountry.Rwanda.countryCode())) {
                        //set the are unit radiobutton to visible
                        rdAre.setVisibility(View.VISIBLE);
                        if (oldAreaUnit.equals(EnumAreaUnits.ARE.unitName(context))) {
                            rdAre.setChecked(true);
                        }
                    }
                }
            }


        } catch (Exception ex) {
            //TODO log crashes to third party service
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (areaUnit == null || areaUnit.isEmpty()) {
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
