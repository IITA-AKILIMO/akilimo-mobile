package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.data.UserDataCleaner
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity(
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            runCatching { launchAppFlow() }
                .onFailure { ex ->
                    if (ex !is CancellationException) {
                        Sentry.captureException(ex)
                    }
                    fallbackLaunch()
                }
        }
    }

    private suspend fun launchAppFlow() {
        val isInDevMode = BuildConfig.DEBUG

        val akilimoEndpoint = sessionManager.akilimoEndpoint
        val fuelrodEndpoint = sessionManager.fuelrodEndpoint

        RetrofitManager.init(this, akilimoEndpoint, fuelrodEndpoint)

        if (!isInDevMode) {
            withContext(dispatchers.io) {
                UserDataCleaner(database).clearUserRelatedData()
            }
        }

        navigateToNextActivity(isInDevMode)
    }

    private fun navigateToNextActivity(isInDevMode: Boolean) {
        val nextActivity = if (isInDevMode) {
            RecommendationsActivity::class.java
        } else {
            HomeStepperActivity::class.java
        }

        openActivity(Intent(this, nextActivity))
        finish()
    }

    private fun fallbackLaunch() {
        startActivity(Intent(this, HomeStepperActivity::class.java))
        finish()
    }
}
