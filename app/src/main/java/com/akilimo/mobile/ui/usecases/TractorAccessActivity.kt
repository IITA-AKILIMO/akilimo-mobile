package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.StringHelper.formatWithLandSize
import kotlinx.coroutines.launch

class TractorAccessActivity : BaseActivity<ActivityTractorAccessBinding>() {
    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var fieldOperationCostsRepo: FieldOperationCostsRepo


    override fun inflateBinding() = ActivityTractorAccessBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        fieldOperationCostsRepo = FieldOperationCostsRepo(database.fieldOperationCostsDao())

        setupTractorAccessToggle()
        setupImplementToggle()
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch

            val sizeUnitLabel = user.enumAreaUnit?.label(this@TractorAccessActivity).orEmpty()
            val landSizePloughHint = formatWithLandSize(
                R.string.lbl_tractor_plough_cost_hint, user.farmSize, sizeUnitLabel
            )
            val landSizeRidgeHint = formatWithLandSize(
                R.string.lbl_tractor_ridge_cost_hint, user.farmSize, sizeUnitLabel
            )
            val landSizeHarrowHint = formatWithLandSize(
                R.string.lbl_tractor_harrow_cost_hint, user.farmSize, sizeUnitLabel
            )

            binding.apply {
                tilTractorPloughCost.hint = landSizePloughHint
                tilTractorRidgeCost.hint = landSizeRidgeHint
                tilTractorHarrowCost.hint = landSizeHarrowHint
            }
            fieldOperationCostsRepo.getCostForUser(user.id ?: 0)?.let { cost ->
                binding.etTractorRidgeCost.setText(cost.tractorRidgeCost.toString())
                binding.etTractorPloughCost.setText(cost.tractorPloughCost.toString())
                binding.etTractorHarrowCost.setText(cost.tractorHarrowCost.toString())

                if (cost.tractorPloughCost > 0.0) {
                    binding.toggleGroupImplements.check(R.id.btn_impl_plough)
                    binding.tilTractorPloughCost.visibility = View.VISIBLE
                }
                if (cost.tractorRidgeCost > 0.0) {
                    binding.toggleGroupImplements.check(R.id.btn_impl_ridge)
                    binding.tilTractorRidgeCost.visibility = View.VISIBLE
                }
                if (cost.tractorHarrowCost > 0.0) {
                    binding.toggleGroupImplements.check(R.id.btn_impl_harrow)
                    binding.tilTractorHarrowCost.visibility = View.VISIBLE
                }

                binding.toggleTractorAccess.check(
                    if (cost.tractorAvailable) R.id.btn_yes else R.id.btn_no
                )
            }

        }
        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                val ridingCost = binding.etTractorRidgeCost.text?.toString()?.toDoubleOrNull()
                val ploughingCost = binding.etTractorPloughCost.text?.toString()?.toDoubleOrNull()
                val harrowCost = binding.etTractorHarrowCost.text?.toString()?.toDoubleOrNull()
                saveTractorOperationCosts(ridingCost, ploughingCost, harrowCost)
            }
            etTractorPloughCost.addTextChangedListener { toggleFab() }
            etTractorRidgeCost.addTextChangedListener { toggleFab() }
            etTractorHarrowCost.addTextChangedListener { toggleFab() }
            lytFabButton.fabSave.hide()
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
                R.id.btn_impl_plough -> toggleField(
                    isChecked, tilTractorPloughCost, etTractorPloughCost
                )

                R.id.btn_impl_ridge -> toggleField(
                    isChecked, tilTractorRidgeCost, etTractorRidgeCost
                )

                R.id.btn_impl_harrow -> toggleField(
                    isChecked, tilTractorHarrowCost, etTractorHarrowCost
                )
            }
            toggleFab()
        }
    }


    private fun saveTractorOperationCosts(
        ridingCost: Double?, ploughingCost: Double?, harrowingCost: Double?
    ) {
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: return@launch
            val tractorAvailable = isTractorAvailable()
            val newCosts = FieldOperationCost(
                userId = userId,
                tractorAvailable = tractorAvailable,
                tractorRidgeCost = if (tractorAvailable) (ridingCost ?: 0.0) else 0.0,
                tractorPloughCost = if (tractorAvailable) (ploughingCost ?: 0.0) else 0.0,
                tractorHarrowCost = if (tractorAvailable) (harrowingCost ?: 0.0) else 0.0
            )
            val existing = fieldOperationCostsRepo.getCostForUser(userId)
            val merged = existing?.copy(
                tractorAvailable = newCosts.tractorAvailable,
                tractorRidgeCost = if (ridingCost != null) newCosts.tractorRidgeCost else existing.tractorRidgeCost,
                tractorPloughCost = if (ploughingCost != null) newCosts.tractorPloughCost else existing.tractorPloughCost,
                tractorHarrowCost = if (harrowingCost != null) newCosts.tractorHarrowCost else existing.tractorHarrowCost
            ) ?: newCosts

            fieldOperationCostsRepo.saveCost(cost = merged)
            val completion =
                AdviceCompletionDto(EnumAdviceTask.MANUAL_TILLAGE_COST, EnumStepStatus.COMPLETED)
            setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
            finish()
        }
    }

    private fun isTractorAvailable(): Boolean {
        return binding.toggleTractorAccess.checkedButtonId == R.id.btn_yes
    }


    private fun hasFormChanged(cost: FieldOperationCost) = with(binding) {
        fun parseNonNegativeDouble(text: CharSequence?): Double? {
            val value = text?.toString()?.toDoubleOrNull()
            return if (value != null && value >= 0) value else null
        }

        val ploughInput = parseNonNegativeDouble(etTractorPloughCost.text)
        val ridgeInput = parseNonNegativeDouble(etTractorRidgeCost.text)
        val harrowInput = parseNonNegativeDouble(etTractorHarrowCost.text)

        val ploughChanged = ploughInput != null && ploughInput != cost.tractorPloughCost
        val ridgeChanged = ridgeInput != null && ridgeInput != cost.tractorRidgeCost
        val harrowChanged = harrowInput != null && harrowInput != cost.tractorHarrowCost
        val tractorAvailableChanged = isTractorAvailable() != cost.tractorAvailable

        val ploughToggleChanged =
            toggleGroupImplements.checkedButtonIds.contains(R.id.btn_impl_plough) != (cost.tractorPloughCost > 0.0)
        val ridgeToggleChanged =
            toggleGroupImplements.checkedButtonIds.contains(R.id.btn_impl_ridge) != (cost.tractorRidgeCost > 0.0)
        val harrowToggleChanged =
            toggleGroupImplements.checkedButtonIds.contains(R.id.btn_impl_harrow) != (cost.tractorHarrowCost > 0.0)

        ploughChanged || ridgeChanged || harrowChanged || tractorAvailableChanged || ploughToggleChanged || ridgeToggleChanged || harrowToggleChanged
    }

    private fun toggleField(
        isChecked: Boolean, textInputLayout: View, editText: android.widget.EditText
    ) {
        if (isChecked) {
            textInputLayout.visibility = View.VISIBLE
        } else {
            textInputLayout.visibility = View.GONE
            editText.text = null // clear value when hidden
        }
    }


    private fun toggleFab() = safeScope.launch {
        userRepo.getUser(sessionManager.akilimoUser)?.let { user ->
            val operationCost = fieldOperationCostsRepo.getCostForUser(user.id ?: 0)
            val shouldShowFab = when {
                operationCost == null -> true
                else -> hasFormChanged(operationCost)
            }
            binding.lytFabButton.fabSave.apply {
                takeIf { shouldShowFab }?.show() ?: hide()
            }
        }
    }
}
