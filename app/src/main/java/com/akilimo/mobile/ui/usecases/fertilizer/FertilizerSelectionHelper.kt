package com.akilimo.mobile.ui.usecases.fertilizer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.GenericSelectableAdapter
import com.akilimo.mobile.databinding.DialogPriceSelectionBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Helper class for fertilizer price selection dialogs.
 * Encapsulates the dialog creation, price selection, and persistence logic.
 */
class FertilizerSelectionHelper(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val priceRepo: FertilizerPriceRepo,
    private val selectedRepo: SelectedFertilizerRepo
) {
    /**
     * Shows price selection dialog for a fertilizer.
     * @param fertilizer The fertilizer to select price for
     * @param userId Current user ID
     * @param onSelectionChanged Callback when selection changes
     */
    fun showPriceDialog(
        fertilizer: Fertilizer,
        userId: Int,
        onSelectionChanged: (fertilizerId: Int, price: Double?, displayPrice: String?, isSelected: Boolean) -> Unit
    ) = lifecycleScope.launch {
        val prices = priceRepo.getByFertilizerKey(fertilizer.key.orEmpty())
        if (prices.isEmpty()) return@launch

        val selectedId =
            selectedRepo.getSelectedByFertilizer(fertilizer.id ?: 0)?.fertilizerPriceId ?: 0
        val updatedPriceList = prices.map { it.copy().apply { isSelected = it.id == selectedId } }

        showPriceSelectionDialog(
            fertilizer,
            updatedPriceList,
            selectedId,
            userId,
            onSelectionChanged
        )
    }

    private fun showPriceSelectionDialog(
        fertilizer: Fertilizer,
        prices: List<FertilizerPrice>,
        preselectedPriceId: Int,
        userId: Int,
        onSelectionChanged: (fertilizerId: Int, price: Double?, displayPrice: String?, isSelected: Boolean) -> Unit
    ) {
        val binding = DialogPriceSelectionBinding.inflate(LayoutInflater.from(context))
        var selectedFertilizerPrice: FertilizerPrice? = null

        binding.removeSelectionButton.visibility =
            if (preselectedPriceId > 0) View.VISIBLE else View.GONE

        val adapter = createPriceAdapter(lifecycleScope) { price, exactAmount ->
            selectedFertilizerPrice = exactAmount?.let { price.copy(pricePerBag = it) } ?: price
        }

        binding.priceRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        adapter.updateItems(prices)

        val titleText =
            context.getString(R.string.price_per_bag, fertilizer.name, fertilizer.countryCode)

        val dialog = AlertDialog.Builder(context)
            .setTitle(titleText)
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.lbl_save)) { _, _ ->
                saveSelection(fertilizer, selectedFertilizerPrice, userId, onSelectionChanged)
            }
            .setNegativeButton(context.getString(R.string.lbl_cancel), null)
            .create()

        binding.removeSelectionButton.setOnClickListener {
            removeSelection(fertilizer.id ?: 0, userId, onSelectionChanged)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createPriceAdapter(
        scope: CoroutineScope,
        onPriceSelected: (FertilizerPrice, Double?) -> Unit
    ) = GenericSelectableAdapter<FertilizerPrice>(
        scope = scope,
        getId = { it.id },
        getLabel = { price ->
            val countryEnum = EnumCountry.valueOf(price.countryCode)
            when {
                price.pricePerBag == 0.0 -> context.getString(R.string.lbl_do_not_know)
                price.pricePerBag < 0.0 -> context.getString(
                    R.string.exact_fertilizer_price_currency,
                    countryEnum.currencyName()
                )

                else -> price.priceRange
            }
        },
        isSelected = { it.isSelected },
        isExactPrice = { it.pricePerBag < 0.0 },
        onItemClick = { onPriceSelected(it, null) },
        onExactAmount = { price, amount -> onPriceSelected(price, amount) }
    )

    private fun saveSelection(
        fertilizer: Fertilizer,
        selectedPrice: FertilizerPrice?,
        userId: Int,
        onSelectionChanged: (fertilizerId: Int, price: Double?, displayPrice: String?, isSelected: Boolean) -> Unit
    ) = lifecycleScope.launch {
        val fertilizerId = fertilizer.id ?: return@launch
        val bagPrice = selectedPrice?.pricePerBag ?: return@launch

        val finalPrice = if (bagPrice >= 0.0) bagPrice else selectedPrice.pricePerBag
        val isExactPrice = bagPrice < 0.0
        val displayPrice = if (isExactPrice) {
            "$finalPrice ${selectedPrice.currencySymbol}"
        } else {
            selectedPrice.priceRange
        }

        if (finalPrice >= 0.0) {
            val selected = SelectedFertilizer(
                userId = userId,
                fertilizerId = fertilizerId,
                fertilizerPriceId = selectedPrice.id,
                fertilizerPrice = finalPrice,
                displayPrice = displayPrice,
                isExactPrice = isExactPrice
            )
            selectedRepo.select(selected)
            onSelectionChanged(fertilizerId, finalPrice, displayPrice, true)
        }
    }

    private fun removeSelection(
        fertilizerId: Int,
        userId: Int,
        onSelectionChanged: (fertilizerId: Int, price: Double?, displayPrice: String?, isSelected: Boolean) -> Unit
    ) = lifecycleScope.launch {
        selectedRepo.deselect(userId, fertilizerId)
        onSelectionChanged(fertilizerId, 0.0, null, false)
    }
}