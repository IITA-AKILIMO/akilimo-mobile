package com.akilimo.mobile.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.utils.SessionManager
import io.sentry.Sentry


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    val LOG_TAG: String = this::class.java.simpleName

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
            launchActivity()
            //TODO  send this to third party logs tracker
        }
    }

    override fun validate(backPressed: Boolean) {}

    override fun initToolbar() {}

    private fun launchActivity() {
        val isInDevMode = BuildConfig.DEBUG
        try {
            val sessionManager = SessionManager(this@SplashActivity)
            if (!isInDevMode) {
                val db = getDatabase(this)
                if (db != null) {
                    with(db) {

                        if (!sessionManager.rememberUserInfo) {
                            profileInfoDao().deleteAll()
                        }

                        if (!sessionManager.rememberAreaUnit) {
                            mandatoryInfoDao().deleteAll()
                        }

                        adviceStatusDao().deleteAll()
                        cassavaMarketDao().deleteAll()
                        cassavaPriceDao().deleteAll()
                        currencyDao().deleteAll()
                        currentPracticeDao().deleteAll()
                        fertilizerDao().deleteAll()
                        fertilizerPriceDao().deleteAll()
                        fieldOperationCostDao().deleteAll()
                        fieldYieldDao().deleteAll()
                        investmentAmountDao().deleteAll()
                        investmentAmountDtoDao().deleteAll()
                        locationInfoDao().deleteAll()
                        maizeMarketDao().deleteAll()
                        maizePerformanceDao().deleteAll()
                        maizePriceDao().deleteAll()
                        potatoMarketDao().deleteAll()
                        scheduleDateDao().deleteAll()
                        starchFactoryDao().deleteAll()

                    }
                }
            }

        } catch (ex: Exception) {
            //TODO  send this to third party logs tracker
        }
        var intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
        if (isInDevMode) {
            intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
//            intent = Intent(this@SplashActivity, RecommendationsActivity::class.java)
//            intent = Intent(this@SplashActivity, FertilizerRecActivity::class.java)
//            intent = Intent(this@SplashActivity, RootYieldActivity::class.java)
//            intent = Intent(this@SplashActivity, InvestmentAmountActivity::class.java)
        }
        startActivity(intent)
        finish()
        Animatoo.animateSlideDown(this)
    }
}
