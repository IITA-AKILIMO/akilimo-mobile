package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.viewmodels.ManualTillageCostViewModel
import com.akilimo.mobile.utils.StringHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManualTillageCostFragment : BaseFragment<ActivityManualTillageCostBinding>() {

    private val viewModel: ManualTillageCostViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityManualTillageCostBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                val state = viewModel.uiState.value
                viewModel.saveCosts(
                    if (state.performRidging) etRidingCost.text?.toString()?.toDoubleOrNull() else null,
                    if (state.performPloughing) etPloughingCost.text?.toString()?.toDoubleOrNull() else null
                )
            }
            etPloughingCost.addTextChangedListener { toggleFab() }
            etRidingCost.addTextChangedListener { toggleFab() }
            lytFabButton.fabSave.hide()
        }

        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.saved) {
                        val completion = AdviceCompletionDto(EnumAdviceTask.MANUAL_TILLAGE_COST, EnumStepStatus.COMPLETED)
                        parentFragmentManager.setFragmentResult(
                            UseCaseResults.ADVICE_COMPLETION,
                            bundleOf(UseCaseResults.ADVICE_COMPLETION_DTO to completion)
                        )
                        viewModel.onSaveHandled()
                        findNavController().popBackStack()
                        return@collect
                    }

                    if (state.userId == 0) return@collect

                    val sizeUnitLabel = state.enumAreaUnit.label(requireContext()).orEmpty()
                    binding.apply {
                        tvManualRidge.isVisible = state.performRidging
                        tilRidgingCost.isVisible = state.performRidging
                        tvManualPlough.isVisible = state.performPloughing
                        tilPloughingCost.isVisible = state.performPloughing

                        if (state.performPloughing) {
                            tvManualPlough.text = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_tillage_cost, state.farmSize, sizeUnitLabel) } }
                            tilPloughingCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_tillage_cost_hint, state.farmSize, sizeUnitLabel) } }
                            if (state.manualPloughCost != null && etPloughingCost.text.isNullOrEmpty()) etPloughingCost.setText(state.manualPloughCost.toString())
                        }
                        if (state.performRidging) {
                            tvManualRidge.text = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_ridge_cost, state.farmSize, sizeUnitLabel) } }
                            tilRidgingCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_ridge_cost_hint, state.farmSize, sizeUnitLabel) } }
                            if (state.manualRidgeCost != null && etRidingCost.text.isNullOrEmpty()) etRidingCost.setText(state.manualRidgeCost.toString())
                        }
                    }
                    toggleFab()
                }
            }
        }
    }

    private fun hasFormChanged(): Boolean {
        val state = viewModel.uiState.value
        if (state.userId == 0) return false
        fun parse(text: CharSequence?) = text?.toString()?.toDoubleOrNull()?.takeIf { it >= 0 }
        val ploughChanged = state.performPloughing && parse(binding.etPloughingCost.text).let { it != null && it != (state.manualPloughCost ?: 0.0) }
        val ridgeChanged = state.performRidging && parse(binding.etRidingCost.text).let { it != null && it != (state.manualRidgeCost ?: 0.0) }
        return ploughChanged || ridgeChanged
    }

    private fun toggleFab() {
        val state = viewModel.uiState.value
        val costMissing = (state.performPloughing && state.manualPloughCost == null) ||
                          (state.performRidging && state.manualRidgeCost == null)
        val show = state.userId != 0 && (costMissing || hasFormChanged())
        binding.lytFabButton.fabSave.apply { if (show) show() else hide() }
    }
}
