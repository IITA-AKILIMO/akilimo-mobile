package com.akilimo.mobile.ui.fragments

import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FrFragment : AbstractRecommendationFragment() {

    override val enumUseCase = EnumUseCase.FR

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.AVAILABLE_FERTILIZERS),
        UseCaseOption(EnumAdviceTask.INVESTMENT_AMOUNT),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
    )

    override fun mapTaskToDestination(task: EnumAdviceTask): Int? = when (task) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS -> R.id.fertilizerFragment
        EnumAdviceTask.INVESTMENT_AMOUNT -> R.id.investmentAmountFragment
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> R.id.cassavaMarketFragment
        EnumAdviceTask.CURRENT_CASSAVA_YIELD -> R.id.cassavaYieldFragment
        else -> null
    }
}
