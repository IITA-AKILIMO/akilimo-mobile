package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.dto.LanguageOption
import com.akilimo.mobile.ui.viewmodels.WelcomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.processphoenix.ProcessPhoenix
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeFragment : BaseStepFragment<FragmentWelcomeBinding>() {

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private val viewModel: WelcomeViewModel by lazy {
        ViewModelProvider(this, WelcomeViewModel.factory(database))[WelcomeViewModel::class.java]
    }

    private val languageOptions: List<LanguageOption> by lazy {
        Locales.supportedLocales.map {
            LanguageOption(it.getDisplayLanguage(it), it.toLanguageTag(), it.toLanguageTag())
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentWelcomeBinding = FragmentWelcomeBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        val languageAdapter = ValueOptionAdapter(requireContext(), languageOptions)
        binding.dropLanguage.setAdapter(languageAdapter)
        binding.dropLanguage.setOnItemClickListener { _, _, position, _ ->
            val selected = languageOptions[position]
            binding.dropLanguage.setText(selected.displayLabel, false)

            sessionManager.languageCode = selected.valueOption

            val selectedLocale = Locales.supportedLocales
                .find { it.toLanguageTag() == selected.valueOption }
                ?: Locales.english
            AppLocale.desiredLocale = selectedLocale

            viewModel.saveLanguage(selected, sessionManager.akilimoUser)
            promptRestart()
        }
    }

    override fun prefillFromEntity() {
        viewModel.loadLanguage(sessionManager.akilimoUser)
        safeScope.launch {
            viewModel.uiState.collect { state ->
                val selected = languageOptions.find { it.valueOption == state.currentLanguageCode }
                    ?: languageOptions.find { it.valueOption == Locales.english.toLanguageTag() }
                    ?: languageOptions.first()
                withContext(Dispatchers.Main) {
                    binding.dropLanguage.setText(selected.displayLabel, false)
                }
            }
        }
    }

    private fun promptRestart() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.lbl_restart_required)
            .setMessage(R.string.lbl_restart_language_message)
            .setCancelable(false)
            .setPositiveButton(R.string.lbl_restart_now) { _, _ ->
                ProcessPhoenix.triggerRebirth(requireContext())
            }
            .setNegativeButton(R.string.lbl_restart_later) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
