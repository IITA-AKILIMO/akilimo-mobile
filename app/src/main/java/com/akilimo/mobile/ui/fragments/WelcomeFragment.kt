package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akilimo.mobile.Locales
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.dto.LanguageOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.launch

class WelcomeFragment : BaseStepFragment<FragmentWelcomeBinding>() {

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo

    private val languageOptions: List<LanguageOption> by lazy {
        Locales.supportedLocales.map {
            LanguageOption(it.getDisplayLanguage(it), it.language, it.language)
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentWelcomeBinding = FragmentWelcomeBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
        val languageAdapter = ValueOptionAdapter(requireContext(), languageOptions)
        binding.dropLanguage.setAdapter(languageAdapter)
        binding.dropLanguage.setOnItemClickListener { _, _, position, _ ->
            val selected = languageOptions[position]
            binding.dropLanguage.setText(selected.displayLabel, false)
        }

    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            val currentLangCode =
                user?.languageCode?.takeIf { it.isNotBlank() } ?: Locales.English.language

            val selectedOption = languageOptions.find { it.valueOption == currentLangCode }
                ?: languageOptions.find { it.valueOption == Locales.English.language }
                ?: languageOptions.first()

            binding.dropLanguage.setText(selectedOption.displayLabel, false)
        }
    }

    override fun verifyStep(): VerificationError? = with(binding) {
        val langCode =
            languageOptions.find { it.displayLabel == dropLanguage.text.toString() }?.valueOption
                ?: Locales.English.language

        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser(
                userName = sessionManager.akilimoUser
            )

            userRepository.saveOrUpdateUser(
                user.copy(
                    languageCode = langCode
                ), sessionManager.akilimoUser
            )
        }
        return null
    }
}
