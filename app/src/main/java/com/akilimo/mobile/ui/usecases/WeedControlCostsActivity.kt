package com.akilimo.mobile.ui.usecases

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.BaseValueOptionAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityWeedControlCostsBinding
import com.akilimo.mobile.dto.WeedControlOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.utils.StringHelper.formatWithLandSize
import kotlinx.coroutines.launch

class WeedControlCostsActivity : BaseActivity<ActivityWeedControlCostsBinding>() {

    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var fieldOperationCostsRepo: FieldOperationCostsRepo

    private lateinit var currentPracticeRepo: CurrentPracticeRepo

    private val weedControlOptions = mutableListOf<WeedControlOption>()

    override fun inflateBinding() = ActivityWeedControlCostsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        fieldOperationCostsRepo = FieldOperationCostsRepo(database.fieldOperationCostsDao())
        currentPracticeRepo = CurrentPracticeRepo(database.currentPracticeDao())

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
                val selected =
                    weedControlOptions.getOrNull(position) ?: return@setOnItemClickListener
                dropWeedControl.setText(
                    selected.valueOption.label(this@WeedControlCostsActivity), false
                )
            }

        }

        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: 0
            val sizeUnitLabel = user.enumAreaUnit.label(this@WeedControlCostsActivity).orEmpty()
            val firstWeedingText = formatWithLandSize(
                R.string.lbl_cost_of_first_weeding_operation,
                user.farmSize,
                sizeUnitLabel
            )
            val secondWeedingText = formatWithLandSize(
                R.string.lbl_cost_of_second_weeding_operation,
                user.farmSize,
                sizeUnitLabel
            )
            binding.apply {
                tilFirstWeedingCost.helperText = firstWeedingText
                tilSecondWeedingCost.helperText = secondWeedingText
            }

            val operationCosts = fieldOperationCostsRepo.getCostForUser(userId)
            val currentPractice = currentPracticeRepo.getPracticeForUser(userId)
            operationCosts?.let { cost ->
                val firstWeedingCosts = cost.firstWeedingOperationCost
                val secondWeedingCosts = cost.secondWeedingOperationCost
                binding.etFirstWeedingCost.setText(firstWeedingCosts.toString())
                binding.etSecondWeedingCost.setText(secondWeedingCosts.toString())
            }

            currentPractice?.let { practice ->
                val controlMethod =
                    EnumWeedControlMethod.entries.firstOrNull { it == practice.weedControlMethod }

                binding.dropWeedControl.setText(
                    controlMethod?.label(this@WeedControlCostsActivity),
                    false
                )
            }
        }

        binding.apply {
            lytFabButton.fabSave.setOnClickListener {
                val firstCost = binding.etFirstWeedingCost.text?.toString()?.toDoubleOrNull()
                val secondCost = binding.etSecondWeedingCost.text?.toString()?.toDoubleOrNull()
                saveOperationCosts(firstCost, secondCost)
            }
            etFirstWeedingCost.addTextChangedListener { toggleFab() }
            etSecondWeedingCost.addTextChangedListener { toggleFab() }
            lytFabButton.fabSave.hide()
        }
        binding.lytFabButton.fabSave.hide()
    }

    private fun saveOperationCosts(
        firstCost: Double?,
        secondCost: Double?,
    ) {
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: return@launch
            val newCosts = FieldOperationCost(
                userId = userId,
                firstWeedingOperationCost = firstCost ?: 0.0,
                secondWeedingOperationCost = secondCost ?: 0.0
            )
            val existing = fieldOperationCostsRepo.getCostForUser(userId)
            val merged = existing?.copy(
                manualRidgeCost = if (firstCost != null) newCosts.firstWeedingOperationCost else existing.firstWeedingOperationCost,
                manualPloughCost = if (secondCost != null) newCosts.secondWeedingOperationCost else existing.secondWeedingOperationCost,
            ) ?: newCosts
            fieldOperationCostsRepo.saveCost(cost = merged)
            val completion =
                AdviceCompletionDto(EnumAdviceTask.COST_OF_WEED_CONTROL, EnumStepStatus.COMPLETED)
            setResult(RESULT_OK, Intent().putExtra(COMPLETED_TASK, completion))
            finish()
        }
    }


    private fun hasFormChanged(cost: FieldOperationCost) = with(binding) {
        fun parseNonNegativeDouble(text: CharSequence?): Double? {
            val value = text?.toString()?.toDoubleOrNull()
            return if (value != null && value >= 0) value else null
        }

        val firstWeeding = parseNonNegativeDouble(etFirstWeedingCost.text)
        val secondWeeding = parseNonNegativeDouble(etSecondWeedingCost.text)

        val firstCostChanged =
            firstWeeding != null && firstWeeding != cost.firstWeedingOperationCost
        val secondCostChanged =
            secondWeeding != null && secondWeeding != cost.secondWeedingOperationCost

        firstCostChanged || secondCostChanged
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