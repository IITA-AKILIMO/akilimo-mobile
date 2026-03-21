package com.akilimo.mobile.ui.fragments

import android.content.Intent
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.CassavaMarketActivity
import com.akilimo.mobile.ui.usecases.CassavaYieldActivity
import com.akilimo.mobile.ui.usecases.DatesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SphFragment : AbstractRecommendationFragment() {

    override val enumUseCase = EnumUseCase.SP

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
    )

    override fun mapTaskToIntent(task: EnumAdviceTask): Intent? = when (task) {
        EnumAdviceTask.PLANTING_AND_HARVEST -> Intent(requireContext(), DatesActivity::class.java)
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(requireContext(), CassavaMarketActivity::class.java)
        EnumAdviceTask.CURRENT_CASSAVA_YIELD -> Intent(requireContext(), CassavaYieldActivity::class.java)
        else -> null
    }
}
