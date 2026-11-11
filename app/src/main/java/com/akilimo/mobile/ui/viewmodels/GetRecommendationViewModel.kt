package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.database.AppDatabase
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.network.dto.UserFeedBackRequest
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.parseError
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.RecommendationBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import com.akilimo.mobile.data.ReadingTextScale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GetRecommendationViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val appSettings: AppSettingsDataStore,
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Success(val title: String, val description: String) : UiState

        /** [messageRes] is resolved by the screen so it follows the in-app locale. */
        data class Error(@StringRes val messageRes: Int) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Reader-chosen text size (1.0 = default), remembered across screens and launches. */
    val textScale: StateFlow<Float> = appSettings.readingTextScaleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ReadingTextScale.DEFAULT)

    fun setTextScale(scale: Float) {
        viewModelScope.launch { appSettings.setReadingTextScale(scale) }
    }

    private val _feedbackResult = MutableSharedFlow<Boolean>()
    val feedbackResult = _feedbackResult.asSharedFlow()

    /**
     * Fetch once per screen instance. The screen calls this from a LaunchedEffect, which
     * re-runs after rotation; skipping when a request is in flight or already succeeded
     * avoids discarding the result and repeating the network call. Errors can be retried.
     */
    fun fetchRecommendation(useCase: EnumUseCase) {
        val current = _uiState.value
        if (current is UiState.Loading || current is UiState.Success) return
        loadRecommendation(useCase)
    }

    private fun loadRecommendation(useCase: EnumUseCase) =
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val builder = RecommendationBuilder(database, appSettings, useCase)
                val payload = builder.build() ?: run {
                    // The only reason build() returns null is a missing farmer profile
                    _uiState.value = UiState.Error(R.string.error_rec_profile_missing)
                    return@launch
                }

                val base = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO)
                val client = ApiClient.createService<AkilimoApi>(context, base)
                val result = client.computeRecommendations(payload)

                if (result.isSuccessful) {
                    val resp = result.body()
                    _uiState.value = if (resp != null) {
                        UiState.Success(
                            title = resp.data.recType,
                            description = resp.data.recommendation
                        )
                    } else {
                        UiState.Error(R.string.lbl_no_recommendations_prompt)
                    }
                } else {
                    // Never show the raw server message: it is English-only and technical
                    val outsideCoverage = result.parseError()?.isOutsideCoverage == true
                    _uiState.value = UiState.Error(
                        if (outsideCoverage) R.string.error_recommendation_outside_coverage
                        else errorMessageForHttpCode(result.code())
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Offline/timeouts are expected in the field; only report real bugs
                if (e !is IOException) Sentry.captureException(e)
                _uiState.value = UiState.Error(
                    if (e is IOException) R.string.error_rec_network else R.string.error_fetch_recommendation
                )
            }
        }

    fun submitFeedback(useCase: EnumUseCase, rating: Int, npsScore: Int) =
        viewModelScope.launch {
            try {
                val user = userRepo.getUser(appSettings.akilimoUser)
                if (user == null) {
                    // Without a profile there is nothing to send; tell the user it failed
                    _feedbackResult.emit(false)
                    return@launch
                }
                val base = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO)
                val client = ApiClient.createService<AkilimoApi>(context, base)
                val request = UserFeedBackRequest(
                    satisfactionRating = rating,
                    npsScore = npsScore,
                    useCase = useCase.name,
                    akilimoUsage = user.akilimoInterest.orEmpty(),
                    userType = user.akilimoInterest.orEmpty(),
                    deviceToken = user.deviceToken.orEmpty(),
                    deviceLanguage = user.languageCode.orEmpty()
                )
                val result = client.submitUserFeedback(request)
                _feedbackResult.emit(result.isSuccessful)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is IOException) Sentry.captureException(e)
                _feedbackResult.emit(false)
            }
        }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_UNPROCESSABLE = 422
        private const val HTTP_TIMEOUT = 408
        private const val HTTP_TOO_MANY = 429
        private const val HTTP_SERVER_MIN = 500

        /** Maps an unsuccessful HTTP status to a farmer-friendly, translatable message. */
        @StringRes
        fun errorMessageForHttpCode(code: Int): Int = when {
            code == HTTP_BAD_REQUEST || code == HTTP_UNPROCESSABLE -> R.string.error_rec_invalid_input
            code == HTTP_TIMEOUT || code == HTTP_TOO_MANY || code >= HTTP_SERVER_MIN ->
                R.string.error_rec_server_busy
            else -> R.string.error_fetch_recommendation
        }
    }
}
