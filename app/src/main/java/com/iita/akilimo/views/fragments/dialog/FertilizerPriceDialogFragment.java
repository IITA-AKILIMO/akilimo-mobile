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
import com.iita.akilimo.interfaces.IDismissListener;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.FertilizerPrices;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import java.util.List;

import butterknife.ButterKnife;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class FertilizerPriceDialogFragment extends DialogFragment {

    private static final String LOG_TAG = FertilizerPriceDialogFragment.class.getSimpleName();

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


    private MathHelper mathHelper;
    private Context context;
    private ObjectBoxEntityProcessor objectBox;
    private Fertilizer fertilizer;
    private List<FertilizerPrices> fertilizerPricesList;

    private double savedPricePerBag = 0.0;
    private String countryCode;
    private String currencyCode;
    private String bagPrice;
    private String bagPriceRange = "NA";
    private String exactPrice = "0";

    private IDismissListener onDismissListener;

    public FertilizerPriceDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        objectBox = ObjectBoxEntityProcessor.getInstance(context);
        mathHelper = new MathHelper();
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
        ButterKnife.bind(dialog);

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;


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
            String titleText = context.getString(R.string.price_per_bag, currencyCode, fertilizer.getName());
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
            fertilizer.setPrice("NA");
            fertilizer.setPricePerBag(0.0);
            fertilizer.setPriceRange(null);
            fertilizer.setSelected(false);
            fertilizer.setExactPrice(false);
            dismiss();
        });
        //save the data
        btnUpdate.setOnClickListener(v -> {
            if (isExactPriceRequired) {
                bagPrice = editExactFertilizerPrice.getText().toString();
                if (Strings.isEmptyOrWhitespace(bagPrice)) {
                    editExactFertilizerPrice.setError("Please provide a valid bag price");
                    isPriceValid = false;
                    return;
                }
                savedPricePerBag = Double.parseDouble(bagPrice);
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
        if (objectBox != null) {
            fertilizerPricesList = objectBox.getFertilizerPrices(countryCode);
            addPriceRadioButtons(fertilizerPricesList, fertilizer);
        }
        return dialog;
    }

    private void radioSelected(RadioGroup radioGroup) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = dialog.findViewById(radioButtonId);
        long itemTagIndex = (long) radioButton.getTag();

        try {
            FertilizerPrices pricesResp = fertilizerPricesList.get((int) itemTagIndex);
            isExactPriceRequired = false;
            isPriceValid = true;
            savedPricePerBag = pricesResp.getPricePerBag();
            bagPrice = String.valueOf(savedPricePerBag);
            bagPriceRange = pricesResp.getPriceRange();


            exactPriceWrapper.setVisibility(View.GONE);
            exactPriceWrapper.getEditText().setText(null);
            if (savedPricePerBag >= 0 && savedPricePerBag <= 0) {
                bagPrice = "NA";
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

    private void addPriceRadioButtons(List<FertilizerPrices> fertilizerPricesList, Fertilizer fertilizer) {
        radioGroup.removeAllViews();
        double selectedPrice = 0.0;
        if (fertilizer != null) {
            selectedPrice = fertilizer.getPricePerBag();
            isExactPriceRequired = fertilizer.isExactPrice();
            if (fertilizer.isSelected()) {
                btnUpdate.setText(R.string.lbl_update);
                btnRemove.setVisibility(View.VISIBLE);
            }
            if (isExactPriceRequired) {
                exactPrice = fertilizer.getPrice();
            }
        }
        for (FertilizerPrices pricesResp : fertilizerPricesList) {

            long listIndex = pricesResp.getPriceId() - 1;//reduce by one so as to match the index in the list

            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);
//            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.spacing_large));

            double price = pricesResp.getPricePerBag();
            String radioLabel = pricesResp.getPriceRange();
            if (price >= 0 && price <= 0) {
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
                editExactFertilizerPrice.setText(exactPrice);
            } else if (pricesResp.getPricePerBag() == selectedPrice) {
                radioButton.setChecked(true);
            }
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this.priceSpecified, this.fertilizer, this.removeSelected);
        }
    }

    public void setOnDismissListener(IDismissListener dismissListener) {
        this.onDismissListener = dismissListener;
    }
}
