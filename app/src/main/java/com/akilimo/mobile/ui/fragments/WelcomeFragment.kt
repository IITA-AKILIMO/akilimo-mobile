package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.akilimo.mobile.Locales
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.dto.LanguageOption
import com.akilimo.mobile.ui.viewmodels.WelcomeViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeFragment : BaseStepFragment<FragmentWelcomeBinding>() {

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private val viewModel: WelcomeViewModel by viewModels()

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

            val selectedLocale = Locales.supportedLocales
                .find { it.toLanguageTag() == selected.valueOption }
                ?: Locales.english
            AppLocale.desiredLocale = selectedLocale

            // DB writes (user entity + prefs) can run async — they don't block recreation
            viewModel.saveLanguage(selected, sessionManager.akilimoUser)

            // DataStore MUST be written before setApplicationLocales() triggers recreation,
            // otherwise attachBaseContext() reads the old locale from DataStore.
            safeScope.launch {
                appSettings.setLanguageTag(selected.valueOption)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(selected.valueOption)
                )
            }
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

}
