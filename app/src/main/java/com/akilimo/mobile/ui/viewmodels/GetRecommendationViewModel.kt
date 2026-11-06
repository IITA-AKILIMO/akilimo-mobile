package com.akilimo.mobile.ui.viewmodels

import android.content.Context
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
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _feedbackResult = MutableSharedFlow<Boolean>()
    val feedbackResult = _feedbackResult.asSharedFlow()

    /**
     * Fetch once per screen instance. The screen calls this from a LaunchedEffect, which
     * re-runs after rotation; skipping when a request is in flight or already succeeded
     * avoids discarding the result and repeating the network call. Errors can be retried.
     */
    fun fetchRecommendation(useCase: EnumUseCase, noRecsLabel: String, errorLabel: String) {
        val current = _uiState.value
        if (current is UiState.Loading || current is UiState.Success) return
        loadRecommendation(useCase, noRecsLabel, errorLabel)
    }

    private fun loadRecommendation(useCase: EnumUseCase, noRecsLabel: String, errorLabel: String) =
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val builder = RecommendationBuilder(database, appSettings, useCase)
                val payload = builder.build() ?: run {
                    _uiState.value = UiState.Error(errorLabel)
                    return@launch
                }

                val base = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO)
                val client = ApiClient.createService<AkilimoApi>(context, base)
                val result = client.computeRecommendations(payload)

                if (result.isSuccessful) {
                    val resp = result.body()
                    if (resp != null) {
                        _uiState.value = UiState.Success(
                            title = resp.data.recType,
                            description = resp.data.recommendation
                        )
                    } else {
                        _uiState.value = UiState.Error(noRecsLabel)
                    }
                } else {
                    val error = result.parseError()
                    _uiState.value = UiState.Error(
                        when {
                            error?.isOutsideCoverage == true ->
                                context.getString(R.string.error_recommendation_outside_coverage)
                            else -> error?.message ?: errorLabel
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Offline/timeouts are expected in the field; only report real bugs
                if (e !is IOException) Sentry.captureException(e)
                // Show the localized message, not raw text like "Unable to resolve host"
                _uiState.value = UiState.Error(errorLabel)
            }
        }

    fun submitFeedback(useCase: EnumUseCase, rating: Int, npsScore: Int) =
        viewModelScope.launch {
            try {
                val user = userRepo.getUser(appSettings.akilimoUser) ?: return@launch
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
}
