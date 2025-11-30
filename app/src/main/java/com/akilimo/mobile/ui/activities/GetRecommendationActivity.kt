package com.akilimo.mobile.ui.activities

import android.os.Bundle
import android.view.View
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.databinding.ActivityGetRecommendationBinding
import com.akilimo.mobile.databinding.BottomSheetFeedbackBinding
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.parseError
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.RecommendationBuilder
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetRecommendationActivity : BaseActivity<ActivityGetRecommendationBinding>() {

    companion object {
        const val EXTRA_USE_CASE = "extra_use_case"
    }

    private lateinit var useCase: EnumUseCase

    override fun inflateBinding() = ActivityGetRecommendationBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // Get the use case from intent extras
        @Suppress("DEPRECATION")
        useCase = intent.getParcelableExtra(EXTRA_USE_CASE)
            ?: return showError("No use case specified")

        setupToolbar()
        setupClickListeners()
        fetchRecommendation()
    }

    private fun setupToolbar() {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
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

        safeScope.launch(Dispatchers.IO) {
            try {
                val builder = RecommendationBuilder(
                    database = database,
                    session = sessionManager,
                    useCase = useCase
                )
                val payload = builder.build()

                if (payload == null) {
                    withContext(Dispatchers.Main) {
                        showError("Unable to build recommendation request")
                    }
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

                withContext(Dispatchers.Main) {
                    if (result.isSuccessful) {
                        val resp = result.body()
                        if (resp != null) {

                            // Extract actual data from response
                            val title = resp.recType ?: "DST Recommendation"
                            val description = resp.recommendation
                                ?: "No recommendation details available"

                            showRecommendation(title, description)
                        } else {
                            showError("Empty response from server")
                        }
                    } else {
                        val error = result.parseError()
                        val errorDetail = error?.error ?: "Failed to fetch recommendation"
                        binding.emptyState.text = errorDetail
                        Sentry.captureMessage(errorDetail)
                        showError(error?.message ?: "Failed to fetch recommendation")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Sentry.captureException(e)
                    showError(e.message ?: "An unexpected error occurred")
                }
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

        val ratingButtons = listOf(
            sheetBinding.btnRating1 to 1,
            sheetBinding.btnRating2 to 2,
            sheetBinding.btnRating3 to 3,
            sheetBinding.btnRating4 to 4,
            sheetBinding.btnRating5 to 5
        )

        ratingButtons.forEach { (button, rating) ->
            button.setOnClickListener {
                sheetBinding.selectedRating.apply {
                    visibility = View.VISIBLE
                    text = "Thank you for rating: $rating/5"
                }
                // Delay dismissal slightly for user to see confirmation
                binding.root.postDelayed({
                    bottomSheetDialog.dismiss()
                    submitFeedback(rating)
                }, 500)
            }
        }

        bottomSheetDialog.show()
    }

    private fun submitFeedback(rating: Int) {
        safeScope.launch(Dispatchers.IO) {
            try {
                // TODO: Replace with actual API call
                // Example:
                // val feedbackRequest = FeedbackRequest(
                //     recommendationId = currentRecommendationId,
                //     rating = rating
                // )
                // val result = client.submitFeedback(feedbackRequest)

                delay(1000) // Simulating API call

                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        this@GetRecommendationActivity,
                        "Thank you for your feedback: $rating/5",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        this@GetRecommendationActivity,
                        "Failed to submit feedback: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}