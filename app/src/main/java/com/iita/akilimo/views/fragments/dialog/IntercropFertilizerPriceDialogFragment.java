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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.textfield.TextInputLayout;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.FertilizerPrice;
import com.iita.akilimo.entities.InterCropFertilizer;
import com.iita.akilimo.inherit.BaseDialogFragment;
import com.iita.akilimo.interfaces.IDismissIntercropListener;
import com.iita.akilimo.utils.CurrencyCode;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.List;

;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
@Deprecated
public class IntercropFertilizerPriceDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = IntercropFertilizerPriceDialogFragment.class.getSimpleName();

    public static final String FERTILIZER_TYPE = "selected_type";
    public static final String ARG_ITEM_ID = "fertilizer_dialog_fragment";

    private boolean isExactPriceRequired = false;
    private boolean isPriceValid = false;
    private boolean priceSpecified = false;
    private boolean removeSelected = false;

    private Dialog dialog;
    private RadioGroup radioGroup;
    private TextInputLayout exactPriceWrapper;
    private EditText editExactFertilizerPrice;

    private Button btnClose;
    private Button btnUpdate;
    private Button btnRemove;


    private InterCropFertilizer fertilizer;
    private List<FertilizerPrice> fertilizerPricesList;

    private double savedPricePerBag = 0.0;
    private String countryCode;
    private String currencyCode;
    private Double bagPrice;
    private String bagPriceRange = "NA";
    private Double exactPrice = 0.0;

    private IDismissIntercropListener onDismissListener;

    public IntercropFertilizerPriceDialogFragment(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fertilizer = bundle.getParcelable(FERTILIZER_TYPE);
        }
        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_fertilizer_price_dialog);

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;


        btnClose = dialog.findViewById(R.id.close_button);
        btnUpdate = dialog.findViewById(R.id.update_button);
        btnRemove = dialog.findViewById(R.id.remove_button);

        radioGroup = dialog.findViewById(R.id.radioGroup);
        exactPriceWrapper = dialog.findViewById(R.id.exactPriceWrapper);
        editExactFertilizerPrice = dialog.findViewById(R.id.editExactFertilizerPrice);
        TextView lblPricePerBag = dialog.findViewById(R.id.lblFragmentTitle);

        if (fertilizer != null) {
            countryCode = fertilizer.getCountryCode();
            currencyCode = fertilizer.getCurrency();
            String currencySymbol = currencyCode;
            ExtendedCurrency extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode);
            if (extendedCurrency != null) {
                currencySymbol = extendedCurrency.getSymbol();
            }
            String titleText = context.getString(R.string.price_per_bag, currencySymbol, fertilizer.getName());
            lblPricePerBag.setText(titleText);
        }


        btnClose.setOnClickListener(view -> {
            priceSpecified = false;
            removeSelected = false;
            dismiss();
        });

        btnRemove.setOnClickListener(view -> {
            priceSpecified = false;
            removeSelected = true;
            fertilizer.setPrice(0.0);
            fertilizer.setPricePerBag(0.0);
            fertilizer.setPriceRange(null);
            fertilizer.setSelected(false);
            fertilizer.setExactPrice(false);
            dismiss();
        });
        //save the data
        btnUpdate.setOnClickListener(v -> {

            if (isExactPriceRequired) {
                try {
                    bagPrice = Double.valueOf(editExactFertilizerPrice.getText().toString());
                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                    Crashlytics.logException(ex);
                }
                if (bagPrice <= 0) {
                    editExactFertilizerPrice.setError("Please provide a valid bag price");
                    isPriceValid = false;
                    return;
                }
                savedPricePerBag = bagPrice;
                bagPriceRange = mathHelper.formatNumber(savedPricePerBag, currencyCode);
                isPriceValid = true;
                editExactFertilizerPrice.setError(null);
            }

            fertilizer.setPrice(bagPrice);
            fertilizer.setPricePerBag(savedPricePerBag);
            fertilizer.setPriceRange(bagPriceRange);
            fertilizer.setSelected(true);
            fertilizer.setExactPrice(isExactPriceRequired);

            if (isPriceValid) {
                priceSpecified = true;
                removeSelected = false;
                dismiss();
            }


        });

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> radioSelected(radioGroup));
        if (database != null) {
            fertilizerPricesList = database.fertilizerPriceDao().findAllByCountry(countryCode);
            addPriceRadioButtons(fertilizerPricesList, fertilizer);
        }
        return dialog;
    }


    private void radioSelected(RadioGroup radioGroup) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = dialog.findViewById(radioButtonId);
        long itemTagIndex = (long) radioButton.getTag();

        try {
            FertilizerPrice pricesResp = fertilizerPricesList.get((int) itemTagIndex);
            isExactPriceRequired = false;
            isPriceValid = true;
            savedPricePerBag = pricesResp.getPricePerBag();
            bagPriceRange = pricesResp.getPriceRange();

            bagPrice = savedPricePerBag;
            exactPriceWrapper.setVisibility(View.GONE);
            exactPriceWrapper.getEditText().setText(null);
            if (savedPricePerBag == 0) {
                bagPrice = 0.0;
                bagPriceRange = "NA";
            } else if (savedPricePerBag < 0) {
                isExactPriceRequired = true;
                isPriceValid = false;
                exactPriceWrapper.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Radio selection issues");
            Crashlytics.logException(ex);
        }
    }

    private void addPriceRadioButtons(List<FertilizerPrice> fertilizerPricesList, InterCropFertilizer fertilizer) {
        radioGroup.removeAllViews();
        double selectedPrice = 0.0;
        String currencySymbol = currencyCode;
        ExtendedCurrency extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode);
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.getSymbol();
        }
        if (fertilizer != null) {
            selectedPrice = fertilizer.getPricePerBag();
            isExactPriceRequired = fertilizer.getExactPrice();
            if (fertilizer.getSelected()) {
                btnUpdate.setText(R.string.lbl_update);
                btnRemove.setVisibility(View.VISIBLE);
            }
            if (isExactPriceRequired) {
                isPriceValid = true;
                exactPrice = fertilizer.getPrice();
                if (exactPrice < 0) {
                    exactPrice = 0.0;
                }
                editExactFertilizerPrice.setText(String.valueOf(exactPrice));
            }
        }
        for (FertilizerPrice pricesResp : fertilizerPricesList) {

            long listIndex = pricesResp.getPriceId() - 1;//reduce by one so as to match the index in the list

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);
            //radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.spacing_large));

            double price = pricesResp.getPricePerBag();
            String radioLabel = String.format("%s-%s %s", pricesResp.getMinLocalPrice(), pricesResp.getMaxLocalPrice(), currencySymbol);

            if (price == 0) {
                radioLabel = context.getString(R.string.lbl_do_not_know);
            } else if (price < 0) {
                radioLabel = context.getString(R.string.exact_fertilizer_price);
            }
            radioButton.setText(radioLabel);

            radioGroup.addView(radioButton);
            //set relevant radio button as selected based on the price range
            if (isExactPriceRequired) {
                radioButton.setChecked(true);
                isPriceValid = true;
                exactPriceWrapper.setVisibility(View.VISIBLE);
                editExactFertilizerPrice.setText(String.valueOf(exactPrice));
            } else if (price == selectedPrice) {
                radioButton.setChecked(true);
            }
            isExactPriceRequired = false; //reset this one
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this.priceSpecified, this.fertilizer, this.removeSelected);
        }
    }

    public void setOnDismissListener(IDismissIntercropListener dismissListener) {
        this.onDismissListener = dismissListener;
    }
}
