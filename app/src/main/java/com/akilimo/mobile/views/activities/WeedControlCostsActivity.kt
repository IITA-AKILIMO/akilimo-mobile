package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityWeedControlCostBinding
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.repo.DatabaseRepository
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumWeedControlMethod
import com.akilimo.mobile.viewmodels.WeedControlCostsViewModel
import com.akilimo.mobile.viewmodels.factory.WeedControlCostsViewModelFactory
import io.sentry.Sentry

class WeedControlCostsActivity : BaseActivity() {

    private lateinit var binding: ActivityWeedControlCostBinding
    private val viewModel: WeedControlCostsViewModel by viewModels {
        WeedControlCostsViewModelFactory(DatabaseRepository(database))
    }

    private val minCost = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeedControlCostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupListeners()

        viewModel.loadInitialData()
        showCustomNotificationDialog()
    }

    private fun setupToolbar() {
        setupToolbar(binding.toolbar, R.string.title_weed_control) {
            validateAndSave()
        }
    }

    private fun setupObservers() {
        viewModel.currencyName.observe(this) { updateCostLabels() }
        viewModel.fieldSize.observe(this) { updateCostLabels() }
        viewModel.currentPractice.observe(this) { practice ->
            practice?.weedRadioIndex?.let {
                binding.weedControlCosts.rdgWeedControl.check(it)
            }
        }

        viewModel.fieldOperationCost.observe(this) { cost ->
            binding.weedControlCosts.editFirstWeedingOpCost.setText(
                if (cost.firstWeedingOperationCost > 0) cost.firstWeedingOperationCost.toString() else ""
            )
            binding.weedControlCosts.editSecondWeedingOpCost.setText(
                if (cost.secondWeedingOperationCost > 0) cost.secondWeedingOperationCost.toString() else ""
            )
        }

        viewModel.saveStatus.observe(this) { result ->
            result.onSuccess {
                closeActivity(false)
            }.onFailure {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(it)
            }
        }
    }

    private fun setupListeners() {
        binding.weedControlCosts.rdgWeedControl.setOnCheckedChangeListener { _, checkedId ->
            val (method, herbicide) = when (checkedId) {
                R.id.rdManualOnlyControl -> EnumWeedControlMethod.MANUAL to false
                R.id.rdHerbicideControl -> EnumWeedControlMethod.HERBICIDE to true
                R.id.rdManualHerbicideControl -> EnumWeedControlMethod.MANUAL to true
                else -> EnumWeedControlMethod.NONE to false
            }
            viewModel.updateWeedControlMethod(method, checkedId, herbicide)
        }

        binding.twoButtons.btnFinish.setOnClickListener {
            validateAndSave()
        }

        binding.twoButtons.btnCancel.setOnClickListener {
            closeActivity(false)
        }
    }

    private fun validateAndSave() {
        val firstCost =
            mathHelper.convertToDouble(binding.weedControlCosts.editFirstWeedingOpCost.text.toString())
        val secondCost =
            mathHelper.convertToDouble(binding.weedControlCosts.editSecondWeedingOpCost.text.toString())

        if (firstCost <= minCost) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_cost),
                getString(R.string.lbl_first_weeding_costs_prompt)
            )
            return
        }

        if (secondCost <= minCost) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_cost),
                getString(R.string.lbl_second_weeding_cost_prompt)
            )
            return
        }

        viewModel.updateWeedingCosts(firstCost, secondCost)
        viewModel.saveData()
    }

    private fun updateCostLabels() {
        val language = LanguageManager.getLanguage(this)
        val areaUnit = viewModel.areaUnit.value ?: return
        val fieldSize = viewModel.fieldSize.value ?: return
        val currencyCode = viewModel.currencyCode.value ?: ""
        val currencyName = viewModel.currencyName.value ?: ""

        val translatedUnit = if (areaUnit == "ha")
            getString(R.string.lbl_ha) else getString(R.string.lbl_acre)
        val finalUnit = translatedUnit.lowercase()
        val size = mathHelper.removeLeadingZero(fieldSize)

        val firstLabel = if (language == "sw") {
            getString(R.string.lbl_cost_of_first_weeding_operation, currencyCode, finalUnit, size)
        } else {
            getString(R.string.lbl_cost_of_first_weeding_operation, currencyName, size, finalUnit)
        }

        val secondLabel = if (language == "sw") {
            getString(R.string.lbl_cost_of_second_weeding_operation, currencyCode, finalUnit, size)
        } else {
            getString(R.string.lbl_cost_of_second_weeding_operation, currencyName, size, finalUnit)
        }

        binding.weedControlCosts.firstWeedingOpCostTitle.text = firstLabel
        binding.weedControlCosts.secondWeedingOpCostTitle.text = secondLabel
    }
}
