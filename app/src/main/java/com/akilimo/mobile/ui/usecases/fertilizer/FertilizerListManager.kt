package com.akilimo.mobile.ui.usecases.fertilizer

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.adapters.FertilizerAdapter
import com.akilimo.mobile.entities.Fertilizer

/**
 * Manages fertilizer list display and layout toggling.
 * Data is supplied by FertilizerViewModel via submitList/updateSelection.
 */
class FertilizerListManager(
    private val recyclerView: RecyclerView,
    private val gridSpanCount: Int = 2
) {
    val adapter = FertilizerAdapter()
    private var isGridLayout = false

    fun initialize(
        initialGridMode: Boolean = false,
        onItemClick: (Fertilizer) -> Unit
    ) {
        isGridLayout = initialGridMode
        adapter.onItemClick = onItemClick
        adapter.setLayoutMode(isGridLayout)

        recyclerView.apply {
            layoutManager = createLayoutManager()
            this.adapter = this@FertilizerListManager.adapter
            setHasFixedSize(true)
        }
    }

    fun toggleLayout(): Boolean {
        isGridLayout = !isGridLayout
        recyclerView.layoutManager = createLayoutManager()
        adapter.setLayoutMode(isGridLayout)
        return isGridLayout
    }

    fun setGridLayout(isGrid: Boolean) {
        isGridLayout = isGrid
        recyclerView.layoutManager = createLayoutManager()
        adapter.setLayoutMode(isGridLayout)
    }

    private fun createLayoutManager(): RecyclerView.LayoutManager {
        return if (isGridLayout) {
            GridLayoutManager(recyclerView.context, gridSpanCount)
        } else {
            LinearLayoutManager(recyclerView.context)
        }
    }

    /**
     * Updates a single fertilizer's selection state in the adapter.
     */
    fun updateItemSelection(
        fertilizerId: Int,
        price: Double?,
        displayPrice: String?,
        isSelected: Boolean
    ) {
        adapter.currentList.find { it.id == fertilizerId }?.apply {
            this.isSelected = isSelected
            this.selectedPrice = price ?: 0.0
            this.displayPrice = displayPrice
        }
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }
}