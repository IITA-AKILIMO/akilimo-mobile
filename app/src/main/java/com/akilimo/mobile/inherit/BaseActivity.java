package com.akilimo.mobile.inherit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.akilimo.mobile.utils.enums.EnumAreaUnits;
import com.android.volley.RequestQueue;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.akilimo.mobile.R;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.utils.SessionManager;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.utils.enums.EnumCountry;
import com.akilimo.mobile.utils.enums.EnumUseCase;
import com.akilimo.mobile.views.activities.DstRecommendationActivity;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

import dev.b3nedikt.app_locale.AppLocale;
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

@SuppressLint("LogNotTimber")
public abstract class BaseActivity extends AppCompatActivity {

    protected String LOG_TAG = BaseActivity.class.getSimpleName();

    protected Context context;
    protected SessionManager sessionManager;
    protected AppDatabase database;


    protected RequestQueue queue;

    protected String countryCode = EnumCountry.Nigeria.countryCode();
    protected String currency = EnumCountry.Nigeria.currency();
    protected String currencyName = "";
    protected String currencyCode = EnumCountry.Nigeria.currency();
    protected String currencySymbol = EnumCountry.Nigeria.currency();
    protected String baseCurrency = "USD";
    protected EnumUseCase enumUseCase;
    protected String areaUnit = "acre";
    protected String areaUnitText = "acre";
    protected double fieldSize = 0;
    protected double fieldSizeAcre = 2.471;

    public BaseActivity() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(AppLocale.wrap(newBase)));
    }

    @Override
    public Resources getResources() {
        return AppLocale.wrap(getBaseContext()).getResources();
    }

    protected abstract void initToolbar();

    protected abstract void initComponent();

    protected abstract void validate(boolean backPressed);

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void closeActivity(boolean backPressed) {
        if (!backPressed) {
            finish();
        }
        Animatoo.animateSwipeRight(this);
    }

    protected void openActivity() {
        Animatoo.animateSwipeLeft(this);
    }


    protected void showCustomNotificationDialog() {
        showCustomNotificationDialog(getString(R.string.title_realistic_price), getString(R.string.lbl_realistic_price));
    }

    protected void showCustomNotificationDialog(String titleText, String contentText) {
        showCustomNotificationDialog(titleText, contentText, null);
    }

    /**
     * @param titleText   title of the warning
     * @param contentText stepTitle of the warning
     */
    protected void showCustomWarningDialog(String titleText, String contentText) {
        showCustomWarningDialog(titleText, contentText, null);
    }


    protected void showCustomNotificationDialog(String titleText, String contentText, String buttonTitle) {

        //update the notification count with shared preferences data
        if (sessionManager == null) {
            sessionManager = new SessionManager(this);
        }
        int notificationCount = sessionManager.getNotificationCount();
        if (notificationCount <= 0) {
            return;
        }
        sessionManager.updateNotificationCount(notificationCount);

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_notification);
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

    }

    protected void showCustomWarningDialog(String titleText, String contentText, String buttonTitle) {
        try {
            final Dialog dialog = new Dialog(this);
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

            if (buttonTitle != null && !buttonTitle.isEmpty()) {
                btnClose.setText(buttonTitle);
            }
            btnClose.setOnClickListener(view -> {
                dialog.dismiss();
            });

            dialog.show();
            dialog.getWindow().setAttributes(lp);
        } catch (Exception ex) {
            //@TODO add latest crash logger for google
        }
    }

    protected void checkAppPermissions(String rationale) {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.VIBRATE
        };

        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle("Info")
                .setSettingsDialogTitle("Warning");

        Permissions.check(this/*context*/, permissions, rationale/*rationale*/, options/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
            }
        });
    }

    protected void processRecommendations(@NonNull Activity activity) {
        Intent intent = new Intent(activity, DstRecommendationActivity.class);
        activity.startActivity(intent);
    }

    protected Locale getCurrentLocale() {
        SharedPrefsAppLocaleRepository prefs = new SharedPrefsAppLocaleRepository(this);
        Locale desiredLocale = prefs.getDesiredLocale();
        if (desiredLocale == null) {
            desiredLocale = new Locale("en", "NG");
        }
        AppLocale.setDesiredLocale(desiredLocale);


        return desiredLocale;
    }

    protected AdviceStatus checkStatus(EnumAdviceTasks taskName) {
        if (database != null) {
            AdviceStatus adviceStatus = database.adviceStatusDao().findOne(taskName.name());
            if (adviceStatus != null) {
                return adviceStatus;
            }
        }
        return new AdviceStatus(taskName.name(), false);
    }
}
