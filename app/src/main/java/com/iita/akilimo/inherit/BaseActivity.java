package com.iita.akilimo.inherit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.RequestQueue;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.utils.SessionManager;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.activities.DstRecommendationActivity;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import io.objectbox.BoxStore;

public abstract class BaseActivity extends AppCompatActivity {

    protected String LOG_TAG = BaseActivity.class.getSimpleName();

    protected Context context;
    protected BoxStore boxStore;
    protected SessionManager sessionManager;
    protected RequestQueue queue;
    protected ObjectBoxEntityProcessor objectBoxEntityProcessor;
    protected String countryCode = "ALL";
    protected String baseCurrency = "USD";
    protected String currency = "";
    protected String areaUnit = "";

    protected double fieldSize = 2.471;

//    protected AppUpdateHelper appUpdateHelper;
//    protected AppUpdater appUpdater;

    public BaseActivity() {
    }

    protected abstract void initToolbar();

    protected abstract void initComponent();

    protected abstract void validate(boolean backPressed);

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity(true);
    }

    protected void closeActivity(boolean backPressed) {
        if (!backPressed) {
            finish();
        }
        Animatoo.animateShrink(this);
    }

    /**
     * @param titleText   title of the warning
     * @param contentText stepTitle of the warning
     */
    protected void showCustomWarningDialog(String titleText, String contentText) {
        showCustomWarningDialog(titleText, contentText, null);
    }

    /**
     * @param titleText   title of the warning
     * @param contentText stepTitle of the warning
     * @param buttonTitle warning button title
     */
    protected void showCustomWarningDialog(String titleText, String contentText, String buttonTitle) {
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

        if (!Strings.isEmptyOrWhitespace(buttonTitle)) {
            btnClose.setText(buttonTitle);
        }
        btnClose.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), ((AppCompatButton) v).getText().toString() + " Clicked", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
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

//        if (postData == null) {
//            Toast.makeText(activity, "Unable to prepare recommendations data, please try again", Toast.LENGTH_SHORT).show();
//            return;
//        }
        Intent intent = new Intent(activity, DstRecommendationActivity.class);
//        intent.putExtra(DstRecommendationActivity.REC_TAG, postData);
        activity.startActivity(intent);
    }

}
