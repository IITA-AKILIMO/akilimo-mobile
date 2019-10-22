package com.iita.akilimo.views.fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
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

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.utils.enums.EnumAreaUnits;
import com.iita.akilimo.utils.enums.EnumFieldArea;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FieldSizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FieldSizeFragment extends BaseFragment {

    @BindView(R.id.title)
    AppCompatTextView title;

    @BindView(R.id.specifiedArea)
    AppCompatTextView specifiedArea;
    @BindView(R.id.rdgFieldArea)
    RadioGroup rdgFieldArea;

    @BindView(R.id.rd_specify_acre)
    RadioButton rdSpecifyArea;

    @BindView(R.id.rd_quarter_acre)
    RadioButton rd_quarter_acre;

    @BindView(R.id.rd_half_acre)
    RadioButton rd_half_acre;

    @BindView(R.id.rd_one_acre)
    RadioButton rd_one_acre;

    @BindView(R.id.rd_one_half_acre)
    RadioButton rd_one_half_acre;

    @BindView(R.id.rd_two_half_acre)
    RadioButton rd_two_half_acre;

    protected Context context;
    private double areaSize;
    private String myFieldSize = "";


    private String quarter_acre;
    private String half_acre;
    private String one_acre;
    private String one_half_acres;
    private String two_half_acres;
    private String exact_acre;
    private String titleMessage;

    private ProfileInfo profileInfo;
    private MandatoryInfo mandatoryInfo;
    private EnumAreaUnits areaUnits = EnumAreaUnits.ACRE;
    private EnumFieldArea fieldAreaEnum = EnumFieldArea.UNKNOWN;


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
        return inflater.inflate(R.layout.fragment_field_size, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rdgFieldArea.setOnCheckedChangeListener((radioGroup, radioIndex) -> radioSelected(radioIndex));

        rdSpecifyArea.setOnClickListener(view1 -> {
            if (rdSpecifyArea.isChecked()) {
                showCustomDialog();
            }
        });
    }

    @Override
    public void refreshData() {
        profileInfo = objectBoxEntityProcessor.getProfileInfo();
        mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            fieldAreaEnum = mandatoryInfo.getFieldAreaEnum();
            areaUnits = mandatoryInfo.getAreaUnitsEnum();

            setFieldLabels(areaUnits);
            myFieldSize = String.valueOf(mandatoryInfo.getAreaSize());
            specifiedArea.setText(null);
            switch (fieldAreaEnum) {
                case QUARTER_ACRE:
                    rdgFieldArea.check(R.id.rd_quarter_acre);
                    break;
                case HALF_ACRE:
                    rdgFieldArea.check(R.id.rd_half_acre);
                    break;
                case ONE_ACRE:
                    rdgFieldArea.check(R.id.rd_one_acre);
                    break;
                case ONE_HALF_ACRE:
                    rdgFieldArea.check(R.id.rd_one_half_acre);
                    break;
                case TWO_HALF_ACRE:
                    rdgFieldArea.check(R.id.rd_two_half_acre);
                    break;
                case FIVE_ACRE:
                    rdgFieldArea.check(R.id.rd_five_acre);
                    break;
                case EXACT_AREA:
                    rdgFieldArea.check(R.id.rd_specify_acre);
                    specifiedArea.setText(String.format("%s %s", myFieldSize, areaUnits));
                    specifiedArea.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void radioSelected(int checked) {
        areaSize = 0;
        specifiedArea.setVisibility(View.GONE);
        switch (checked) {
            case R.id.rd_quarter_acre:
                fieldAreaEnum = EnumFieldArea.QUARTER_ACRE;
                areaSize = EnumFieldArea.QUARTER_ACRE.areaValue();
                break;
            case R.id.rd_half_acre:
                fieldAreaEnum = EnumFieldArea.HALF_ACRE;
                areaSize = EnumFieldArea.HALF_ACRE.areaValue();
                break;
            case R.id.rd_one_acre:
                fieldAreaEnum = EnumFieldArea.ONE_ACRE;
                areaSize = EnumFieldArea.ONE_ACRE.areaValue();
                break;
            case R.id.rd_one_half_acre:
                fieldAreaEnum = EnumFieldArea.ONE_HALF_ACRE;
                areaSize = EnumFieldArea.ONE_HALF_ACRE.areaValue();
                break;
            case R.id.rd_two_half_acre:
                fieldAreaEnum = EnumFieldArea.TWO_HALF_ACRE;
                areaSize = EnumFieldArea.TWO_HALF_ACRE.areaValue();
                break;
            case R.id.rd_five_acre:
                fieldAreaEnum = EnumFieldArea.FIVE_ACRE;
                areaSize = EnumFieldArea.FIVE_ACRE.areaValue();
                break;
            case R.id.rd_specify_acre:
                specifiedArea.setVisibility(View.VISIBLE);
                return;
            default:
                return;
        }

        saveFieldSize();
    }

    private void saveFieldSize() {
        profileInfo = objectBoxEntityProcessor.getProfileInfo();
        if (mandatoryInfo == null) {
            mandatoryInfo = new MandatoryInfo();
        }
        mandatoryInfo.setFieldAreaEnum(fieldAreaEnum);
        mandatoryInfo.setAreaSize(fieldAreaEnum.areaValue());
        mandatoryInfo.setAreaSize(areaSize);
        objectBoxEntityProcessor.saveMandatoryInfo(mandatoryInfo);
    }

//    @SuppressLint("StringFormatInvalid")
    private void setFieldLabels(EnumAreaUnits areaUnits) {
        switch (areaUnits) {
            case ACRE:
                quarter_acre = getString(R.string.quarter_acre);
                half_acre = getString(R.string.half_acre);
                one_acre = getString(R.string.one_acre);
                one_half_acres = getString(R.string.one_half_acre);
                two_half_acres = getString(R.string.two_half_acres);
                exact_acre = getString(R.string.exact_field_area);
                break;
            case HA:
                quarter_acre = getString(R.string.quarter_acre_to_ha);
                half_acre = getString(R.string.half_acre_to_ha);
                one_acre = getString(R.string.one_acre_to_ha);
                two_half_acres = getString(R.string.two_half_acre_to_ha);
                one_half_acres = getString(R.string.one_half_acre_to_ha);
                exact_acre = getString(R.string.exact_acre_to_ha);
                break;
            case SQM:
                quarter_acre = getString(R.string.quarter_acre_to_m2);
                half_acre = getString(R.string.half_acre_to_m2);
                one_acre = getString(R.string.one_acre_to_m2);
                one_half_acres = getString(R.string.one_half_acre_to_m2);
                two_half_acres = getString(R.string.two_half_acre_to_m2);
                exact_acre = getString(R.string.exact_acre_to_m2);
                break;
        }

        rd_quarter_acre.setText(quarter_acre);
        rd_half_acre.setText(half_acre);
        rd_one_acre.setText(one_acre);
        rd_one_half_acre.setText(one_half_acres);
        rd_two_half_acre.setText(two_half_acres);
        rdSpecifyArea.setText(exact_acre);
        titleMessage = context.getString(R.string.lbl_cassava_field_size, areaUnits.unitString());
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
                Toast.makeText(context, "Please enter a valid field size", Toast.LENGTH_SHORT).show();
            } else {
                areaSize = Double.parseDouble(myFieldSize);
                dialog.dismiss();
                specifiedArea.setText(String.format("%s %s", myFieldSize, areaUnits));
                saveFieldSize();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
}
