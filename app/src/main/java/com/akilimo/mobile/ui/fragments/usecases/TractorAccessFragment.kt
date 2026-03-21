package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.viewmodels.TractorAccessViewModel
import com.akilimo.mobile.utils.StringHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TractorAccessFragment : BaseFragment<ActivityTractorAccessBinding>() {

    private val viewModel: TractorAccessViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityTractorAccessBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupTractorAccessToggle()
        setupImplementToggle()

        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                viewModel.saveCosts(
                    isTractorAvailable(),
                    etTractorRidgeCost.text?.toString()?.toDoubleOrNull(),
                    etTractorPloughCost.text?.toString()?.toDoubleOrNull(),
                    etTractorHarrowCost.text?.toString()?.toDoubleOrNull()
                )
            }
            etTractorPloughCost.addTextChangedListener { toggleFab() }
            etTractorRidgeCost.addTextChangedListener { toggleFab() }
            etTractorHarrowCost.addTextChangedListener { toggleFab() }
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
                        tilTractorPloughCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_tractor_plough_cost_hint, state.farmSize, sizeUnitLabel) } }
                        tilTractorRidgeCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_tractor_ridge_cost_hint, state.farmSize, sizeUnitLabel) } }
                        tilTractorHarrowCost.hint = with(requireContext()) { StringHelper.run { formatWithLandSize(R.string.lbl_tractor_harrow_cost_hint, state.farmSize, sizeUnitLabel) } }
                        if (etTractorRidgeCost.text.isNullOrEmpty()) etTractorRidgeCost.setText(state.tractorRidgeCost.toString())
                        if (etTractorPloughCost.text.isNullOrEmpty()) etTractorPloughCost.setText(state.tractorPloughCost.toString())
                        if (etTractorHarrowCost.text.isNullOrEmpty()) etTractorHarrowCost.setText(state.tractorHarrowCost.toString())
                        if (state.tractorPloughCost > 0.0) { toggleGroupImplements.check(R.id.btn_impl_plough); tilTractorPloughCost.visibility = View.VISIBLE }
                        if (state.tractorRidgeCost > 0.0) { toggleGroupImplements.check(R.id.btn_impl_ridge); tilTractorRidgeCost.visibility = View.VISIBLE }
                        if (state.tractorHarrowCost > 0.0) { toggleGroupImplements.check(R.id.btn_impl_harrow); tilTractorHarrowCost.visibility = View.VISIBLE }
                        toggleTractorAccess.check(if (state.tractorAvailable) R.id.btn_yes else R.id.btn_no)
                    }
                    toggleFab()
                }
            }
        }
    }

    private fun setupTractorAccessToggle() = with(binding) {
        toggleTractorAccess.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_yes -> { tvImplementsHeader.visibility = View.VISIBLE; toggleGroupImplements.visibility = View.VISIBLE }
                    R.id.btn_no -> {
                        tvImplementsHeader.visibility = View.GONE; toggleGroupImplements.visibility = View.GONE
                        toggleGroupImplements.clearChecked()
                        toggleField(false, tilTractorPloughCost, etTractorPloughCost)
                        toggleField(false, tilTractorRidgeCost, etTractorRidgeCost)
                        toggleField(false, tilTractorHarrowCost, etTractorHarrowCost)
                    }
                }
                toggleFab()
            }
        }
    }

    private fun setupImplementToggle() = with(binding) {
        toggleGroupImplements.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.btn_impl_plough -> toggleField(isChecked, tilTractorPloughCost, etTractorPloughCost)
                R.id.btn_impl_ridge -> toggleField(isChecked, tilTractorRidgeCost, etTractorRidgeCost)
                R.id.btn_impl_harrow -> toggleField(isChecked, tilTractorHarrowCost, etTractorHarrowCost)
            }
            toggleFab()
        }
    }

    private fun isTractorAvailable() = binding.toggleTractorAccess.checkedButtonId == R.id.btn_yes

    private fun hasFormChanged(): Boolean {
        val state = viewModel.uiState.value
        if (state.userId == 0) return false
        fun parse(text: CharSequence?) = text?.toString()?.toDoubleOrNull()?.takeIf { it >= 0 }
        val checks = binding.toggleGroupImplements.checkedButtonIds
        return (parse(binding.etTractorPloughCost.text)?.let { it != state.tractorPloughCost } ?: false) ||
               (parse(binding.etTractorRidgeCost.text)?.let { it != state.tractorRidgeCost } ?: false) ||
               (parse(binding.etTractorHarrowCost.text)?.let { it != state.tractorHarrowCost } ?: false) ||
               isTractorAvailable() != state.tractorAvailable ||
               checks.contains(R.id.btn_impl_plough) != (state.tractorPloughCost > 0.0) ||
               checks.contains(R.id.btn_impl_ridge) != (state.tractorRidgeCost > 0.0) ||
               checks.contains(R.id.btn_impl_harrow) != (state.tractorHarrowCost > 0.0)
    }

    private fun toggleField(isChecked: Boolean, layout: View, editText: EditText) {
        layout.visibility = if (isChecked) View.VISIBLE else View.GONE
        if (!isChecked) editText.text = null
    }

    private fun toggleFab() {
        val show = viewModel.uiState.value.let { it.userId != 0 && (!it.tractorAvailable || hasFormChanged()) }
        binding.lytFabButton.fabSave.apply { if (show) show() else hide() }
    }
}
