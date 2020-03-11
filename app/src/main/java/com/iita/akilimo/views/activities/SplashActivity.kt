package com.iita.akilimo.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics

class SplashActivity : AppCompatActivity() {

    companion object {
        val LOG_TAG = SplashActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initComponent()
    }

    fun initComponent() {
        try {
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

    private fun launchActivity() {
        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
//        val intent = Intent(this@SplashActivity, MaizeMarketActivity::class.java)
        startActivity(intent)
        closeActivity()
    }

    fun closeActivity() {
        finish()
    }
}
