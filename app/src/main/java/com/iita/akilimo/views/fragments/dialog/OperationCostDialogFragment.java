package com.iita.akilimo.views.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentOperationCostDialogBinding;
import com.iita.akilimo.entities.OperationCost;
import com.iita.akilimo.entities.TillageOperations;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.MathHelper;

import org.jetbrains.annotations.NotNull;

public class OperationCostDialogFragment extends DialogFragment {
    protected String LOG_TAG = OperationCostDialogFragment.class.getSimpleName();
    public static final String ARG_ITEM_ID = "operation_cost_fragment";
    public static final String SELECTED_OPERATIONS = "selected_operations";
    public static final String CURRENCY_CODE = "currency_code";


    private int request_code = 0;
    private String currencyCode;

    private IFragmentCallBack callbackResult;
    private Dialog dialog;
    private Context context;
    private View root_view;
    private TextView tv_email;
    private EditText et_name, et_location;
    private AppCompatCheckBox cb_allday;
    private AppCompatSpinner spn_timezone;

    AppCompatImageButton bt_close;
    Button bt_save;
    FragmentOperationCostDialogBinding binding;
    TillageOperations event;

    double firstManualPlough;
    double secondManualPlough;
    double firstMechPlough;
    double manualRidging;
    double secondMechPlough;
    double tractorHarrow;
    double tractorRidging;

    public static OperationCostDialogFragment newInstance() {
        return new OperationCostDialogFragment();
    }

    public void setOnCallbackResult(final IFragmentCallBack callbackResult) {
        this.callbackResult = callbackResult;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOperationCostDialogBinding.inflate(inflater, container, false);
        root_view = binding.getRoot();
        return root_view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            currencyCode = bundle.getString(CURRENCY_CODE);
            event = bundle.getParcelable(SELECTED_OPERATIONS);
        }

        if (event != null) {
            //hide fields based on selection
            String tillageOperation = event.getTillageOperation();
            if (event.getTractorAvailable()) {
                if (event.getTractorPlough()) {
                    binding.inputLayoutRidgingTractor.setVisibility(View.VISIBLE);
                    binding.inputLayoutFirstPloughTractor.setVisibility(View.VISIBLE);
                }

//                binding.inputLayoutSecondPloughTractor.setVisibility(View.VISIBLE);
            }

            if (!tillageOperation.equalsIgnoreCase("NA")) {
                binding.inputLayoutFirstManual.setVisibility(View.VISIBLE);
            }

            if (event.getTractorHarrow()) {
                binding.inputLayoutHarrow.setVisibility(View.VISIBLE);
            }

            binding.setOperationCost(event);
        }
        binding.btClose.setOnClickListener(v -> dismiss());
        binding.btSave.setOnClickListener(v -> sendDataResult());
    }

    private OperationCost convertToDefaultCurrency(String currencyCode) {
        MathHelper mathHelper = new MathHelper();

        OperationCost def = OperationCost.newInstance();

        firstManualPlough = mathHelper.convertToLocalCurrency(def.getFirstManualPlough(), currencyCode);
        secondManualPlough = mathHelper.convertToLocalCurrency(def.getSecondManualPlough(), currencyCode);
        firstMechPlough = mathHelper.convertToLocalCurrency(def.getFirstMechPlough(), currencyCode);
        manualRidging = mathHelper.convertToLocalCurrency(def.getManualRidging(), currencyCode);
        secondMechPlough = mathHelper.convertToLocalCurrency(def.getSecondMechPlough(), currencyCode);
        tractorHarrow = mathHelper.convertToLocalCurrency(def.getTractorHarrowCost(), currencyCode);
        tractorRidging = mathHelper.convertToLocalCurrency(def.getTractorRidging(), currencyCode);

        def.setFirstManualPlough(firstManualPlough);
        def.setSecondManualPlough(secondManualPlough);
        def.setFirstMechPlough(firstMechPlough);
        def.setSecondMechPlough(secondMechPlough);
        def.setManualRidging(manualRidging);
        def.setTractorRidging(tractorRidging);
        def.setTractorHarrowCost(tractorHarrow);

        return def;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        return dialog;
    }

    private void sendDataResult() {
        try {
            //get the text values
            String firstManualPloughing = binding.inputLayoutFirstManual.getEditText().getText().toString();
            String firstTractorPloughing = binding.inputLayoutFirstPloughTractor.getEditText().getText().toString();
            String secondManualPloughing = binding.inputLayoutSecondManual.getEditText().getText().toString();
            String secondTractorPloughing = binding.inputLayoutSecondPloughTractor.getEditText().getText().toString();

            String tractorHarrow = binding.inputLayoutHarrow.getEditText().getText().toString();
            String manualRidging = binding.inputLayoutRidgingManual.getEditText().getText().toString();
            String tractorRidging = binding.inputLayoutRidgingTractor.getEditText().getText().toString();

            event.setFirstManualPlough(Double.parseDouble(firstManualPloughing));
            event.setFirstMechPlough(Double.parseDouble(firstTractorPloughing));
            event.setSecondManualPlough(Double.parseDouble(secondManualPloughing));
            event.setSecondMechPlough(Double.parseDouble(secondTractorPloughing));
            event.setTractorHarrowCost(Double.parseDouble(tractorHarrow));
            event.setManualRidging(Double.parseDouble(manualRidging));
            event.setTractorRidging(Double.parseDouble(tractorRidging));

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred fetching info");
            Crashlytics.logException(ex);
        }
        if (callbackResult != null && event != null) {
            callbackResult.sendResult(request_code, event);
            dismiss();
        } else {
            Snackbar.make(root_view, "Unable to save operation costs, please try again", Snackbar.LENGTH_LONG).show();
        }
    }

    public void setRequestCode(int request_code) {
        this.request_code = request_code;
    }
}
