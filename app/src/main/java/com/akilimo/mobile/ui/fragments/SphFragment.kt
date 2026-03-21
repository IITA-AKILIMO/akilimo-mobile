package com.akilimo.mobile.ui.fragments

import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SphFragment : AbstractRecommendationFragment() {

    override val enumUseCase = EnumUseCase.SP

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
    )

    override fun mapTaskToDestination(task: EnumAdviceTask): Int? = when (task) {
        EnumAdviceTask.PLANTING_AND_HARVEST -> R.id.datesFragment
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> R.id.cassavaMarketFragment
        EnumAdviceTask.CURRENT_CASSAVA_YIELD -> R.id.cassavaYieldFragment
        else -> null
    }
}
