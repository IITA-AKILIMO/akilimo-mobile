package com.akilimo.mobile.views.fragments.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputLayout;
import com.akilimo.mobile.R;
import com.akilimo.mobile.inherit.BaseDialogFragment;
import com.akilimo.mobile.models.OperationCost;
import com.akilimo.mobile.utils.enums.EnumCountry;

import java.util.ArrayList;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class OperationCostsDialogFragment extends BaseDialogFragment {

    public static final String OPERATION_NAME = "operation_type";
    public static final String COUNTRY_CODE = "country";
    public static final String CURRENCY_CODE = "currency_code";
    public static final String CURRENCY_SYMBOL = "currency_symbol";
    public static final String COST_LIST = "cost_list";
    public static final String DIALOG_TITLE = "dialog_title";
    public static final String EXACT_PRICE_HINT = "exact_price_title";

    private static final String LOG_TAG = OperationCostsDialogFragment.class.getSimpleName();

    public static final String ARG_ITEM_ID = "fertilizer_dialog_fragment";

    private boolean isExactCostRequired = false;
    private boolean isPriceValid = false;
    private boolean priceSpecified = false;
    private boolean removeSelected = false;
    private boolean cancelled = false;

    private Dialog dialog;
    private RadioGroup radioGroup;
    private TextInputLayout exactPriceWrapper;
    private EditText editExactCost;

    private double selectedCost = 0.0;
    private String translatedSuffix;
    private String currencyCode;
    private String currencySymbol;
    private String operationName;
    private String bagPrice;
    private String bagPriceRange = "NA";
    private String exactPrice = "0";
    private String dialogTitle;
    private String exactPriceHint;

    private String countryCode;
    private IDismissDialog onDismissListener;
    private ArrayList<OperationCost> operationCosts;
    private OperationCost operationCost;

    public OperationCostsDialogFragment(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            operationCosts = bundle.getParcelableArrayList(COST_LIST);
            countryCode = bundle.getString(COUNTRY_CODE);
            currencyCode = bundle.getString(CURRENCY_CODE);
            currencySymbol = bundle.getString(CURRENCY_SYMBOL);
            operationName = bundle.getString(OPERATION_NAME);
            dialogTitle = bundle.getString(DIALOG_TITLE);
            exactPriceHint = bundle.getString(EXACT_PRICE_HINT);
        }

        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_operation_cost_dialog);


        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;


        Button btnClose = dialog.findViewById(R.id.close_button);
        Button btnSave = dialog.findViewById(R.id.save_button);
        TextView lblPricePerBag = dialog.findViewById(R.id.lblFragmentTitle);

        radioGroup = dialog.findViewById(R.id.radioGroup);
        exactPriceWrapper = dialog.findViewById(R.id.exactPriceWrapper);
        editExactCost = dialog.findViewById(R.id.editExactCost);


        translatedSuffix = context.getString(R.string.lbl_to);


        String titleText = context.getString(R.string.lbl_operation_cost);
        if (!Strings.isEmptyOrWhitespace(dialogTitle)) {
            titleText = dialogTitle;
        }
        lblPricePerBag.setText(titleText);


        btnClose.setOnClickListener(view -> {
            priceSpecified = false;
            removeSelected = false;
            cancelled = true;
            dismiss();
        });

        //save the data
        btnSave.setOnClickListener(v -> {
            if (isExactCostRequired) {
                bagPrice = editExactCost.getText().toString();
                if (Strings.isEmptyOrWhitespace(bagPrice)) {
                    editExactCost.setError("Please provide a valid cost value");
                    isPriceValid = false;
                    return;
                }
                selectedCost = Double.parseDouble(bagPrice);
                bagPriceRange = mathHelper.formatNumber(selectedCost, currencySymbol);
                isPriceValid = true;
                cancelled = false;
                editExactCost.setError(null);
            }

            if (isPriceValid) {
                priceSpecified = true;
                removeSelected = false;
                dismiss();
            }

        });

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> radioSelected(radioGroup));
        addCostRadioButtons(operationCosts);
        exactPriceWrapper.setHint(exactPriceHint);
        return dialog;
    }

    private void radioSelected(RadioGroup radioGroup) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = dialog.findViewById(radioButtonId);
        long itemTagIndex = (long) radioButton.getTag();

        try {
            operationCost = operationCosts.get((int) itemTagIndex);
            if (countryCode.equals(EnumCountry.Nigeria.countryCode())) {
                selectedCost = operationCost.getAverageNgnPrice();
            } else if (countryCode.equals(EnumCountry.Tanzania.countryCode())) {
                selectedCost = operationCost.getAverageTzsPrice();
            }
            isExactCostRequired = false;
            isPriceValid = true;

            bagPrice = String.valueOf(selectedCost);


            if (selectedCost < 0) {
                isExactCostRequired = true;
                isPriceValid = false;
                exactPriceWrapper.setVisibility(View.VISIBLE);
            } else {
                exactPriceWrapper.setVisibility(View.GONE);
                exactPriceWrapper.getEditText().setText(null);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Radio selection issues");
            Crashlytics.logException(ex);
        }
    }

    private void addCostRadioButtons(@NonNull ArrayList<OperationCost> operationCosts) {
        radioGroup.removeAllViews();

        for (OperationCost operationCost : operationCosts) {
            long listIndex = operationCost.getListIndex();
            double price = operationCost.getAverageUsdPrice();
            double maxPrice = operationCost.getMaxUsd();
            double minPrice = operationCost.getMinUsd();

            if (countryCode.equals(EnumCountry.Nigeria.countryCode())) {
                price = operationCost.getAverageNgnPrice();
                maxPrice = operationCost.getMaxNgn();
                minPrice = operationCost.getMinNgn();
            } else if (countryCode.equals(EnumCountry.Tanzania.countryCode())) {
                price = operationCost.getAverageTzsPrice();
                maxPrice = operationCost.getMaxTzs();
                minPrice = operationCost.getMinTzs();
            }

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);



            String radioLabel = String.format(getString(R.string.lbl_operation_cost_label), mathHelper.formatNumber(maxPrice, null), currencySymbol);

            if (price >= 0 && price <= 0) {
                radioLabel = context.getString(R.string.lbl_do_not_know);
            } else if (price < 0) {
                radioLabel = context.getString(R.string.exact_fertilizer_price);
            }
            radioButton.setText(radioLabel);

            radioGroup.addView(radioButton);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(operationCost, operationName, selectedCost, cancelled, isExactCostRequired);
        }
    }

    public void setOnDismissListener(IDismissDialog dismissListener) {
        this.onDismissListener = dismissListener;
    }

    /* callback interface for pricing specification*/
    public interface IDismissDialog {
        void onDismiss(OperationCost operationCost, String operationName, double selectedCost, boolean cancelled, boolean isExactCost);
    }
}
