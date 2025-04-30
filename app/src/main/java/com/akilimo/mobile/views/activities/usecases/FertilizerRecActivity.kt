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
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.FertilizersActivity
import com.akilimo.mobile.views.activities.InvestmentAmountActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import io.sentry.Sentry

class FertilizerRecActivity : BaseRecommendationActivity() {
    private var _binding: ActivityFertilizerRecBinding? = null
    private val binding get() = _binding!!
    private var mAdapter: RecOptionsAdapter = RecOptionsAdapter(emptyList())
    private var dataPositionChanged = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFertilizerRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dataSet = getRecommendationOptions()
        mAdapter = RecOptionsAdapter(dataSet)

        binding.apply {

            setupToolbar(toolbarLayout.toolbar, R.string.lbl_fertilizer_recommendations) {
                closeActivity(false)
            }

            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@FertilizerRecActivity)
                setHasFixedSize(true)
                adapter = mAdapter
            }

            singleButton.btnAction.setOnClickListener {
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
            override fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int) {
                var intent: Intent? = null
                val advice = obj?.adviceName
                dataPositionChanged = position
                when (advice) {
                    EnumAdviceTasks.PLANTING_AND_HARVEST -> intent =
                        Intent(this@FertilizerRecActivity, DatesActivity::class.java)

                    EnumAdviceTasks.AVAILABLE_FERTILIZERS -> intent =
                        Intent(this@FertilizerRecActivity, FertilizersActivity::class.java)

                    EnumAdviceTasks.INVESTMENT_AMOUNT -> intent =
                        Intent(this@FertilizerRecActivity, InvestmentAmountActivity::class.java)

                    EnumAdviceTasks.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@FertilizerRecActivity, RootYieldActivity::class.java)

                    EnumAdviceTasks.MARKET_OUTLET_CASSAVA -> intent =
                        Intent(this@FertilizerRecActivity, CassavaMarketActivity::class.java)

                    else -> EnumAdviceTasks.NOT_SELECTED
                }

                if (intent != null) {
                    startActivity(intent)
                    openActivity()
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        val recList = getRecommendationOptions()
        mAdapter.setData(recList, dataPositionChanged)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun getRecommendationOptions(): List<RecommendationOptions> {
        val plantingString = getString(R.string.lbl_planting_harvest)
        val fertilizerString = getString(R.string.lbl_available_fertilizers)
        val investmentString = getString(R.string.lbl_investment_amount)
        val rootYieldString = getString(R.string.lbl_typical_yield)
        val marketOutletString = getString(R.string.lbl_market_outlet)

        val myItems: MutableList<RecommendationOptions> = ArrayList()
        myItems.add(
            RecommendationOptions(
                marketOutletString,
                EnumAdviceTasks.MARKET_OUTLET_CASSAVA,
                checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)
            )
        )
        myItems.add(
            RecommendationOptions(
                fertilizerString,
                EnumAdviceTasks.AVAILABLE_FERTILIZERS,
                checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS)
            )
        )
        myItems.add(
            RecommendationOptions(
                investmentString,
                EnumAdviceTasks.INVESTMENT_AMOUNT,
                checkStatus(EnumAdviceTasks.INVESTMENT_AMOUNT)
            )
        )
        myItems.add(
            RecommendationOptions(
                rootYieldString,
                EnumAdviceTasks.CURRENT_CASSAVA_YIELD,
                checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)
            )
        )

        return myItems
    }
}
