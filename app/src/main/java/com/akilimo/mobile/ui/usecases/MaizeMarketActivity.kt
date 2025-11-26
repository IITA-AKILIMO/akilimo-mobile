package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.BaseValueOptionAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.dao.ProduceMarketRepo
import com.akilimo.mobile.databinding.ActivityMaizeMarketBinding
import com.akilimo.mobile.dto.UnitOfSaleOption
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import kotlinx.coroutines.launch

class MaizeMarketActivity : BaseActivity<ActivityMaizeMarketBinding>() {
    private val userRepo by lazy { AkilimoUserRepo(database.akilimoUserDao()) }
    private val marketRepo by lazy { ProduceMarketRepo(database.produceMarketDao()) }

    private var currencyCode = ""

    override fun inflateBinding() = ActivityMaizeMarketBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .setTitle(getString(R.string.lbl_maize_market))
            .onNavigationClick { finish() }
            .build()

        binding.apply {
            inputPrice.addTextChangedListener { validateForm() }
            unitSpinner.setOnItemClickListener { _, _, _, _ -> validateForm() }
            lytFab.fabSave.setOnClickListener { saveMarketEntry() }
        }

        binding.produceTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            binding.unitSpinner.setText(null, false)
            binding.inputPrice.setText(null)
            when (checkedId) {
                binding.freshCobsOption.id -> {
                    updateUnits(isFreshCob = true)
                    binding.priceInputLayout.hint =
                        getString(R.string.lbl_price_per_cob_in_currency_unit)
                            .replace("{currency}", currencyCode)
                    binding.unitLabel.visibility = View.VISIBLE
                    binding.unitInputLayout.visibility = View.VISIBLE
                }

                binding.dryGrainOption.id -> {
                    updateUnits(isFreshCob = false)
                    binding.priceInputLayout.hint = getString(R.string.lbl_price_per_kg_bag)
                    binding.unitLabel.visibility = View.VISIBLE
                    binding.unitInputLayout.visibility = View.VISIBLE
                }
            }
            validateForm()
        }
        updateUnits(isFreshCob = true)
        binding.lytFab.fabSave.isVisible = false
        binding.unitLabel.visibility = View.VISIBLE
        binding.unitInputLayout.visibility = View.VISIBLE

        prefillFromEntity()
    }

    private fun prefillFromEntity() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val maizeMarket = marketRepo.getLastEntryForUser(user.id ?: 0, EnumMarketType.MAIZE_MARKET)
        val currency = user.enumCountry
        currencyCode = currency.currencyCode
        binding.apply {
            priceInputLayout.prefixText = "$currencyCode "
            priceInputLayout.hint = getString(R.string.lbl_price_per_cob_in_currency_unit)
                .replace("{currency}", currencyCode)
        }

        maizeMarket?.let { entry ->
            when (entry.produceType) {
                EnumProduceType.MAIZE_FRESH_COB -> binding.freshCobsOption.isChecked = true
                EnumProduceType.MAIZE_GRAIN -> binding.dryGrainOption.isChecked = true
                else -> Unit
            }
            val isFreshCob = entry.produceType == EnumProduceType.MAIZE_FRESH_COB
            val unitOfSale = entry.unitOfSale
            updateUnits(isFreshCob)
            binding.apply {
                inputPrice.setText(entry.unitPrice.toString())
                unitSpinner.setText(unitOfSale.unitOfSale(this@MaizeMarketActivity), false)
                marketHintText.text = getString(R.string.lbl_selling_hint)
                    .replace("{currency}", currencyCode)
                    .replace("{price}", entry.unitPrice.toString())
                    .replace("{unit_of_sale}", unitOfSale.unitOfSale(this@MaizeMarketActivity))
                marketHintCard.visibility = View.VISIBLE
            }
        }
    }

    private fun updateUnits(isFreshCob: Boolean) {
        val filteredUnits = EnumUnitOfSale.entries.filter { unit ->
            if (isFreshCob) unit == EnumUnitOfSale.FRESH_COB else unit.isUniversal
        }.map { UnitOfSaleOption(valueOption = it) }

        val adapter = BaseValueOptionAdapter(
            this,
            filteredUnits,
            getDisplayText = { option -> option.label(this) }
        )

        binding.unitSpinner.setAdapter(adapter)
        binding.unitSpinner.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener
            binding.unitSpinner.setText(selected.valueOption.unitOfSale(this), false)
            validateForm()
        }
    }

    private fun validateForm() = with(binding) {
        val priceValid =
            inputPrice.text?.toString()?.toDoubleOrNull()?.let { it > 0 } ?: false
        val unitValid = !unitSpinner.text.isNullOrEmpty()
        lytFab.fabSave.isVisible = priceValid && unitValid
    }

    private fun saveMarketEntry() {
        binding.unitInputLayout.error = null
        binding.priceInputLayout.error = null
        val price = binding.inputPrice.text?.toString()?.toDoubleOrNull()
        val unitText = binding.unitSpinner.text.toString()

        if (price == null || price <= 0) {
            binding.priceInputLayout.error = getString(R.string.lbl_invalid_grain_price)
            return
        }
        val unit = EnumUnitOfSale.entries.firstOrNull { it.unitOfSale(this) == unitText }
        if (unit == null) {
            binding.unitInputLayout.error = getString(R.string.lbl_invalid_sale_unit)
            return
        }

        val produceType = when {
            binding.freshCobsOption.isChecked -> EnumProduceType.MAIZE_FRESH_COB
            binding.dryGrainOption.isChecked -> EnumProduceType.MAIZE_GRAIN
            else -> EnumProduceType.UNKNOWN
        }

        safeScope.launch {
            val userId = userRepo.getUser(sessionManager.akilimoUser)?.id ?: return@launch
            val entry = ProduceMarket(
                userId = userId,
                unitPrice = price,
                marketType = EnumMarketType.MAIZE_MARKET,
                produceType = produceType,
                unitOfSale = unit
            )
            marketRepo.saveMarketEntry(entry)
            finish()
        }
    }
}
