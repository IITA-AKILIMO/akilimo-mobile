package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.ManualTillageCostViewModel
import com.akilimo.mobile.utils.StringHelper.formatWithLandSize
import kotlinx.coroutines.launch

class ManualTillageCostActivity : BaseActivity<ActivityManualTillageCostBinding>() {

    private val viewModel: ManualTillageCostViewModel by lazy {
        ViewModelProvider(
            this,
            ManualTillageCostViewModel.factory(database)
        )[ManualTillageCostViewModel::class.java]
    }

    override fun inflateBinding() = ActivityManualTillageCostBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                val ridingCost = etRidingCost.text?.toString()?.toDoubleOrNull()
                val ploughingCost = etPloughingCost.text?.toString()?.toDoubleOrNull()
                viewModel.saveCosts(ridingCost, ploughingCost)
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
                        val completion = AdviceCompletionDto(
                            EnumAdviceTask.MANUAL_TILLAGE_COST, EnumStepStatus.COMPLETED
                        )
                        setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
                        viewModel.onSaveHandled()
                        finish()
                        return@collect
                    }

                    if (state.userId == 0) return@collect

                    val sizeUnitLabel = state.enumAreaUnit.label(this@ManualTillageCostActivity).orEmpty()
                    binding.apply {
                        tvManualPlough.text = formatWithLandSize(
                            R.string.lbl_manual_tillage_cost, state.farmSize, sizeUnitLabel
                        )
                        tilPloughingCost.hint = formatWithLandSize(
                            R.string.lbl_manual_tillage_cost_hint, state.farmSize, sizeUnitLabel
                        )
                        tvManualRidge.text = formatWithLandSize(
                            R.string.lbl_manual_ridge_cost, state.farmSize, sizeUnitLabel
                        )
                        tilRidgingCost.hint = formatWithLandSize(
                            R.string.lbl_manual_ridge_cost_hint, state.farmSize, sizeUnitLabel
                        )

                        if (state.manualRidgeCost != null && etRidingCost.text.isNullOrEmpty()) {
                            etRidingCost.setText(state.manualRidgeCost.toString())
                        }
                        if (state.manualPloughCost != null && etPloughingCost.text.isNullOrEmpty()) {
                            etPloughingCost.setText(state.manualPloughCost.toString())
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

        fun parseNonNegativeDouble(text: CharSequence?): Double? {
            val value = text?.toString()?.toDoubleOrNull()
            return if (value != null && value >= 0) value else null
        }

        val ploughInput = parseNonNegativeDouble(binding.etPloughingCost.text)
        val ridgeInput = parseNonNegativeDouble(binding.etRidingCost.text)

        val ploughChanged = ploughInput != null && ploughInput != (state.manualPloughCost ?: 0.0)
        val ridgeChanged = ridgeInput != null && ridgeInput != (state.manualRidgeCost ?: 0.0)

        return ploughChanged || ridgeChanged
    }

    private fun toggleFab() {
        val shouldShow = viewModel.uiState.value.let { it.userId != 0 && (it.manualPloughCost == null || hasFormChanged()) }
        binding.lytFabButton.fabSave.apply {
            if (shouldShow) show() else hide()
        }
    }
}
