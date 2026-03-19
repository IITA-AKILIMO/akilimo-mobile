package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.BaseValueOptionAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityWeedControlCostsBinding
import com.akilimo.mobile.dto.WeedControlOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.WeedControlCostsViewModel
import com.akilimo.mobile.utils.StringHelper.formatWithLandSize
import kotlinx.coroutines.launch

class WeedControlCostsActivity : BaseActivity<ActivityWeedControlCostsBinding>() {

    private val viewModel: WeedControlCostsViewModel by lazy {
        ViewModelProvider(
            this,
            WeedControlCostsViewModel.factory(database)
        )[WeedControlCostsViewModel::class.java]
    }

    private val weedControlOptions = mutableListOf<WeedControlOption>()

    override fun inflateBinding() = ActivityWeedControlCostsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        weedControlOptions.add(WeedControlOption(EnumWeedControlMethod.MANUAL))
        weedControlOptions.add(WeedControlOption(EnumWeedControlMethod.HERBICIDE))
        weedControlOptions.add(WeedControlOption(EnumWeedControlMethod.HERBICIDE_AND_MANUAL))

        val areaUnitAdapter = BaseValueOptionAdapter(
            this@WeedControlCostsActivity,
            weedControlOptions,
            getDisplayText = { option -> option.label(this@WeedControlCostsActivity) })

        binding.apply {
            dropWeedControl.setAdapter(areaUnitAdapter)
            dropWeedControl.setOnItemClickListener { _, _, position, _ ->
                val selected = weedControlOptions.getOrNull(position) ?: return@setOnItemClickListener
                dropWeedControl.setText(
                    selected.valueOption.label(this@WeedControlCostsActivity), false
                )
            }
            lytFabButton.fabSave.setOnClickListener {
                val firstCost = etFirstWeedingCost.text?.toString()?.toDoubleOrNull()
                val secondCost = etSecondWeedingCost.text?.toString()?.toDoubleOrNull()
                viewModel.saveCosts(firstCost, secondCost)
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
                        val completion = AdviceCompletionDto(
                            EnumAdviceTask.COST_OF_WEED_CONTROL, EnumStepStatus.COMPLETED
                        )
                        setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
                        viewModel.onSaveHandled()
                        finish()
                        return@collect
                    }

                    if (state.userId == 0) return@collect

                    val sizeUnitLabel = state.enumAreaUnit.label(this@WeedControlCostsActivity).orEmpty()
                    binding.apply {
                        tilFirstWeedingCost.helperText = formatWithLandSize(
                            R.string.lbl_cost_of_first_weeding_operation, state.farmSize, sizeUnitLabel
                        )
                        tilSecondWeedingCost.helperText = formatWithLandSize(
                            R.string.lbl_cost_of_second_weeding_operation, state.farmSize, sizeUnitLabel
                        )

                        if (etFirstWeedingCost.text.isNullOrEmpty()) {
                            etFirstWeedingCost.setText(state.firstWeedingCost.toString())
                        }
                        if (etSecondWeedingCost.text.isNullOrEmpty()) {
                            etSecondWeedingCost.setText(state.secondWeedingCost.toString())
                        }

                        state.weedControlMethod?.let { method ->
                            dropWeedControl.setText(method.label(this@WeedControlCostsActivity), false)
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

        val firstWeeding = parseNonNegativeDouble(binding.etFirstWeedingCost.text)
        val secondWeeding = parseNonNegativeDouble(binding.etSecondWeedingCost.text)

        val firstCostChanged = firstWeeding != null && firstWeeding != state.firstWeedingCost
        val secondCostChanged = secondWeeding != null && secondWeeding != state.secondWeedingCost

        return firstCostChanged || secondCostChanged
    }

    private fun toggleFab() {
        val state = viewModel.uiState.value
        val shouldShow = state.userId != 0 && (state.firstWeedingCost == 0.0 || hasFormChanged())
        binding.lytFabButton.fabSave.apply {
            if (shouldShow) show() else hide()
        }
    }
}
