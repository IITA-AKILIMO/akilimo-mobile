package com.akilimo.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.StepperAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
import com.akilimo.mobile.ui.components.SimpleStepperListener
import com.akilimo.mobile.ui.fragments.AreaUnitFragment
import com.akilimo.mobile.ui.fragments.BioDataFragment
import com.akilimo.mobile.ui.fragments.CountryFragment
import com.akilimo.mobile.ui.fragments.DisclaimerFragment
import com.akilimo.mobile.ui.fragments.InvestmentPrefFragment
import com.akilimo.mobile.ui.fragments.LocationFragment
import com.akilimo.mobile.ui.fragments.PlantingDateFragment
import com.akilimo.mobile.ui.fragments.SummaryFragment
import com.akilimo.mobile.ui.fragments.TermsFragment
import com.akilimo.mobile.ui.fragments.TillageOperationFragment
import com.akilimo.mobile.ui.fragments.WelcomeFragment
import com.stepstone.stepper.Step
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import kotlin.system.exitProcess

class HomeStepperActivity : BaseActivity<ActivityHomeStepperBinding>(),
    StepperLayout.StepperListener by SimpleStepperListener() {

    override fun inflateBinding() = ActivityHomeStepperBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        val steps = buildStepList()
        val stepperAdapter = StepperAdapter(supportFragmentManager, this, steps)
        binding.stepperLayout.adapter = stepperAdapter
        binding.stepperLayout.setListener(this@HomeStepperActivity)

        networkNotificationView = binding.networkNotificationView
    }

    override fun handleBackPressed(): Boolean {
        AlertDialog.Builder(this)
            .setTitle("Exit Application")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton(getString(R.string.lbl_yes)) { _, _ ->
                finish()
                System.gc()
                exitProcess(0)
            }
            .setNegativeButton(R.string.lbl_no, null)
            .show()
        return true
    }

    private fun buildStepList(): List<Step> = buildList {
        add(WelcomeFragment.newInstance())
        if (!sessionManager.disclaimerRead) add(DisclaimerFragment.newInstance())
        if (!sessionManager.termsAccepted) add(TermsFragment.newInstance())
        add(BioDataFragment.newInstance())
        add(CountryFragment.newInstance())
        add(LocationFragment.newInstance())
        if (!sessionManager.rememberAreaUnit) add(AreaUnitFragment.newInstance())
        add(PlantingDateFragment.newInstance())
        add(TillageOperationFragment.newInstance())
        add(InvestmentPrefFragment.newInstance())
        add(SummaryFragment.newInstance())
    }

    override fun onCompleted(completeButton: View?) {
        openActivity(Intent(this@HomeStepperActivity, RecommendationsActivity::class.java))
    }

    override fun onError(verificationError: VerificationError?) {
        Toast.makeText(this, verificationError?.errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onReturn() {
        finish()
    }
}