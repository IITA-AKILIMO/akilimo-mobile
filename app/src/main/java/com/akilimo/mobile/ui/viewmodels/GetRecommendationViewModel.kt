package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.database.AppDatabase
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.network.dto.ReportKind
import com.akilimo.mobile.network.logRecommendationFailureEvent
import com.akilimo.mobile.network.reportRecommendationFailure
import com.akilimo.mobile.network.technicalDetail
import io.sentry.SentryLevel
import com.akilimo.mobile.network.dto.downloadLinks
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.network.dto.UserFeedBackRequest
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.dto.RecommendationResponse
import com.akilimo.mobile.network.request.RecommendationRequest
import com.akilimo.mobile.repos.account.SavedAdviceRepo
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GetRecommendationViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val appSettings: AppSettingsDataStore,
    private val database: AppDatabase,
    private val accountSession: AccountSessionStore,
    private val savedAdvice: SavedAdviceRepo,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data object Loading : UiState
        data class Success(
            val title: String,
            val description: String,
            /** True when the advice was kept in the farmer's account (signed in, consent given). */
            val savedToAccount: Boolean = false,
            /** Absolute download links of the PDF report, by kind; empty when the API made none. */
            val reportLinks: Map<ReportKind, String> = emptyMap(),
        ) : UiState

        /**
         * [messageRes] is resolved by the screen so it follows the in-app locale. [technicalDetail] is the raw
         * failure (HTTP status and path, or the exception), shown small so a farmer can quote it when reporting.
         */
        data class Error(
            @param:StringRes val messageRes: Int,
            val technicalDetail: String? = null,
            val kind: ErrorKind = ErrorKind.UNKNOWN,
        ) : UiState

        /** Why advice could not be shown; the screen picks a calm icon, title and action for each. */
        enum class ErrorKind { OUTSIDE_COVERAGE, OFFLINE, BUSY, INPUT, PROFILE, NO_ADVICE, UNKNOWN }
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Reader-chosen text size (1.0 = default), remembered across screens and launches. */
    val textScale: StateFlow<Float> = appSettings.readingTextScaleFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            ReadingTextScale.DEFAULT
        )

    fun setTextScale(scale: Float) {
        viewModelScope.launch { appSettings.setReadingTextScale(scale) }
    }

    /** Whether the reader keeps this screen in landscape; remembered across visits. */
    val landscapeLocked: StateFlow<Boolean> = appSettings.readingLandscapeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun setLandscapeLocked(locked: Boolean) {
        viewModelScope.launch { appSettings.setReadingLandscape(locked) }
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
                    _uiState.value = UiState.Error(R.string.error_rec_profile_missing, kind = UiState.ErrorKind.PROFILE)
                    return@launch
                }

                val base = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO)
                // A signed-in farmer's token goes along, so the server saves the advice in their account;
                // without one the call is anonymous, exactly as before.
                val client = ApiClient.createService<AkilimoApi>(
                    context,
                    base,
                    bearerToken = { accountSession.token })
                val result = client.computeRecommendations(payload)

                if (result.isSuccessful) {
                    val resp = result.body()
                    _uiState.value = if (resp != null) {
                        UiState.Success(
                            title = resp.data.recType,
                            description = resp.data.recommendation,
                            savedToAccount = keepInAccount(useCase, payload, resp),
                            reportLinks = resp.report.downloadLinks(base),
                        )
                    } else {
                        failure(
                            UiState.ErrorKind.NO_ADVICE,
                            R.string.lbl_no_recommendations_prompt,
                            "HTTP ${result.code()} empty response body",
                            status = result.code(),
                        )
                    }
                } else {
                    // Never show the raw server message: it is English-only and technical
                    val errorBody = result.parseError()
                    val outsideCoverage = errorBody?.isOutsideCoverage == true
                    val kind = if (outsideCoverage) UiState.ErrorKind.OUTSIDE_COVERAGE else errorKindForHttpCode(result.code())
                    _uiState.value = failure(
                        kind,
                        if (outsideCoverage) R.string.error_recommendation_outside_coverage
                        else errorMessageForHttpCode(result.code()),
                        result.technicalDetail(errorBody),
                        status = result.code(),
                        apiCode = errorBody?.code,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = if (e is IOException) {
                    failure(UiState.ErrorKind.OFFLINE, R.string.error_rec_network, e.technicalDetail(), cause = e)
                } else {
                    failure(UiState.ErrorKind.UNKNOWN, R.string.error_rec_unknown, e.technicalDetail(), cause = e)
                }
            }
        }

    /**
     * Builds the error state and sends a copy to Sentry. The Sentry reference is added to the technical details, so
     * a farmer who reports the problem gives support the exact event. Offline and busy cases are throttled.
     */
    private fun failure(
        kind: UiState.ErrorKind,
        @StringRes messageRes: Int,
        detail: String,
        status: Int? = null,
        apiCode: String? = null,
        cause: Throwable? = null,
    ): UiState.Error {
        val calm = kind != UiState.ErrorKind.UNKNOWN
        val reference = reportRecommendationFailure(
            kind = kind.name.lowercase(),
            detail = detail,
            level = if (calm) SentryLevel.INFO else SentryLevel.ERROR,
            status = status,
            apiCode = apiCode,
            cause = cause,
            throttle = kind == UiState.ErrorKind.OFFLINE || kind == UiState.ErrorKind.BUSY,
        )
        logRecommendationFailureEvent(context, kind.name.lowercase(), status, apiCode)
        val full = if (reference == null) detail else "$detail \u00B7 ref=$reference"
        return UiState.Error(messageRes, full, kind)
    }

    /** Keeps a copy on the phone for the saved-advice list; the server saved its own when the token was valid. */
    private suspend fun keepInAccount(
        useCase: EnumUseCase,
        payload: RecommendationRequest,
        resp: RecommendationResponse
    ): Boolean {
        val session = accountSession.session.value ?: return false
        if (session.consentRequired) return false
        savedAdvice.saveComputed(
            requestId = resp.requestId,
            useCase = useCase.name,
            countryCode = payload.computeRequest.farmInformation.countryCode,
            recType = resp.data.recType,
            recommendation = resp.data.recommendation,
        )
        return true
    }

    /** True while a farmer is signed in, so the screen can say the advice is saved or offer to save it. */
    val signedIn: StateFlow<Boolean> = accountSession.session
        .map { it != null && !it.consentRequired }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            accountSession.isSignedIn
        )

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

        /** Maps an unsuccessful HTTP status to the kind of failure the screen shows. */
        fun errorKindForHttpCode(code: Int): UiState.ErrorKind = when {
            code == HTTP_BAD_REQUEST || code == HTTP_UNPROCESSABLE -> UiState.ErrorKind.INPUT
            code == HTTP_TIMEOUT || code == HTTP_TOO_MANY || code >= HTTP_SERVER_MIN -> UiState.ErrorKind.BUSY
            else -> UiState.ErrorKind.UNKNOWN
        }

        /** Maps an unsuccessful HTTP status to a farmer-friendly, translatable message. */
        @StringRes
        fun errorMessageForHttpCode(code: Int): Int = when {
            code == HTTP_BAD_REQUEST || code == HTTP_UNPROCESSABLE -> R.string.error_rec_invalid_input
            code == HTTP_TIMEOUT || code == HTTP_TOO_MANY || code >= HTTP_SERVER_MIN ->
                R.string.error_rec_server_busy

            else -> R.string.error_rec_unknown
        }
    }
}
