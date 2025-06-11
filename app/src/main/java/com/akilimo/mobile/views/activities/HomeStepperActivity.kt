package com.akilimo.mobile.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyStepperAdapter
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.InAppUpdate
import com.akilimo.mobile.viewmodels.HomeStepperViewModel
import com.akilimo.mobile.views.fragments.AreaUnitFragment
import com.akilimo.mobile.views.fragments.BioDataFragment
import com.akilimo.mobile.views.fragments.CountryFragment
import com.akilimo.mobile.views.fragments.FieldSizeFragment
import com.akilimo.mobile.views.fragments.InfoFragment
import com.akilimo.mobile.views.fragments.InvestmentPrefFragment
import com.akilimo.mobile.views.fragments.LocationFragment
import com.akilimo.mobile.views.fragments.PlantingDateFragment
import com.akilimo.mobile.views.fragments.PrivacyStatementFragment
import com.akilimo.mobile.views.fragments.SummaryFragment
import com.akilimo.mobile.views.fragments.TillageOperationFragment
import com.akilimo.mobile.views.fragments.WelcomeFragment
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class HomeStepperActivity : BindBaseActivity<ActivityHomeStepperBinding>() {

    private val viewModel: HomeStepperViewModel by viewModels()
    private lateinit var inAppUpdate: InAppUpdate
    private lateinit var stepperAdapter: MyStepperAdapter
    private lateinit var mStepperLayout: StepperLayout

    private var exit = false
    private val fragmentArray = mutableListOf<androidx.fragment.app.Fragment>()

    companion object {
        private const val KEY_API = "api_endpoint"
        private const val KEY_API_DEV = "api_endpoint_dev"
        private const val KEY_LOCATION_IQ = "location_iq"
        private const val KEY_MAPBOX = "mapbox"
        private const val KEY_PRIVACY = "privacy"
        private const val KEY_REFRESH_KEY = "api_refresh_key"
        private const val KEY_API_TOKEN = "api_token"
    }

    override fun inflateBinding() = ActivityHomeStepperBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inAppUpdate = InAppUpdate(this)
        mStepperLayout = binding.stepperLayout
        inAppUpdate.checkForUpdates()

        observeViewModel()
        viewModel.loadRemoteConfig()

        fragmentArray.addAll(createStepperFragments())
        setupStepper()

        checkAppPermissions(getString(R.string.lbl_permission_rationale))
        handleBackPress()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.configData.observe(this@HomeStepperActivity) {
                    applyRemoteConfigs(it)
                }

                viewModel.errorMessage.observe(this@HomeStepperActivity) {
                    it?.let { err ->
                        Sentry.captureMessage(err)
                        showToast("Unable to load remote configuration: $err")
                    }
                }
            }
        }
    }

    private fun setupStepper() {
        stepperAdapter = MyStepperAdapter(supportFragmentManager, this, fragmentArray)
        mStepperLayout.adapter = stepperAdapter
        mStepperLayout.setListener(object : StepperLayout.StepperListener {
            override fun onCompleted(completeButton: View?) {
                openActivity(Intent(this@HomeStepperActivity, RecommendationsActivity::class.java))
            }

            override fun onError(verificationError: VerificationError) {
                showCustomWarningDialog(
                    getString(R.string.empty_text),
                    verificationError.errorMessage
                )
            }

            override fun onStepSelected(newStepPosition: Int) {
                // optional: implement tracking logic
            }

            override fun onReturn() {
                finish()
            }
        })
    }

    private fun applyRemoteConfigs(configMap: Map<String, String>) {
        with(sessionManager) {
            if (BuildConfig.DEBUG) {
                configMap[KEY_API_DEV]?.let { setAkilimoEndpoint(it) }
            } else {
                configMap[KEY_API]?.let { setAkilimoEndpoint(it) }
            }

            configMap[KEY_LOCATION_IQ]?.let { setLocationIqToken(it) }
            configMap[KEY_MAPBOX]?.let { setMapBoxApiKey(it) }
            configMap[KEY_PRIVACY]?.let { setTermsLink(it) }
            configMap[KEY_REFRESH_KEY]?.let { setApiRefreshToken(it) }
            configMap[KEY_API_TOKEN]?.let { setApiToken(it) }
        }
    }

    private fun createStepperFragments(): List<androidx.fragment.app.Fragment> = buildList {
        add(WelcomeFragment.newInstance())

        if (!sessionManager.getDisclaimerRead()) add(InfoFragment.newInstance())
        if (!sessionManager.getTermsAccepted()) add(PrivacyStatementFragment.newInstance())

        add(BioDataFragment.newInstance())
        add(CountryFragment.newInstance())
        add(LocationFragment.newInstance())
        add(AreaUnitFragment.newInstance())
        add(FieldSizeFragment.newInstance())
        add(PlantingDateFragment.newInstance())
        add(TillageOperationFragment.newInstance())
        add(InvestmentPrefFragment.newInstance())
        add(SummaryFragment.newInstance())
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (exit) {
                    finish()
                    exitProcess(0)
                } else {
                    showToast(getString(R.string.lbl_exit_tip))
                    exit = true
                    Handler(Looper.getMainLooper()).postDelayed({ exit = false }, 3_000)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        inAppUpdate.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdate.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdate.onActivityResult(requestCode, resultCode)
    }

    private fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}
