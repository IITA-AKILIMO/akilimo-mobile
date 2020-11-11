package com.iita.akilimo.views.activities

import android.content.Intent
import android.os.Bundle
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
            launchActivity()
        }
    }

    override fun validate(backPressed: Boolean) {
    }

    override fun initToolbar() {
    }

    private fun launchActivity() {

        try {
            if (!BuildConfig.DEBUG) {
                getDatabase(this)?.clearAllTables()
            }
        } catch (ex: Exception) {
            FirebaseCrashlytics.getInstance().log(ex.message!!)
            FirebaseCrashlytics.getInstance().recordException(ex)
        }
        val intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
        startActivity(intent)
        finish()
        Animatoo.animateSlideDown(this)

    }
}
