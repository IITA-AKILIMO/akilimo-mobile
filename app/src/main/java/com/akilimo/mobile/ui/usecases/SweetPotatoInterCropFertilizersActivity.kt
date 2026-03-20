package com.akilimo.mobile.ui.usecases

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.fertilizer.BaseFertilizerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SweetPotatoInterCropFertilizersActivity :
    BaseFertilizerActivity<ActivityFertilizersBinding>() {

    override val useCase = EnumUseCase.CIS
    override val adviseTask: EnumAdviceTask = EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS
    override val fertilizerFlow = EnumFertilizerFlow.CIS

    override fun inflateBinding() = ActivityFertilizersBinding.inflate(layoutInflater)

    override fun getToolbar() = binding.lytToolbar.toolbar
    override fun getRecyclerView(): RecyclerView = binding.fertilizersList
    override fun getRootView(): View = binding.root
    override fun getEmptyStateView(): View = binding.emptyState
    override fun getSyncIndicator(): View = binding.syncIndicator
    override fun getRefreshFab(): View = binding.fabRefresh
}