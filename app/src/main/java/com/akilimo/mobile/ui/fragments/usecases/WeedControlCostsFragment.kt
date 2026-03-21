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
import com.akilimo.mobile.adapters.BaseValueOptionAdapter
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.ActivityWeedControlCostsBinding
import com.akilimo.mobile.dto.WeedControlOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.ui.viewmodels.WeedControlCostsViewModel
import com.akilimo.mobile.utils.StringHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.akilimo.mobile.R

@AndroidEntryPoint
class WeedControlCostsFragment : BaseFragment<ActivityWeedControlCostsBinding>() {

    private val viewModel: WeedControlCostsViewModel by viewModels()
    private val weedControlOptions = mutableListOf<WeedControlOption>()
    private var selectedWeedControlMethod: EnumWeedControlMethod? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityWeedControlCostsBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        weedControlOptions.addAll(listOf(
            WeedControlOption(EnumWeedControlMethod.MANUAL),
            WeedControlOption(EnumWeedControlMethod.HERBICIDE),
            WeedControlOption(EnumWeedControlMethod.HERBICIDE_AND_MANUAL)
        ))

        val adapter = BaseValueOptionAdapter(
            requireContext(), weedControlOptions,
            getDisplayText = { it.label(requireContext()) }
        )

        binding.apply {
            dropWeedControl.setAdapter(adapter)
            dropWeedControl.setOnItemClickListener { _, _, position, _ ->
                val selected = weedControlOptions.getOrNull(position) ?: return@setOnItemClickListener
                selectedWeedControlMethod = selected.valueOption
                dropWeedControl.setText(selected.valueOption.label(requireContext()), false)
            }
            lytFabButton.fabSave.setOnClickListener {
                val firstCost = etFirstWeedingCost.text?.toString()?.toDoubleOrNull()
                val secondCost = etSecondWeedingCost.text?.toString()?.toDoubleOrNull()
                viewModel.saveCosts(firstCost, secondCost, selectedWeedControlMethod)
            }
            etFirstWeedingCost.addTextChangedListener { toggleFab() }
            etSecondWeedingCost.addTextChangedListener { toggleFab() }
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
                        val completion = AdviceCompletionDto(EnumAdviceTask.COST_OF_WEED_CONTROL, EnumStepStatus.COMPLETED)
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
                        tilFirstWeedingCost.helperText = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_cost_of_first_weeding_operation, state.farmSize, sizeUnitLabel) } }
                        tilSecondWeedingCost.helperText = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_cost_of_second_weeding_operation, state.farmSize, sizeUnitLabel) } }
                        if (etFirstWeedingCost.text.isNullOrEmpty()) etFirstWeedingCost.setText(state.firstWeedingCost.toString())
                        if (etSecondWeedingCost.text.isNullOrEmpty()) etSecondWeedingCost.setText(state.secondWeedingCost.toString())
                        state.weedControlMethod?.let {
                        if (selectedWeedControlMethod == null) selectedWeedControlMethod = it
                        dropWeedControl.setText(it.label(requireContext()), false)
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
        val first = parse(binding.etFirstWeedingCost.text)
        val second = parse(binding.etSecondWeedingCost.text)
        return (first != null && first != state.firstWeedingCost) || (second != null && second != state.secondWeedingCost)
    }

    private fun toggleFab() {
        val state = viewModel.uiState.value
        val show = state.userId != 0 && (state.firstWeedingCost == 0.0 || hasFormChanged())
        binding.lytFabButton.fabSave.apply { if (show) show() else hide() }
    }
}
