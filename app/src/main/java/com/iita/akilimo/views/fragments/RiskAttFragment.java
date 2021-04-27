package com.iita.akilimo.views.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentRiskAttBinding;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.utils.enums.EnumRiskAtt;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RiskAttFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RiskAttFragment extends BaseStepFragment {

    private FragmentRiskAttBinding binding;
    private ProfileInfo profileInfo;

    AppCompatButton btnPickRisk;
    AppCompatTextView txtRiskText;

    String riskName;
    private int riskAtt = 0;
    private int selectedRiskIndex = -1;

    private String[] risks = null;

    public RiskAttFragment() {
        // Required empty public constructor
    }

    public static RiskAttFragment newInstance() {
        return new RiskAttFragment();
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
        binding = FragmentRiskAttBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtRiskText = binding.riskAttText;
        btnPickRisk = binding.btnPickRiskAtt;

        btnPickRisk.setOnClickListener(pickerDialog -> {

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.lbl_select_risk_att));
            builder.setSingleChoiceItems(risks, selectedRiskIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectedRiskIndex = i;
                }
            });
            builder.setPositiveButton(context.getString(R.string.lbl_ok), (dialogInterface, whichButton) -> {
                if (selectedRiskIndex >= 0) {
                    riskName = risks[selectedRiskIndex];
                    switch (riskName.toLowerCase()) {
                        case "never":
                        default:
                            riskAtt = 0;
                            break;
                        case "sometimes":
                            riskAtt = 1;
                            break;
                        case "often":
                            riskAtt = 2;
                            break;
                    }
                    txtRiskText.setText(riskName);
                    dialogInterface.dismiss();
                    updatedRiskAttitude();
                }
            });
            builder.setNegativeButton(context.getString(R.string.lbl_cancel), ((dialogInterface, i) -> {
                dialogInterface.dismiss();
            }));

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });
    }

    private void updatedRiskAttitude() {
        try {
            if (profileInfo == null) {
                profileInfo = new ProfileInfo();
            }

            profileInfo.setSelectedRiskIndex(selectedRiskIndex);
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
                selectedRiskIndex = profileInfo.getSelectedRiskIndex();
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
