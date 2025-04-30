package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.InvestmentAmountResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.mynameismidori.currencypicker.ExtendedCurrency
import io.sentry.Sentry


class InvestmentAmountActivity : BaseActivity() {

    var toolbar: Toolbar? = null
    var rdgInvestmentAmount: RadioGroup? = null
    var rd_exact_investment: RadioButton? = null
    var txtEditInvestmentAmount: EditText? = null
    var txtEditInvestmentAmountLayout: TextInputLayout? = null
    var btnFinish: MaterialButton? = null
    var binding: ActivityInvestmentAmountBinding? = null
    var extendedCurrency: ExtendedCurrency? = null

    var investmentAmountError: String? = null

    private var isExactAmount = false
    private var hasErrors = false

    private var fieldAreaAcre: String? = null
    private var fieldArea: String? = null

    private var investmentAmountUSD = 0.0
    private var investmentAmountLocal = 0.0
    private val minInvestmentUSD = 1.0
    private val maxInvestmentUSD = 1.0
    private var minimumAmountUSD = 0.0
    private var minimumAmountLocal = 0.0
    private var maxAmountLocal = 0.0
    private var selectedPrice = -1.0
    private var selectedFieldArea: String? = null

    //private var investmentAmountList: List<InvestmentAmount> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentAmountBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        toolbar = binding!!.toolbar
        rdgInvestmentAmount = binding!!.rdgInvestmentAmount
        txtEditInvestmentAmount = binding!!.editInvestmentAmount
        txtEditInvestmentAmountLayout = binding!!.editInvestmentAmountLayout
        btnFinish = binding!!.btnFinish

        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_activity_investment_amount)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar!!.setNavigationOnClickListener { v: View? ->
            validateInvestmentAmount()
            if (!hasErrors) {
                closeActivity(false)
            } else {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_investment_amount), investmentAmountError
                )
            }
        }
    }

    override fun initComponent() {
        investmentAmountError = getString(R.string.lbl_investment_amount_prompt)

        rdgInvestmentAmount!!.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val radioButtonId = group.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(radioButtonId)
            val radioTag = radioButton.tag as String

            isExactAmount = false
            txtEditInvestmentAmountLayout!!.visibility = View.GONE
            val inv = database.investmentAmountDao().findOneByItemTag(radioTag)
            if (inv != null) {
                investmentAmountLocal = inv.investmentAmount
                if (inv.sortOrder == 0L) {
                    isExactAmount = true
                    txtEditInvestmentAmountLayout!!.visibility = View.VISIBLE
                    txtEditInvestmentAmountLayout!!.requestFocus()
                }
            }
        }
        txtEditInvestmentAmount!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                validateEditText(editable)
            }
        })
        btnFinish!!.setOnClickListener {
            validateInvestmentAmount()
            if (hasErrors) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_investment_amount), investmentAmountError
                )
                return@setOnClickListener
            }

            var invAmount = database.investmentAmountDao().findOne()
            if (invAmount == null) {
                invAmount = InvestmentAmount()
            }
            try {
                val amountToInvestRaw = mathHelper.computeInvestmentForSpecifiedAreaUnit(
                    investmentAmountLocal, fieldSize, areaUnit
                )
                val amountToInvest = mathHelper.roundToNDecimalPlaces(amountToInvestRaw, 2.0)
                invAmount.investmentAmount = amountToInvest
                invAmount.minInvestmentAmount = minimumAmountLocal
                invAmount.fieldSize = fieldSizeAcre

                database.investmentAmountDao().insert(invAmount)

                database.adviceStatusDao()
                    .insert(AdviceStatus(EnumAdviceTasks.INVESTMENT_AMOUNT.name, true))

                closeActivity(false)
            } catch (ex: Exception) {
                Toast.makeText(this@InvestmentAmountActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }

        updateLabels()
        showCustomNotificationDialog()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    private fun updateLabels() {
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            if (profileInfo.currencyCode.isNotEmpty()) {
                currencyCode = profileInfo.currencyCode
            }
        }

        val investmentAmount = database.investmentAmountDao().findOne()
        if (investmentAmount != null) {
            selectedPrice = investmentAmount.investmentAmount
        }
        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        if (mandatoryInfo != null) {
            fieldSize = mandatoryInfo.areaSize
            fieldSizeAcre = mandatoryInfo.areaSize
            fieldArea = fieldSize.toString()
            fieldAreaAcre = fieldSizeAcre.toString()
            areaUnit = mandatoryInfo.areaUnit
            areaUnitText = mandatoryInfo.displayAreaUnit!!
        }
        selectedFieldArea =
            String.format(getString(R.string.lbl_investment_amount_label), fieldArea, areaUnitText)

        loadInvestmentAmount() //load amount from API
    }

    private fun validateEditText(editable: Editable) {
        investmentAmountError = validateInvestmentAmount()
        if (TextUtils.isEmpty(editable) || hasErrors) {
            txtEditInvestmentAmountLayout!!.error = investmentAmountError
        } else {
            txtEditInvestmentAmountLayout!!.error = null
        }
    }

    private fun validateInvestmentAmount(): String {
        val amount = txtEditInvestmentAmountLayout!!.editText?.text.toString()

        if (amount.isNotEmpty()) {
            investmentAmountLocal = amount.toDouble()
            investmentAmountUSD = mathHelper.convertToUSD(investmentAmountLocal, currencyCode)
        }

        minimumAmountUSD =
            mathHelper.computeInvestmentAmount(minInvestmentUSD, fieldSizeAcre, baseCurrency)
        minimumAmountLocal = mathHelper.convertToLocalCurrency(minimumAmountUSD, currencyCode)
        hasErrors = investmentAmountLocal < minimumAmountLocal
        return getString(
            R.string.lbl_investment_validation_msg, minimumAmountLocal, currencyCode
        ).also { investmentAmountError = it }
    }

    private fun loadInvestmentAmount() {
        val call = AkilimoApi.apiService.getInvestmentAmounts(countryCode = countryCode)
        call.enqueue(object : retrofit2.Callback<InvestmentAmountResponse> {
            override fun onResponse(
                call: retrofit2.Call<InvestmentAmountResponse>,
                response: retrofit2.Response<InvestmentAmountResponse>
            ) {
                if (response.isSuccessful) {
                    val investmentAmountList = response.body()!!.data
                    if (investmentAmountList.isNotEmpty()) {
                        database.investmentAmountDao().insertAll(investmentAmountList)
                        addInvestmentRadioButtons(investmentAmountList)
                    } else {
                        Toast.makeText(
                            this@InvestmentAmountActivity,
                            getString(R.string.lbl_investment_amount_load_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<InvestmentAmountResponse>, t: Throwable) {
                Toast.makeText(this@InvestmentAmountActivity, t.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(t)
            }
        })
    }

    private fun addInvestmentRadioButtons(investmentAmountList: List<InvestmentAmount>) {
        val context = this@InvestmentAmountActivity
        rdgInvestmentAmount!!.removeAllViews()
        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        if (extendedCurrency != null) {
            currencySymbol = extendedCurrency.symbol
            currencyName = extendedCurrency.name
        }

        val params = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val mediumSpacing = 30
        params.setMargins(0, mediumSpacing, 0, mediumSpacing)

        val exactTextHint = getString(
            R.string.exact_investment_x_per_field_area_hint,
            currencyName,
            fieldSize.toString(),
            areaUnit
        )
        val separator = getString(R.string.lbl_per_separator)

        txtEditInvestmentAmountLayout!!.hint = exactTextHint
        txtEditInvestmentAmount!!.hint = exactTextHint

        for (pricesResp in investmentAmountList) {
            minimumAmountLocal = pricesResp.minInvestmentAmount
            maxAmountLocal = pricesResp.maxInvestmentAmount

            val radioTag = pricesResp.itemTag
            val radioButton = RadioButton(context)

            radioButton.id = View.generateViewId()
            radioButton.tag = radioTag

            val price = pricesResp.investmentAmount
            val amountToInvest =
                mathHelper.computeInvestmentForSpecifiedAreaUnit(price, fieldSize, areaUnit)
            var radioLabel = setLabel(
                amountToInvest,
                currencySymbol = currencySymbol,
                selectedFieldArea = selectedFieldArea,
                separator = separator
            )
            if (price == 0.0) {
                radioLabel = context.getString(R.string.exact_investment_x_per_field_area)
            }

            radioButton.text = radioLabel
            radioButton.layoutParams = params

            rdgInvestmentAmount!!.addView(radioButton)
            val refAmount = mathHelper.roundToNDecimalPlaces(amountToInvest)
            if (refAmount == selectedPrice) {
                radioButton.isChecked = true
            }
        }
    }

    private fun setLabel(
        amountToInvest: Double,
        currencySymbol: String,
        selectedFieldArea: String?,
        separator: String
    ): String {
        val formattedNumber = mathHelper.formatNumber(amountToInvest, currencySymbol)
        return String.format("%s %s %s", formattedNumber, separator, selectedFieldArea)
    }
}
