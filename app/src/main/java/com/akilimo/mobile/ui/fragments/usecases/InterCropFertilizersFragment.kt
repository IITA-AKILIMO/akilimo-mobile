package com.akilimo.mobile.ui.fragments.usecases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.ui.usecases.fertilizer.BaseFertilizerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterCropFertilizersFragment : BaseFertilizerFragment<ActivityFertilizersBinding>() {

    override val adviseTask = EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM
    override val fertilizerFlow = EnumFertilizerFlow.CIM

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityFertilizersBinding.inflate(inflater, container, false)

    override fun getToolbar() = binding.lytToolbar.toolbar
    override fun getRecyclerView(): RecyclerView = binding.fertilizersList
    override fun getRootView(): View = binding.root
    override fun getEmptyStateView(): View = binding.emptyState
    override fun getSyncIndicator(): View = binding.syncIndicator
    override fun getRefreshFab(): View = binding.fabRefresh
}
