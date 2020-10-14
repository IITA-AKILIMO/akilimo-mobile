package com.iita.akilimo.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.iita.akilimo.BuildConfig
import com.iita.akilimo.dao.AppDatabase.Companion.getDatabase
import com.iita.akilimo.inherit.BaseActivity


class SplashActivity : BaseActivity() {
    val LOG_TAG: String = SplashActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initComponent()
    }

    override fun initComponent() {
        try {
            val background = object : Thread() {
                override fun run() {
                    launchActivity()
                }
            }
            background.start()
        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
            launchActivity()
        }
    }

    override fun validate(backPressed: Boolean) {
    }

    override fun initToolbar() {
    }

    private fun launchActivity() {

    }
    private fun launchActivityOld() {
        try {
            if (!BuildConfig.DEBUG) {
                getDatabase(this)?.clearAllTables()
            }
        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
        }
        val intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
        startActivity(intent)
        finish()
        Animatoo.animateSplit(this)
    }
}
