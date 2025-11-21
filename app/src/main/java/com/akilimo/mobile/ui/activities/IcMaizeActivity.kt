package com.akilimo.mobile.ui.activities

import android.content.Intent
import com.akilimo.mobile.base.AbstractRecommendationActivity
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.usecases.CassavaMarketActivity
import com.akilimo.mobile.ui.usecases.DatesActivity
import com.akilimo.mobile.ui.usecases.InterCropFertilizersActivity

class IcMaizeActivity : AbstractRecommendationActivity() {

    override fun getAdviceOptions() = listOf(
        UseCaseOption(EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM),
        UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
        UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.MAIZE_MARKET_OUTLET),
        UseCaseOption(EnumAdviceTask.MAIZE_PERFORMANCE),
    )

    override fun mapTaskToIntent(task: EnumAdviceTask): Intent? = when (task) {

        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM -> Intent(
            this,
            InterCropFertilizersActivity::class.java
        )

        EnumAdviceTask.PLANTING_AND_HARVEST -> Intent(
            this, DatesActivity::class.java
        )

        EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(
            this, CassavaMarketActivity::class.java
        )

        EnumAdviceTask.MAIZE_MARKET_OUTLET -> Intent(
            this, CassavaMarketActivity::class.java
        )

        EnumAdviceTask.MAIZE_PERFORMANCE -> Intent(
            this, CassavaMarketActivity::class.java
        )

        else -> null
    }
}