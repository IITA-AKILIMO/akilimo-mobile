package com.akilimo.mobile.ui.fragments

import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IcSweetPotatoFragment : AbstractRecommendationFragment() {

    override val enumUseCase = EnumUseCase.CIS

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS),
        UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET)
    )

    override fun mapTaskToDestination(task: EnumAdviceTask): Int? = when (task) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS -> R.id.sweetPotatoInterCropFertilizersFragment
        EnumAdviceTask.PLANTING_AND_HARVEST -> R.id.datesFragment
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> R.id.cassavaMarketFragment
        EnumAdviceTask.SWEET_POTATO_MARKET_OUTLET -> R.id.sweetPotatoMarketFragment
        else -> null
    }
}
