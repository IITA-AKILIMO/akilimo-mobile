package com.akilimo.mobile.views.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.akilimo.mobile.databinding.FragmentInvestmentPrefBinding;
import com.akilimo.mobile.views.fragments.dialog.SingleSelectDialogFragment;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.enums.EnumRiskAtt;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InvestmentPrefFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InvestmentPrefFragment extends BaseStepFragment {

    private FragmentInvestmentPrefBinding binding;
    private ProfileInfo profileInfo;

    AppCompatButton btnPickRisk;
    AppCompatTextView txtRiskText;

    String riskName;
    private int riskAtt = 0;
    private int riskIndex = -1;

    private String[] risks = null;

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
        risks = new String[]{
                EnumRiskAtt.Never.riskName(context),
                EnumRiskAtt.Sometimes.riskName(context),
                EnumRiskAtt.Often.riskName(context)
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

        txtRiskText = binding.riskAttText;
        btnPickRisk = binding.btnPickRiskAtt;

        Bundle arguments = new Bundle();
        final FragmentManager fm = getChildFragmentManager();

        btnPickRisk.setOnClickListener(pickerDialog -> {
            SingleSelectDialogFragment dialogFragment = new SingleSelectDialogFragment(context);

            arguments.putStringArray(SingleSelectDialogFragment.RISK_LIST, risks);
            arguments.putInt(SingleSelectDialogFragment.RISK_INDEX, riskIndex);
            dialogFragment.setArguments(arguments);

            dialogFragment.setOnDismissListener(new SingleSelectDialogFragment.IDismissDialog() {
                @Override
                public void onDismiss(String selectedRiskName, int selectedRiskAtt, int selectedRiskIndex, boolean cancelled) {
                    if (!cancelled) {
                        riskAtt = selectedRiskAtt;
                        riskIndex = selectedRiskIndex;
                        txtRiskText.setText(selectedRiskName);
                        updatedRiskAttitude();
                    }
                }
            });

            dialogFragment.show(fm, SingleSelectDialogFragment.ARG_ITEM_ID);

        });
    }


    private void updatedRiskAttitude() {
        try {
            if (profileInfo == null) {
                profileInfo = new ProfileInfo();
            }

            profileInfo.setSelectedRiskIndex(riskIndex);
            profileInfo.setRiskAtt(riskAtt);

            dataIsValid = !Strings.isEmptyOrWhitespace(riskName);
            if (profileInfo.getProfileId() != null) {
                int id = profileInfo.getProfileId();
                if (id > 0) {
                    database.profileInfoDao().update(profileInfo);
                }
            } else {
                database.profileInfoDao().insert(profileInfo);
            }
        } catch (Exception ex) {
            dataIsValid = false;
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (!dataIsValid) {
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
                riskIndex = profileInfo.getSelectedRiskIndex();
                dataIsValid = true;
            }

            riskName = risks[riskAtt];
            txtRiskText.setText(riskName);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }
}
