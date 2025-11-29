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
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.RecommendationBuilder
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetRecommendationActivity : BaseActivity<ActivityGetRecommendationBinding>() {

    override fun inflateBinding() = ActivityGetRecommendationBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // Setup toolbar
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()


        fetchRecommendation()
        binding.btnRetry.setOnClickListener {
            fetchRecommendation()
        }
        binding.fabFeedback.setOnClickListener {
            showFeedbackBottomSheet()
        }
    }

    private fun fetchRecommendation() {
        // Show loading
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.recommendationCard.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.fabFeedback.visibility = View.GONE

        safeScope.launch(Dispatchers.IO) {
            try {
                val builder = RecommendationBuilder(
                    database = database,
                    session = sessionManager,
                    useCase = EnumUseCase.FR
                )
                val payload = builder.build()   // suspend, but lightweight

                val base = AppConfig.resolveBaseUrlFor(
                    applicationContext, EnumServiceType.AKILIMO
                )
                val client =
                    ApiClient.createService<AkilimoApi>(this@GetRecommendationActivity, base)
                payload?.let { recRequest ->
                    val result = client.computeRecommendations(recRequest)

                }

                val title = "DST Recommendation"
                val description =
                    "This is a detailed recommendation paragraph fetched from the API."

                withContext(Dispatchers.Main) {
                    showRecommendation(title, description)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError()
                }
            }
        }
    }

    private fun showRecommendation(
        title: String, description: String
    ) {
        binding.loadingIndicator.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.recommendationCard.visibility = View.VISIBLE
        binding.fabFeedback.visibility = View.VISIBLE

        binding.recommendationTitle.text = title
        binding.recommendationDescription.text = description
    }

    private fun showError() {
        binding.loadingIndicator.visibility = View.GONE
        binding.recommendationCard.visibility = View.GONE
        binding.errorState.visibility = View.VISIBLE
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
                sheetBinding.selectedRating.text = "Selected: $rating"
                bottomSheetDialog.dismiss()
                submitFeedback(rating)
            }
        }

        bottomSheetDialog.show()
    }

    private fun submitFeedback(rating: Int) {
        safeScope.launch(Dispatchers.IO) {
            try {
                // Replace with real API call
                delay(1000)
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        this@GetRecommendationActivity,
                        "Feedback submitted: $rating/5",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        this@GetRecommendationActivity,
                        "Failed to submit feedback",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
