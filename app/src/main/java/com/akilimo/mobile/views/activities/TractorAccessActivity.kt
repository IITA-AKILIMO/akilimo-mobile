package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityTractorAccessBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.inherit.CostBaseActivity
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationType
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.OperationCostsDialogFragment
import io.sentry.Sentry

class TractorAccessActivity : CostBaseActivity() {
    var toolbar: Toolbar? = null
    var implementTitle: TextView? = null
    var rdgTractor: RadioGroup? = null
    var implementCard: CardView? = null
    var chkPlough: CheckBox? = null
    var chkRidger: CheckBox? = null

    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null

    private var _binding: ActivityTractorAccessBinding? = null
    private val binding get() = _binding!!

    var fieldOperationCost: FieldOperationCost? = null
    var currentPractice: CurrentPractice? = null


    private var hasTractor = false
    private var hasPlough = false
    private var hasRidger = false
    private val hasHarrow = false
    private val isDialogOpen = false

    private var exactPloughCost = false
    private var exactRidgeCost = false

    private val ploughCostText: String? = null
    private val ridgingCostText: String? = null

    private var dataValid = false
    private var tractorPloughCost = 0.0
    private var tractorRidgeCost = 0.0
    private var dialogOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityTractorAccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.areaUnit
            fieldSize = mandatoryInfo.areaSize
        }

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currency = profileInfo.currencyCode
            currencyCode = profileInfo.currencyCode
            val myAkilimoCurrency = database.currencyDao().findOneByCurrencyCode(currencyCode)
            currencySymbol = myAkilimoCurrency.currencySymbol
        }

        toolbar = binding.toolbar
        implementTitle = binding.tractorAccess.implementTitle
        rdgTractor = binding.tractorAccess.rdgTractor
        implementCard = binding.tractorAccess.implementCard
        chkPlough = binding.tractorAccess.chkPlough
        chkRidger = binding.tractorAccess.chkRidger
        btnFinish = binding.twoButtons.btnFinish
        btnCancel = binding.twoButtons.btnCancel

        initToolbar()
        initComponent()
        fieldOperationCost = database.fieldOperationCostDao().findOne()
        currentPractice = database.currentPracticeDao().findOne()
        if (fieldOperationCost != null) {
            tractorPloughCost = fieldOperationCost!!.tractorPloughCost
            tractorRidgeCost = fieldOperationCost!!.tractorRidgeCost
        }
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_tillage_operations)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> validate(false) }
    }

    override fun initComponent() {
        rdgTractor!!.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            when (radioIndex) {
                R.id.rdYesTractor -> {
                    hasTractor = true
                    implementTitle!!.visibility = View.VISIBLE
                    implementCard!!.visibility = View.VISIBLE
                }

                R.id.rdNoTractor -> {
                    hasTractor = false
                    implementTitle!!.visibility = View.GONE
                    implementCard!!.visibility = View.GONE
                    chkRidger!!.isChecked = false
                    chkPlough!!.isChecked = false
                }

                else -> {
                    hasTractor = false
                    implementTitle!!.visibility = View.GONE
                    implementCard!!.visibility = View.GONE
                    chkRidger!!.isChecked = false
                    chkPlough!!.isChecked = false
                }
            }
        }

        val myLocale = getCurrentLocale()
        var translatedUnit = this@TractorAccessActivity.getString(R.string.lbl_acre)
        if (areaUnit == "ha") {
            translatedUnit = this@TractorAccessActivity.getString(R.string.lbl_ha)
        }
        val finalTranslatedUnit = translatedUnit.lowercase(myLocale)


        chkPlough!!.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            hasPlough = isChecked
            if (buttonView.isPressed && isChecked && !dialogOpen) {
                var title = (getString(
                    R.string.lbl_tractor_plough_cost,
                    mathHelper.removeLeadingZero(fieldSize),
                    finalTranslatedUnit
                ))
                var hintText = (getString(
                    R.string.lbl_tractor_plough_cost_hint,
                    mathHelper.removeLeadingZero(fieldSize),
                    finalTranslatedUnit
                ))
                if (myLocale.language == "sw") {
                    title = (getString(
                        R.string.lbl_tractor_plough_cost,
                        finalTranslatedUnit,
                        mathHelper.removeLeadingZero(fieldSize)
                    ))
                    hintText = (getString(
                        R.string.lbl_tractor_plough_cost_hint,
                        finalTranslatedUnit,
                        mathHelper.removeLeadingZero(fieldSize)
                    ))
                }
                loadOperationCost(
                    EnumOperation.TILLAGE.name, EnumOperationType.MECHANICAL.operationName(),
                    title, hintText
                )
            }
        }
        chkRidger!!.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            hasRidger = isChecked
            if (buttonView.isPressed && isChecked && !dialogOpen) {
                var title = (getString(
                    R.string.lbl_tractor_ridge_cost,
                    mathHelper.removeLeadingZero(fieldSize),
                    finalTranslatedUnit
                ))
                var hintText = (getString(
                    R.string.lbl_tractor_ridge_cost_hint,
                    mathHelper.removeLeadingZero(fieldSize),
                    finalTranslatedUnit
                ))
                if (myLocale.language == "sw") {
                    title = (getString(
                        R.string.lbl_tractor_ridge_cost,
                        finalTranslatedUnit,
                        mathHelper.removeLeadingZero(fieldSize)
                    ))
                    hintText = (getString(
                        R.string.lbl_tractor_ridge_cost_hint,
                        finalTranslatedUnit,
                        mathHelper.removeLeadingZero(fieldSize)
                    ))
                }
                loadOperationCost(
                    EnumOperation.RIDGING.name, EnumOperationType.MECHANICAL.operationName(),
                    title, hintText
                )
            }
        }
        btnFinish!!.setOnClickListener { view: View? -> validate(false) }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }
    }

    override fun validate(backPressed: Boolean) {
        setData()
        if (dataValid) {
            closeActivity(backPressed)
        }
    }

    private fun setData() {
        try {
            if (fieldOperationCost == null) {
                fieldOperationCost = FieldOperationCost()
            }
            if (currentPractice == null) {
                currentPractice = CurrentPractice()
            }

            dataValid = true
            currentPractice!!.tractorAvailable = hasTractor
            currentPractice!!.tractorPlough = hasPlough
            currentPractice!!.tractorHarrow = hasHarrow
            currentPractice!!.tractorRidger = hasRidger

            database.currentPracticeDao().insert(currentPractice!!)

            fieldOperationCost!!.tractorPloughCost = tractorPloughCost
            fieldOperationCost!!.tractorRidgeCost = tractorRidgeCost
            fieldOperationCost!!.exactTractorPloughPrice = exactPloughCost
            fieldOperationCost!!.exactTractorRidgePrice = exactRidgeCost

            database.fieldOperationCostDao().insert(fieldOperationCost!!)
            database.adviceStatusDao()
                .insert(AdviceStatus(EnumAdviceTasks.TRACTOR_ACCESS.name, true))
        } catch (ex: Exception) {
            Toast.makeText(this@TractorAccessActivity, ex.message, Toast.LENGTH_SHORT).show()
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

        showCustomNotificationDialog()
        arguments.putParcelableArrayList(OperationCostsDialogFragment.COST_LIST, operationCostList)
        arguments.putString(OperationCostsDialogFragment.OPERATION_NAME, operationName)
        arguments.putString(OperationCostsDialogFragment.CURRENCY_CODE, currency)
        arguments.putString(OperationCostsDialogFragment.CURRENCY_SYMBOL, currencySymbol)
        arguments.putString(OperationCostsDialogFragment.COUNTRY_CODE, countryCode)
        arguments.putString(OperationCostsDialogFragment.DIALOG_TITLE, dialogTitle)
        arguments.putString(OperationCostsDialogFragment.EXACT_PRICE_HINT, hintText)

        val dialogFragment = getOperationCostsDialogFragment(arguments)


        showDialogFragmentSafely(
            supportFragmentManager,
            dialogFragment,
            OperationCostsDialogFragment.ARG_ITEM_ID
        )
        dialogOpen = true
    }

    private fun getOperationCostsDialogFragment(arguments: Bundle): OperationCostsDialogFragment {
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
                        mathHelper.roundToNearestSpecifiedValue(selectedCost, 1000.0)
                    when (operationName) {
                        "TILLAGE" -> {
                            tractorPloughCost = roundedCost
                            exactPloughCost = isExactCost
                        }

                        "RIDGING" -> {
                            tractorRidgeCost = roundedCost
                            exactRidgeCost = isExactCost
                        }
                    }
                }
                dialogOpen = false
            }
        })

        return dialogFragment
    }
}
