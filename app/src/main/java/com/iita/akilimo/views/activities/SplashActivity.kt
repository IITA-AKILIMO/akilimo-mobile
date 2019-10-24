package com.iita.akilimo.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.utils.FireBaseConfig

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initComponent()
    }

    override fun initComponent() {
        try {
            val fireBaseConfig = FireBaseConfig(this)
            fireBaseConfig.fetchNewRemoteConfig()

            val background = object : Thread() {
                override fun run() {
                    launchActivity()
                }
            }
            background.start()
        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Error running background thread for splash screen")
            Crashlytics.logException(ex)
            launchActivity()
        }
    }

    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun closeActivity(backPressed: Boolean) {
        finish()
    }

    private fun launchActivity() {
        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
        startActivity(intent)
        closeActivity(false)
    }
}
