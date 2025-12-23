package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractProduceMarketActivity
import com.akilimo.mobile.databinding.ActivityMaizeMarketBinding
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.ProduceMarketViews
import kotlinx.coroutines.launch


class MaizeMarketActivity : AbstractProduceMarketActivity<ActivityMaizeMarketBinding>(
    marketType = EnumMarketType.MAIZE_MARKET,
    titleRes = R.string.lbl_maize_market
) {

    override val views: ProduceMarketViews
        get() = ProduceMarketViews.Binding(
            unitLabel = binding.unitLabel,
            unitInputLayout = binding.unitInputLayout,
            unitSpinner = binding.unitSpinner,
            inputPrice = binding.inputPrice,
            priceInputLayout = binding.priceInputLayout,
            fabSave = binding.lytFab.fabSave,
            marketHintCard = binding.marketHintCard,
            marketHintText = binding.marketHintText,
            toolbar = binding.lytToolbar.toolbar
        )


    override fun inflateBinding() = ActivityMaizeMarketBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        super.onBindingReady(savedInstanceState)


        // ✅ If no entity was found, set Fresh Cob as default
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val maizeMarket =
                marketRepo.getLastEntryForUser(user.id ?: 0, EnumMarketType.MAIZE_MARKET)

            if (maizeMarket == null) {
                // No saved entry → default to Fresh Cob
                binding.produceTypeGroup.check(binding.freshCobsOption.id)

                updateUnits { it == EnumUnitOfSale.FRESH_COB }
                binding.unitSpinner.setText(
                    EnumUnitOfSale.FRESH_COB.unitOfSale(this@MaizeMarketActivity),
                    false
                )
                binding.priceInputLayout.hint =
                    getString(R.string.lbl_price_per_cob_in_currency_unit)
                        .replace("{currency}", currencyCode)

                validateForm()
            }
        }
    }


    override fun resolveProduceType(): EnumProduceType {
        return when {
            binding.freshCobsOption.isChecked -> EnumProduceType.MAIZE_FRESH_COB
            binding.dryGrainOption.isChecked -> EnumProduceType.MAIZE_GRAIN
            else -> EnumProduceType.UNKNOWN
        }
    }

    override fun setupProduceTypeSelection() = with(views) {
        binding.produceTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            unitSpinner.setText(null, false)
            inputPrice.setText(null)

            when (checkedId) {
                binding.freshCobsOption.id -> {
                    // ✅ Pass filter hook: only Fresh Cob unit
                    updateUnits { it == EnumUnitOfSale.FRESH_COB }
                    priceInputLayout.hint =
                        getString(R.string.lbl_price_per_cob_in_currency_unit)
                            .replace("{currency}", currencyCode)
                }

                binding.dryGrainOption.id -> {
                    // ✅ Pass filter hook: all universal units
                    updateUnits { it.isUniversal }
                    priceInputLayout.hint = getString(R.string.lbl_price_per_kg_bag)
                }
            }
            validateForm()
        }
    }
}
