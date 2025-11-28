package com.akilimo.mobile.ui.usecases.fertilizer

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.adapters.FertilizerAdapter
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Manages fertilizer list display, layout toggling, and selection state.
 * Provides a clean API for observing and updating fertilizer data.
 */
class FertilizerListManager(
    private val recyclerView: RecyclerView,
    private val fertilizerRepo: FertilizerRepo,
    private val selectedRepo: SelectedFertilizerRepo,
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
     * Observes fertilizer data and selection state.
     * Call this from a coroutine scope (e.g., lifecycleScope or safeScope).
     */
    fun observeFertilizers(
        scope: CoroutineScope,
        country: EnumCountry,
        userId: Int,
        onDataChanged: (isEmpty: Boolean) -> Unit
    ) {
        scope.launch {
            fertilizerRepo.observeByCountry(country).collectLatest { fertilizers ->
                val selectedList = selectedRepo.getSelectedSync(userId)
                val mapped = mapFertilizersWithSelection(fertilizers, selectedList)
                adapter.submitList(mapped)
                onDataChanged(mapped.isEmpty())
            }
        }

        scope.launch {
            selectedRepo.observeSelected(userId).collectLatest { selectedList ->
                val selectedIds = selectedList.map { it.fertilizerId }.toSet()
                adapter.updateSelection(selectedIds)
            }
        }
    }

    private fun mapFertilizersWithSelection(
        fertilizers: List<Fertilizer>,
        selectedList: List<SelectedFertilizer>
    ): List<Fertilizer> {
        val selectedIds = selectedList.map { it.fertilizerId }.toSet()
        val selectedMap = selectedList.associateBy { it.fertilizerId }

        return fertilizers.map { fertilizer ->
            fertilizer.apply {
                val selected = selectedMap[id]
                isSelected = selectedIds.contains(id)
                displayPrice = selected?.displayPrice.orEmpty()
                selectedPrice = if (isSelected) selected?.fertilizerPrice ?: 0.0 else 0.0
            }
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