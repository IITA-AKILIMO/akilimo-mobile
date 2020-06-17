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
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.utils.RealmProcessor;
import com.iita.akilimo.utils.SessionManager;

import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import dev.b3nedikt.reword.Reword;
import io.realm.Realm;

@SuppressWarnings("WeakerAccess")
public abstract class BaseFragment extends Fragment {

    protected String LOG_TAG = BaseFragment.class.getSimpleName();

    protected int nextTab = 0;
    protected int prevTab = 0;
    protected double baseAcre = 2.471;

    protected String currency;
    protected String countryCode;
    protected String countryName;


    String emptyText = "";

    private String appVersion;
    protected Context context;
    protected RequestQueue queue;

    protected SessionManager sessionManager;
    protected RealmProcessor realmProcessor;
    protected Realm myRealm;

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
        sessionManager = new SessionManager(getContext());
        queue = Volley.newRequestQueue(context.getApplicationContext());
        appVersion = sessionManager.getAppVersion();
        myRealm = Realm.getDefaultInstance();
        realmProcessor = new RealmProcessor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = loadFragmentLayout(inflater, container, savedInstanceState);
        Reword.reword(view);
        return view;
    }

    protected abstract View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myRealm != null) {
            myRealm.close();
        }
    }

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
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred while displaying alert dialog");
            Crashlytics.logException(ex);
        }
    }
}
