package com.akilimo.mobile.views.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.inherit.CostBaseActivity
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.ManualTillageCostViewModel
import com.akilimo.mobile.viewmodels.factory.OperationCostViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment

class ManualTillageCostActivity : CostBaseActivity<ActivityManualTillageCostBinding>() {

    private val viewModel: ManualTillageCostViewModel by viewModels {
        OperationCostViewModelFactory(this.application)
    }

    private var manualPloughCost = 0.0
    private var manualRidgeCost = 0.0
    private var dataValid = false
    private var dialogOpen = false

    private lateinit var activeLanguage: String
    private lateinit var finalTranslatedUnit: String

    override fun inflateBinding() = ActivityManualTillageCostBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeLanguage = LanguageManager.getLanguage(this)

        setupToolbar(binding.toolbar, R.string.title_manual_tillage_cost) { validate(false) }

        setupObservers()
        viewModel.loadInitialData { unit, size, symbol ->
            areaUnit = unit
            fieldSize = size
            currencySymbol = symbol
            finalTranslatedUnit =
                getString(if (unit == "ha") R.string.lbl_ha else R.string.lbl_acre).lowercase()

            setupTextsAndButtons()
            showCustomNotificationDialog()
        }
        setupTextsAndButtons()
        showCustomNotificationDialog()
    }

    override fun setupObservers() {
        viewModel.manualPloughCost.observe(this) {
            binding.manualTillage.manualPloughCostText.text =
                getCostText(R.string.lbl_ploughing_cost_text, it)
        }

        viewModel.manualRidgeCost.observe(this) {
            binding.manualTillage.manualRidgingCostText.text =
                getCostText(R.string.lbl_ridging_cost_text, it)
        }

        viewModel.errorMessage.observe(this) {
            it?.let { showCustomWarningDialog(getString(R.string.lbl_invalid_selection), it) }
        }

        viewModel.dataValid.observe(this) {
            if (it) closeActivity(false)
        }
    }

    private fun setupTextsAndButtons() = with(binding) {
        val fieldSizeStr = mathHelper.removeLeadingZero(fieldSize)
        val ploughTitle = getLocalizedString(R.string.lbl_manual_tillage_cost, fieldSizeStr)
        val ridgeTitle = getLocalizedString(R.string.lbl_manual_ridge_cost, fieldSizeStr)

        manualTillage.apply {
            manualPloughCostTitle.text = ploughTitle
            manualRidgeCostTitle.text = ridgeTitle

            manualPloughCostText.text =
                getCostText(R.string.lbl_ploughing_cost_text, manualPloughCost)
            manualRidgingCostText.text =
                getCostText(R.string.lbl_ridging_cost_text, manualRidgeCost)

            btnPloughCost.setOnClickListener {
                showCostDialog(
                    EnumOperation.TILLAGE,
                    ploughTitle,
                    R.string.lbl_manual_tillage_cost_hint
                )
            }

            btnRidgeCost.setOnClickListener {
                showCostDialog(
                    EnumOperation.RIDGING,
                    ridgeTitle,
                    R.string.lbl_manual_ridge_cost_hint
                )
            }
        }

        twoButtons.btnFinish.setOnClickListener { validate(false) }
        twoButtons.btnCancel.setOnClickListener { closeActivity(false) }
    }

    private fun getLocalizedString(resId: Int, value: String): String {
        return if (activeLanguage == "sw") {
            getString(resId, finalTranslatedUnit, value)
        } else {
            getString(resId, value, finalTranslatedUnit)
        }
    }

    private fun getCostText(resId: Int, cost: Double): String {
        val field = mathHelper.removeLeadingZero(fieldSize)
        val amount = mathHelper.removeLeadingZero(cost)
        return if (activeLanguage == "sw") {
            getString(resId, finalTranslatedUnit, field, amount, currencySymbol)
        } else {
            getString(resId, field, finalTranslatedUnit, amount, currencySymbol)
        }
    }

    private fun showCostDialog(
        operation: EnumOperation,
        title: String,
        hintResId: Int
    ) {
        if (dialogOpen) return
        val hint =
            getString(hintResId, mathHelper.removeLeadingZero(fieldSize), finalTranslatedUnit)
        loadOperationCost(
            operation,
            EnumOperationMethod.MANUAL,
            title,
            hint
        )
    }

    override fun showDialogFullscreen(
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    ) {
        if (dialogOpen) return
        dialogOpen = true

        val args = Bundle().apply {
            putString(OperationCostsDialogFragment.OPERATION_NAME, operationName.name)
            putString(OperationCostsDialogFragment.OPERATION_TYPE, operationType.name)
            putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle)
            putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, hintText)
            putString(OperationCostsDialogFragment.CURRENCY_CODE, currencySymbol)
            putString(OperationCostsDialogFragment.COUNTRY_CODE, countryCode)
        }

        OperationCostsDialogFragment().apply {
            arguments = args
            setOnDismissListener(object : OperationCostsDialogFragment.IDismissDialog {
                override fun onDismiss(
                    operationCost: OperationCost?,
                    operationName: String?,
                    selectedCost: Double,
                    cancelled: Boolean,
                    isExactCost: Boolean
                ) {
                    if (!cancelled && operationName != null) {
                        when (operationName) {
                            EnumOperation.TILLAGE.name -> {
                                manualPloughCost = selectedCost
                                binding.manualTillage.manualPloughCostText.text =
                                    getCostText(R.string.lbl_ploughing_cost_text, selectedCost)
                            }

                            EnumOperation.RIDGING.name -> {
                                manualRidgeCost = selectedCost
                                binding.manualTillage.manualRidgingCostText.text =
                                    getCostText(R.string.lbl_ridging_cost_text, selectedCost)
                            }
                        }
                    }
                    dialogOpen = false
                }
            })
            showDialogFragmentSafely(
                supportFragmentManager,
                this,
                OperationCostsDialogFragment.ARG_ITEM_ID
            )
        }
    }

    override fun validate(backPressed: Boolean) {
        viewModel.saveCosts(
            viewModel.manualPloughCost.value ?: 0.0,
            viewModel.manualRidgeCost.value ?: 0.0
        )
    }
}
