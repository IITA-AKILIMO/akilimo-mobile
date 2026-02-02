package com.akilimo.mobile.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.GenericSelectableAdapter
import com.akilimo.mobile.databinding.BottomSheetPriceSelectionBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.enums.EnumCountry
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.MathHelper

class CassavaPriceSelectionBottomSheet(
    private val unit: CassavaUnit,
    private val selectedMarket: SelectedCassavaMarket?,
    private val prices: List<CassavaMarketPrice>,
    private val onPriceSelected: (CassavaMarketPrice?, EnumUnitOfSale) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPriceSelectionBinding
    private var selectedMarketPrice: CassavaMarketPrice? = null
    private var customPriceValue: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPriceSelectionBinding.inflate(inflater, container, false)

        val unitOfSaleEnum = EnumUnitOfSale.entries.find {
            it.name.equals(unit.label, ignoreCase = true)
        } ?: EnumUnitOfSale.THOUSAND_KG

        val adapter = GenericSelectableAdapter<CassavaMarketPrice>(
            scope = lifecycleScope,
            getId = { it.id },
            getLabel = {
                val computedPrice = MathHelper.computeUnitPrice(it.averagePrice, unitOfSaleEnum)
                when {
                    it.averagePrice == 0.0 -> getString(R.string.lbl_do_not_know)
                    it.averagePrice < 0.0 -> getString(R.string.lbl_exact_price)
                    else -> "$computedPrice ${it.currencySymbol}"
                }
            },
            isSelected = { it.isSelected },
            isExactPrice = { it.exactPrice },
            getExactPrice = {
                val matches = (selectedMarket?.marketPriceId ?: -1) == it.id
                if (matches) selectedMarket?.unitPrice else null
            },
            onItemClick = { selectedPrice -> selectedMarketPrice = selectedPrice },
            onExactAmount = { selectedPrice, amount ->
                selectedMarketPrice = selectedPrice.copy(exactPrice = true, averagePrice = amount)
                customPriceValue = amount
            }
        )

        binding.priceRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.priceRecycler.adapter = adapter
        adapter.updateItems(prices)

        binding.saveButton.setOnClickListener {
            val finalPrice = customPriceValue ?: selectedMarketPrice?.averagePrice
            if (finalPrice != null && finalPrice > 0.0) {
                val updated = selectedMarketPrice?.copy(
                    averagePrice = finalPrice,
                    exactPrice = customPriceValue != null
                )
                onPriceSelected(updated, unitOfSaleEnum)
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener { dismiss() }
        binding.removeSelectionButton.setOnClickListener {
            onPriceSelected(null, unitOfSaleEnum)
            dismiss()
        }

        return binding.root
    }
}
