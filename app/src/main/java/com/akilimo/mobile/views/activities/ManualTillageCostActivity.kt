package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityManualTillageCostBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.inherit.CostBaseActivity
import com.akilimo.mobile.models.OperationCost
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationType
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment
import com.android.volley.toolbox.Volley
import io.sentry.Sentry

class ManualTillageCostActivity : CostBaseActivity() {
    var toolbar: Toolbar? = null
    var manualPloughCostTitle: TextView? = null
    var manualRidgeCostTitle: TextView? = null
    var manualPloughCostText: TextView? = null
    var manualRidgingCostText: TextView? = null

    var btnPloughCost: AppCompatButton? = null
    var btnRidgeCost: AppCompatButton? = null
    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null

    private var _binding: ActivityManualTillageCostBinding? = null
    private val binding get() = _binding!!

    var mathHelper: MathHelper? = null
    var fieldOperationCost: FieldOperationCost? = null

    private var manualPloughCost = 0.0
    private var manualRidgeCost = 0.0
    private var dataValid = false
    private var dialogOpen = false

    private var hintText: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityManualTillageCostBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        queue = Volley.newRequestQueue(this)
        mathHelper = MathHelper()

        toolbar = binding.toolbar
        manualPloughCostTitle = binding.manualTillage.manualPloughCostTitle
        manualRidgeCostTitle = binding.manualTillage.manualRidgeCostTitle
        manualPloughCostText = binding.manualTillage.manualPloughCostText
        manualRidgingCostText = binding.manualTillage.manualRidgingCostText
        btnPloughCost = binding.manualTillage.btnPloughCost
        btnRidgeCost = binding.manualTillage.btnRidgeCost
        btnFinish = binding.twoButtons.btnFinish
        btnCancel = binding.twoButtons.btnCancel

        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.areaUnit!!
            fieldSize = mandatoryInfo.areaSize
        }

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode!!
            currencyCode = profileInfo.currencyCode!!
            val myAkilimoCurrency = database.currencyDao().findOneByCurrencyCode(currencyCode)
            currencySymbol = myAkilimoCurrency.currencySymbol
        }

        initToolbar()
        initComponent()

        fieldOperationCost = database.fieldOperationCostDao().findOne()
        if (fieldOperationCost != null) {
            manualPloughCost = fieldOperationCost!!.manualPloughCost
            manualRidgeCost = fieldOperationCost!!.manualRidgeCost

            manualPloughCostText!!.text = getString(
                R.string.lbl_ploughing_cost_text,
                mathHelper!!.removeLeadingZero(fieldSize),
                areaUnit,
                mathHelper!!.removeLeadingZero(manualPloughCost),
                currencySymbol
            )
            manualRidgingCostText!!.text = getString(
                R.string.lbl_ridging_cost_text,
                mathHelper!!.removeLeadingZero(fieldSize),
                areaUnit,
                mathHelper!!.removeLeadingZero(manualRidgeCost),
                currencySymbol
            )
        }
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_manual_tillage_cost)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> validate(false) }
    }

    override fun initComponent() {
        val myLocale = getCurrentLocale()
        var translatedUnit = context.getString(R.string.lbl_acre)
        if (areaUnit == "ha") {
            translatedUnit = context.getString(R.string.lbl_ha)
        }
        val finalTranslatedUnit = translatedUnit.lowercase(myLocale)

        var ploughTitle = context.getString(
            R.string.lbl_manual_tillage_cost,
            mathHelper!!.removeLeadingZero(fieldSize),
            finalTranslatedUnit
        )
        var ridgeTitle = context.getString(
            R.string.lbl_manual_ridge_cost,
            mathHelper!!.removeLeadingZero(fieldSize),
            finalTranslatedUnit
        )
        if (myLocale.language == "sw") {
            ploughTitle = context.getString(
                R.string.lbl_manual_tillage_cost,
                finalTranslatedUnit,
                mathHelper!!.removeLeadingZero(fieldSize)
            )
            ridgeTitle = context.getString(
                R.string.lbl_manual_ridge_cost,
                finalTranslatedUnit,
                mathHelper!!.removeLeadingZero(fieldSize)
            )
        }
        val finalPloughTitle = ploughTitle
        btnPloughCost!!.setOnClickListener { view: View? ->
            hintText = context.getString(
                R.string.lbl_manual_tillage_cost_hint,
                mathHelper!!.removeLeadingZero(fieldSize),
                finalTranslatedUnit
            )
            if (!dialogOpen) {
                loadOperationCost(
                    EnumOperation.TILLAGE.name, EnumOperationType.MANUAL.name, finalPloughTitle,
                    hintText!!
                )
            }
        }

        val finalRidgeTitle = ridgeTitle
        btnRidgeCost!!.setOnClickListener { view: View? ->
            hintText = context.getString(
                R.string.lbl_manual_ridge_cost_hint,
                mathHelper!!.removeLeadingZero(fieldSize),
                finalTranslatedUnit
            )
            if (!dialogOpen) {
                loadOperationCost(
                    EnumOperation.RIDGING.name, EnumOperationType.MANUAL.name, finalRidgeTitle,
                    hintText!!
                )
            }
        }

        btnFinish!!.setOnClickListener { view: View? -> validate(false) }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }

        manualPloughCostTitle!!.text = ploughTitle
        manualRidgeCostTitle!!.text = ridgeTitle


        showCustomNotificationDialog()
    }

    override fun validate(backPressed: Boolean) {
        setData()
        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTasks.MANUAL_TILLAGE_COST.name, dataValid))
        if (dataValid) {
            closeActivity(backPressed)
        }
    }

    private fun setData() {
        if (fieldOperationCost == null) {
            fieldOperationCost = FieldOperationCost()
        }

        dataValid = false
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
            fieldOperationCost!!.manualPloughCost = manualPloughCost
            fieldOperationCost!!.manualRidgeCost = manualRidgeCost
            database.fieldOperationCostDao().insert(fieldOperationCost!!)
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }


    override fun showDialogFullscreen(
        operationCostList: ArrayList<OperationCost>?,
        operationName: String?,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String?
    ) {
        val arguments = Bundle()

        if (dialogOpen) {
            return
        }

        val myLocale = getCurrentLocale()
        var translatedUnit = context.getString(R.string.lbl_acre)
        if (areaUnit == "ha") {
            translatedUnit = context.getString(R.string.lbl_ha)
        }
        val finalTranslatedUnit = translatedUnit.lowercase(myLocale)


        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList)
        arguments.putString(OperationCostsDialogFragment.OPERATION_NAME, operationName)
        arguments.putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle)
        arguments.putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, hintText)
        arguments.putString(OperationCostsDialogFragment.CURRENCY_CODE, currency)
        arguments.putString(OperationCostsDialogFragment.CURRENCY_SYMBOL, currencySymbol)
        arguments.putString(OperationCostsDialogFragment.COUNTRY_CODE, countryCode)

        val dialogFragment = OperationCostsDialogFragment()
        dialogFragment.arguments = arguments

        dialogFragment.setOnDismissListener(object : OperationCostsDialogFragment.IDismissDialog {
            override fun onDismiss(
                operationCost: OperationCost?,
                operationName: String?,
                selectedCost: Double,
                cancelled: Boolean,
                isExactCost: Boolean
            ) {
                if (!cancelled && operationName != null) {
                    val roundedCost =
                        mathHelper!!.roundToNearestSpecifiedValue(selectedCost, 1000.0)
                    when (operationName) {
                        "TILLAGE" -> {
                            manualPloughCost = roundedCost
                            var manualTillageText = getString(
                                R.string.lbl_ploughing_cost_text,
                                mathHelper!!.removeLeadingZero(fieldSize),
                                finalTranslatedUnit,
                                mathHelper!!.formatNumber(roundedCost, null),
                                currencySymbol
                            )
                            if (myLocale.language == "sw") {
                                manualTillageText = getString(
                                    R.string.lbl_ploughing_cost_text,
                                    finalTranslatedUnit,
                                    mathHelper!!.removeLeadingZero(fieldSize),
                                    mathHelper!!.formatNumber(roundedCost, null),
                                    currencySymbol
                                )
                            }
                            manualPloughCostText!!.text = manualTillageText
                        }

                        "RIDGING" -> {
                            manualRidgeCost = roundedCost
                            var manualRidgeText = getString(
                                R.string.lbl_ridging_cost_text,
                                mathHelper!!.removeLeadingZero(fieldSize),
                                finalTranslatedUnit,
                                mathHelper!!.formatNumber(roundedCost, null),
                                currencySymbol
                            )
                            if (myLocale.language == "sw") {
                                manualRidgeText = getString(
                                    R.string.lbl_ridging_cost_text,
                                    finalTranslatedUnit,
                                    mathHelper!!.removeLeadingZero(fieldSize),
                                    mathHelper!!.formatNumber(roundedCost, null),
                                    currencySymbol
                                )
                            }
                            manualRidgingCostText!!.text = manualRidgeText
                        }
                    }
                }
                dialogOpen = false
            }

        })



        supportFragmentManager.beginTransaction().apply {
            dialogOpen = true
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            setCustomAnimations(
                R.anim.animate_slide_in_left,
                R.anim.animate_slide_out_right
            )
            supportFragmentManager.findFragmentByTag(OperationCostsDialogFragment.ARG_ITEM_ID)
                ?.let {
                    remove(it)
                }
            addToBackStack(null)
        }.also { transaction ->
            dialogFragment.show(transaction, OperationCostsDialogFragment.ARG_ITEM_ID)
            transaction.commit()
        }

    }
}
