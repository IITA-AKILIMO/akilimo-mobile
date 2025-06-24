package com.akilimo.mobile.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MySpinnerAdapter
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.viewmodels.WelcomeViewModel
import com.akilimo.mobile.viewmodels.factory.WelcomeViewModelFactory
import com.akilimo.mobile.views.activities.SplashActivity
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import com.stepstone.stepper.VerificationError

class WelcomeFragment : BindBaseStepFragment<FragmentWelcomeBinding>() {

    private val viewModel: WelcomeViewModel by viewModels {
        WelcomeViewModelFactory(requireActivity().application)
    }

    private lateinit var myAdapter: MySpinnerAdapter


    companion object {
        fun newInstance(): WelcomeFragment = WelcomeFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentWelcomeBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.loadLanguages()
        binding.apply {
            welcomeLanguageSpinner.setOnTouchListener { v, event ->
                viewModel.setLanguagePicked()
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
                        viewModel.languageOptions.value?.get(position)?.let { languageOption ->
                            viewModel.onLanguageSelected(languageOption.code)
                            if (viewModel.languagePicked.value == true) {
                                showRestartSnackBar()
                            }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        //Nothing
                    }
                }
        }
    }


    override fun setupObservers() {
        viewModel.languageOptions.observe(viewLifecycleOwner) { options ->
            myAdapter = MySpinnerAdapter(requireContext(), options.map { it.displayName })
            binding.welcomeLanguageSpinner.adapter = myAdapter
        }
        viewModel.selectedIndex.observe(viewLifecycleOwner) { index ->
            binding.welcomeLanguageSpinner.setSelection(index)
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
