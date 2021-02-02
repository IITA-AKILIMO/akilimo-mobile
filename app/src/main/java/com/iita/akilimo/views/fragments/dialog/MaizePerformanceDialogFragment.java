package com.iita.akilimo.views.fragments.dialog;


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

import com.iita.akilimo.R;
import com.iita.akilimo.entities.MaizePerformance;
import com.iita.akilimo.inherit.BaseDialogFragment;
import com.iita.akilimo.interfaces.IMaizePerformanceDismissListener;
import com.iita.akilimo.utils.Tools;

import java.util.Locale;

;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class MaizePerformanceDialogFragment extends BaseDialogFragment {


    private static final String LOG_TAG = MaizePerformanceDialogFragment.class.getSimpleName();

    public static final String PERFORMANCE_DATA = "performance_data";
    public static final String ARG_ITEM_ID = "maize_performance_dialog_fragment";


    private boolean performanceConfirmed = false;

    private Dialog dialog;
    private Button btnClose;
    private Button btnConfirm;
    private TextView lblFragmentTitle;
    private TextView perfDescription;
    private ImageView performanceImage;
    private Button btnRemove;


    private MaizePerformance maizePerformance;

    private IMaizePerformanceDismissListener onDismissListener;

    public MaizePerformanceDialogFragment(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            maizePerformance = bundle.getParcelable(PERFORMANCE_DATA);
        }
        dialog = new Dialog(context);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_maize_perf_dialog);

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;


        btnClose = dialog.findViewById(R.id.close_button);
        btnConfirm = dialog.findViewById(R.id.confirm_button);
        btnRemove = dialog.findViewById(R.id.cancel_button);

        lblFragmentTitle = dialog.findViewById(R.id.lblFragmentTitle);
        perfDescription = dialog.findViewById(R.id.perfDescription);
        performanceImage = dialog.findViewById(R.id.rootYieldImage);

        if (maizePerformance != null) {
            String yieldAmountLabel = maizePerformance.getMaizePerformanceLabel();
            String yieldDesc = maizePerformance.getMaizePerformanceDesc();

            lblFragmentTitle.setText(yieldAmountLabel);
            perfDescription.setText(yieldDesc);

            Tools.displayImageOriginal(this.context, performanceImage, maizePerformance.getImageId());
        }


        btnClose.setOnClickListener(view -> {
            performanceConfirmed = false;
            dismiss();
        });

        btnRemove.setOnClickListener(view -> {
            performanceConfirmed = false;
            dismiss();
        });
        //save the data
        btnConfirm.setOnClickListener(v -> {
            performanceConfirmed = true;
            dismiss();
        });

        return dialog;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this.maizePerformance, performanceConfirmed);
        }
    }

    public void setOnDismissListener(IMaizePerformanceDismissListener dismissListener) {
        this.onDismissListener = dismissListener;
    }
}
