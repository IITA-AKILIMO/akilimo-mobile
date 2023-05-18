package com.akilimo.mobile.views.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.enums.EnumInvestmentPref;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InvestmentPrefFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InvestmentPrefFragment extends BaseStepFragment {

    private FragmentInvestmentPrefBinding binding;
    private ProfileInfo profileInfo;


    String riskName;
    private int riskAtt = 0;
    private int riskRadioIndex = -1;

    private String[] investmentPreference = null;
    private boolean rememberInvestmentPref = false;

    public InvestmentPrefFragment() {
        // Required empty public constructor
    }

    public static InvestmentPrefFragment newInstance() {
        return new InvestmentPrefFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        investmentPreference = new String[]{
                EnumInvestmentPref.Rarely.prefName(context),
                EnumInvestmentPref.Sometimes.prefName(context),
                EnumInvestmentPref.Often.prefName(context)
        };
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInvestmentPrefBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addRiskRadioButtons(investmentPreference);
        binding.rdgRiskGroup.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            RadioButton radioButton = binding.getRoot().findViewById(radioButtonId);
            if (radioButton == null) {
                return;
            }
            int itemTagIndex = (int) radioButton.getTag();
            switch (itemTagIndex) {
                default:
                case 0:
                    riskAtt = 0;
                    break;
                case 1:
                    riskAtt = 1;
                    break;
                case 2:
                    riskAtt = 2;
                    break;
            }
            riskName = investmentPreference[riskAtt];
            updateInvestmentPref(riskAtt, radioButton.getId());
        });

        binding.chkRememberDetails.setOnCheckedChangeListener((compoundButton, rememberInfo) -> {
            rememberInvestmentPref = rememberInfo;
            sessionManager.setRememberInvestmentPref(rememberInvestmentPref);
        });
    }

    private void addRiskRadioButtons(@NonNull String[] risks) {
        binding.rdgRiskGroup.removeAllViews();
        for (int listIndex = 0; listIndex < risks.length; listIndex++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(listIndex);
            radioButton.setTag(listIndex);
            radioButton.setText(risks[listIndex]);
            binding.rdgRiskGroup.addView(radioButton);
        }
    }

    private void updateInvestmentPref(int investmentPref, int investmentRadioIndex) {
        try {
            if (profileInfo != null) {
                profileInfo.setSelectedRiskIndex(investmentRadioIndex);
                profileInfo.setRiskAtt(investmentPref);

                if (profileInfo.getProfileId() != null) {
                    database.profileInfoDao().update(profileInfo);
                }
            }
        } catch (Exception ex) {
            //TODO add crash logging
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (riskName.isEmpty()) {
            return new VerificationError("Please select an option");
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

    private void refreshData() {
        try {
            profileInfo = database.profileInfoDao().findOne();
            if (profileInfo != null) {
                riskAtt = profileInfo.getRiskAtt();
                riskRadioIndex = profileInfo.getSelectedRiskIndex();
                binding.rdgRiskGroup.check(riskRadioIndex);
            }
            riskName = investmentPreference[riskAtt];
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
