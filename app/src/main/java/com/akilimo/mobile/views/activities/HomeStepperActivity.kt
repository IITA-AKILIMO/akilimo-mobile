package com.akilimo.mobile.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyStepperAdapter
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.data.RemoteConfig
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.FuelrodApiInterface
import com.akilimo.mobile.interfaces.IFragmentCallBack
import com.akilimo.mobile.utils.InAppUpdate
import com.akilimo.mobile.utils.SessionManager
import com.akilimo.mobile.views.activities.usecases.RecommendationsActivity
import com.akilimo.mobile.views.fragments.*
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.StepperLayout.StepperListener
import com.stepstone.stepper.VerificationError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.system.exitProcess


class HomeStepperActivity : BaseActivity(), IFragmentCallBack {
    companion object {
        const val MAP_BOX_PLACE_PICKER_REQUEST_CODE = 208
    }

    private lateinit var activity: Activity
    private lateinit var binding: ActivityHomeStepperBinding
    private lateinit var fuelrodApiInterface: FuelrodApiInterface
    private lateinit var inAppUpdate: InAppUpdate

    private lateinit var stepperAdapter: MyStepperAdapter

    private lateinit var mStepperLayout: StepperLayout

    private val fragmentArray: MutableList<Fragment> = arrayListOf()

    private val configListDict = HashMap<String, String>()

    private var exit: Boolean = false
    private var stepperReduction = 0;

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
        binding = ActivityHomeStepperBinding.inflate(layoutInflater)
        fuelrodApiInterface = FuelrodApiInterface.create()
        inAppUpdate = InAppUpdate(this@HomeStepperActivity)

        setContentView(binding.root)

        activity = this
        context = this
        database = AppDatabase.getDatabase(context)
        sessionManager = SessionManager(this)
        mStepperLayout = binding.stepperLayout

        inAppUpdate.checkForUpdates()
        loadConfig()
        createFragmentArray()
        initComponent()
    }

    private fun loadConfig() {
        val configReader = fuelrodApiInterface.readConfig("akilimo")


        configReader.enqueue(object : Callback<List<RemoteConfig>> {
            override fun onResponse(
                call: Call<List<RemoteConfig>>, response: Response<List<RemoteConfig>>
            ) {
                val configList = response.body()
                if (!configList.isNullOrEmpty()) {
                    configList.forEach { config ->
                        configListDict[config.configName] = config.configValue
                    }
                }

                if (configListDict.isNotEmpty()) {
                    if (configListDict.containsKey("api_endpoint")) {
                        sessionManager.apiEndPoint = configListDict["api_endpoint"]
                    }

                    if (configListDict.containsKey("location_iq")) {
                        sessionManager.locationIqToken = configListDict["location_iq"]
                    }

                    if (configListDict.containsKey("mapbox")) {
                        sessionManager.mapBoxApiKey = configListDict["mapbox"]
                    }

                    if (configListDict.containsKey("privacy")) {
                        sessionManager.termsLink = configListDict["privacy"]
                    }

                    if (configListDict.containsKey("api_user")) {
                        sessionManager.apiUser = configListDict["api_user"]
                    }

                    if (configListDict.containsKey("api_pass")) {
                        sessionManager.apiPass = configListDict["api_pass"]
                    }

                    if (configListDict.containsKey("api_refresh_key")) {
                        sessionManager.apiRefreshToken = configListDict["api_refresh_token"]
                    }

                    if (configListDict.containsKey("api_token")) {
                        sessionManager.apiToken = configListDict["api_token"]
                    }
                }
            }

            override fun onFailure(call: Call<List<RemoteConfig>>, t: Throwable) {
                val errorMessage = t.message.toString()
                Toast.makeText(
                    applicationContext,
                    "Unable to load remote configurations, using default config: $errorMessage",
                    Toast.LENGTH_LONG
                ).show();
            }
        })
    }

    private fun createFragmentArray() {


        fragmentArray.add(WelcomeFragment.newInstance())
        if (!sessionManager.disclaimerRead) {
            fragmentArray.add(InfoFragment.newInstance())
            stepperReduction++
        }
        if (!sessionManager.termsAccepted) {
            fragmentArray.add(PrivacyStatementFragment.newInstance())
            stepperReduction++
        }
        fragmentArray.add(BioDataFragment.newInstance())
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
//        if (!sessionManager.rememberAreaUnit) {
//            fragmentArray.add(AreaUnitFragment.newInstance())
//            stepperReduction++
//        }
        fragmentArray.add(FieldSizeFragment.newInstance())
        fragmentArray.add(PlantingDateFragment.newInstance())

        fragmentArray.add(TillageOperationFragment.newInstance())
//        if (!sessionManager.rememberInvestmentPref) {
//            fragmentArray.add(InvestmentPrefFragment.newInstance())
//            stepperReduction++
//        }
        fragmentArray.add(SummaryFragment.newInstance())
    }

    override fun initComponent() {
        stepperAdapter = MyStepperAdapter(supportFragmentManager, context, fragmentArray)
        mStepperLayout.adapter = stepperAdapter

        mStepperLayout.setListener(object : StepperListener {
            override fun onCompleted(completeButton: View?) {
                val intent = Intent(context, RecommendationsActivity::class.java)
                startActivity(intent)
                openActivity()
            }

            override fun onError(verificationError: VerificationError) {
                showCustomWarningDialog(
                    getString(R.string.empty_text), verificationError.errorMessage
                );
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

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdate.onActivityResult(requestCode, resultCode)
    }

    override fun onResume() {
        super.onResume()
        inAppUpdate.onResume();
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdate.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        try {
            if (exit) {
                finish() // finish activity
                System.gc() //run the garbage collector to free up memory resources
                exitProcess(0) //exit the system
            } else {
                Toast.makeText(
                    this, getString(R.string.lbl_exit_tip), Toast.LENGTH_SHORT
                ).show()
                exit = true
                Thread(Runnable {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {
                        exit = false
                    }, 3 * 1000)
                }).start()
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun reloadView() {
        // Not imlemented
    }
}
