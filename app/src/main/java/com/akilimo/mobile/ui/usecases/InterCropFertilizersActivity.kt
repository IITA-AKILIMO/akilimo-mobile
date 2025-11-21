package com.akilimo.mobile.ui.usecases

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.databinding.ActivityFertilizersBinding
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.fertilizer.BaseFertilizerActivity
import kotlinx.coroutines.flow.Flow

class InterCropFertilizersActivity : BaseFertilizerActivity<ActivityFertilizersBinding>() {

    override val useCase = EnumUseCase.CIM
    override val adviseTask: EnumAdviceTask = EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM

    override fun inflateBinding() = ActivityFertilizersBinding.inflate(layoutInflater)

    override fun getToolbar() = binding.lytToolbar.toolbar
    override fun getRecyclerView(): RecyclerView = binding.fertilizersList
    override fun getRootView(): View = binding.root
    override fun getEmptyStateView(): View = binding.emptyState
    override fun getSyncIndicator(): View = binding.syncIndicator
    override fun getRefreshFab(): View = binding.fabRefresh
    
    override fun fetchFertilizers(country: EnumCountry): Flow<List<Fertilizer>> {
        return fertilizerRepo.observeByCountryAndUseCase(country, useCase)
    }
}