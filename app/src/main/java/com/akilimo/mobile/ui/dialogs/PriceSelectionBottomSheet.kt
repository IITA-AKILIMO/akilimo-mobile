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

class PriceSelectionBottomSheet(
    private val fertilizer: Fertilizer,
    private val prices: List<FertilizerPrice>,
    private val userId: Int,
    private val onSelectionChanged: (fertilizerId: Int, price: Double?, displayPrice: String?, isSelected: Boolean) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPriceSelectionBinding
    private var selectedPrice: FertilizerPrice? = null
    private var customPriceValue: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetPriceSelectionBinding.inflate(inflater, container, false)

        val adapter = GenericSelectableAdapter<FertilizerPrice>(
            scope = lifecycleScope,
            getId = { it.id },
            getLabel = { price ->
                when {
                    price.pricePerBag == 0.0 -> getString(R.string.lbl_do_not_know)
                    price.pricePerBag < 0.0 -> getString(R.string.lbl_exact_price)
                    else -> price.priceRange
                }
            },
            isSelected = { it.isSelected },
            isExactPrice = { it.pricePerBag < 0.0 },
            onItemClick = { selectedPrice = it },
            onExactAmount = { price, amount ->
                selectedPrice = price.copy(pricePerBag = amount)
                customPriceValue = amount
            }
        )

        binding.priceRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.priceRecycler.adapter = adapter
        adapter.updateItems(prices)
// Highlight the last selected item
        val preselectedId = prices.firstOrNull { it.isSelected }?.id ?: -1
        if (preselectedId != -1) {
            adapter.setSelectedItem(preselectedId)
        }

        binding.btnSave.setOnClickListener {
            val finalPrice = customPriceValue ?: selectedPrice?.pricePerBag
            if (finalPrice != null && finalPrice > 0.0) {
                val displayPrice = if (customPriceValue != null) {
                    "$finalPrice ${EnumCountry.fromCode(fertilizer.countryCode).currencyName()}"
                } else selectedPrice?.priceRange

                onSelectionChanged(fertilizer.id ?: 0, finalPrice, displayPrice, true)
            }
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnRemove.setOnClickListener {
            onSelectionChanged(fertilizer.id ?: 0, 0.0, null, false)
            dismiss()
        }

        return binding.root
    }
}
