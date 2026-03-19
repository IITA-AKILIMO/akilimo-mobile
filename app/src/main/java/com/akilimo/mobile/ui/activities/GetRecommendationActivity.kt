package com.akilimo.mobile.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityGetRecommendationBinding
import com.akilimo.mobile.databinding.BottomSheetFeedbackBinding
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.GetRecommendationViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GetRecommendationActivity : BaseActivity<ActivityGetRecommendationBinding>() {

    companion object {
        const val EXTRA_USE_CASE = "extra_use_case"
    }

    private lateinit var useCase: EnumUseCase
    private val viewModel: GetRecommendationViewModel by viewModels()

    override fun inflateBinding() = ActivityGetRecommendationBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        useCase = intent.getParcelableExtra(EXTRA_USE_CASE)
            ?: return showError(getString(R.string.error_no_use_case))

        setupToolbar()
        setupClickListeners()
        observeViewModel()
        viewModel.fetchRecommendation(
            useCase = useCase,
            noRecsLabel = getString(R.string.lbl_no_recommendations_prompt),
            errorLabel = getString(R.string.error_fetch_recommendation)
        )
    }

    private fun setupToolbar() {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .setTitle(getString(R.string.lbl_recommendations))
            .onNavigationClick { finish() }
            .build()
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            viewModel.fetchRecommendation(
                useCase = useCase,
                noRecsLabel = getString(R.string.lbl_no_recommendations_prompt),
                errorLabel = getString(R.string.error_fetch_recommendation)
            )
        }
        binding.fabFeedback.setOnClickListener {
            showFeedbackBottomSheet()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is GetRecommendationViewModel.UiState.Loading -> showLoading()
                        is GetRecommendationViewModel.UiState.Success -> {
                            val title = when (state.title) {
                                "FR" -> getString(R.string.lbl_fertilizer_rec)
                                "IC" -> getString(R.string.lbl_intercrop_rec)
                                "PP" -> getString(R.string.lbl_planting_practices_rec)
                                "SP" -> getString(R.string.lbl_scheduled_planting_rec)
                                else -> getString(R.string.lbl_no_recommendations_prompt)
                            }
                            showRecommendation(title, state.description)
                        }
                        is GetRecommendationViewModel.UiState.Error -> showError(state.message)
                        is GetRecommendationViewModel.UiState.Idle -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.feedbackResult.collect { success ->
                    val msg = if (success) getString(R.string.feedback_success)
                    else getString(R.string.feedback_error)
                    Toast.makeText(this@GetRecommendationActivity, msg, Toast.LENGTH_SHORT).show()
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
                updateButtonSelection(ratingButtons, button)
                sheetBinding.selectedRating.apply {
                    visibility = View.VISIBLE
                    text = getRatingLabel(rating)
                }
                updateSubmitButton(sheetBinding, selectedRating, selectedNps)
            }
        }

        val npsButtons = listOf(
            sheetBinding.btnNps0 to 0, sheetBinding.btnNps1 to 1, sheetBinding.btnNps2 to 2,
            sheetBinding.btnNps3 to 3, sheetBinding.btnNps4 to 4, sheetBinding.btnNps5 to 5,
            sheetBinding.btnNps6 to 6, sheetBinding.btnNps7 to 7, sheetBinding.btnNps8 to 8,
            sheetBinding.btnNps9 to 9, sheetBinding.btnNps10 to 10
        )

        npsButtons.forEach { (button, score) ->
            button.setOnClickListener {
                selectedNps = score
                updateButtonSelection(npsButtons, button)
                sheetBinding.selectedNps.apply {
                    visibility = View.VISIBLE
                    text = getNpsLabel(score)
                }
                updateSubmitButton(sheetBinding, selectedRating, selectedNps)
            }
        }

        sheetBinding.btnSubmitFeedback.setOnClickListener {
            val rating = selectedRating
            val nps = selectedNps
            if (rating != null && nps != null) {
                bottomSheetDialog.dismiss()
                viewModel.submitFeedback(useCase, rating, nps)
            }
        }

        bottomSheetDialog.show()
    }

    private fun getRatingLabel(rating: Int) = when (rating) {
        1 -> getString(R.string.feedback_very_poor)
        2 -> getString(R.string.feedback_poor)
        3 -> getString(R.string.feedback_okay)
        4 -> getString(R.string.feedback_good)
        5 -> getString(R.string.feedback_excellent)
        else -> ""
    }

    private fun getNpsLabel(score: Int) = when {
        score <= 6 -> getString(R.string.feedback_nps_low)
        score <= 8 -> getString(R.string.feedback_nps_medium)
        else -> getString(R.string.feedback_nps_high)
    }

    private fun updateButtonSelection(buttons: List<Pair<MaterialButton, Int>>, selected: MaterialButton) {
        buttons.forEach { (button, _) -> button.isSelected = button == selected }
    }

    private fun updateSubmitButton(binding: BottomSheetFeedbackBinding, rating: Int?, nps: Int?) {
        binding.btnSubmitFeedback.isEnabled = rating != null && nps != null
    }
}
