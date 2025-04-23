package com.akilimo.mobile.views.fragments.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akilimo.mobile.R;
import com.akilimo.mobile.entities.FieldYield;
import com.akilimo.mobile.inherit.BaseDialogFragment;
import com.akilimo.mobile.interfaces.IFieldYieldDismissListener;
import com.akilimo.mobile.utils.Tools;

;import java.util.Locale;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class RootYieldDialogFragment extends BaseDialogFragment {


    private static final String LOG_TAG = RootYieldDialogFragment.class.getSimpleName();

    public static final String YIELD_DATA = "yield_data";
    public static final String ARG_ITEM_ID = "root_yield_dialog_fragment";


    private boolean yieldConfirmed = false;

    private Dialog dialog;
    private Button btnClose;
    private Button btnConfirm;
    private TextView lblFragmentTitle;
    private ImageView rootYieldImage;
    private Button btnRemove;


    private FieldYield fieldYield;

    private IFieldYieldDismissListener onDismissListener;

    public RootYieldDialogFragment(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fieldYield = bundle.getParcelable(YIELD_DATA);
        }
        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_root_yield_dialog);

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;


        btnClose = dialog.findViewById(R.id.close_button);
        btnConfirm = dialog.findViewById(R.id.confirm_button);
        btnRemove = dialog.findViewById(R.id.cancel_button);

        lblFragmentTitle = dialog.findViewById(R.id.lblFragmentTitle);
        rootYieldImage = dialog.findViewById(R.id.rootYieldImage);

        if (fieldYield != null) {
            String yieldLabel = fieldYield.getFieldYieldLabel();
            String yieldAmountLabel = fieldYield.getFieldYieldAmountLabel();
            String yieldDesc = fieldYield.getFieldYieldDesc();
            String selectedTitle = getString(R.string.lbl_you_expect_yield, yieldAmountLabel.toLowerCase(Locale.ENGLISH), yieldDesc);

            Tools.displayImageOriginal(this.context, rootYieldImage, fieldYield.getImageId());
            lblFragmentTitle.setText(selectedTitle);
        }


        btnClose.setOnClickListener(view -> {
            yieldConfirmed = false;
            dismiss();
        });

        btnRemove.setOnClickListener(view -> {
            yieldConfirmed = false;
            dismiss();
        });
        //save the data
        btnConfirm.setOnClickListener(v -> {
            yieldConfirmed = true;
            dismiss();
        });

        return dialog;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this.fieldYield, yieldConfirmed);
        }
    }

    public void setOnDismissListener(IFieldYieldDismissListener dismissListener) {
        this.onDismissListener = dismissListener;
    }
}
