package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.databinding.ActivityFertilizerRecBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseRecommendationActivity
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.BaseFertilizersActivity
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.FertilizersActivity
import com.akilimo.mobile.views.activities.InvestmentAmountActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import io.sentry.Sentry

class FertilizerRecActivity : BaseRecommendationActivity<ActivityFertilizerRecBinding>() {

    override fun inflateBinding(): ActivityFertilizerRecBinding {
        return ActivityFertilizerRecBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            setupToolbar(fertilizerRecToolbar.toolbar, R.string.lbl_fertilizer_recommendations) {
                closeActivity(false)
            }
            fertilizerRecList.apply {
                layoutManager = LinearLayoutManager(this@FertilizerRecActivity)
                setHasFixedSize(true)
                adapter = mAdapter
            }

            fertilizerRecButton.btnAction.setOnClickListener {
                try {
                    var useCase = database.useCaseDao().findOne()
                    if (useCase == null) {
                        useCase = UseCase()
                    }
                    useCase.fertilizerRecommendation = true
                    useCase.maizeInterCropping = false
                    useCase.sweetPotatoInterCropping = false
                    useCase.scheduledPlantingHighStarch = false
                    useCase.scheduledPlanting = false
                    useCase.bestPlantingPractices = false
                    useCase.useCaseName = EnumUseCase.FR.name

                    database.useCaseDao().insertAll(useCase)
                    processRecommendations(this@FertilizerRecActivity)
                } catch (ex: Exception) {
                    Toast.makeText(this@FertilizerRecActivity, ex.message, Toast.LENGTH_SHORT)
                        .show()
                    Sentry.captureException(ex)
                }
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
                    EnumAdviceTask.PLANTING_AND_HARVEST ->
                        intent = Intent(this@FertilizerRecActivity, DatesActivity::class.java)

                    EnumAdviceTask.AVAILABLE_FERTILIZERS -> {
                        intent = Intent(this@FertilizerRecActivity, FertilizersActivity::class.java)
                            .apply {
                                putExtra(
                                    BaseFertilizersActivity.useCaseTag,
                                    EnumUseCase.FR.name
                                )
                            }
                    }

                    EnumAdviceTask.INVESTMENT_AMOUNT -> intent =
                        Intent(this@FertilizerRecActivity, InvestmentAmountActivity::class.java)

                    EnumAdviceTask.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@FertilizerRecActivity, RootYieldActivity::class.java)

                    EnumAdviceTask.MARKET_OUTLET_CASSAVA -> intent =
                        Intent(this@FertilizerRecActivity, CassavaMarketActivity::class.java)

                    else -> EnumAdviceTask.NOT_SELECTED
                }
                openActivity(intent)
            }
        })
    }

    override fun getRecommendationOptions(): List<RecommendationOptions> {
        val fertilizerString = getString(R.string.lbl_available_fertilizers)
        val investmentString = getString(R.string.lbl_investment_amount)
        val rootYieldString = getString(R.string.lbl_typical_yield)
        val marketOutletString = getString(R.string.lbl_market_outlet)

        val myItems: MutableList<RecommendationOptions> = ArrayList()
        myItems.add(
            RecommendationOptions(
                recommendationName = marketOutletString,
                adviceName = EnumAdviceTask.MARKET_OUTLET_CASSAVA,
                adviceStatus = checkStatus(EnumAdviceTask.MARKET_OUTLET_CASSAVA)
            )
        )
        myItems.add(
            RecommendationOptions(
                recommendationName = fertilizerString,
                adviceName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
                adviceStatus = checkStatus(EnumAdviceTask.AVAILABLE_FERTILIZERS)
            )
        )
        myItems.add(
            RecommendationOptions(
                recommendationName = investmentString,
                adviceName = EnumAdviceTask.INVESTMENT_AMOUNT,
                adviceStatus = checkStatus(EnumAdviceTask.INVESTMENT_AMOUNT)
            )
        )
        myItems.add(
            RecommendationOptions(
                recommendationName = rootYieldString,
                adviceName = EnumAdviceTask.CURRENT_CASSAVA_YIELD,
                adviceStatus = checkStatus(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
            )
        )

        return myItems
    }
}
