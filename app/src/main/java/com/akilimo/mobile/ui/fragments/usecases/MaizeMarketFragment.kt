package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractProduceMarketFragment
import com.akilimo.mobile.databinding.ActivityMaizeMarketBinding
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.ProduceMarketViews
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MaizeMarketFragment : AbstractProduceMarketFragment<ActivityMaizeMarketBinding>(
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

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityMaizeMarketBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        super.onBindingReady(savedInstanceState)

        // Apply defaults when no saved entry exists
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.userId != 0 && state.lastEntry == null) {
                        binding.produceTypeGroup.check(binding.freshCobsOption.id)
                        updateUnits { it == EnumUnitOfSale.FRESH_COB }
                        binding.unitSpinner.setText(
                            EnumUnitOfSale.FRESH_COB.unitOfSale(requireContext()),
                            false
                        )
                        binding.priceInputLayout.hint =
                            getString(R.string.lbl_price_per_cob_in_currency_unit)
                                .replace("{currency}", currencyCode)
                        validateForm()
                    }
                }
            }
        }
    }

    override fun resolveProduceType(): EnumProduceType = when {
        binding.freshCobsOption.isChecked -> EnumProduceType.MAIZE_FRESH_COB
        binding.dryGrainOption.isChecked -> EnumProduceType.MAIZE_GRAIN
        else -> EnumProduceType.UNKNOWN
    }

    override fun setupProduceTypeSelection() {
        binding.produceTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            views.unitSpinner.setText(null, false)
            views.inputPrice.setText(null)

            when (checkedId) {
                binding.freshCobsOption.id -> {
                    updateUnits { it == EnumUnitOfSale.FRESH_COB }
                    views.priceInputLayout.hint =
                        getString(R.string.lbl_price_per_cob_in_currency_unit)
                            .replace("{currency}", currencyCode)
                }
                binding.dryGrainOption.id -> {
                    updateUnits { it.isUniversal }
                    views.priceInputLayout.hint = getString(R.string.lbl_price_per_kg_bag)
                }
            }
            validateForm()
        }
    }

    override fun onEntryPrefilled(entry: ProduceMarket) {
        when (entry.produceType) {
            EnumProduceType.MAIZE_FRESH_COB -> binding.produceTypeGroup.check(binding.freshCobsOption.id)
            EnumProduceType.MAIZE_GRAIN -> binding.produceTypeGroup.check(binding.dryGrainOption.id)
            else -> Unit
        }
    }
}
