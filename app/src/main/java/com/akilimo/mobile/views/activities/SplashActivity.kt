package com.akilimo.mobile.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import com.akilimo.mobile.utils.SessionManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
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
            Sentry.captureException(ex)
        }
    }

    override fun validate(backPressed: Boolean) {}

    override fun initToolbar() {
        Sentry.captureMessage("Empty toolbar initialization")
    }

    private fun launchActivity() {
        val isInDevMode = BuildConfig.DEBUG
        try {
            val sessionManager = SessionManager(this@SplashActivity)
            val akilimoEndpoint = sessionManager.akilimoEndpoint
            val fuelrodEndpoint = sessionManager.fuelrodEndpoint

            RetrofitManager.init(akilimoEndpoint, fuelrodEndpoint)

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
            Sentry.captureException(ex)
        }
        var intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
        if (isInDevMode) {
//            intent = Intent(this@SplashActivity, HomeStepperActivity::class.java)
//            intent = Intent(this@SplashActivity, ApiTestActivity::class.java)
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
