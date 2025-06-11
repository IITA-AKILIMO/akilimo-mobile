package com.akilimo.mobile.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MySpinnerAdapter
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.LanguageOption
import com.akilimo.mobile.utils.Locales
import com.akilimo.mobile.views.activities.SplashActivity
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import com.stepstone.stepper.VerificationError

class WelcomeFragment : BindBaseStepFragment<FragmentWelcomeBinding>() {

    private var languagePicked = false

    companion object {
        fun newInstance(): WelcomeFragment = WelcomeFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentWelcomeBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        val languageOptions = Locales.LOCALE_COUNTRIES.map {
            LanguageOption(it.language, it.getDisplayLanguage(it))
        }
        val savedLanguageCode = LanguageManager.getLanguage(requireContext())
        val selectedIndex = languageOptions.indexOfFirst { it.code == savedLanguageCode }

        val adapter = MySpinnerAdapter(requireContext(), languageOptions.map { it.displayName })
        binding.apply {
            welcomeLanguageSpinner.adapter = adapter

            welcomeLanguageSpinner.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

            welcomeLanguageSpinner.setOnTouchListener { v, event ->
                languagePicked = true
                v.performClick()
                v.onTouchEvent(event)
            }


            welcomeLanguageSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedLanguage = languageOptions[position]
                        LanguageManager.saveLanguage(requireContext(), selectedLanguage.code)
                        LanguageManager.setLocale(requireContext(), selectedLanguage.code)
                        if (languagePicked) {
                            showRestartSnackBar()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
        }

    }

    private fun showRestartSnackBar() {
        Snackbar.make(
            binding.welcomeLayout, getString(R.string.lbl_restart_app_prompt), Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.lbl_ok)) {
            val restartIntent = Intent(requireContext(), SplashActivity::class.java)
            ProcessPhoenix.triggerRebirth(requireContext(), restartIntent)
        }.show()
    }

    override fun verifyStep(): VerificationError? {
        return verificationError
    }
}