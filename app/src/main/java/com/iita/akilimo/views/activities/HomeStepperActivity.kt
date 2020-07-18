package com.iita.akilimo.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import com.iita.akilimo.R
import com.iita.akilimo.adapters.MyStepperAdapter
import com.iita.akilimo.dao.AppDatabase
import com.iita.akilimo.databinding.ActivityHomeStepperBinding
import com.iita.akilimo.entities.LocationInfo
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.views.fragments.*
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.StepperLayout.StepperListener
import com.stepstone.stepper.VerificationError
import kotlin.system.exitProcess


class HomeStepperActivity : BaseActivity() {
    companion object {
        const val MAP_BOX_PLACE_PICKER_REQUEST_CODE = 208
    }

    private lateinit var activity: Activity
    private lateinit var binding: ActivityHomeStepperBinding

    private lateinit var stepperAdapter: MyStepperAdapter

    private lateinit var mStepperLayout: StepperLayout

    private val fragmentArray: MutableList<Fragment> = arrayListOf()

    private var currentLat: Double = 0.0
    private var currentLong: Double = 0.0
    private var currentAlt: Double = 0.0
    private var placeName: String? = null
    private var address: String? = null
    private var defaultPlaceName: String = ""
    private var location: LocationInfo? = null
    private var exit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeStepperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activity = this
        context = this
        database = AppDatabase.getDatabase(context)
        mStepperLayout = binding.stepperLayout

        createFragmentArray()
        initComponent()
    }

    private fun createFragmentArray() {

        fragmentArray.add(WelcomeFragment.newInstance())
        fragmentArray.add(InfoFragment.newInstance())
        /**
         * @TODO Add privacy statement links to the app
         * @body Check for updated content from christine.
         * @body The privacy statement should also be translated to the relevant languages
         */
//        fragmentArray.add(PrivacyStatementFragment.newInstance())
        fragmentArray.add(BioDataFragment.newInstance())
        fragmentArray.add(CountryFragment.newInstance())
        fragmentArray.add(LocationFragment.newInstance())
        fragmentArray.add(FieldInfoFragment.newInstance())
        fragmentArray.add(AreaUnitFragment.newInstance())
        fragmentArray.add(FieldSizeFragment.newInstance())
//        fragmentArray.add(CurrentPracticeFragment.newInstance())
//        fragmentArray.add(SummaryFragment.newInstance())
    }

    override fun initComponent() {
        stepperAdapter = MyStepperAdapter(supportFragmentManager, context, fragmentArray)
        mStepperLayout.adapter = stepperAdapter

        mStepperLayout.setListener(object : StepperListener {
            override fun onCompleted(completeButton: View?) {
            }

            override fun onError(verificationError: VerificationError) {
                Toast.makeText(
                    context,
                    "onError! -> " + verificationError.errorMessage,
                    Toast.LENGTH_SHORT
                ).show();
            }

            override fun onStepSelected(newStepPosition: Int) {
            }

            override fun onReturn() {
                finish()
            }
        })

        val rationale: String = getString(R.string.lbl_permission_rationale)

        checkAppPermissions(rationale)
        fetchFireBaseConfig(activity)
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
}