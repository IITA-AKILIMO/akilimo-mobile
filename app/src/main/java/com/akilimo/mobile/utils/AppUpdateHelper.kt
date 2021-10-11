package com.akilimo.mobile.utils

import android.content.Context
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.akilimo.mobile.R

class AppUpdateHelper(context: Context?) {
    private val appUpdater: AppUpdater = AppUpdater(context)
        .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)

    fun showUpdateMessage(displayType: Display?): AppUpdater {
        return appUpdater.setDisplay(displayType)
            .setIcon(R.drawable.ic_akilimo_head) //                .showAppUpdated(true)
            .setCancelable(false)
    }

}
