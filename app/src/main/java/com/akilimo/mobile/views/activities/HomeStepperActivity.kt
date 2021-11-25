package com.akilimo.mobile.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyStepperAdapter
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.IFragmentCallBack
import com.akilimo.mobile.utils.AppUpdateHelper
import com.akilimo.mobile.utils.SessionManager
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.views.activities.usecases.RecommendationsActivity
import com.akilimo.mobile.views.fragments.*
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.StepperLayout.StepperListener
import com.stepstone.stepper.VerificationError
import kotlin.system.exitProcess


class HomeStepperActivity : BaseActivity(), IFragmentCallBack {
    companion object {
        const val MAP_BOX_PLACE_PICKER_REQUEST_CODE = 208
    }

    private lateinit var activity: Activity
    private lateinit var binding: ActivityHomeStepperBinding

    private lateinit var stepperAdapter: MyStepperAdapter

    private lateinit var mStepperLayout: StepperLayout
    private lateinit var appUpdateHelper: AppUpdateHelper
    private lateinit var appUpdater: AppUpdater

    private val fragmentArray: MutableList<Fragment> = arrayListOf()

    private var exit: Boolean = false

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
        setContentView(binding.root)

        activity = this
        context = this
        database = AppDatabase.getDatabase(context)
        sessionManager = SessionManager(this)
        mStepperLayout = binding.stepperLayout

        appUpdateHelper = AppUpdateHelper(this)
        appUpdater = appUpdateHelper
            .showUpdateMessage(Display.DIALOG)
            .setButtonDoNotShowAgain("")

        appUpdater.start()

        createFragmentArray()
        initComponent()
    }

    private fun createFragmentArray() {


        fragmentArray.add(WelcomeFragment.newInstance())
        fragmentArray.add(InfoFragment.newInstance())
        if (!sessionManager.termsAccepted()) {
            fragmentArray.add(PrivacyStatementFragment.newInstance())
        }
        fragmentArray.add(BioDataFragment.newInstance())
        fragmentArray.add(RiskAttFragment.newInstance())
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
        fragmentArray.add(FieldInfoFragment.newInstance())
        fragmentArray.add(AreaUnitFragment.newInstance())
        fragmentArray.add(FieldSizeFragment.newInstance())
        fragmentArray.add(PlantingDateFragment.newInstance())

//        if (!sessionManager.country.equals(EnumCountry.Ghana.countryCode())) {
        fragmentArray.add(TillageOperationFragment.newInstance())
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
                Toast.makeText(
                    context,
                    verificationError.errorMessage,
                    Toast.LENGTH_SHORT
                ).show();
            }

            override fun onStepSelected(newStepPosition: Int) {

                if (newStepPosition == 0) {
                    sessionManager.setForward(true)
                    appUpdater.start()
                } else if (newStepPosition == 10) {
                    if (sessionManager.country.equals(EnumCountry.Ghana.countryCode())) {
                        if (sessionManager.goForward()) {
                            mStepperLayout.currentStepPosition = 11
                            sessionManager.setForward(false)
                        } else {
                            mStepperLayout.currentStepPosition = 9
                            sessionManager.setForward(true)
                        }
                    }
                } else {
                    appUpdater.stop()
                }
            }

            override fun onReturn() {
//                appUpdater.start()
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

    @Suppress("RedundantOverride")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        try {
            if (exit) {
                finish() // finish activity
                System.gc() //run the garbage collector to free up memory resources
                exitProcess(0) //exit the system
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.lbl_exit_tip),
                    Toast.LENGTH_SHORT
                ).show()
                exit = true
                Handler().postDelayed({ exit = false }, (3 * 1000).toLong())
            }
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Crashlytics.log(
                Log.ERROR,
                LOG_TAG,
                ex.message
            )
            Crashlytics.logException(ex)
        }
    }

    override fun reloadView() {

    }
}
