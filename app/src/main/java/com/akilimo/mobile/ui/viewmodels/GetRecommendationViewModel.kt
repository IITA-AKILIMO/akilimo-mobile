package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.dto.UserFeedBackRequest
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.parseError
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.utils.RecommendationBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    fun fetchRecommendation(useCase: EnumUseCase, noRecsLabel: String, errorLabel: String) =
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
                            title = resp.recType ?: "",
                            description = resp.recommendation ?: noRecsLabel
                        )
                    } else {
                        _uiState.value = UiState.Error(noRecsLabel)
                    }
                } else {
                    val error = result.parseError()
                    Sentry.captureMessage(error?.error ?: "Recommendation fetch failed")
                    _uiState.value = UiState.Error(error?.message ?: errorLabel)
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.value = UiState.Error(e.message ?: errorLabel)
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
            } catch (e: Exception) {
                Sentry.captureException(e)
                _feedbackResult.emit(false)
            }
        }
}
