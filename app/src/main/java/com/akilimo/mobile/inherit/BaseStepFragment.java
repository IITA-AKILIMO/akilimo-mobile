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

import com.akilimo.mobile.utils.MathHelper;
import com.akilimo.mobile.utils.SessionManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.entities.LocationInfo;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.Reword;
import io.sentry.Sentry;

@SuppressWarnings("WeakerAccess")
public abstract class BaseStepFragment extends Fragment implements Step {

    protected String LOG_TAG = BaseStepFragment.class.getSimpleName();

    protected String currency;
    protected String countryCode = "";
    protected String countryName;
    protected String errorMessage = "";

    protected AppDatabase database;
    protected VerificationError verificationError = null;

    protected Context context;
    protected RequestQueue queue;

    protected SessionManager sessionManager;
    protected MathHelper mathHelper;

    @Deprecated()
    protected boolean dataIsValid;

    protected BaseStepFragment() {
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

    protected void showCustomWarningDialog(String titleText) {
        showCustomWarningDialog(titleText, titleText, null);
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
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;


            final TextView title = dialog.findViewById(R.id.title);
            final TextView content = dialog.findViewById(R.id.content);
            final AppCompatButton btnClose = dialog.findViewById(R.id.bt_close);
            title.setText(titleText);
            content.setText(contentText);

            if (buttonTitle != null && btnClose != null && !buttonTitle.isEmpty() && buttonTitle.length() > 1) {
                btnClose.setText(buttonTitle);
            }
            btnClose.setOnClickListener(view -> dialog.dismiss());
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.getWindow().setAttributes(layoutParams);
        } catch (Exception ex) {
            Sentry.captureException(ex);
        }
    }

    protected StringBuilder loadLocationInfo(LocationInfo locationInfo) {
        StringBuilder stBuilder = new StringBuilder();
        if (locationInfo != null) {
            String lat = mathHelper.removeLeadingZero(locationInfo.getLatitude(), "#.####");
            String lon = mathHelper.removeLeadingZero(locationInfo.getLongitude(), "#.####");

            String place = locationInfo.getLocationCountryName();
            stBuilder.append(String.format("%s %n%s,%s", place, lat, lon));
        }

        return stBuilder;
    }


    protected Locale getCurrentLocale() {
        SharedPrefsAppLocaleRepository prefs = new SharedPrefsAppLocaleRepository(context);
        Locale desiredLocale = prefs.getDesiredLocale();
        if (desiredLocale != null) {
            AppLocale.setDesiredLocale(desiredLocale);
        }
        return desiredLocale;
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        //not implemented
    }

}
