package com.akilimo.mobile.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.data.UserDataCleaner
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import com.akilimo.mobile.views.activities.usecases.FertilizerRecActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

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

    override fun validate(backPressed: Boolean) {
        // No back navigation handling needed on splash
    }

    @Deprecated("No longer used. Remove once toolbar code is fully refactored.")
    override fun initToolbar() {
        Sentry.captureMessage("Deprecated toolbar initialization in SplashActivity")
    }

    @Deprecated("Deprecated method. Can be safely removed.")
    override fun initComponent() {
        // Intentionally unimplemented
    }

    private suspend fun launchAppFlow() {
        val isInDevMode = BuildConfig.DEBUG

        try {
            // Initialize API endpoints
            val akilimoEndpoint = sessionManager.getAkilimoEndpoint()
            val fuelrodEndpoint = sessionManager.getFuelrodEndpoint()
            RetrofitManager.init(akilimoEndpoint, fuelrodEndpoint)

            if (!isInDevMode) {
                withContext(Dispatchers.IO) {
                    val cleaner = UserDataCleaner(database, sessionManager)
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
        val intent = if (isInDevMode) {
            Intent(this, FertilizerRecActivity::class.java)
        } else {
            Intent(this, HomeStepperActivity::class.java)
        }

        startActivity(intent)
        finish()
        Animatoo.animateSlideDown(this)
    }

    private fun fallbackLaunch() {
        startActivity(Intent(this, HomeStepperActivity::class.java))
        finish()
    }
}