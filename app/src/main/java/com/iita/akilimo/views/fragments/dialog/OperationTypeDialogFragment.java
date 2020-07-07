package com.iita.akilimo.views.fragments.dialog;

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
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseDialogFragment;
import com.iita.akilimo.interfaces.IDismissOperationsDialogListener;
import com.iita.akilimo.utils.enums.EnumOperationType;


public class OperationTypeDialogFragment extends BaseDialogFragment {
    public static final String ARG_ITEM_ID = "OperationTypeDialogFragment";
    public static final String OPERATION_TYPE = "operation_type";
    private static final String LOG_TAG = OperationTypeDialogFragment.class.getSimpleName();


    private Button btnClose;
    private Button btnUpdate;
    private Button btnRemove;
    private RadioGroup radioGroup;
    private TextView lblSelectionError;

    private Dialog dialog;
    private IDismissOperationsDialogListener onDismissListener;
    private EnumOperationType enumOperationType;
    private String operation;
    private boolean cancelled;

    public OperationTypeDialogFragment(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            operation = bundle.getString(OPERATION_TYPE);
        }
        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_operation_type_dialog);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

        btnClose = dialog.findViewById(R.id.close_button);
        btnUpdate = dialog.findViewById(R.id.update_button);
        btnRemove = dialog.findViewById(R.id.remove_button);
        radioGroup = dialog.findViewById(R.id.rdgOperationType);
        lblSelectionError = dialog.findViewById(R.id.lblError);

        TextView lblFragmentTitle = dialog.findViewById(R.id.lblFragmentTitle);
        if (operation.equals("Plough")) {
            lblFragmentTitle.setText(R.string.lbl_plough_op_type);
        } else if (operation.equals("Ridge")) {
            lblFragmentTitle.setText(R.string.lbl_ridge_op_type);
        }

        btnUpdate.setOnClickListener(view -> {
            if (enumOperationType != null) {
                cancelled = false;
                dismiss();
            }
            lblSelectionError.setVisibility(View.VISIBLE);
        });

        btnClose.setOnClickListener(view -> {
            cancelled = true;
            enumOperationType = EnumOperationType.NONE;
            dismiss();
        });

        radioGroup.setOnCheckedChangeListener((radioGroup, radioIndex) -> radioSelected(radioIndex));
        return dialog;
    }

    private void radioSelected(int radioIndex) {
        lblSelectionError.setVisibility(View.GONE);
        switch (radioIndex) {
            case R.id.rdMechanical:
                enumOperationType = EnumOperationType.MECHANICAL;
                break;
            case R.id.rdManual:
                enumOperationType = EnumOperationType.MANUAL;
                break;
            default:
                enumOperationType = EnumOperationType.NONE;
                break;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null && enumOperationType != null) {
            onDismissListener.onDismiss(operation, enumOperationType, cancelled);
        }
    }

    public void setOnDismissListener(IDismissOperationsDialogListener dismissListener) {
        this.onDismissListener = dismissListener;
    }
}
