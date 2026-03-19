package com.akilimo.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import androidx.work.workDataOf
import com.akilimo.mobile.helper.SessionManager
import com.akilimo.mobile.network.NetworkMonitor
import com.akilimo.mobile.utils.StartupManager
import com.akilimo.mobile.workers.CassavaPriceWorker
import com.akilimo.mobile.workers.CassavaUnitWorker
import com.akilimo.mobile.workers.FertilizerPriceWorker
import com.akilimo.mobile.workers.FertilizerWorker
import com.akilimo.mobile.workers.InvestmentAmountWorker
import com.akilimo.mobile.workers.StarchFactoryWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.blongho.country_data.World
import dagger.hilt.android.HiltAndroidApp
import com.google.firebase.analytics.FirebaseAnalytics
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository


@HiltAndroidApp
@SuppressLint("LogNotTimber")
class AkilimoApp : MultiDexApplication(), Configuration.Provider {

    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(this) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()

    companion object {
        private lateinit var _instance: AkilimoApp
        private lateinit var analytics: FirebaseAnalytics
        val instance: AkilimoApp
            get() = _instance
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        analytics = FirebaseAnalytics.getInstance(this)
        networkMonitor.startMonitoring()

        analytics.setAnalyticsCollectionEnabled(true)
        analytics.setUserProperty("app_version", BuildConfig.VERSION_NAME)
        analytics.setUserProperty("app_name", BuildConfig.APPLICATION_ID)

        initLocale()
        initDarkMode()
        initVectorSupport()
        initTimeAndCountry()
        runStartupTasks()

        WorkerScheduler.scheduleOneTimeWorker<InvestmentAmountWorker>(
            context = this,
            workName = WorkConstants.INVESTMENT_AMOUNT_WORK_NAME,
            inputData = workDataOf("perPage" to 100)
        )

        WorkerScheduler.scheduleOneTimeWorker<CassavaPriceWorker>(
            context = this,
            workName = WorkConstants.CASSAVA_MARKET_PRICES_WORK_NAME,
            inputData = workDataOf("perPage" to 100)
        )

        WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
            context = this,
            workName = WorkConstants.CASSAVA_UNITS_WORK_NAME,
            inputData = workDataOf("perPage" to 100)
        )

        WorkerScheduler.scheduleOneTimeWorker<StarchFactoryWorker>(
            context = this,
            workName = WorkConstants.STARCH_FACTORY_WORK_NAME,
            inputData = workDataOf("perPage" to 100)
        )


        WorkerScheduler.scheduleChainedWorkers<FertilizerWorker>(
            context = this,
            secondWorker = FertilizerPriceWorker::class.java,
            firstName = WorkConstants.FERTILIZER_WORK_NAME,
            secondName = WorkConstants.FERTILIZER_PRICE_WORK_NAME
        )


//        WorkerScheduler.schedulePeriodicWorker<FertilizerWorker>(
//            context = this,
//            workName = WorkConstants.FERTILIZER_WORK_NAME
//        )

//        WorkerScheduler.scheduleChainedWorkers(
//            context = this,
//            firstWorker = FertilizerSyncWorker::class.java,
//            secondWorker = FertilizerPriceSyncWorker::class.java,
//            firstName = "FertilizerSync",
//            secondName = "FertilizerPriceSync"
//        )
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.stopMonitoring()
    }

    private fun initLocale() {
        AppLocale.supportedLocales = Locales.supportedLocales

        val appLocaleRepo = SharedPrefsAppLocaleRepository(this)
        AppLocale.appLocaleRepository = appLocaleRepo

        // Prefer the locale stored by the AppLocale library; fall back to the value
        // saved by SessionManager so the two sources stay in sync after a language change.
        val locale = appLocaleRepo.desiredLocale
            ?: run {
                val savedTag = SessionManager.get(this).languageCode
                    .takeIf { it.isNotBlank() }
                    ?: Locales.english.toLanguageTag()
                Locales.supportedLocales.find { it.toLanguageTag() == savedTag }
                    ?: Locales.english
            }

        AppLocale.desiredLocale = locale
        Log.d("Akilimo", "Locale initialized to: $locale")
    }


    private fun initDarkMode() {
        val darkMode = SessionManager.get(this).darkMode
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun initVectorSupport() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    private fun initTimeAndCountry() {
//        AndroidThreeTen.init(this)
        World.init(this)
    }

    private fun runStartupTasks() {
        StartupManager.runHousekeeping(this)
    }
}