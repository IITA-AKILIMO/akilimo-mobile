package com.akilimo.mobile.inherit;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.akilimo.mobile.utils.SessionManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.entities.LocationInfo;
import com.akilimo.mobile.utils.MathHelper;
import com.stepstone.stepper.VerificationError;

import dev.b3nedikt.reword.Reword;
import io.sentry.Sentry;

@SuppressWarnings("WeakerAccess")
public abstract class BaseFragment extends Fragment {

    protected String LOG_TAG = BaseFragment.class.getSimpleName();

    protected int nextTab = 0;
    protected int prevTab = 0;

    protected String currency;
    protected String currencySymbol;
    protected String countryCode;
    protected String countryName;

    protected AppDatabase database;
    protected VerificationError verificationError = null;


    String emptyText = "";

    private String appVersion;
    protected Context context;
    protected RequestQueue queue;

    protected SessionManager sessionManager;
    protected MathHelper mathHelper;

//

    public BaseFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        sessionManager = new SessionManager(context);
        queue = Volley.newRequestQueue(context.getApplicationContext());
        appVersion = sessionManager.getAppVersion();
        database = AppDatabase.getDatabase(context);
        mathHelper = new MathHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = loadFragmentLayout(inflater, container, savedInstanceState);
        Reword.reword(view);
        return view;
    }

    protected abstract View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public abstract void refreshData();

    protected StringBuilder loadLocationInfo(LocationInfo locationInfo) {
        StringBuilder stBuilder = new StringBuilder();
        if (locationInfo != null) {
            String latitude = String.valueOf(locationInfo.getLatitude());
            String longitude = String.valueOf(locationInfo.getLongitude());
            stBuilder.append("Lat:");
            stBuilder.append(latitude);
            stBuilder.append(" ");
            stBuilder.append("Lon:");
            stBuilder.append(longitude);
        }

        return stBuilder;
    }


    protected void showCustomWarningDialog(String titleText, String contentText) {
        showCustomWarningDialog(titleText, contentText, null);
    }

    protected void showCustomWarningDialog(String titleText, String contentText, String buttonTitle) {
        try {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
            dialog.setContentView(R.layout.dialog_warning);
            dialog.setCancelable(true);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;


            final TextView title = dialog.findViewById(R.id.title);
            final TextView content = dialog.findViewById(R.id.content);
            final AppCompatButton btnClose = dialog.findViewById(R.id.bt_close);
            title.setText(titleText);
            content.setText(contentText);

            if (!Strings.isEmptyOrWhitespace(buttonTitle)) {
                btnClose.setText(buttonTitle);
            }
            btnClose.setOnClickListener(view -> {
                dialog.dismiss();
            });
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        } catch (Exception ex) {
            Sentry.captureException(ex);
        }
    }
}
