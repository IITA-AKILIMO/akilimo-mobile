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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.android.material.textfield.TextInputLayout;
import com.akilimo.mobile.R;
import com.akilimo.mobile.entities.MaizePrice;
import com.akilimo.mobile.inherit.BaseDialogFragment;
import com.akilimo.mobile.interfaces.IPriceDialogDismissListener;
import com.akilimo.mobile.utils.CurrencyCode;
import com.akilimo.mobile.utils.enums.EnumUnitOfSale;
import com.mynameismidori.currencypicker.ExtendedCurrency;

import java.util.List;

import io.sentry.Sentry;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class MaizePriceDialogFragment extends BaseDialogFragment {

    private static final String LOG_TAG = MaizePriceDialogFragment.class.getSimpleName();

    public static final String ARG_ITEM_ID = "mazie_price_dialog_fragment";
    public static final String AVERAGE_PRICE = "average_price";
    public static final String PRODUCE_TYPE = "produce_type";
    public static final String SELECTED_PRICE = "selected_price";
    public static final String UNIT_OF_SALE = "unit_of_sale";
    public static final String ENUM_UNIT_OF_SALE = "enum_unit_of_sale";
    public static final String CURRENCY_CODE = "currency_code";
    public static final String CURRENCY_NAME = "currency_name";
    public static final String COUNTRY_CODE = "country_code";

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

    private double averagePrice;
    private double maizePrice;
    private String currencyName;
    private List<MaizePrice> maizePriceList;

    private String countryCode;
    private String currencyCode;
    private String produceType;
    private String unitOfSale;
    private EnumUnitOfSale unitOfSaleEnum;

    private IPriceDialogDismissListener onDismissListener;

    public MaizePriceDialogFragment(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            averagePrice = bundle.getDouble(AVERAGE_PRICE);
            maizePrice = bundle.getDouble(SELECTED_PRICE);
            produceType = bundle.getString(PRODUCE_TYPE, "grain");
            currencyCode = bundle.getString(CURRENCY_CODE);
            currencyName = bundle.getString(CURRENCY_NAME);
            unitOfSale = bundle.getString(UNIT_OF_SALE);
            countryCode = bundle.getString(COUNTRY_CODE);
            unitOfSaleEnum = bundle.getParcelable(ENUM_UNIT_OF_SALE);
        }
        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_cassava_price_dialog);

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

        btnClose.setOnClickListener(view -> {
            dismiss();
        });

        btnRemove.setOnClickListener(view -> {
            averagePrice = 0;
            dismiss();
        });
        //save the data
        btnUpdate.setOnClickListener(v -> {

            if (isExactPriceRequired) {
                try {
                    maizePrice = Double.parseDouble(editExactFertilizerPrice.getText().toString());
                } catch (Exception ex) {
                    Sentry.captureException(ex);
                }
                if (maizePrice <= 0) {
                    editExactFertilizerPrice.setError(getString(R.string.lbl_provide_valid_unit_price));
                    isPriceValid = false;
                    return;
                }
                isPriceValid = true;
                editExactFertilizerPrice.setError(null);
            }
            if (isPriceValid) {
                dismiss();
            }

        });

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> radioSelected(radioGroup));
        if (database != null) {
            maizePriceList = database.maizePriceDao().findAllByCountryAndProduceType(countryCode, produceType);
            addPriceRadioButtons(maizePriceList, averagePrice);
        }
        return dialog;
    }

    private void radioSelected(RadioGroup radioGroup) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = dialog.findViewById(radioButtonId);
        long itemTagIndex = (long) radioButton.getTag();

        try {
            MaizePrice pricesResp = database.maizePriceDao().findPriceByPriceIndex((int) itemTagIndex);
            isExactPriceRequired = false;
            isPriceValid = true;
            averagePrice = pricesResp.getAveragePrice();
            exactPriceWrapper.setVisibility(View.GONE);
            exactPriceWrapper.getEditText().setText(null);

            if (averagePrice < 0) {
                isExactPriceRequired = true;
                isPriceValid = false;
                exactPriceWrapper.setVisibility(View.VISIBLE);
            } else {
                maizePrice = pricesResp.getAveragePrice();
            }
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Sentry.captureException(ex);
        }
    }

    private void addPriceRadioButtons(List<MaizePrice> maizePriceList, double avgPrice) {
        radioGroup.removeAllViews();
        double selectedPrice = 0.0;

        if (avgPrice < 0) {
            btnUpdate.setText(R.string.lbl_update);
            btnRemove.setVisibility(View.VISIBLE);
        }

        for (MaizePrice pricesResp : maizePriceList) {
            double price = pricesResp.getAveragePrice();
            long listIndex = pricesResp.getPriceIndex();//reduce by one so as to match the index in the list

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);


            String radioLabel = labelText(pricesResp.getMinLocalPrice(), pricesResp.getMaxLocalPrice(), currencyCode, unitOfSale);
            if (price < 0) {
                radioLabel = context.getString(R.string.lbl_exact_price_x_per_unit_of_sale);
                String exactTextHint = getString(R.string.exact_fertilizer_price_currency, currencyName);
                exactPriceWrapper.setHint(exactTextHint);
            } else if (price == 0) {
                radioLabel = context.getString(R.string.lbl_do_not_know);
            }
            radioButton.setText(radioLabel);
            radioGroup.addView(radioButton);

            //set relevant radio button as selected based on the price range
            if (avgPrice < 0) {
                radioButton.setChecked(true);
                isPriceValid = true;
                isExactPriceRequired = true;
                exactPriceWrapper.setVisibility(View.VISIBLE);
                editExactFertilizerPrice.setText(String.valueOf(maizePrice));
            }

            if (pricesResp.getAveragePrice() == selectedPrice) {
                radioButton.setChecked(true);
            }
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(maizePrice, isExactPriceRequired);
        }
    }

    public void setOnDismissListener(IPriceDialogDismissListener dismissListener) {
        this.onDismissListener = dismissListener;
    }

    private String labelText(double unitPriceLower, double unitPriceUpper, String currencyCode, String uos, boolean... doConversions) {
        double priceLower = unitPriceLower;
        double priceHigher = unitPriceUpper;
        String currencySymbol = currencyCode;
        ExtendedCurrency extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode);
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.getSymbol();
        }

        double finalPrice = 0.0;
        switch (unitOfSaleEnum) {
            case ONE_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.ONE_KG.unitWeight()) / 1000;

                finalPrice = mathHelper.roundToNearestSpecifiedValue(priceHigher, 10);
                break;
            case FIFTY_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.FIFTY_KG.unitWeight()) / 1000;

                finalPrice = mathHelper.roundToNearestSpecifiedValue(priceHigher, 10);
                break;
            case HUNDRED_KG:
                priceLower = (unitPriceLower * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000;
                priceHigher = (unitPriceUpper * EnumUnitOfSale.HUNDRED_KG.unitWeight()) / 1000;

                finalPrice = mathHelper.roundToNearestSpecifiedValue(priceHigher, 100);
                break;
            case FRESH_COB:
                finalPrice = priceHigher;
                break;
        }
        return context.getString(R.string.unit_price_label_single, finalPrice, currencySymbol, uos);
    }

}
