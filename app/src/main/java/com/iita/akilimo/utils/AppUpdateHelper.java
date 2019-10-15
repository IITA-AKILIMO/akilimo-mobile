package com.iita.akilimo.utils;

import android.content.Context;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.iita.akilimo.R;

public class AppUpdateHelper {

    private AppUpdater appUpdater;

    public AppUpdateHelper(Context context) {
        appUpdater = new AppUpdater(context)
                .setUpdateFrom(UpdateFrom.GOOGLE_PLAY);
    }

    public AppUpdater showUpdateMessage(Display displayType) {
        return appUpdater.setDisplay(displayType)
                .setIcon(R.drawable.ic_akilimo_head)
//                .showAppUpdated(true)
                .setCancelable(false);
    }

}
