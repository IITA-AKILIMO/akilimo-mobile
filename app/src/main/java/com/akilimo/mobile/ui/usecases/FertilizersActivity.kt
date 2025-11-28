package com.akilimo.mobile.ui.usecases

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.fertilizer.BaseFertilizerActivity

/**
 * Default fertilizer selection activity.
 * Uses country-based fertilizer fetching from the base class.
 */
class FertilizersActivity : BaseFertilizerActivity<ActivityFertilizersBinding>() {

    override val useCase = EnumUseCase.FR
    override val adviseTask: EnumAdviceTask  = EnumAdviceTask.AVAILABLE_FERTILIZERS

    override fun inflateBinding() = ActivityFertilizersBinding.inflate(layoutInflater)

    // View accessors
    override fun getToolbar() = binding.lytToolbar.toolbar
    override fun getRecyclerView(): RecyclerView = binding.fertilizersList
    override fun getRootView(): View = binding.root
    override fun getEmptyStateView(): View = binding.emptyState
    override fun getSyncIndicator(): View = binding.syncIndicator
    override fun getRefreshFab(): View = binding.fabRefresh

    // Uses default fetchFertilizers from base class (by country)
}