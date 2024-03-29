package com.akilimo.mobile.views.fragments.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akilimo.mobile.R;
import com.akilimo.mobile.inherit.BaseDialogFragment;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class SingleSelectDialogFragment extends BaseDialogFragment {


    public static final String RISK_LIST = "risk-list";
    public static final String RISK_INDEX = "risk-index";

    private static final String LOG_TAG = SingleSelectDialogFragment.class.getSimpleName();

    public static final String ARG_ITEM_ID = "single-select-dialog";


    private Dialog dialog;
    private RadioGroup radioGroup;

    private boolean cancelled;


    private int selectedIndex = -1;
    private int rabioButtonValueInt = 0;
    private String selectedLabel;

    private IDismissDialog onDismissListener;
    private String[] risks;

    public SingleSelectDialogFragment(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            risks = bundle.getStringArray(RISK_LIST);
            selectedIndex = bundle.getInt(RISK_INDEX, -1);
        }

        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.dialog_single_item);


        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;


        Button btnClose = dialog.findViewById(R.id.btnCancel);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        radioGroup = dialog.findViewById(R.id.rdgSingleChoice);


        btnClose.setOnClickListener(view -> {
            cancelled = true;
            dismiss();
        });

        //save the data
        btnSave.setOnClickListener(v -> {
            cancelled = false;
            dismiss();

        });

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> radioSelected(radioGroup));
        addCostRadioButtons(risks);
        return dialog;
    }

    private void radioSelected(RadioGroup radioGroup) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = dialog.findViewById(radioButtonId);
        int itemTagIndex = (int) radioButton.getTag();

        selectedLabel = risks[itemTagIndex];
        switch (itemTagIndex) {
            default:
            case 0:
                rabioButtonValueInt = 0;
                break;
            case 1:
                rabioButtonValueInt = 1;
                break;
            case 2:
                rabioButtonValueInt = 2;
                break;
        }
    }

    private void addCostRadioButtons(@NonNull String[] risks) {
        radioGroup.removeAllViews();
        for (int listIndex = 0; listIndex <= risks.length - 1; listIndex++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(listIndex);
            String radioLabel = risks[listIndex];

            selectedLabel = radioLabel;
            radioButton.setText(radioLabel);
            radioGroup.addView(radioButton);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(selectedLabel, rabioButtonValueInt, selectedIndex, cancelled);
        }
    }

    public void setOnDismissListener(IDismissDialog dismissListener) {
        this.onDismissListener = dismissListener;
    }

    /* callback interface for pricing specification*/
    public interface IDismissDialog {
        void onDismiss(String selectedLabel, int rabioButtonValueInt, int selectedIndex, boolean cancelled);
    }
}
