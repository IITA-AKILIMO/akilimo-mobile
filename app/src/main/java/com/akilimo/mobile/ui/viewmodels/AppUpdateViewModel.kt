package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.di.InstalledVersionCode
import com.akilimo.mobile.repos.AppVersionRepo
import com.akilimo.mobile.repos.UpdateLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Asks the API whether the installed build is too old: at launch, when the app comes back after a while and when the
 * recommendations screen opens. A failure (offline, server error) keeps the last known answer, and before any answer
 * the level is [UpdateLevel.NONE], so a farmer without a connection is never locked out.
 */
@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val repo: AppVersionRepo,
    @InstalledVersionCode private val installedVersionCode: Int,
) : ViewModel() {

    data class UiState(
        val level: UpdateLevel = UpdateLevel.NONE,
        /** Optional text from the server to show with the prompt. */
        val message: String? = null,
        val storeUrl: String? = null,
        val bannerDismissed: Boolean = false,
    ) {
        /** The farmer cannot use the app until it is updated. */
        val blocking: Boolean get() = level == UpdateLevel.REQUIRED

        val showBanner: Boolean get() = level == UpdateLevel.AVAILABLE && !bannerDismissed
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Time source; replaced in tests. */
    internal var clock: () -> Long = System::currentTimeMillis

    private var checkJob: Job? = null
    private var lastCheckAt: Long? = null

    init {
        recheck()
    }

    /**
     * Asks the API again unless a check ran less than [minIntervalMs] ago or one is still running. A failed check keeps
     * what is already known, so being offline never clears or raises a prompt.
     */
    fun recheck(minIntervalMs: Long = 0L) {
        if (checkJob?.isActive == true) return
        val last = lastCheckAt
        if (last != null && clock() - last < minIntervalMs) return
        checkJob = viewModelScope.launch {
            val policy = repo.fetchPolicy() ?: return@launch
            lastCheckAt = clock()
            _uiState.update {
                it.copy(
                    level = policy.levelFor(installedVersionCode),
                    message = policy.message,
                    storeUrl = policy.storeUrl,
                )
            }
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(bannerDismissed = true) }
    }
}
