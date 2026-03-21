package com.akilimo.mobile.ui.fragments

import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
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

    override fun mapTaskToDestination(task: EnumAdviceTask): Int? = when (task) {
        EnumAdviceTask.PLANTING_AND_HARVEST -> R.id.datesFragment
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> R.id.cassavaMarketFragment
        EnumAdviceTask.CURRENT_CASSAVA_YIELD -> R.id.cassavaYieldFragment
        EnumAdviceTask.MANUAL_TILLAGE_COST -> R.id.manualTillageCostFragment
        EnumAdviceTask.TRACTOR_ACCESS -> R.id.tractorAccessFragment
        EnumAdviceTask.COST_OF_WEED_CONTROL -> R.id.weedControlCostsFragment
        else -> null
    }
}
