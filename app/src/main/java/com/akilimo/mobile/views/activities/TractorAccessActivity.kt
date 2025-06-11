package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.inherit.CostBaseActivity
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment
import io.sentry.Sentry

class TractorAccessActivity : CostBaseActivity<ActivityTractorAccessBinding>() {

    private var fieldOperationCost: FieldOperationCost? = null
    private var currentPractice: CurrentPractice? = null

    private var hasTractor = false
    private var hasPlough = false
    private var hasRidger = false
    private val hasHarrow = false

    private var exactPloughCost = false
    private var exactRidgeCost = false
    private var dataValid = false

    private var tractorPloughCost = 0.0
    private var tractorRidgeCost = 0.0
    private var dialogOpen = false

    override fun inflateBinding() = ActivityTractorAccessBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupData()
        bindViews()
        setupListeners()
    }

    private fun setupData() {
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

        fieldOperationCost = database.fieldOperationCostDao().findOne()
        currentPractice = database.currentPracticeDao().findOne()

        fieldOperationCost?.let {
            tractorPloughCost = it.tractorPloughCost
            tractorRidgeCost = it.tractorRidgeCost
        }
    }

    private fun bindViews() = with(binding) {
        setupToolbar(toolbar, R.string.title_tillage_operations) {
            validate(false)
        }

        twoButtons.btnFinish.setOnClickListener { validate(false) }
        twoButtons.btnCancel.setOnClickListener { closeActivity(false) }
    }

    private fun setupListeners() = with(binding.tractorAccess) {

        tractorToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_yes_tractor -> {
                        hasTractor = true
                        implementTitle.visibility = View.VISIBLE
                        implementCard.visibility = View.VISIBLE
                    }

                    R.id.btn_no_tractor -> {
                        hasTractor = false
                        implementToggleGroup.clearChecked()
                        implementTitle.visibility = View.GONE
                        implementCard.visibility = View.GONE
                    }
                }
            }
        }

        implementToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_plough -> showOperationDialog(EnumOperation.TILLAGE)
                    R.id.btn_ridger -> showOperationDialog(EnumOperation.RIDGING)
                }
            }
        }
    }


    private fun showOperationDialog(enumOperationName: EnumOperation) {
        val language = LanguageManager.getLanguage(this)
        val translatedUnit = when (areaUnit.lowercase()) {
            "ha" -> getString(R.string.lbl_ha)
            "acre" -> getString(R.string.lbl_acre)
            else -> areaUnit
        }.lowercase()

        val (titleResId, hintResId, flagSetter) = when (enumOperationName) {
            EnumOperation.TILLAGE -> Triple(
                R.string.lbl_tractor_plough_cost,
                R.string.lbl_tractor_plough_cost_hint
            ) { hasPlough = true }

            EnumOperation.RIDGING -> Triple(
                R.string.lbl_tractor_ridge_cost,
                R.string.lbl_tractor_ridge_cost_hint
            ) { hasRidger = true }

            else -> throw IllegalArgumentException("Unknown operation selected: ${enumOperationName.name}")
        }

        /**
         * flagSetter in the refactored function is a lambda function (anonymous function)
         * used to set a flag (hasPlough or hasRidger) depending on the EnumOperation.
         */
        flagSetter()

        val title = getCostTitle(titleResId, language, translatedUnit)
        val hint = getCostTitle(hintResId, language, translatedUnit)

        loadOperationCost(
            enumOperationName,
            EnumOperationMethod.TRACTOR,
            title,
            hint
        )
    }

    private fun getCostTitle(@StringRes resId: Int, language: String, areaUnit: String): String {
        val formattedSize = mathHelper.removeLeadingZero(fieldSize)

        return when (language.lowercase()) {
            "sw" -> getString(resId, areaUnit, formattedSize) // Swahili: "Kwa ekari 5"
            else -> getString(resId, formattedSize, areaUnit) // English: "5 per acre"
        }
    }


    override fun validate(backPressed: Boolean) {
        setData()
        if (dataValid) {
            closeActivity(backPressed)
        }
    }

    private fun setData() {
        try {
            if (fieldOperationCost == null) fieldOperationCost = FieldOperationCost()
            if (currentPractice == null) currentPractice = CurrentPractice()

            dataValid = true

            currentPractice?.apply {
                tractorAvailable = hasTractor
                tractorPlough = hasPlough
                tractorHarrow = hasHarrow
                tractorRidger = hasRidger
                database.currentPracticeDao().insert(this)
            }

            fieldOperationCost?.apply {
                tractorPloughCost = this@TractorAccessActivity.tractorPloughCost
                tractorRidgeCost = this@TractorAccessActivity.tractorRidgeCost
                exactTractorPloughPrice = exactPloughCost
                exactTractorRidgePrice = exactRidgeCost
                database.fieldOperationCostDao().insertOrUpdate(this)
            }

            database.adviceStatusDao()
                .insert(AdviceStatus(EnumAdviceTask.TRACTOR_ACCESS.name, true))

        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    override fun showDialogFullscreen(
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    ) {
        if (dialogOpen) return

        val args = Bundle().apply {
            putString(OperationCostsDialogFragment.OPERATION_NAME, operationName.name)
            putString(OperationCostsDialogFragment.OPERATION_TYPE, operationType.name)
            putString(OperationCostsDialogFragment.CURRENCY_CODE, currencySymbol)
            putString(OperationCostsDialogFragment.COUNTRY_CODE, countryCode)
            putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle)
            putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, hintText)
        }

        val dialogFragment = getOperationCostsDialogFragment(args)
        showDialogFragmentSafely(
            supportFragmentManager,
            dialogFragment,
            OperationCostsDialogFragment.ARG_ITEM_ID
        )
        dialogOpen = true
    }

    private fun getOperationCostsDialogFragment(arguments: Bundle): OperationCostsDialogFragment {
        return OperationCostsDialogFragment().apply {
            this.arguments = arguments
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
                                tractorPloughCost = selectedCost
                                exactPloughCost = isExactCost
                            }

                            EnumOperation.RIDGING.name -> {
                                tractorRidgeCost = selectedCost
                                exactRidgeCost = isExactCost
                            }
                        }
                    }
                    dialogOpen = false
                }
            })
        }
    }
}

