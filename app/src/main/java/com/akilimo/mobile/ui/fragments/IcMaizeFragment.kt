package com.akilimo.mobile.ui.fragments

import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IcMaizeFragment : AbstractRecommendationFragment() {

    override val enumUseCase = EnumUseCase.CIM

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM),
        UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.MAIZE_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.MAIZE_PERFORMANCE)
    )

    override fun mapTaskToDestination(task: EnumAdviceTask): Int? = when (task) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM -> R.id.interCropFertilizersFragment
        EnumAdviceTask.PLANTING_AND_HARVEST -> R.id.datesFragment
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> R.id.cassavaMarketFragment
        EnumAdviceTask.MAIZE_MARKET_OUTLET -> R.id.maizeMarketFragment
        EnumAdviceTask.MAIZE_PERFORMANCE -> R.id.maizePerformanceFragment
        else -> null
    }
}
