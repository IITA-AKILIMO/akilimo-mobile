package com.akilimo.mobile.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.databinding.ActivityGetRecommendationBinding
import com.akilimo.mobile.databinding.BottomSheetFeedbackBinding
import com.akilimo.mobile.dto.UserFeedBackRequest
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.parseError
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.RecommendationBuilder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import io.sentry.Sentry
import kotlinx.coroutines.launch

class GetRecommendationActivity : BaseActivity<ActivityGetRecommendationBinding>() {

    companion object {
        const val EXTRA_USE_CASE = "extra_use_case"
    }

    private lateinit var useCase: EnumUseCase
    private lateinit var userRepo: AkilimoUserRepo

    override fun inflateBinding() = ActivityGetRecommendationBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        useCase = intent.getParcelableExtra(EXTRA_USE_CASE)
            ?: return showError(getString(R.string.error_no_use_case))

        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        setupToolbar()
        setupClickListeners()
        fetchRecommendation()
    }

    private fun setupToolbar() {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .setTitle(getString(R.string.lbl_recommendations))
            .onNavigationClick { finish() }
            .build()
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            fetchRecommendation()
        }
        binding.fabFeedback.setOnClickListener {
            showFeedbackBottomSheet()
        }
    }

    private fun fetchRecommendation() {
        showLoading()

        safeScope.launch {
            try {
                val builder = RecommendationBuilder(
                    database = database,
                    session = sessionManager,
                    useCase = useCase
                )
                val payload = builder.build()

                if (payload == null) {
                    showError(getString(R.string.error_build_request))
                    return@launch
                }

                val base = AppConfig.resolveBaseUrlFor(
                    applicationContext,
                    EnumServiceType.AKILIMO
                )
                val client = ApiClient.createService<AkilimoApi>(
                    this@GetRecommendationActivity,
                    base
                )

                val result = client.computeRecommendations(payload)

                if (result.isSuccessful) {
                    val resp = result.body()
                    if (resp != null) {
                        val title = when (resp.recType) {
                            "FR" -> getString(R.string.lbl_fertilizer_rec)
                            "IC" -> getString(R.string.lbl_intercrop_rec)
                            "PP" -> getString(R.string.lbl_planting_practices_rec)
                            "SP" -> getString(R.string.lbl_scheduled_planting_rec)
                            else -> getString(R.string.lbl_no_recommendations_prompt)
                        }

                        val description = resp.recommendation
                            ?: getString(R.string.lbl_no_recommendations_prompt)

                        showRecommendation(title, description)
                    } else {
                        showError(getString(R.string.error_empty_response))
                    }
                } else {
                    val error = result.parseError()
                    val errorDetail = error?.error ?: getString(R.string.error_fetch_recommendation)
                    binding.emptyState.text = errorDetail
                    Sentry.captureMessage(errorDetail)
                    showError(error?.message ?: getString(R.string.error_fetch_recommendation))
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                showError(e.message ?: getString(R.string.error_unexpected))
            }
        }
    }

    private fun showLoading() = with(binding) {
        loadingIndicator.visibility = View.VISIBLE
        recommendationCard.visibility = View.GONE
        errorState.visibility = View.GONE
        fabFeedback.visibility = View.GONE
    }

    private fun showRecommendation(title: String, description: String) = with(binding) {
        loadingIndicator.visibility = View.GONE
        errorState.visibility = View.GONE
        recommendationCard.visibility = View.VISIBLE
        fabFeedback.visibility = View.VISIBLE

        recommendationTitle.text = title
        recommendationDescription.text = description
    }

    private fun showError(error: String) {
        binding.loadingIndicator.visibility = View.GONE
        binding.recommendationCard.visibility = View.GONE
        binding.errorState.visibility = View.VISIBLE
        binding.emptyStateSubtitle.text = error
        binding.fabFeedback.visibility = View.GONE
    }

    private fun showFeedbackBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetFeedbackBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sheetBinding.root)

        var selectedRating: Int? = null
        var selectedNps: Int? = null

        val ratingButtons = listOf(
            sheetBinding.btnRating1 to 1,
            sheetBinding.btnRating2 to 2,
            sheetBinding.btnRating3 to 3,
            sheetBinding.btnRating4 to 4,
            sheetBinding.btnRating5 to 5
        )

        ratingButtons.forEach { (button, rating) ->
            button.setOnClickListener {
                selectedRating = rating
                updateRatingSelection(ratingButtons, button)
                sheetBinding.selectedRating.apply {
                    visibility = View.VISIBLE
                    text = getRatingLabel(rating)
                }
                updateSubmitButton(sheetBinding, selectedRating, selectedNps)
            }
        }

        val npsButtons = listOf(
            sheetBinding.btnNps0 to 0,
            sheetBinding.btnNps1 to 1,
            sheetBinding.btnNps2 to 2,
            sheetBinding.btnNps3 to 3,
            sheetBinding.btnNps4 to 4,
            sheetBinding.btnNps5 to 5,
            sheetBinding.btnNps6 to 6,
            sheetBinding.btnNps7 to 7,
            sheetBinding.btnNps8 to 8,
            sheetBinding.btnNps9 to 9,
            sheetBinding.btnNps10 to 10
        )

        npsButtons.forEach { (button, score) ->
            button.setOnClickListener {
                selectedNps = score
                updateNpsSelection(npsButtons, button)
                sheetBinding.selectedNps.apply {
                    visibility = View.VISIBLE
                    text = getNpsLabel(score)
                }
                updateSubmitButton(sheetBinding, selectedRating, selectedNps)
            }
        }

        sheetBinding.btnSubmitFeedback.setOnClickListener {
            if (selectedRating != null && selectedNps != null) {
                bottomSheetDialog.dismiss()
                submitFeedback(selectedRating, selectedNps)
            }
        }

        bottomSheetDialog.show()
    }

    private fun getRatingLabel(rating: Int): String {
        return when (rating) {
            1 -> getString(R.string.feedback_very_poor)
            2 -> getString(R.string.feedback_poor)
            3 -> getString(R.string.feedback_okay)
            4 -> getString(R.string.feedback_good)
            5 -> getString(R.string.feedback_excellent)
            else -> ""
        }
    }

    private fun getNpsLabel(score: Int): String {
        return when {
            score <= 6 -> getString(R.string.feedback_nps_low)
            score <= 8 -> getString(R.string.feedback_nps_medium)
            else -> getString(R.string.feedback_nps_high)
        }
    }

    private fun updateRatingSelection(
        buttons: List<Pair<MaterialButton, Int>>,
        selectedButton: MaterialButton
    ) {
        buttons.forEach { (button, _) ->
            button.isSelected = button == selectedButton
        }
    }

    private fun updateNpsSelection(
        buttons: List<Pair<MaterialButton, Int>>,
        selectedButton: MaterialButton
    ) {
        buttons.forEach { (button, _) ->
            button.isSelected = button == selectedButton
        }
    }

    private fun updateSubmitButton(
        binding: BottomSheetFeedbackBinding,
        rating: Int?,
        nps: Int?
    ) {
        binding.btnSubmitFeedback.isEnabled = rating != null && nps != null
    }

    private fun submitFeedback(rating: Int, npsScore: Int) {
        safeScope.launch {
            try {
                val base = AppConfig.resolveBaseUrlFor(
                    applicationContext,
                    EnumServiceType.AKILIMO
                )
                val client = ApiClient.createService<AkilimoApi>(
                    this@GetRecommendationActivity,
                    base
                )

                val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
                val feedbackDto = UserFeedBackRequest(
                    satisfactionRating = rating,
                    npsScore = npsScore,
                    useCase = useCase.name,
                    akilimoUsage = user.akilimoInterest.orEmpty(),
                    userType = user.akilimoInterest.orEmpty(),
                    deviceToken = user.deviceToken.orEmpty(),
                    deviceLanguage = user.languageCode.orEmpty()
                )

                val result = client.submitUserFeedback(feedbackDto)

                if (result.isSuccessful) {
                    Toast.makeText(
                        this@GetRecommendationActivity,
                        getString(R.string.feedback_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val error = result.parseError()
                    Toast.makeText(
                        this@GetRecommendationActivity,
                        error?.message ?: getString(R.string.feedback_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                Toast.makeText(
                    this@GetRecommendationActivity,
                    getString(R.string.feedback_error_exception, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}