package com.akilimo.mobile.views.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentFieldSizeBinding;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.enums.EnumFieldArea;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FieldSizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FieldSizeFragment extends BaseStepFragment {


    FragmentFieldSizeBinding binding;

    AppCompatTextView title;
    AppCompatTextView specifiedArea;
    RadioGroup rdgFieldArea;
    RadioButton rdSpecifyArea;
    RadioButton rd_quarter_acre;
    RadioButton rd_half_acre;
    RadioButton rd_one_acre;
    RadioButton rd_two_half_acre;

    private ProfileInfo profileInfo;
    private MandatoryInfo mandatoryInfo;

    private String myFieldSize = "";
    private double areaSize;
    private boolean isExactArea;
    private boolean areaUnitChanged;
    private String titleMessage;
    private String areaUnit = "";
    private String displayLanguage = "";
    private String displayAreaUnit = "";
    private String oldAreaUnit = "";
    private int fieldSizeRadioIndex;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public FieldSizeFragment() {
        // Required empty public constructor
    }

    public static FieldSizeFragment newInstance() {
        return new FieldSizeFragment();
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFieldSizeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = binding.title;
        specifiedArea = binding.specifiedArea;
        rdgFieldArea = binding.rdgFieldArea;
        rdSpecifyArea = binding.rdSpecifyAcre;
        rd_quarter_acre = binding.rdQuarterAcre;
        rd_half_acre = binding.rdHalfAcre;
        rd_one_acre = binding.rdOneAcre;
        rd_two_half_acre = binding.rdTwoHalfAcre;


        rdgFieldArea.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            RadioButton radioButton = view.findViewById(radioIndex);
            if (radioButton != null) {
                if (radioButton.isPressed()) {
                    radioSelected(radioIndex);
                }
            }

        });

        rdSpecifyArea.setOnClickListener(radioButton -> {
            if (radioButton != null && radioButton.isPressed()) {
                showCustomDialog();
            }
        });
    }

    public void refreshData() {
        try {
            profileInfo = database.profileInfoDao().findOne();
            if (profileInfo != null) {
                displayLanguage = profileInfo.getLanguage();
            }
            mandatoryInfo = database.mandatoryInfoDao().findOne();
            if (mandatoryInfo != null) {
                isExactArea = mandatoryInfo.getExactArea();
                areaUnit = mandatoryInfo.getAreaUnit();
                displayAreaUnit = mandatoryInfo.getDisplayAreaUnit();
                oldAreaUnit = mandatoryInfo.getOldAreaUnit();
                areaSize = mandatoryInfo.getAreaSize();
                fieldSizeRadioIndex = mandatoryInfo.getFieldSizeRadioIndex();
                myFieldSize = String.valueOf(areaSize);
                areaUnitChanged = !areaUnit.equalsIgnoreCase(oldAreaUnit);

                setFieldLabels(areaUnit);
                if (areaUnitChanged) {
                    areaSize = 0;
                    myFieldSize = null;
                    rdgFieldArea.clearCheck();
                } else {
                    rdgFieldArea.check(fieldSizeRadioIndex);
                }
                if (isExactArea) {
                    setExactAreaText(areaSize, displayAreaUnit);
                    specifiedArea.setVisibility(View.VISIBLE);
                } else {
                    specifiedArea.setVisibility(View.GONE);
                }

            }
        } catch (Exception ex) {
            //TODO send crash logs to third party service
        }
    }

    private void setExactAreaText(double areaSize, String displayUnit) {
        String fieldSize = mathHelper.removeLeadingZero(areaSize);
        String areaUnitLabel = String.format("%s %s", fieldSize, displayUnit);
        if (displayLanguage.equalsIgnoreCase("sw")) {
            areaUnitLabel = String.format("%s %s", displayUnit, fieldSize);
        }
        specifiedArea.setText(areaUnitLabel);
    }


    private void radioSelected(int checked) {
        specifiedArea.setVisibility(View.GONE);
        isExactArea = false;
        switch (checked) {
            case R.id.rd_quarter_acre:
                areaSize = EnumFieldArea.QUARTER_ACRE.areaValue();
                break;
            case R.id.rd_half_acre:
                areaSize = EnumFieldArea.HALF_ACRE.areaValue();
                break;
            case R.id.rd_one_acre:
                areaSize = EnumFieldArea.ONE_ACRE.areaValue();
                break;
            case R.id.rd_two_half_acre:
                areaSize = EnumFieldArea.TWO_HALF_ACRE.areaValue();
                break;
            case R.id.rd_specify_acre:
                isExactArea = true;
                areaSize = EnumFieldArea.EXACT_AREA.areaValue();
                myFieldSize = null;
                return;
        }

        //convert to specified area unit
        double convertedAreaSize = mathHelper.convertFromAcreToSpecifiedArea(areaSize, areaUnit);
        saveFieldSize(convertedAreaSize);
    }

    private void setFieldLabels(String areaUnit) {
        String quarterAcre;
        String halfAcre;
        String oneAcre;
        String twoHalfAcre;
        rd_two_half_acre.setVisibility(View.GONE);
        switch (areaUnit) {
            default:
            case "acre":
                quarterAcre = getString(R.string.quarter_acre);
                halfAcre = getString(R.string.half_acre);
                oneAcre = getString(R.string.one_acre);
                twoHalfAcre = getString(R.string.two_half_acres);
                break;
            case "ha":
                quarterAcre = getString(R.string.quarter_acre_to_ha);
                halfAcre = getString(R.string.half_acre_to_ha);
                oneAcre = getString(R.string.one_acre_to_ha);
                twoHalfAcre = getString(R.string.two_half_acre_to_ha);
                rd_two_half_acre.setVisibility(View.VISIBLE);
                break;
            case "are":
                quarterAcre = getString(R.string.quarter_acre_to_are);
                halfAcre = getString(R.string.half_acre_to_are);
                oneAcre = getString(R.string.one_acre_to_are);
                twoHalfAcre = getString(R.string.two_half_acre_to_are);
                break;
            case "sqm":
                quarterAcre = getString(R.string.quarter_acre_to_m2);
                halfAcre = getString(R.string.half_acre_to_m2);
                oneAcre = getString(R.string.one_acre_to_m2);
                twoHalfAcre = getString(R.string.two_half_acre_to_m2);
                break;
        }


        String exactArea = context.getString(R.string.exact_field_area);
        rd_quarter_acre.setText(quarterAcre);
        rd_half_acre.setText(halfAcre);
        rd_one_acre.setText(oneAcre);
        rd_two_half_acre.setText(twoHalfAcre);
        rdSpecifyArea.setText(exactArea);
        titleMessage = context.getString(R.string.lbl_cassava_field_size, displayAreaUnit);
        title.setText(titleMessage);
    }

    private void showCustomDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_field_size);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        titleMessage = context.getString(R.string.lbl_cassava_field_size, displayAreaUnit);

        final TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        final EditText et_post = dialog.findViewById(R.id.et_post);

        if (isExactArea && myFieldSize != null) {
            et_post.setText(myFieldSize);
        }
        dialogTitle.setText(titleMessage);

        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            rdgFieldArea.clearCheck();
            areaSize = -1;
            isExactArea = false;
        });

        dialog.findViewById(R.id.bt_submit).setOnClickListener(v -> {
            myFieldSize = et_post.getText().toString().trim();
            if (myFieldSize.isEmpty()) {
                String prompt = context.getString(R.string.lbl_field_size_prompt, displayAreaUnit);
                Toast.makeText(context, prompt, Toast.LENGTH_SHORT).show();
            } else {
                areaSize = Double.parseDouble(myFieldSize);
                dialog.dismiss();
                setExactAreaText(areaSize, displayAreaUnit);
                saveFieldSize(areaSize);
                specifiedArea.setVisibility(View.VISIBLE);
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void saveFieldSize(double convertedAreaSize) {
        fieldSizeRadioIndex = rdgFieldArea.getCheckedRadioButtonId();
        if (convertedAreaSize <= 0) {
            showCustomWarningDialog(context.getString(R.string.lbl_field_size_prompt, displayAreaUnit), context.getString(R.string.lbl_field_size_prompt, displayAreaUnit));
            return;
        }

        areaUnitChanged = false; //Reset the area unit changed flag
        try {
            if (mandatoryInfo == null) {
                mandatoryInfo = new MandatoryInfo();
            }
            mandatoryInfo.setFieldSizeRadioIndex(fieldSizeRadioIndex);
            mandatoryInfo.setAreaSize(convertedAreaSize);
            mandatoryInfo.setOldAreaUnit(areaUnit);
            mandatoryInfo.setExactArea(isExactArea);
            if (mandatoryInfo.getId() != null) {
                database.mandatoryInfoDao().update(mandatoryInfo);
            } else {
                database.mandatoryInfoDao().insert(mandatoryInfo);
            }
            mandatoryInfo = database.mandatoryInfoDao().findOne();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (!(areaSize <= 0)) {
            return null;
        }
        errorMessage = context.getString(R.string.lbl_field_size_prompt, displayAreaUnit);
        return new VerificationError(errorMessage);
    }

    @Override
    public void onSelected() {
        refreshData();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
