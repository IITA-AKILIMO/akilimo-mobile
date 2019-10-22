package com.iita.akilimo.views.activities

import android.content.Intent
import android.os.Bundle
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.utils.FireBaseConfig

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initComponent()
    }

    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    override fun initComponent() {
        val fireBaseConfig = FireBaseConfig(this)
        fireBaseConfig.fetchNewRemoteConfig()

        val background = object : Thread() {
            override fun run() {
                try {
                    val intent = Intent(this@SplashActivity, FertilizersActivity::class.java)
                    startActivity(intent)
                    closeActivity(false)
                } catch (ex: Exception) {
                }

            }
        }
        background.start()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun closeActivity(backPressed: Boolean) {
        finish()
    }
}
