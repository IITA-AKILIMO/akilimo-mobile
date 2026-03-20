package com.akilimo.mobile.ui.fragments

import android.content.Intent
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.CassavaMarketActivity
import com.akilimo.mobile.ui.usecases.CassavaYieldActivity
import com.akilimo.mobile.ui.usecases.FertilizersActivity
import com.akilimo.mobile.ui.usecases.InvestmentAmountActivity
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

    override fun mapTaskToIntent(task: EnumAdviceTask): Intent? = when (task) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS -> Intent(requireContext(), FertilizersActivity::class.java)
        EnumAdviceTask.INVESTMENT_AMOUNT -> Intent(requireContext(), InvestmentAmountActivity::class.java)
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(requireContext(), CassavaMarketActivity::class.java)
        EnumAdviceTask.CURRENT_CASSAVA_YIELD -> Intent(requireContext(), CassavaYieldActivity::class.java)
        else -> null
    }
}
