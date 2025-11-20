package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.StringHelper.formatWithLandSize
import kotlinx.coroutines.launch

class ManualTillageCostActivity : BaseActivity<ActivityManualTillageCostBinding>() {

    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var fieldOperationCostsRepo: FieldOperationCostsRepo

    override fun inflateBinding() = ActivityManualTillageCostBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        fieldOperationCostsRepo = FieldOperationCostsRepo(database.fieldOperationCostsDao())

        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch

            val sizeUnitLabel = user.enumAreaUnit?.label(this@ManualTillageCostActivity).orEmpty()

            val landPloughText = formatWithLandSize(
                R.string.lbl_manual_tillage_cost,
                user.farmSize,
                sizeUnitLabel
            )
            val landSizePloughHint = formatWithLandSize(
                R.string.lbl_manual_tillage_cost_hint,
                user.farmSize,
                sizeUnitLabel
            )
            val landRidgeText = formatWithLandSize(
                R.string.lbl_manual_ridge_cost,
                user.farmSize,
                sizeUnitLabel
            )
            val landSizeRidgeHint = formatWithLandSize(
                R.string.lbl_manual_ridge_cost_hint,
                user.farmSize,
                sizeUnitLabel
            )

            binding.apply {
                tvManualPlough.text = landPloughText
                tilPloughingCost.hint = landSizePloughHint
                tvManualRidge.text = landRidgeText
                tilRidgingCost.hint = landSizeRidgeHint
            }
            fieldOperationCostsRepo.getCostForUser(user.id ?: 0)?.let {
                binding.etRidingCost.setText(it.manualRidgeCost.toString())
                binding.etPloughingCost.setText(it.manualPloughCost.toString())
            }

        }
        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                val ridingCost = binding.etRidingCost.text?.toString()?.toDoubleOrNull()
                val ploughingCost = binding.etPloughingCost.text?.toString()?.toDoubleOrNull()
                saveManualTillageCosts(ridingCost, ploughingCost)
            }
            etPloughingCost.addTextChangedListener { toggleFab() }
            etRidingCost.addTextChangedListener { toggleFab() }
            lytFabButton.fabSave.hide()
        }
    }

    private fun saveManualTillageCosts(
        ridingCost: Double?,
        ploughingCost: Double?,
    ) {
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: return@launch
            val newCosts = FieldOperationCost(
                userId = userId,
                manualRidgeCost = ridingCost ?: 0.0,
                manualPloughCost = ploughingCost ?: 0.0
            )
            val existing = fieldOperationCostsRepo.getCostForUser(userId)
            val merged = existing?.copy(
                manualRidgeCost = if (ridingCost != null) newCosts.manualRidgeCost else existing.manualRidgeCost,
                manualPloughCost = if (ploughingCost != null) newCosts.manualPloughCost else existing.manualPloughCost,
            ) ?: newCosts
            fieldOperationCostsRepo.saveCost(cost = merged)
            val completion =
                AdviceCompletionDto(EnumAdviceTask.MANUAL_TILLAGE_COST, EnumStepStatus.COMPLETED)
            setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
            finish()
        }
    }


    private fun hasFormChanged(cost: FieldOperationCost) = with(binding) {
        fun parseNonNegativeDouble(text: CharSequence?): Double? {
            val value = text?.toString()?.toDoubleOrNull()
            return if (value != null && value >= 0) value else null
        }

        val ploughInput = parseNonNegativeDouble(etPloughingCost.text)
        val ridgeInput = parseNonNegativeDouble(etRidingCost.text)

        val ploughChanged = ploughInput != null && ploughInput != cost.manualPloughCost
        val ridgeChanged = ridgeInput != null && ridgeInput != cost.manualRidgeCost

        ploughChanged || ridgeChanged
    }


    private fun toggleFab() = safeScope.launch {
        userRepo.getUser(sessionManager.akilimoUser)?.let { user ->
            val operationCost = fieldOperationCostsRepo.getCostForUser(user.id ?: 0)

            val shouldShowFab = when {
                // No operation cost yet → show FAB so user can create one
                operationCost == null -> true
                // Operation cost exists → check if form has changed
                else -> hasFormChanged(operationCost)
            }
            binding.lytFabButton.fabSave.apply {
                takeIf { shouldShowFab }?.show() ?: hide()
            }
        }
    }

}