package com.akilimo.mobile.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import androidx.navigation.fragment.NavHostFragment
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.WizardAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityHomeStepperBinding
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
import com.akilimo.mobile.wizard.WizardStep
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class HomeStepperActivity : BaseActivity<ActivityHomeStepperBinding>() {

    private lateinit var wizardAdapter: WizardAdapter

    private val currentPosition get() = binding.viewPager.currentItem
    private val lastPosition get() = wizardAdapter.itemCount - 1

    override fun inflateBinding() = ActivityHomeStepperBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        networkNotificationView = binding.networkNotificationView

        val steps = buildStepList()
        wizardAdapter = WizardAdapter(this, steps)
        binding.viewPager.apply {
            adapter = wizardAdapter
            isUserInputEnabled = false
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateNavBar(position)
                    wizardAdapter.getStep(position).onSelected()
                }
            })
        }

        binding.btnNext.setOnClickListener { onNextClicked() }
        binding.btnBack.setOnClickListener { onBackClicked() }
        binding.fabSettings.setOnClickListener {
            startActivity(android.content.Intent(this, UserSettingsActivity::class.java))
        }

        updateNavBar(0)
        // Defer onSelected() until ViewPager2 has attached the fragment —
        // calling it synchronously here crashes with "detached fragment".
        binding.viewPager.post {
            wizardAdapter.getStep(0).onSelected()
        }
    }

    override fun handleBackPressed(): Boolean {
        if (currentPosition > 0) {
            binding.viewPager.currentItem = currentPosition - 1
            return true
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.lbl_exit_application))
            .setMessage(getString(R.string.lbl_confirm_app_exit))
            .setPositiveButton(getString(R.string.lbl_yes)) { _, _ ->
                finish()
                System.gc()
                exitProcess(0)
            }
            .setNegativeButton(R.string.lbl_no, null)
            .show()
        return true
    }

    private fun onNextClicked() {
        val error = currentStep().verifyStep()
        if (error != null) {
            currentStep().onError(error)
            return
        }
        if (currentPosition == lastPosition) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_home) as NavHostFragment
            navHostFragment.navController.navigate(R.id.recommendationsFragment)
        } else {
            binding.viewPager.currentItem = currentPosition + 1
        }
    }

    private fun onBackClicked() {
        if (currentPosition > 0) {
            binding.viewPager.currentItem = currentPosition - 1
        } else {
            finish()
        }
    }

    private fun updateNavBar(position: Int) {
        binding.btnBack.isVisible = position > 0
        binding.tvStepCounter.text = "${position + 1} / ${wizardAdapter.itemCount}"
        binding.btnNext.text = if (position == lastPosition) {
            getString(R.string.lbl_finish)
        } else {
            getString(R.string.lbl_next)
        }
    }

    private fun currentStep(): WizardStep = wizardAdapter.getStep(currentPosition)

    private fun buildStepList(): List<WizardStep> = buildList {
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
}
