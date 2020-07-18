package com.iita.akilimo.views.fragments;


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
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentFieldSizeBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.utils.enums.EnumFieldArea;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FieldSizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FieldSizeFragment extends BaseStepFragment {


    AppCompatTextView title;
    AppCompatTextView specifiedArea;
    RadioGroup rdgFieldArea;
    RadioButton rdSpecifyArea;
    RadioButton rd_quarter_acre;
    RadioButton rd_half_acre;
    RadioButton rd_one_acre;
    RadioButton rd_one_half_acre;
    RadioButton rd_two_half_acre;

    FragmentFieldSizeBinding binding;


    private String myFieldSize = "";

    private double areaSize;
    private boolean isExactArea;
    private String quarterAcre;
    private String halfAcre;
    private String oneAcre;
    private String oneHalfAcres;
    private String twoHalfAcres;
    private String exactAcre;
    private String titleMessage;

    private MandatoryInfo mandatoryInfo;
    private String areaUnit = "";
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
        rd_one_half_acre = binding.rdOneHalfAcre;
        rd_two_half_acre = binding.rdTwoHalfAcre;

        rdgFieldArea.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            RadioButton radioButton = view.findViewById(radioIndex);
            if (radioButton != null) {
                if (radioButton.isPressed()) {
                    radioSelected(radioIndex);
                }
            }

        });
    }

    public void refreshData() {
        try {
            mandatoryInfo = database.mandatoryInfoDao().findOne();
            if (mandatoryInfo != null) {
                isExactArea = mandatoryInfo.getExactArea();
                areaUnit = mandatoryInfo.getAreaUnit();
                areaSize = mandatoryInfo.getAreaSize();
                fieldSizeRadioIndex = mandatoryInfo.getFieldSizeRadioIndex();
                myFieldSize = String.valueOf(areaSize);
                dataIsValid = areaSize > 0;

                if (dataIsValid) {
                    setFieldLabels(areaUnit);
                }

                rdgFieldArea.check(fieldSizeRadioIndex);
                if (isExactArea) {
                    specifiedArea.setText(String.format("%s %s", myFieldSize, areaUnit));
                    specifiedArea.setVisibility(View.VISIBLE);
                } else {
                    specifiedArea.setVisibility(View.GONE);
                }

            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred fetching info");
            Crashlytics.logException(ex);
        }
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
            case R.id.rd_one_half_acre:
                areaSize = EnumFieldArea.ONE_HALF_ACRE.areaValue();
                break;
            case R.id.rd_two_half_acre:
                areaSize = EnumFieldArea.TWO_HALF_ACRE.areaValue();
                break;
            case R.id.rd_five_acre:
                areaSize = EnumFieldArea.FIVE_ACRE.areaValue();
                break;
            case R.id.rd_specify_acre:
                isExactArea = true;
                areaSize = EnumFieldArea.EXACT_AREA.areaValue();
                specifiedArea.setVisibility(View.VISIBLE);
                showCustomDialog();
                return;
        }

        //convert to specified area unit
        double convertedAreaSize = mathHelper.convertFromAcreToSpecifiedArea(areaSize, areaUnit);
        saveFieldSize(convertedAreaSize);
    }

    private void saveFieldSize(double convertedAreaSize) {
        fieldSizeRadioIndex = rdgFieldArea.getCheckedRadioButtonId();
        dataIsValid = convertedAreaSize > 0;
        if (!dataIsValid) {
            showCustomWarningDialog(context.getString(R.string.lbl_field_size_prompt), context.getString(R.string.lbl_field_size_prompt));
            return;
        }

        try {
            if (mandatoryInfo == null) {
                mandatoryInfo = new MandatoryInfo();
            }
            mandatoryInfo.setFieldSizeRadioIndex(fieldSizeRadioIndex);
            mandatoryInfo.setAreaSize(convertedAreaSize);
            if (mandatoryInfo.getId() != null) {
                database.mandatoryInfoDao().update(mandatoryInfo);
            } else {
                database.mandatoryInfoDao().insert(mandatoryInfo);
            }
            mandatoryInfo = database.mandatoryInfoDao().findOne();
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    private void setFieldLabels(String areaUnit) {
        switch (areaUnit) {
            default:
            case "acre":
                quarterAcre = getString(R.string.quarter_acre);
                halfAcre = getString(R.string.half_acre);
                oneAcre = getString(R.string.one_acre);
                oneHalfAcres = getString(R.string.one_half_acre);
                twoHalfAcres = getString(R.string.two_half_acres);
                exactAcre = getString(R.string.exact_field_area);
                break;
            case "ha":
                quarterAcre = getString(R.string.quarter_acre_to_ha);
                halfAcre = getString(R.string.half_acre_to_ha);
                oneAcre = getString(R.string.one_acre_to_ha);
                twoHalfAcres = getString(R.string.two_half_acre_to_ha);
                oneHalfAcres = getString(R.string.one_half_acre_to_ha);
                exactAcre = getString(R.string.exact_acre_to_ha);
                break;
            case "sqm":
                quarterAcre = getString(R.string.quarter_acre_to_m2);
                halfAcre = getString(R.string.half_acre_to_m2);
                oneAcre = getString(R.string.one_acre_to_m2);
                oneHalfAcres = getString(R.string.one_half_acre_to_m2);
                twoHalfAcres = getString(R.string.two_half_acre_to_m2);
                exactAcre = getString(R.string.exact_acre_to_m2);
                break;
        }

        rd_quarter_acre.setText(quarterAcre);
        rd_half_acre.setText(halfAcre);
        rd_one_acre.setText(oneAcre);
        rd_one_half_acre.setText(oneHalfAcres);
        rd_two_half_acre.setText(twoHalfAcres);
        rdSpecifyArea.setText(exactAcre);
        titleMessage = context.getString(R.string.lbl_cassava_field_size, areaUnit);
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


        final TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        final EditText et_post = dialog.findViewById(R.id.et_post);

        et_post.setText(myFieldSize);
        dialogTitle.setText(titleMessage);

        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.bt_submit).setOnClickListener(v -> {
            myFieldSize = et_post.getText().toString().trim();
            if (Strings.isEmptyOrWhitespace(myFieldSize)) {
                Toast.makeText(context, R.string.lbl_field_size_prompt, Toast.LENGTH_SHORT).show();
            } else {
                areaSize = Double.parseDouble(myFieldSize);
                dialog.dismiss();
                specifiedArea.setText(String.format("%s %s", myFieldSize, areaUnit));
                saveFieldSize(areaSize);
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (!dataIsValid) {
            errorMessage = context.getString(R.string.lbl_field_size_prompt);
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
