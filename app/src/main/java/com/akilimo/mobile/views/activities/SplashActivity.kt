package com.akilimo.mobile.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.data.UserDataCleaner
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity(
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                launchAppFlow()
            } catch (ex: Exception) {
                Sentry.captureException(ex)
                fallbackLaunch()
            }
        }
    }

    private suspend fun launchAppFlow() {
        val isInDevMode = BuildConfig.DEBUG

        try {
            val akilimoEndpoint = sessionManager.akilimoEndpoint
            val fuelrodEndpoint = sessionManager.fuelrodEndpoint
            RetrofitManager.init(this, akilimoEndpoint, fuelrodEndpoint)
            if (!isInDevMode) {
                withContext(dispatchers.io) {
                    val cleaner = UserDataCleaner(database)
                    cleaner.clearUserRelatedData()
                }
            }

            launchNextActivity(isInDevMode)

        } catch (ex: Exception) {
            Sentry.captureException(ex)
            fallbackLaunch()
        }
    }

    private fun launchNextActivity(isInDevMode: Boolean) {
        var intent = Intent(this, HomeStepperActivity::class.java)

        if (isInDevMode) {
            intent = Intent(this, RecommendationsActivity::class.java)
        }
        startActivity(intent)
        finish()
        Animatoo.animateFade(this@SplashActivity)
    }

    private fun fallbackLaunch() {
        startActivity(Intent(this, HomeStepperActivity::class.java))
        finish()
    }
}