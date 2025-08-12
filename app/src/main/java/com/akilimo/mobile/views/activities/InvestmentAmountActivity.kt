package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.viewmodels.InvestmentAmountViewModel
import com.akilimo.mobile.viewmodels.factory.InvestmentAmountViewModelFactory

class InvestmentAmountActivity : BindBaseActivity<ActivityInvestmentAmountBinding>() {

    private val viewModel: InvestmentAmountViewModel by viewModels {
        InvestmentAmountViewModelFactory(application = this.application, mathHelper = mathHelper)
    }

    override fun inflateBinding() = ActivityInvestmentAmountBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(binding.toolbar, R.string.title_activity_investment_amount)
        setupObservers()
        setupListeners()
    }

    override fun setupObservers() {
        viewModel.investmentAmounts.observe(this) { list ->
            val label = getString(
                R.string.lbl_investment_amount_label,
                viewModel.fieldSize.toString(),
                viewModel.areaUnitText
            )
            addRadioButtons(list, label)
        }

        viewModel.isExactAmount.observe(this) { isExact ->
            binding.exactAmountInputLayout.visibility = if (isExact) View.VISIBLE else View.GONE
        }

        viewModel.selectedInvestmentAmount.observe(this) { selectedPrice ->
            // Could update UI based on selection if needed
        }

        viewModel.minInvestmentLocal.observe(this) { minLocal ->
            // Optional: update UI hints/errors based on min investment
        }
    }

    private fun setupListeners() {
        binding.investmentOptionsGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedButton =
                findViewById<RadioButton>(checkedId) ?: return@setOnCheckedChangeListener
            val tag = selectedButton.tag as? String ?: return@setOnCheckedChangeListener
            val investment = database.investmentAmountDao().findOneByItemTag(tag)
                ?: return@setOnCheckedChangeListener
            viewModel.onInvestmentOptionSelected(investment)
        }

        binding.exactAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val valid = viewModel.validateInvestmentInput(s?.toString() ?: "")
                binding.exactAmountInputLayout.error = if (valid) null else
                    getString(
                        R.string.lbl_investment_validation_msg,
                        viewModel.minInvestmentLocal.value ?: 0.0,
                        viewModel.currencyCode
                    )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /** Do nothing**/
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /** Do nothing **/
            }
        })

        binding.btnSubmitInvestment.setOnClickListener {
            if (viewModel.isExactAmount.value == true) {
                val inputText = binding.exactAmountEditText.text.toString()
                if (!viewModel.validateInvestmentInput(inputText)) {
                    val errorMsg = getString(
                        R.string.lbl_investment_validation_msg,
                        viewModel.minInvestmentLocal.value ?: 0.0,
                        viewModel.currencyCode
                    )
                    binding.exactAmountInputLayout.error = errorMsg
                    showCustomWarningDialog(
                        getString(R.string.lbl_invalid_investment_amount),
                        errorMsg
                    )
                    return@setOnClickListener
                }
            }

            val success = viewModel.saveInvestmentAmount(viewModel.fieldSizeAcre)
            // TODO: Track status of each activity
//                database.adviceStatusDao()
//                    .insert(AdviceStatus(EnumTask.INVESTMENT_AMOUNT.name, true))
            if (success) {
                closeActivity(false)
            } else {
                Toast.makeText(this, "Failed to save investment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addRadioButtons(
        investmentAmountList: List<InvestmentAmount>,
        fieldAreaLabel: String
    ) {
        binding.investmentOptionsGroup.removeAllViews()


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
//                isChecked = mathHelper.roundToNDecimalPlaces(investment) == selectedPrice
                isChecked =
                    viewModel.mathHelper.roundToNDecimalPlaces(investment) == (viewModel.selectedInvestmentAmount.value
                        ?: -1.0)
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
}
