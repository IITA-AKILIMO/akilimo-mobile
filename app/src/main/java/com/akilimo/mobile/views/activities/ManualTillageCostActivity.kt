package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.inherit.CostBaseActivity
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationType
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment
import io.sentry.Sentry

class ManualTillageCostActivity : CostBaseActivity<ActivityManualTillageCostBinding>() {

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
        loadInitialData()
        setupToolbar(binding.toolbar, R.string.title_manual_tillage_cost) { validate(false) }
        setupTextsAndButtons()
        showCustomNotificationDialog()
    }

    private fun loadInitialData() {
        database.mandatoryInfoDao().findOne()?.let {
            areaUnit = it.areaUnit
            fieldSize = it.areaSize
        }

        database.profileInfoDao().findOne()?.let { profile ->
            countryCode = profile.countryCode
            currencyCode = profile.currencyCode
            database.currencyDao().findOneByCurrencyCode(currencyCode)?.let {
                currencyCode = it.currencyCode
                currencySymbol = it.currencySymbol
            }
        }

        database.fieldOperationCostDao().findOne()?.let { cost ->
            manualPloughCost = cost.manualPloughCost
            manualRidgeCost = cost.manualRidgeCost
        }

        finalTranslatedUnit = getString(
            if (areaUnit == "ha") R.string.lbl_ha else R.string.lbl_acre
        ).lowercase()
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
            operation.name,
            EnumOperationType.MANUAL.name,
            title,
            hint
        )
    }

    override fun showDialogFullscreen(
        operationName: String?,
        operationType: String?,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    ) {
        if (dialogOpen) return
        dialogOpen = true

        val args = Bundle().apply {
            putString(OperationCostsDialogFragment.OPERATION_NAME, operationName)
            putString(OperationCostsDialogFragment.OPERATION_TYPE, operationType)
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
        setData()
        database.adviceStatusDao().insert(
            AdviceStatus(EnumAdviceTasks.MANUAL_TILLAGE_COST.name, dataValid)
        )
        if (dataValid) {
            closeActivity(backPressed)
        }
    }

    private fun setData() {
        if (manualPloughCost <= 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_selection),
                getString(R.string.lbl_manual_plough_cost_prompt)
            )
            return
        }

        if (manualRidgeCost <= 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_selection),
                getString(R.string.lbl_manual_ridge_cost_prompt)
            )
            return
        }

        dataValid = true
        try {
            val cost = database.fieldOperationCostDao().findOne() ?: FieldOperationCost()
            cost.manualPloughCost = manualPloughCost
            cost.manualRidgeCost = manualRidgeCost
            database.fieldOperationCostDao().insertOrUpdate(cost)
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }
}
