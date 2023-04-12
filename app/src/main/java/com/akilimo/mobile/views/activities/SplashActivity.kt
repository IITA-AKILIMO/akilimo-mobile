package com.akilimo.mobile.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.crashlytics.android.Crashlytics
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.utils.SessionManager
import com.akilimo.mobile.views.activities.usecases.FertilizerRecActivity
import com.akilimo.mobile.views.activities.usecases.RecommendationsActivity


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
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
            launchActivity()
        }
    }

    override fun validate(backPressed: Boolean) {}

    override fun initToolbar() {}

    private fun launchActivity() {
        try {
            val sessionManager = SessionManager(this@SplashActivity)
            if (!BuildConfig.DEBUG) {
                //For developers sanity no data should be cleared in debug mode
            }
            val db = getDatabase(this)
            if (db != null) {
                with(db) {
                    if (sessionManager.userInfoRemembered()) {
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
                        mandatoryInfoDao().deleteAll()
                        potatoMarketDao().deleteAll()
//                        profileInfoDao().deleteAll()
                        scheduleDateDao().deleteAll()
                        starchFactoryDao().deleteAll()
                    } else {
                        clearAllTables()
                    }
                }
            }

        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
        }
        var intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
        if (BuildConfig.DEBUG) {
//            intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
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
