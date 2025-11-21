package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityMaizeMarketBinding
import com.akilimo.mobile.ui.components.ToolbarHelper

class MaizeMarketActivity : BaseActivity<ActivityMaizeMarketBinding>() {
    override fun inflateBinding() = ActivityMaizeMarketBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {

        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .setTitle(getString(R.string.lbl_maize_market))
            .onNavigationClick { finish() }.build()

        setupProduceTypeToggle()
        setupFormValidation()

        updateUnits(R.array.units_cobs)
        binding.priceInputLayout.hint =
            getString(R.string.lbl_price_per_cob_in_currency_unit).replace("{currency}", "KES")
        binding.unitLabel.visibility = View.GONE
        binding.unitInputLayout.visibility = View.GONE
    }

    private fun setupProduceTypeToggle() {
        val currencyName = "KES"
        binding.produceTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            when (checkedId) {
                binding.freshCobsOption.id -> {
                    updateUnits(R.array.units_cobs)
                    binding.priceInputLayout.hint =
                        getString(R.string.lbl_price_per_cob_in_currency_unit)
                            .replace("{currency}", currencyName)

                    binding.unitLabel.visibility = View.GONE
                    binding.unitInputLayout.visibility = View.GONE
                }

                binding.dryGrainOption.id -> {
                    updateUnits(R.array.units_grain)
                    binding.priceInputLayout.hint = "Price per kg/bag"

                    binding.unitLabel.visibility = View.VISIBLE
                    binding.unitInputLayout.visibility = View.VISIBLE
                }
            }
            validateForm()
        }
    }

    private fun updateUnits(arrayRes: Int) {
        val items = resources.getStringArray(arrayRes)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        binding.unitSpinner.setAdapter(adapter)
        binding.unitSpinner.setText("", false) // clear selection when switching type
    }

    private fun setupFormValidation() {
        binding.apply {
            inputPrice.addTextChangedListener { validateForm() }
            unitSpinner.setOnItemClickListener { _, _, _, _ ->
                validateForm()
            }
        }
    }

    private fun validateForm() {
        val priceValid =
            binding.inputPrice.text?.toString()?.toDoubleOrNull()?.let { it > 0 } ?: false
        val unitValid = !binding.unitSpinner.text.isNullOrEmpty()

        binding.submitButton.isEnabled = priceValid && unitValid
    }
}
