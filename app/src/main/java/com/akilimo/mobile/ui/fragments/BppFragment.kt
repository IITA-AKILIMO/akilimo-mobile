package com.akilimo.mobile.ui.fragments

import android.content.Intent
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.CassavaMarketActivity
import com.akilimo.mobile.ui.usecases.CassavaYieldActivity
import com.akilimo.mobile.ui.usecases.DatesActivity
import com.akilimo.mobile.ui.usecases.ManualTillageCostActivity
import com.akilimo.mobile.ui.usecases.TractorAccessActivity
import com.akilimo.mobile.ui.usecases.WeedControlCostsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BppFragment : AbstractRecommendationFragment() {

    override val enumUseCase = EnumUseCase.PP

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.CURRENT_CASSAVA_YIELD),
        UseCaseOption(EnumAdviceTask.MANUAL_TILLAGE_COST),
        UseCaseOption(EnumAdviceTask.TRACTOR_ACCESS),
        UseCaseOption(EnumAdviceTask.COST_OF_WEED_CONTROL)
    )

    override fun mapTaskToIntent(task: EnumAdviceTask): Intent? = when (task) {
        EnumAdviceTask.PLANTING_AND_HARVEST -> Intent(requireContext(), DatesActivity::class.java)
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(requireContext(), CassavaMarketActivity::class.java)
        EnumAdviceTask.CURRENT_CASSAVA_YIELD -> Intent(requireContext(), CassavaYieldActivity::class.java)
        EnumAdviceTask.MANUAL_TILLAGE_COST -> Intent(requireContext(), ManualTillageCostActivity::class.java)
        EnumAdviceTask.TRACTOR_ACCESS -> Intent(requireContext(), TractorAccessActivity::class.java)
        EnumAdviceTask.COST_OF_WEED_CONTROL -> Intent(requireContext(), WeedControlCostsActivity::class.java)
        else -> null
    }
}
