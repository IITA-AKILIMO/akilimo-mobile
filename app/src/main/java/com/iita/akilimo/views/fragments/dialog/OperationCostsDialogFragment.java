package com.iita.akilimo.views.fragments.dialog;


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
import androidx.fragment.app.DialogFragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputLayout;
import com.iita.akilimo.R;
import com.iita.akilimo.models.OperationCost;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumOperation;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class OperationCostsDialogFragment extends DialogFragment {

    public static final String OPERATION_NAME = "operation_type";
    public static final String SELECTED_COUNTRY = "country";
    public static final String COST_LIST = "cost_list";
    public static final String DIALOG_TITLE = "dialog_title";

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


    private MathHelper mathHelper;
    private Context context;

    private double selectedCost = 0.0;
    private String translatedSuffix;
    private String currencyCode;
    private String operationName;
    private String bagPrice;
    private String bagPriceRange = "NA";
    private String exactPrice = "0";
    private String dialogTitle;

    private EnumCountry enumCountry;
    private IDismissDialog onDismissListener;
    private ArrayList<OperationCost> operationCosts;
    private OperationCost operationCost;
    private EnumOperation enumOperation;

    public OperationCostsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        mathHelper = new MathHelper();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            operationCosts = bundle.getParcelableArrayList(COST_LIST);
            enumCountry = bundle.getParcelable(SELECTED_COUNTRY);
            enumOperation = bundle.getParcelable(OPERATION_NAME);
            dialogTitle = bundle.getString(DIALOG_TITLE);
        }

        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_operation_cost_dialog);
        ButterKnife.bind(dialog);

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
                bagPriceRange = mathHelper.formatNumber(selectedCost, currencyCode);
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
        return dialog;
    }

    private void radioSelected(RadioGroup radioGroup) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = dialog.findViewById(radioButtonId);
        long itemTagIndex = (long) radioButton.getTag();

        try {
            operationCost = operationCosts.get((int) itemTagIndex);
            if (enumCountry.equals(EnumCountry.NIGERIA)) {
                selectedCost = operationCost.getAverageNgnPrice();
            } else if (enumCountry.equals(EnumCountry.TANZANIA)) {
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

            if (enumCountry.equals(EnumCountry.NIGERIA)) {
                price = operationCost.getAverageNgnPrice();
                maxPrice = operationCost.getMaxNgn();
                minPrice = operationCost.getMinNgn();
            } else if (enumCountry.equals(EnumCountry.TANZANIA)) {
                price = operationCost.getAverageTzsPrice();
                maxPrice = operationCost.getMaxTzs();
                minPrice = operationCost.getMinTzs();
            }

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);


            String radioLabel = String.format("%s %s %s %s", minPrice, translatedSuffix, maxPrice, enumCountry.currency());

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
            onDismissListener.onDismiss(operationCost, enumOperation, selectedCost, cancelled, isExactCostRequired);
        }
    }

    public void setOnDismissListener(IDismissDialog dismissListener) {
        this.onDismissListener = dismissListener;
    }

    /* callback interface for pricing specification*/
    public interface IDismissDialog {
        void onDismiss(OperationCost operationCost, EnumOperation operation, double selectedCost, boolean cancelled, boolean isExactCost);
    }
}
