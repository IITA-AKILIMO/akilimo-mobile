package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.databinding.ActivityPlantingPracticesBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseRecommendationActivity
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.ManualTillageCostActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import com.akilimo.mobile.views.activities.TractorAccessActivity
import com.akilimo.mobile.views.activities.WeedControlCostsActivity
import io.sentry.Sentry

class PlantingPracticesActivity : BaseRecommendationActivity<ActivityPlantingPracticesBinding>() {


    override fun inflateBinding(): ActivityPlantingPracticesBinding {
        return ActivityPlantingPracticesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbarLayout.toolbar, R.string.lbl_best_planting_practices) {
            closeActivity(false)
        }
        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(this@PlantingPracticesActivity)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        var useCase = database.useCaseDao().findOne()
        if (useCase == null) {
            useCase = UseCase()
        }
        binding.singleButton.btnAction.setOnClickListener {
            try {
                useCase.apply {
                    fertilizerRecommendation = false
                    maizeInterCropping = false
                    sweetPotatoInterCropping = false
                    scheduledPlantingHighStarch = false
                    scheduledPlanting = false
                    bestPlantingPractices = true
                    useCaseName = EnumUseCase.PP.name
                }
                database.useCaseDao().insertAll(useCase)
                processRecommendations(this@PlantingPracticesActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@PlantingPracticesActivity, ex.message, Toast.LENGTH_SHORT)
                    .show()
                Sentry.captureException(ex)
            }
        }


        mAdapter.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View?,
                recommendation: RecommendationOptions,
                position: Int
            ) {
                var intent: Intent? = null
                val advice = recommendation.adviceName
                dataPositionChanged = position
                when (advice) {
                    EnumAdviceTask.PLANTING_AND_HARVEST -> intent =
                        Intent(this@PlantingPracticesActivity, DatesActivity::class.java)

                    EnumAdviceTask.MARKET_OUTLET_CASSAVA -> intent =
                        Intent(this@PlantingPracticesActivity, CassavaMarketActivity::class.java)

                    EnumAdviceTask.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@PlantingPracticesActivity, RootYieldActivity::class.java)

                    EnumAdviceTask.MANUAL_TILLAGE_COST -> intent = Intent(
                        this@PlantingPracticesActivity, ManualTillageCostActivity::class.java
                    )

                    EnumAdviceTask.TRACTOR_ACCESS -> intent =
                        Intent(this@PlantingPracticesActivity, TractorAccessActivity::class.java)

                    EnumAdviceTask.COST_OF_WEED_CONTROL -> intent = Intent(
                        this@PlantingPracticesActivity, WeedControlCostsActivity::class.java
                    )

                    else -> EnumAdviceTask.NOT_SELECTED
                }
                openActivity(intent)

            }
        })
    }


    override fun getRecommendationOptions(): List<RecommendationOptions> {
        val recommendations = getString(R.string.lbl_best_planting_practices)
        val plantingString = getString(R.string.lbl_planting_harvest)
        val marketOutletString = getString(R.string.lbl_market_outlet)
        val weedControlCostString = getString(R.string.lbl_cost_of_weed_control)
        val rootYieldString = getString(R.string.lbl_typical_yield)
        val tillageOperationsString = getString(R.string.lbl_tillage_operations)
        val manualTillageCostsString = getString(R.string.lbl_cost_of_manual_tillage)
        val tractorAccessString = getString(R.string.lbl_tractor_access)

        val myItems: MutableList<RecommendationOptions> = ArrayList()
        myItems.add(
            RecommendationOptions(
                manualTillageCostsString,
                EnumAdviceTask.MANUAL_TILLAGE_COST,
                checkStatus(EnumAdviceTask.MANUAL_TILLAGE_COST)
            )
        )
        myItems.add(
            RecommendationOptions(
                tractorAccessString,
                EnumAdviceTask.TRACTOR_ACCESS,
                checkStatus(EnumAdviceTask.TRACTOR_ACCESS)
            )
        )
        myItems.add(
            RecommendationOptions(
                weedControlCostString,
                EnumAdviceTask.COST_OF_WEED_CONTROL,
                checkStatus(EnumAdviceTask.COST_OF_WEED_CONTROL)
            )
        )
        myItems.add(
            RecommendationOptions(
                rootYieldString,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
                checkStatus(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
            )
        )
        myItems.add(
            RecommendationOptions(
                marketOutletString,
                EnumAdviceTask.MARKET_OUTLET_CASSAVA,
                checkStatus(EnumAdviceTask.MARKET_OUTLET_CASSAVA)
            )
        )
        return myItems
    }

}
