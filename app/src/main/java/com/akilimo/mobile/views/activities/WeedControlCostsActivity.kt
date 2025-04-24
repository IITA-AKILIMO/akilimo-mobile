package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityWeedControlCostBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumCountry
import io.sentry.Sentry

class WeedControlCostsActivity : BaseActivity() {
    var toolbar: Toolbar? = null
    private var firstWeedingOpCostTitle: AppCompatTextView? = null
    private var secondWeedingOpCostTitle: AppCompatTextView? = null
    private var herbicideUseTitle: AppCompatTextView? = null
    private var herbicideUseCard: CardView? = null
    private var btnFinish: AppCompatButton? = null
    private var btnCancel: AppCompatButton? = null
    private var rdgWeedControl: RadioGroup? = null
    private var editFirstWeedingOpCost: EditText? = null
    private var editSecondWeedingOpCost: EditText? = null


    private var mathHelper: MathHelper? = null
    private var currentPractice: CurrentPractice? = null
    private var fieldOperationCost: FieldOperationCost? = null
    private var usesHerbicide = false
    private var weedControlTechnique: String? = null
    private var firstOperationCost = 0.0
    private var secondOperationCost = 0.0
    private var weedRadioIndex = 0

    private val minCost = 1.0

    private var _binding: ActivityWeedControlCostBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWeedControlCostBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        val context = this@WeedControlCostsActivity
        database = getDatabase(context)
        mathHelper = MathHelper()

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
            currencyName = myAkilimoCurrency.currencyName
        }

        fieldOperationCost = database.fieldOperationCostDao().findOne()
        currentPractice = database.currentPracticeDao().findOne()

        toolbar = binding.toolbar
        firstWeedingOpCostTitle = binding.weedControlCosts.firstWeedingOpCostTitle
        secondWeedingOpCostTitle = binding.weedControlCosts.secondWeedingOpCostTitle
        herbicideUseTitle = binding.weedControlCosts.herbicideUseTitle
        herbicideUseCard = binding.weedControlCosts.herbicideUseCard
        btnFinish = binding.twoButtons.btnFinish
        btnCancel = binding.twoButtons.btnCancel
        rdgWeedControl = binding.weedControlCosts.rdgWeedControl
        editFirstWeedingOpCost = binding.weedControlCosts.editFirstWeedingOpCost
        editSecondWeedingOpCost = binding.weedControlCosts.editSecondWeedingOpCost


        initToolbar()
        initComponent()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validate(true)
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_weed_control)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { closeActivity(false) }
    }

    override fun initComponent() {
        val context = this@WeedControlCostsActivity
        val myLocale = getCurrentLocale()
        var translatedUnit = context.getString(R.string.lbl_acre)
        if (areaUnit == "ha") {
            translatedUnit = context.getString(R.string.lbl_ha)
        }
        val finalTranslatedUnit = translatedUnit.lowercase(myLocale)
        when (countryCode) {
            "TZ" -> currencyName = EnumCountry.Tanzania.currencyName(context)
            "NG" -> currencyName = EnumCountry.Nigeria.currencyName(context)
            "GH" -> currencyName = EnumCountry.Ghana.currencyName(context)
            "RW" -> currencyName = EnumCountry.Rwanda.currencyName(context)
            "BI" -> currencyName = EnumCountry.Burundi.currencyName(context)
        }

        if (currentPractice != null) {
            weedRadioIndex = currentPractice!!.weedRadioIndex
            weedControlTechnique = currentPractice!!.weedControlTechnique
            rdgWeedControl!!.check(weedRadioIndex)
        }
        if (fieldOperationCost != null) {
            firstOperationCost = fieldOperationCost!!.firstWeedingOperationCost
            if (firstOperationCost > 0) {
                editFirstWeedingOpCost!!.setText(firstOperationCost.toString())
            }

            secondOperationCost = fieldOperationCost!!.secondWeedingOperationCost
            if (secondOperationCost > 0) {
                editSecondWeedingOpCost!!.setText(secondOperationCost.toString())
            }
        }
        var firstWeedCostTitle = getString(
            R.string.lbl_cost_of_first_weeding_operation,
            currencyName,
            mathHelper!!.removeLeadingZero(fieldSize),
            finalTranslatedUnit
        )
        var secondWeedCostTitle = getString(
            R.string.lbl_cost_of_second_weeding_operation,
            currencyName,
            mathHelper!!.removeLeadingZero(fieldSize),
            finalTranslatedUnit
        )
        if (myLocale.language == "sw") {
            firstWeedCostTitle = getString(
                R.string.lbl_cost_of_first_weeding_operation,
                currencyCode,
                finalTranslatedUnit,
                mathHelper!!.removeLeadingZero(fieldSize)
            )
            secondWeedCostTitle = getString(
                R.string.lbl_cost_of_second_weeding_operation,
                currencyCode,
                finalTranslatedUnit,
                mathHelper!!.removeLeadingZero(fieldSize)
            )
        }
        firstWeedingOpCostTitle!!.text = firstWeedCostTitle
        secondWeedingOpCostTitle!!.text = secondWeedCostTitle

        rdgWeedControl!!.setOnCheckedChangeListener { _: RadioGroup?, radioIndex: Int ->
            when (radioIndex) {
                R.id.rdManualOnlyControl -> {
                    usesHerbicide = false
                    weedControlTechnique = "manual"
                }

                R.id.rdHerbicideControl -> {
                    usesHerbicide = true
                    weedControlTechnique = "herbicide"
                }

                R.id.rdManualHerbicideControl -> {
                    usesHerbicide = true
                    weedControlTechnique = "manual_herbicide"
                }
            }
        }


        btnFinish!!.setOnClickListener { validate(false) }
        btnCancel!!.setOnClickListener { closeActivity(false) }
        showCustomNotificationDialog()
    }

    override fun validate(backPressed: Boolean) {
        if (weedControlTechnique.isNullOrEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_selection),
                getString(R.string.lbl_weed_control_prompt)
            )
            return
        }

        //get the user input
        weedRadioIndex = rdgWeedControl!!.checkedRadioButtonId
        firstOperationCost = mathHelper!!.convertToDouble(editFirstWeedingOpCost!!.text.toString())
        secondOperationCost =
            mathHelper!!.convertToDouble(editSecondWeedingOpCost!!.text.toString())

        if (firstOperationCost <= minCost) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_cost),
                getString(R.string.lbl_first_weeding_costs_prompt)
            )
            return
        }
        if (secondOperationCost <= minCost) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_cost),
                getString(R.string.lbl_second_weeding_cost_prompt)
            )
            return
        }

        try {
            if (currentPractice == null) {
                currentPractice = CurrentPractice()
            }
            if (fieldOperationCost == null) {
                fieldOperationCost = FieldOperationCost()
            }

            currentPractice!!.weedControlTechnique = weedControlTechnique
            currentPractice!!.usesHerbicide = usesHerbicide
            currentPractice!!.weedRadioIndex = weedRadioIndex

            database.currentPracticeDao().insert(currentPractice!!)

            fieldOperationCost!!.firstWeedingOperationCost = firstOperationCost
            fieldOperationCost!!.secondWeedingOperationCost = secondOperationCost

            database.fieldOperationCostDao().insert(fieldOperationCost!!)
            database.adviceStatusDao()
                .insert(AdviceStatus(EnumAdviceTasks.COST_OF_WEED_CONTROL.name, true))

            closeActivity(backPressed)
        } catch (ex: Exception) {
            Toast.makeText(this@WeedControlCostsActivity, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }
}
