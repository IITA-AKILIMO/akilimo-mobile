package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
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
                viewModel.saveCosts(
                    etRidingCost.text?.toString()?.toDoubleOrNull(),
                    etPloughingCost.text?.toString()?.toDoubleOrNull()
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
                        tvManualPlough.text = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_tillage_cost, state.farmSize, sizeUnitLabel) } }
                        tilPloughingCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_tillage_cost_hint, state.farmSize, sizeUnitLabel) } }
                        tvManualRidge.text = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_ridge_cost, state.farmSize, sizeUnitLabel) } }
                        tilRidgingCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_manual_ridge_cost_hint, state.farmSize, sizeUnitLabel) } }
                        if (state.manualRidgeCost != null && etRidingCost.text.isNullOrEmpty()) etRidingCost.setText(state.manualRidgeCost.toString())
                        if (state.manualPloughCost != null && etPloughingCost.text.isNullOrEmpty()) etPloughingCost.setText(state.manualPloughCost.toString())
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
        val plough = parse(binding.etPloughingCost.text)
        val ridge = parse(binding.etRidingCost.text)
        return (plough != null && plough != (state.manualPloughCost ?: 0.0)) ||
               (ridge != null && ridge != (state.manualRidgeCost ?: 0.0))
    }

    private fun toggleFab() {
        val show = viewModel.uiState.value.let { it.userId != 0 && (it.manualPloughCost == null || hasFormChanged()) }
        binding.lytFabButton.fabSave.apply { if (show) show() else hide() }
    }
}
