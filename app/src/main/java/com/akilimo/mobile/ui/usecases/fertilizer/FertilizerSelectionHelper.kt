package com.akilimo.mobile.ui.usecases.fertilizer

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.ui.dialogs.PriceSelectionBottomSheet
import kotlinx.coroutines.launch

class FertilizerSelectionHelper(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val priceRepo: FertilizerPriceRepo,
    private val selectedRepo: SelectedFertilizerRepo
) {

    fun showPriceBottomSheet(
        fertilizer: Fertilizer,
        userId: Int,
        fragmentManager: androidx.fragment.app.FragmentManager,
        onSelectionChanged: (fertilizerId: Int, fertilizerPriceId: Int?, price: Double?, displayPrice: String?, isSelected: Boolean, isExactPrice: Boolean) -> Unit
    ) = lifecycleScope.launch {
        val prices = priceRepo.getByFertilizerKey(fertilizer.key.orEmpty())
        if (prices.isEmpty()) return@launch

        val selectedId =
            selectedRepo.getSelectedByFertilizer(fertilizer.id ?: 0)?.fertilizerPriceId ?: 0
        val updatedPriceList = prices.map { it.copy().apply { isSelected = it.id == selectedId } }


        PriceSelectionBottomSheet(
            fertilizer = fertilizer,
            prices = updatedPriceList,
            userId = userId,
            onSelectionChanged = onSelectionChanged
        ).show(fragmentManager, "PriceSelectionBottomSheet")
    }
}