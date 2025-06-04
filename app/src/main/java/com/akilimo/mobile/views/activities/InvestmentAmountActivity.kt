package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.InvestmentAmountResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.CurrencyCode
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvestmentAmountActivity : BaseActivity() {

    private var _binding: ActivityInvestmentAmountBinding? = null
    private val binding get() = _binding!!

    private var isExactAmount = false
    private var hasErrors = false

    private var investmentAmountUSD = 0.0
    private var investmentAmountLocal = 0.0
    private var minimumAmountUSD = 0.0
    private var minimumAmountLocal = 0.0
    private var selectedPrice = INVALID_SELECTION

    companion object {
        private const val MIN_INVESTMENT_USD = 1.0
        private const val INVALID_SELECTION = -1.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityInvestmentAmountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbar, R.string.title_activity_investment_amount)
        setupListeners()
        updateLabels()
    }

    private fun setupListeners() {
        binding.investmentOptionsGroup.setOnCheckedChangeListener { group, _ ->
            val selectedButton = findViewById<RadioButton>(group.checkedRadioButtonId)
                ?: return@setOnCheckedChangeListener
            val investment =
                database.investmentAmountDao().findOneByItemTag(selectedButton.tag as String)
                    ?: return@setOnCheckedChangeListener

            isExactAmount = investment.sortOrder == 0L
            investmentAmountLocal = investment.investmentAmount
            binding.exactAmountInputLayout.visibility =
                if (isExactAmount) View.VISIBLE else View.GONE
        }

        binding.exactAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateInvestmentInput()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSubmitInvestment.setOnClickListener {
            if (!validateInvestmentAmount()) {
                val errorMsg = getString(
                    R.string.lbl_investment_validation_msg,
                    minimumAmountLocal,
                    currencyCode
                )
                binding.exactAmountInputLayout.error = errorMsg
                showCustomWarningDialog(getString(R.string.lbl_invalid_investment_amount), errorMsg)
                return@setOnClickListener
            }

            try {
                val amountToInvestRaw = mathHelper.computeInvestmentForSpecifiedAreaUnit(
                    investmentAmountLocal, fieldSize, areaUnit
                )
                val roundedAmount = mathHelper.roundToNDecimalPlaces(amountToInvestRaw, 2.0)

                val investment = database.investmentAmountDao().findOne() ?: InvestmentAmount()
                investment.investmentAmount = roundedAmount
                investment.minInvestmentAmount = minimumAmountLocal
                investment.fieldSize = fieldSizeAcre

                database.investmentAmountDao().insert(investment)
                database.adviceStatusDao()
                    .insert(AdviceStatus(EnumAdviceTasks.INVESTMENT_AMOUNT.name, true))

                closeActivity(false)
            } catch (ex: Exception) {
                Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }
    }

    private fun updateLabels() {
        database.profileInfoDao().findOne()?.let { profile ->
            countryCode = profile.countryCode
            currencyCode = profile.currencyCode
        }

        database.mandatoryInfoDao().findOne()?.let { info ->
            fieldSize = info.areaSize
            fieldSizeAcre = info.areaSize
            areaUnit = info.areaUnit
            areaUnitText = info.displayAreaUnit
        }

        database.investmentAmountDao().findOne()?.let {
            selectedPrice = it.investmentAmount
        }

        val label =
            getString(R.string.lbl_investment_amount_label, fieldSize.toString(), areaUnitText)
        loadInvestmentAmounts(label)
    }

    private fun validateInvestmentInput() {
        val amountText = binding.exactAmountEditText.text.toString()
        if (amountText.isBlank()) {
            binding.exactAmountInputLayout.error = getString(R.string.lbl_investment_amount_prompt)
            return
        }

        investmentAmountLocal = amountText.toDoubleOrNull() ?: 0.0
        investmentAmountUSD = mathHelper.convertToUSD(investmentAmountLocal, currencyCode)
        minimumAmountUSD =
            mathHelper.computeInvestmentAmount(MIN_INVESTMENT_USD, fieldSizeAcre, baseCurrency)
        minimumAmountLocal = mathHelper.convertToLocalCurrency(minimumAmountUSD, currencyCode)

        hasErrors = investmentAmountLocal < minimumAmountLocal
        binding.exactAmountInputLayout.error = if (hasErrors) {
            getString(R.string.lbl_investment_validation_msg, minimumAmountLocal, currencyCode)
        } else {
            null
        }
    }

    private fun validateInvestmentAmount(): Boolean {
        val amountText = binding.exactAmountEditText.text.toString()
        if (isExactAmount) {
            investmentAmountLocal = amountText.toDoubleOrNull() ?: return false
        }

        investmentAmountUSD = mathHelper.convertToUSD(investmentAmountLocal, currencyCode)
        minimumAmountUSD =
            mathHelper.computeInvestmentAmount(MIN_INVESTMENT_USD, fieldSizeAcre, baseCurrency)
        minimumAmountLocal = mathHelper.convertToLocalCurrency(minimumAmountUSD, currencyCode)

        return investmentAmountLocal >= minimumAmountLocal
    }

    private fun loadInvestmentAmounts(selectedFieldArea: String) {
        AkilimoApi.apiService.getInvestmentAmounts(countryCode)
            .enqueue(object : Callback<InvestmentAmountResponse> {
                override fun onResponse(
                    call: Call<InvestmentAmountResponse>,
                    response: Response<InvestmentAmountResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.data ?: emptyList()
                        if (list.isNotEmpty()) {
                            database.investmentAmountDao().insertAll(list)
                            addRadioButtons(list, selectedFieldArea)
                        } else {
                            Toast.makeText(
                                this@InvestmentAmountActivity,
                                getString(R.string.lbl_investment_amount_load_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<InvestmentAmountResponse>, t: Throwable) {
                    Toast.makeText(this@InvestmentAmountActivity, t.message, Toast.LENGTH_SHORT)
                        .show()
                    Sentry.captureException(t)
                }
            })
    }

    private fun addRadioButtons(
        investmentAmountList: List<InvestmentAmount>,
        fieldAreaLabel: String
    ) {
        binding.investmentOptionsGroup.removeAllViews()

        CurrencyCode.getCurrencySymbol(currencyCode)?.let {
            currencySymbol = it.symbol
            currencyName = it.name
        }

        val spacing = 30
        val params = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, spacing, 0, spacing)
        }

        val exactTextHint = getString(
            R.string.exact_investment_x_per_field_area_hint,
            currencyName,
            fieldSize.toString(),
            areaUnit
        )
        binding.exactAmountInputLayout.hint = exactTextHint
        binding.exactAmountEditText.hint = exactTextHint

        val separator = getString(R.string.lbl_per_separator)

        investmentAmountList.forEach { item ->
            minimumAmountLocal = item.minInvestmentAmount
            val price = item.investmentAmount
            val investment =
                mathHelper.computeInvestmentForSpecifiedAreaUnit(price, fieldSize, areaUnit)

            val radioButton = RadioButton(this).apply {
                id = View.generateViewId()
                tag = item.itemTag
                layoutParams = params
                text = if (price == 0.0) {
                    getString(R.string.exact_investment_x_per_field_area)
                } else {
                    formatInvestmentLabel(investment, currencySymbol, fieldAreaLabel, separator)
                }
                isChecked = mathHelper.roundToNDecimalPlaces(investment) == selectedPrice
            }

            binding.investmentOptionsGroup.addView(radioButton)
        }
    }

    private fun formatInvestmentLabel(
        amount: Double,
        currency: String,
        fieldArea: String,
        separator: String
    ): String {
        val formatted = mathHelper.formatNumber(amount, currency)
        return "$formatted $separator $fieldArea"
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    @Deprecated(
        "Remove completely and use setupToolbar(toolbar, titleResId) instead.",
        replaceWith = ReplaceWith("setupToolbar(binding.toolbarLayout.toolbar, R.string.your_title)"),
        level = DeprecationLevel.WARNING
    )
    override fun initToolbar() {
    }

    @Deprecated("Deprecated remove it completely")
    override fun initComponent() {
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }
}
