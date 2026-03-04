package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.ValueOptionAdapter
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.dto.LanguageOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WelcomeFragment : BaseStepFragment<FragmentWelcomeBinding>() {

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo
    private lateinit var prefsRepo: UserPreferencesRepo

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
        prefsRepo = UserPreferencesRepo(database.userPreferencesDao())
        val languageAdapter = ValueOptionAdapter(requireContext(), languageOptions)
        binding.dropLanguage.setAdapter(languageAdapter)
        binding.dropLanguage.setOnItemClickListener { _, _, position, _ ->
            val selected = languageOptions[position]
            binding.dropLanguage.setText(selected.displayLabel, false)
            safeScope.launch {
                val user = userRepository.getUser(sessionManager.akilimoUser) ?: AkilimoUser(
                    userName = sessionManager.akilimoUser
                )
                userRepository.saveOrUpdateUser(
                    user.copy(languageCode = selected.valueOption),
                    sessionManager.akilimoUser
                )
                sessionManager.languageCode = selected.valueOption

                withContext(Dispatchers.Main) {
                    promptRestart()
                }
            }
        }
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser)
            val prefs = prefsRepo.getOrDefault()

            val currentLangCode =
                user?.languageCode?.takeIf { it.isNotBlank() } ?: prefs.languageCode

            val selectedOption = languageOptions.find { it.valueOption == currentLangCode }
                ?: languageOptions.find { it.valueOption == Locales.english.language }
                ?: languageOptions.first()

            withContext(Dispatchers.Main) {
                binding.dropLanguage.setText(selectedOption.displayLabel, false)
            }
        }
    }

    private fun promptRestart() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.lbl_restart_required)
            .setMessage(R.string.lbl_restart_language_message)
            .setCancelable(false)
            .setPositiveButton(R.string.lbl_restart_now) { _, _ ->
                val intent = requireActivity().packageManager
                    .getLaunchIntentForPackage(requireActivity().packageName)
                    ?.apply { addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                requireActivity().finish()
                if (intent != null) {
                    startActivity(intent)
                }
            }
            .setNegativeButton(R.string.lbl_restart_later) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}