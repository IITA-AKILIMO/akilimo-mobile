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
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.interfaces.IFragmentCallBack
import com.akilimo.mobile.utils.Locales
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.views.activities.HomeStepperActivity
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import com.stepstone.stepper.VerificationError
import dev.b3nedikt.app_locale.AppLocale.appLocaleRepository
import dev.b3nedikt.app_locale.AppLocale.desiredLocale
import dev.b3nedikt.app_locale.AppLocale.supportedLocales
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import dev.b3nedikt.reword.Reword.reword

class WelcomeFragment : BaseStepFragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private var fragmentCallBack: IFragmentCallBack? = null
    private var languagePicked = false
    private var selectedLanguageIndex = -1

    private val prefs: SharedPrefsAppLocaleRepository by lazy {
        SharedPrefsAppLocaleRepository(requireContext())
    }

    companion object {
        fun newInstance(): WelcomeFragment = WelcomeFragment()
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLanguagePicker()
        populateLanguages()
    }

    private fun setupLanguagePicker() = binding.languagePicker.apply {
        setOnTouchListener { v, event ->
            languagePicked = true
            v.performClick()
            v.onTouchEvent(event)
        }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (languagePicked) {
                    applyLanguageChange(position)
                    showRestartSnackBar()
                    populateLanguages()
                }
                languagePicked = false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun populateLanguages() {
        val localeDisplayNames = Locales.LOCALE_COUNTRIES.map {
            if (it.country.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
                getString(R.string.lbl_kiswahili)
            } else {
                it.displayLanguage
            }
        }

        prefs.desiredLocale?.let { locale ->
            selectedLanguageIndex = supportedLocales.indexOfFirst {
                it.language == locale.language
            }.takeIf { it >= 0 } ?: 0
        }

        val adapter = MySpinnerAdapter(requireContext(), localeDisplayNames)
        binding.languagePicker.adapter = adapter
        binding.languagePicker.setSelection(selectedLanguageIndex)
    }

    private fun applyLanguageChange(position: Int) {
        selectedLanguageIndex = position
        val selectedLocale = supportedLocales[position]
        desiredLocale = selectedLocale
        prefs.desiredLocale = selectedLocale
        appLocaleRepository = prefs

        requireActivity().window.decorView.findViewById<View>(android.R.id.content)?.let {
            reword(it)
        }
    }

    private fun showRestartSnackBar() {
        Snackbar.make(
            binding.lytParent,
            getString(R.string.lbl_restart_app_prompt),
            Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.lbl_ok)) {
            val restartIntent = Intent(requireContext(), HomeStepperActivity::class.java)
            ProcessPhoenix.triggerRebirth(requireContext(), restartIntent)
        }.show()
    }

    override fun verifyStep(): VerificationError? {
        return verificationError
    }

    override fun onSelected() {}
    override fun onError(error: VerificationError) {}

    fun setOnFragmentCloseListener(callBack: IFragmentCallBack?) {
        this.fragmentCallBack = callBack
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}