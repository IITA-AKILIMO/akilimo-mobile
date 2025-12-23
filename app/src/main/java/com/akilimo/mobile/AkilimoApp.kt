package com.akilimo.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.work.workDataOf
import com.akilimo.mobile.network.NetworkMonitor
import com.akilimo.mobile.utils.StartupManager
import com.akilimo.mobile.workers.CassavaPriceWorker
import com.akilimo.mobile.workers.FertilizerPriceWorker
import com.akilimo.mobile.workers.FertilizerWorker
import com.akilimo.mobile.workers.InvestmentAmountWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.blongho.country_data.World
import dev.b3nedikt.app_locale.AppLocale
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository


@SuppressLint("LogNotTimber")
class AkilimoApp : MultiDexApplication() {

    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(this) }

    companion object {
        private lateinit var _instance: AkilimoApp
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
        networkMonitor.startMonitoring()
        initLocale()
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

        val prefs = SharedPrefsAppLocaleRepository(this)
        AppLocale.appLocaleRepository = prefs

        prefs.desiredLocale?.also { desiredLocale ->
            AppLocale.desiredLocale = desiredLocale
            Log.d("Akilimo", "Locale set to: $desiredLocale")
        } ?: Log.w("Akilimo", "No desired locale found; using default.")
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