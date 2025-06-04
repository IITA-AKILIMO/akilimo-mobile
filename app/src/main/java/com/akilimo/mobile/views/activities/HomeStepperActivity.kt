package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyStepperAdapter
import com.akilimo.mobile.data.RemoteConfigResponse
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.FuelrodApi
import com.akilimo.mobile.interfaces.IFragmentCallBack
import com.akilimo.mobile.utils.InAppUpdate
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
import com.stepstone.stepper.StepperLayout.StepperListener
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.system.exitProcess


class HomeStepperActivity : BaseActivity(), IFragmentCallBack {


    private var _binding: ActivityHomeStepperBinding? = null
    private val binding get() = _binding!!

    private lateinit var inAppUpdate: InAppUpdate

    private lateinit var stepperAdapter: MyStepperAdapter

    private lateinit var mStepperLayout: StepperLayout

    private val fragmentArray: MutableList<Fragment> = arrayListOf()

    private val configListDict = HashMap<String, String>()

    private var exit: Boolean = false
    private var stepperReduction = 0

    @Deprecated("Deprecated in Java")
    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is WelcomeFragment -> {
                fragment.setOnFragmentCloseListener(this)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeStepperBinding.inflate(layoutInflater)
        inAppUpdate = InAppUpdate(this@HomeStepperActivity)

        setContentView(binding.root)

        mStepperLayout = binding.stepperLayout

        inAppUpdate.checkForUpdates()
        loadConfig()
        createFragmentArray()

        stepperAdapter =
            MyStepperAdapter(supportFragmentManager, this@HomeStepperActivity, fragmentArray)
        mStepperLayout.adapter = stepperAdapter

        mStepperLayout.setListener(object : StepperListener {
            override fun onCompleted(completeButton: View?) {
                val intent = Intent(this@HomeStepperActivity, RecommendationsActivity::class.java)
                openActivity(intent)
            }

            override fun onError(verificationError: VerificationError) {
                showCustomWarningDialog(
                    getString(R.string.empty_text), verificationError.errorMessage
                )
            }

            override fun onStepSelected(newStepPosition: Int) {
                //not implemented
            }

            override fun onReturn() {
                finish()
            }
        })

        val rationale: String = getString(R.string.lbl_permission_rationale)

        checkAppPermissions(rationale)
    }

    private fun loadConfig() {
        val configReader = FuelrodApi.apiService.readConfig("akilimo")

        configReader.enqueue(object : Callback<List<RemoteConfigResponse>> {
            override fun onResponse(
                call: Call<List<RemoteConfigResponse>>,
                response: Response<List<RemoteConfigResponse>>
            ) {
                val configList = response.body()
                if (!configList.isNullOrEmpty()) {
                    configList.forEach { config ->
                        configListDict[config.configName] = config.configValue
                    }
                }
                if (configListDict.isNotEmpty()) {
                    sessionManager.apply {
                        if (BuildConfig.DEBUG) {
                            configListDict["api_endpoint_dev"]?.let { setAkilimoEndpoint(it) }
                        } else {
                            configListDict["api_endpoint"]?.let { setAkilimoEndpoint(it) }
                        }
                        configListDict["location_iq"]?.let { setLocationIqToken(it) }
                        configListDict["mapbox"]?.let { setMapBoxApiKey(it) }
                        configListDict["privacy"]?.let { setTermsLink(it) }
                        configListDict["api_refresh_key"]?.let { setApiRefreshToken(it) }
                        configListDict["api_token"]?.let { setApiToken(it) }
                    }
                }


            }

            override fun onFailure(call: Call<List<RemoteConfigResponse>>, t: Throwable) {
                Sentry.captureException(t)
                val errorMessage = t.message.toString()
                Toast.makeText(
                    applicationContext,
                    "Unable to load remote configurations, using default config: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun createFragmentArray() {


        fragmentArray.add(WelcomeFragment.newInstance())
        if (!sessionManager.getDisclaimerRead()) {
            fragmentArray.add(InfoFragment.newInstance())
            stepperReduction++
        }
        if (!sessionManager.getTermsAccepted()) {
            fragmentArray.add(PrivacyStatementFragment.newInstance())
            stepperReduction++
        }
        fragmentArray.add(BioDataFragment.newInstance())
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
        if (!sessionManager.getRememberAreaUnit()) {
            fragmentArray.add(AreaUnitFragment.newInstance())
            stepperReduction++
        }
        fragmentArray.add(FieldSizeFragment.newInstance())
        fragmentArray.add(PlantingDateFragment.newInstance())

        fragmentArray.add(TillageOperationFragment.newInstance())
        if (!sessionManager.getRememberInvestmentPref()) {
            fragmentArray.add(InvestmentPrefFragment.newInstance())
            stepperReduction++
        }
        fragmentArray.add(SummaryFragment.newInstance())
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdate.onActivityResult(requestCode, resultCode)
    }

    override fun onResume() {
        super.onResume()
        inAppUpdate.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdate.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        try {
            if (exit) {
                finish() // finish activity
                System.gc() //run the garbage collector to free up memory resources
                exitProcess(0) //exit the system
            } else {
                Toast.makeText(
                    this@HomeStepperActivity, getString(R.string.lbl_exit_tip), Toast.LENGTH_SHORT
                ).show()
                exit = true
                Thread(Runnable {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        exit = false
                    }, 3 * 1000)
                }).start()
            }
        } catch (ex: Exception) {
            Toast.makeText(this@HomeStepperActivity, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun reloadView() {
        // Not implemented
    }
}
