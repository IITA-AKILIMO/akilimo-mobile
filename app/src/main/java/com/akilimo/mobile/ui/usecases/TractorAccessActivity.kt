package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.TractorAccessViewModel
import com.akilimo.mobile.utils.StringHelper.formatWithLandSize
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TractorAccessActivity : BaseActivity<ActivityTractorAccessBinding>() {

    private val viewModel: TractorAccessViewModel by viewModels()

    override fun inflateBinding() = ActivityTractorAccessBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        setupTractorAccessToggle()
        setupImplementToggle()

        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                val ridingCost = etTractorRidgeCost.text?.toString()?.toDoubleOrNull()
                val ploughingCost = etTractorPloughCost.text?.toString()?.toDoubleOrNull()
                val harrowCost = etTractorHarrowCost.text?.toString()?.toDoubleOrNull()
                viewModel.saveCosts(isTractorAvailable(), ridingCost, ploughingCost, harrowCost)
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
                        val completion = AdviceCompletionDto(
                            EnumAdviceTask.MANUAL_TILLAGE_COST, EnumStepStatus.COMPLETED
                        )
                        setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
                        viewModel.onSaveHandled()
                        finish()
                        return@collect
                    }

                    if (state.userId == 0) return@collect

                    val sizeUnitLabel = state.enumAreaUnit.label(this@TractorAccessActivity).orEmpty()
                    binding.apply {
                        tilTractorPloughCost.hint = formatWithLandSize(
                            R.string.lbl_tractor_plough_cost_hint, state.farmSize, sizeUnitLabel
                        )
                        tilTractorRidgeCost.hint = formatWithLandSize(
                            R.string.lbl_tractor_ridge_cost_hint, state.farmSize, sizeUnitLabel
                        )
                        tilTractorHarrowCost.hint = formatWithLandSize(
                            R.string.lbl_tractor_harrow_cost_hint, state.farmSize, sizeUnitLabel
                        )

                        if (etTractorRidgeCost.text.isNullOrEmpty()) {
                            etTractorRidgeCost.setText(state.tractorRidgeCost.toString())
                        }
                        if (etTractorPloughCost.text.isNullOrEmpty()) {
                            etTractorPloughCost.setText(state.tractorPloughCost.toString())
                        }
                        if (etTractorHarrowCost.text.isNullOrEmpty()) {
                            etTractorHarrowCost.setText(state.tractorHarrowCost.toString())
                        }

                        if (state.tractorPloughCost > 0.0) {
                            toggleGroupImplements.check(R.id.btn_impl_plough)
                            tilTractorPloughCost.visibility = View.VISIBLE
                        }
                        if (state.tractorRidgeCost > 0.0) {
                            toggleGroupImplements.check(R.id.btn_impl_ridge)
                            tilTractorRidgeCost.visibility = View.VISIBLE
                        }
                        if (state.tractorHarrowCost > 0.0) {
                            toggleGroupImplements.check(R.id.btn_impl_harrow)
                            tilTractorHarrowCost.visibility = View.VISIBLE
                        }

                        toggleTractorAccess.check(
                            if (state.tractorAvailable) R.id.btn_yes else R.id.btn_no
                        )
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
                    R.id.btn_yes -> {
                        tvImplementsHeader.visibility = View.VISIBLE
                        toggleGroupImplements.visibility = View.VISIBLE
                    }
                    R.id.btn_no -> {
                        tvImplementsHeader.visibility = View.GONE
                        toggleGroupImplements.visibility = View.GONE
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

    private fun isTractorAvailable(): Boolean =
        binding.toggleTractorAccess.checkedButtonId == R.id.btn_yes

    private fun hasFormChanged(): Boolean {
        val state = viewModel.uiState.value
        if (state.userId == 0) return false

        fun parseNonNegativeDouble(text: CharSequence?): Double? {
            val value = text?.toString()?.toDoubleOrNull()
            return if (value != null && value >= 0) value else null
        }

        val ploughInput = parseNonNegativeDouble(binding.etTractorPloughCost.text)
        val ridgeInput = parseNonNegativeDouble(binding.etTractorRidgeCost.text)
        val harrowInput = parseNonNegativeDouble(binding.etTractorHarrowCost.text)

        val ploughChanged = ploughInput != null && ploughInput != state.tractorPloughCost
        val ridgeChanged = ridgeInput != null && ridgeInput != state.tractorRidgeCost
        val harrowChanged = harrowInput != null && harrowInput != state.tractorHarrowCost
        val tractorAvailableChanged = isTractorAvailable() != state.tractorAvailable

        val ploughToggleChanged =
            binding.toggleGroupImplements.checkedButtonIds.contains(R.id.btn_impl_plough) != (state.tractorPloughCost > 0.0)
        val ridgeToggleChanged =
            binding.toggleGroupImplements.checkedButtonIds.contains(R.id.btn_impl_ridge) != (state.tractorRidgeCost > 0.0)
        val harrowToggleChanged =
            binding.toggleGroupImplements.checkedButtonIds.contains(R.id.btn_impl_harrow) != (state.tractorHarrowCost > 0.0)

        return ploughChanged || ridgeChanged || harrowChanged || tractorAvailableChanged ||
                ploughToggleChanged || ridgeToggleChanged || harrowToggleChanged
    }

    private fun toggleField(
        isChecked: Boolean, textInputLayout: View, editText: android.widget.EditText
    ) {
        if (isChecked) {
            textInputLayout.visibility = View.VISIBLE
        } else {
            textInputLayout.visibility = View.GONE
            editText.text = null
        }
    }

    private fun toggleFab() {
        val shouldShow = viewModel.uiState.value.let { it.userId != 0 && (!it.tractorAvailable || hasFormChanged()) }
        binding.lytFabButton.fabSave.apply {
            if (shouldShow) show() else hide()
        }
    }
}
