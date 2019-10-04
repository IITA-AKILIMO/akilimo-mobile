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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.models.MyLocation;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FieldSizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FieldSizeFragment extends BaseFragment {

    @BindView(R.id.radioFieldAreaGroup)
    RadioGroup radioGroup;

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
    private double fieldArea;
    private String myFieldSize = "";
    private String selectedFieldArea;
    private String areaUnits;

    private String quarter_acre;
    private String half_acre;
    private String one_acre;
    private String one_half_acres;
    private String two_half_acres;
    private String exact_acre;

    private boolean exactArea;
    private MyLocation location;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_field_size, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioGroup.setOnCheckedChangeListener((radioGroup, radioIndex) -> radioSelected(radioIndex));

        rdSpecifyArea.setOnClickListener(view1 -> {
            if (rdSpecifyArea.isChecked()) {
                showCustomDialog();
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            location = objectBoxEntityProcessor.getLocation();
            if (location != null) {
                setFieldLabels(location.getAreaUnit());
            }

        }
    }

    private void radioSelected(int checked) {
        fieldArea = 0;
        switch (checked) {
            case R.id.rd_quarter_acre:
                fieldArea = 0.25;
                selectedFieldArea = quarter_acre;
                break;
            case R.id.rd_half_acre:
                fieldArea = 0.5;
                selectedFieldArea = half_acre;
                break;
            case R.id.rd_one_acre:
                fieldArea = 1;
                selectedFieldArea = one_acre;
                break;
            case R.id.rd_one_half_acre:
                fieldArea = 1.5;
                selectedFieldArea = one_half_acres;
                break;
            case R.id.rd_two_half_acre:
                fieldArea = 2.5;
                selectedFieldArea = two_half_acres;
                break;
            case R.id.rd_five_acre:
                fieldArea = 5;
                break;
            default:
                return;
        }

        saveFieldSize();
    }

    private void saveFieldSize() {
        location = objectBoxEntityProcessor.getLocation();
        if (location == null) {
            location = new MyLocation();
        }
        location.setAreaSize(fieldArea);
        objectBoxEntityProcessor.saveLocationData(location);
    }

    private void setFieldLabels(String areaUnits) {
        if (areaUnits == null) {
            areaUnits = "acre";
        }
        switch (areaUnits) {
            case "acre":
                quarter_acre = getString(R.string.quarter_acre);
                half_acre = getString(R.string.half_acre);
                one_acre = getString(R.string.one_acre);
                one_half_acres = getString(R.string.one_half_acre);
                two_half_acres = getString(R.string.two_half_acres);
                exact_acre = getString(R.string.exact_field_area);
                break;
            case "ha":
                quarter_acre = getString(R.string.quarter_acre_to_ha);
                half_acre = getString(R.string.half_acre_to_ha);
                one_acre = getString(R.string.one_acre_to_ha);
                two_half_acres = getString(R.string.two_half_acre_to_ha);
                one_half_acres = getString(R.string.one_half_acre_to_ha);
                exact_acre = getString(R.string.exact_acre_to_ha);
                break;
            case "m2":
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

        final EditText et_post = dialog.findViewById(R.id.et_post);
        et_post.setText(myFieldSize);

        dialog.findViewById(R.id.bt_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.bt_submit).setOnClickListener(v -> {
            myFieldSize = et_post.getText().toString().trim();
            if (Strings.isEmptyOrWhitespace(myFieldSize)) {
                Toast.makeText(context, "Please enter field size", Toast.LENGTH_SHORT).show();
            } else {
                fieldArea = Double.parseDouble(myFieldSize);
                saveFieldSize();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
}
