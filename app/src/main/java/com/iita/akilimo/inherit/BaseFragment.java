package com.iita.akilimo.inherit;

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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.SessionManager;

import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.Reword;

@SuppressWarnings("WeakerAccess")
public abstract class BaseFragment extends Fragment {

    protected String LOG_TAG = BaseFragment.class.getSimpleName();

    protected int nextTab = 0;
    protected int prevTab = 0;
    protected double baseAcre = 2.471;
    protected double baseSqm = 4046.86;

    protected String currency;
    protected String countryCode;
    protected String countryName;

    protected AppDatabase database;


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
        mathHelper = new MathHelper(baseAcre, baseSqm);
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

    protected Locale getCurrentLocale() {
        SharedPrefsAppLocaleRepository prefs = new SharedPrefsAppLocaleRepository(context);
        Locale desiredLocale = prefs.getDesiredLocale();
        if (desiredLocale != null) {
            AppLocale.setDesiredLocale(desiredLocale);
        }

        return desiredLocale;
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

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


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

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }
}
