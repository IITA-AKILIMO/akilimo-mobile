package com.akilimo.mobile.ui.activities

import android.os.Bundle
import android.view.View
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityDstRecommendationBinding
import com.akilimo.mobile.databinding.BottomSheetFeedbackBinding
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DstRecommendationActivity : BaseActivity<ActivityDstRecommendationBinding>() {

    override fun inflateBinding() = ActivityDstRecommendationBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // Setup toolbar
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()


        // Initial load
        fetchRecommendation(binding)

        // Retry button
        binding.btnRetry.setOnClickListener {
            fetchRecommendation(binding)
        }

        // Feedback FAB
        binding.fabFeedback.setOnClickListener {
            showFeedbackBottomSheet()
        }
    }

    private fun fetchRecommendation(binding: ActivityDstRecommendationBinding) {
        // Show loading
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.recommendationCard.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.fabFeedback.visibility = View.GONE

        safeScope.launch(Dispatchers.IO) {
            try {
                // Simulate API call (replace with real network request)
                delay(2000) // pretend network latency
                val title = "DST Recommendation"
                val description = "This is a detailed recommendation paragraph fetched from the API."

                withContext(Dispatchers.Main) {
                    showRecommendation(binding, title, description)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError(binding)
                }
            }
        }
    }

    private fun showRecommendation(
        binding: ActivityDstRecommendationBinding,
        title: String,
        description: String
    ) {
        binding.loadingIndicator.visibility = View.GONE
        binding.errorState.visibility = View.GONE
        binding.recommendationCard.visibility = View.VISIBLE
        binding.fabFeedback.visibility = View.VISIBLE

        binding.recommendationTitle.text = title
        binding.recommendationDescription.text = description
    }

    private fun showError(binding: ActivityDstRecommendationBinding) {
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
                        this@DstRecommendationActivity,
                        "Feedback submitted: $rating/5",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        this@DstRecommendationActivity,
                        "Failed to submit feedback",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}
