package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyStepperAdapter
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.IFragmentCallBack
import com.akilimo.mobile.utils.InAppUpdate
import com.akilimo.mobile.viewmodels.HomeStepperViewModel
import com.akilimo.mobile.views.fragments.*
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import kotlin.system.exitProcess

class HomeStepperActivity : BindBaseActivity<ActivityHomeStepperBinding>(), IFragmentCallBack {

    private val viewModel: HomeStepperViewModel by viewModels()

    private lateinit var inAppUpdate: InAppUpdate
    private lateinit var stepperAdapter: MyStepperAdapter
    private lateinit var mStepperLayout: StepperLayout

    private val fragmentArray = mutableListOf<Fragment>()
    private var exit = false


    override fun inflateBinding() = ActivityHomeStepperBinding.inflate(layoutInflater)

    @Deprecated("Deprecated in Java")
    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is WelcomeFragment) {
            fragment.setOnFragmentCloseListener(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inAppUpdate = InAppUpdate(this@HomeStepperActivity)
        mStepperLayout = binding.stepperLayout

        inAppUpdate.checkForUpdates()

        viewModel.configData.observe(this, Observer { configMap ->
            applyRemoteConfigs(configMap)
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            error?.let {
                Sentry.captureMessage(it)
                Toast.makeText(this, "Unable to load remote configuration: $it", Toast.LENGTH_LONG)
                    .show()
            }
        })
        
        viewModel.loadRemoteConfig()
        createFragmentArray()
        stepperAdapter = MyStepperAdapter(supportFragmentManager, this, fragmentArray)
        mStepperLayout.adapter = stepperAdapter
        mStepperLayout.setListener(object : StepperLayout.StepperListener {
            override fun onCompleted(completeButton: View?) {
                val intent = Intent(this@HomeStepperActivity, RecommendationsActivity::class.java)
                openActivity(intent)
            }

            override fun onError(verificationError: VerificationError) {
                showCustomWarningDialog(
                    getString(R.string.empty_text),
                    verificationError.errorMessage
                )
            }

            override fun onStepSelected(newStepPosition: Int) {
                //Not implemented
            }

            override fun onReturn() {
                finish()
            }

        })

        val rationale = getString(R.string.lbl_permission_rationale)
        checkAppPermissions(rationale)
    }


    private fun applyRemoteConfigs(configMap: Map<String, String>) {
        with(sessionManager) {
            if (BuildConfig.DEBUG) {
                configMap["api_endpoint_dev"]?.let { setAkilimoEndpoint(it) }
            } else {
                configMap["api_endpoint"]?.let { setAkilimoEndpoint(it) }
            }

            configMap["location_iq"]?.let { setLocationIqToken(it) }
            configMap["mapbox"]?.let { setMapBoxApiKey(it) }
            configMap["privacy"]?.let { setTermsLink(it) }
            configMap["api_refresh_key"]?.let { setApiRefreshToken(it) }
            configMap["api_token"]?.let { setApiToken(it) }
        }
    }

    private fun createFragmentArray() = with(sessionManager) {
        fragmentArray.apply {
            add(WelcomeFragment.newInstance())

            if (!getDisclaimerRead()) add(InfoFragment.newInstance())
            if (!getTermsAccepted()) add(PrivacyStatementFragment.newInstance())

            add(BioDataFragment.newInstance())
            add(CountryFragment.newInstance())
            add(LocationFragment.newInstance())

            if (!getRememberAreaUnit()) add(AreaUnitFragment.newInstance())

            add(FieldSizeFragment.newInstance())
            add(PlantingDateFragment.newInstance())
            add(TillageOperationFragment.newInstance())

            if (!getRememberInvestmentPref()) add(InvestmentPrefFragment.newInstance())

            add(SummaryFragment.newInstance())
        }
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        try {
            if (exit) {
                finish()
                System.gc()
                exitProcess(0)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.lbl_exit_tip),
                    Toast.LENGTH_SHORT
                ).show()
                exit = true
                Handler(Looper.getMainLooper()).postDelayed({ exit = false }, 3_000)
            }
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    override fun reloadView() {
        // not implmented
    }
}
