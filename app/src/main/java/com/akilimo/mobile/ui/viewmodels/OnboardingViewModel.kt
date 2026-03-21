package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val prefsRepo: UserPreferencesRepo,
    private val selectedFertilizerRepo: SelectedFertilizerRepo,
    private val currentPracticeRepo: CurrentPracticeRepo,
    private val adviceCompletionRepo: AdviceCompletionRepo
) : ViewModel() {

    suspend fun getUser(userName: String): AkilimoUser? = userRepo.getUser(userName)

    suspend fun getPreferences(): UserPreferences = prefsRepo.getOrDefault()

    suspend fun saveUser(user: AkilimoUser, userName: String) =
        userRepo.saveOrUpdateUser(user, userName)

    suspend fun deleteSelectedFertilizersByUser(userId: Int) =
        selectedFertilizerRepo.deleteByUserId(userId)

    suspend fun saveCurrentPractice(practice: CurrentPractice) =
        currentPracticeRepo.savePractice(practice)

    suspend fun getCurrentPractice(userId: Int): CurrentPractice? =
        currentPracticeRepo.getPracticeForUser(userId)

    suspend fun markStepInProgress(task: EnumAdviceTask) =
        adviceCompletionRepo.markInProgressIfNotCompleted(task)
}
