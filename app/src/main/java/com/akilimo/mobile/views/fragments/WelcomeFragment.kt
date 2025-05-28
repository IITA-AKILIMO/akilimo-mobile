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
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.LanguageOption
import com.akilimo.mobile.utils.Locales
import com.akilimo.mobile.views.activities.SplashActivity
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import com.stepstone.stepper.VerificationError

class WelcomeFragment : BaseStepFragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private var languagePicked = false

    private var fragmentCallBack: IFragmentCallBack? = null

    companion object {
        fun newInstance(): WelcomeFragment = WelcomeFragment()
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateLanguages()
    }

    private fun populateLanguages() {
        val languageOptions = Locales.LOCALE_COUNTRIES.map {
            LanguageOption(it.language, it.getDisplayLanguage(it))
        }


        val savedLanguageCode = LanguageManager.getLanguage(requireContext())
        val selectedIndex = languageOptions.indexOfFirst { it.code == savedLanguageCode }

        val adapter = MySpinnerAdapter(requireContext(), languageOptions.map { it.displayName })
        binding.apply {
            languagePicker.adapter = adapter

            languagePicker.setSelection(if (selectedIndex >= 0) selectedIndex else 0)

            languagePicker.setOnTouchListener { v, event ->
                languagePicked = true
                v.performClick()
                v.onTouchEvent(event)
            }


            languagePicker.onItemSelectedListener =
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
            binding.lytParent, getString(R.string.lbl_restart_app_prompt), Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.lbl_ok)) {
            val restartIntent = Intent(requireContext(), SplashActivity::class.java)
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