package com.akilimo.mobile.ui.fragments

import android.content.Intent
import com.akilimo.mobile.base.AbstractRecommendationFragment
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.usecases.CassavaMarketActivity
import com.akilimo.mobile.ui.usecases.DatesActivity
import com.akilimo.mobile.ui.usecases.InterCropFertilizersActivity
import com.akilimo.mobile.ui.usecases.MaizeMarketActivity
import com.akilimo.mobile.ui.usecases.MaizePerformanceActivity
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

    override fun mapTaskToIntent(task: EnumAdviceTask): Intent? = when (task) {
        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM -> Intent(requireContext(), InterCropFertilizersActivity::class.java)
        EnumAdviceTask.PLANTING_AND_HARVEST -> Intent(requireContext(), DatesActivity::class.java)
        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(requireContext(), CassavaMarketActivity::class.java)
        EnumAdviceTask.MAIZE_MARKET_OUTLET -> Intent(requireContext(), MaizeMarketActivity::class.java)
        EnumAdviceTask.MAIZE_PERFORMANCE -> Intent(requireContext(), MaizePerformanceActivity::class.java)
        else -> null
    }
}
