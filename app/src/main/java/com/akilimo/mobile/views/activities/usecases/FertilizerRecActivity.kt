package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.UseCaseTaskAdapter
import com.akilimo.mobile.databinding.ActivityFertilizerRecBinding
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.BaseFertilizersActivity
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.FertilizersActivity
import com.akilimo.mobile.views.activities.InvestmentAmountActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import io.sentry.Sentry

class FertilizerRecActivity : BaseUseCaseTaskActivity<ActivityFertilizerRecBinding>() {

    override fun inflateBinding(): ActivityFertilizerRecBinding {
        return ActivityFertilizerRecBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setupToolbar(
            binding.fertilizerRecToolbar.toolbar,
            R.string.lbl_fertilizer_recommendations
        ) {
            closeActivity(false)
        }

        setupRecyclerView(binding.fertilizerRecList)

        binding.fertilizerRecButton.btnAction.setOnClickListener {
            try {
                viewModel.insertUseCaseWithTasks(
                    useCase = EnumUseCase.FR,
                    tasks = listOf(
                        EnumTask.MARKET_OUTLET_CASSAVA,
                        EnumTask.AVAILABLE_FERTILIZERS,
                        EnumTask.INVESTMENT_AMOUNT,
                        EnumTask.CURRENT_CASSAVA_YIELD
                    )
                )
                processRecommendations(this@FertilizerRecActivity)

            } catch (ex: Exception) {
                Toast.makeText(this@FertilizerRecActivity, ex.message, Toast.LENGTH_SHORT)
                    .show()
                Sentry.captureException(ex)
            }

        }

        mAdapter.setOnItemClickListener(object : UseCaseTaskAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View?,
                recommendation: UseCaseWithTasks,
                position: Int
            ) {
                var intent: Intent? = null
                val advice = recommendation.adviceName
                dataPositionChanged = position
                when (advice) {
                    EnumTask.PLANTING_AND_HARVEST ->
                        intent = Intent(this@FertilizerRecActivity, DatesActivity::class.java)

                    EnumTask.AVAILABLE_FERTILIZERS -> {
                        intent = Intent(this@FertilizerRecActivity, FertilizersActivity::class.java)
                            .apply {
                                putExtra(
                                    BaseFertilizersActivity.useCaseTag,
                                    EnumUseCase.FR.name
                                )
                            }
                    }

                    EnumTask.INVESTMENT_AMOUNT -> intent =
                        Intent(this@FertilizerRecActivity, InvestmentAmountActivity::class.java)

                    EnumTask.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@FertilizerRecActivity, RootYieldActivity::class.java)

                    EnumTask.MARKET_OUTLET_CASSAVA -> intent =
                        Intent(this@FertilizerRecActivity, CassavaMarketActivity::class.java)

                    else -> EnumTask.NOT_SELECTED
                }
                openActivity(intent)
            }
        })
    }

    override fun getRecommendationOptions(): List<UseCaseWithTasks> {
        val fertilizerString = getString(R.string.lbl_available_fertilizers)
        val investmentString = getString(R.string.lbl_investment_amount)
        val rootYieldString = getString(R.string.lbl_typical_yield)
        val marketOutletString = getString(R.string.lbl_market_outlet)

        val myItems: MutableList<UseCaseWithTasks> = ArrayList()
        myItems.add(
            UseCaseWithTasks(
                useCase = marketOutletString,
                adviceName = EnumTask.MARKET_OUTLET_CASSAVA,
                adviceStatus = checkStatus(EnumTask.MARKET_OUTLET_CASSAVA)
            )
        )
        myItems.add(
            UseCaseWithTasks(
                useCase = fertilizerString,
                adviceName = EnumTask.AVAILABLE_FERTILIZERS,
                adviceStatus = checkStatus(EnumTask.AVAILABLE_FERTILIZERS)
            )
        )
        myItems.add(
            UseCaseWithTasks(
                useCase = investmentString,
                adviceName = EnumTask.INVESTMENT_AMOUNT,
                adviceStatus = checkStatus(EnumTask.INVESTMENT_AMOUNT)
            )
        )
        myItems.add(
            UseCaseWithTasks(
                useCase = rootYieldString,
                adviceName = EnumTask.CURRENT_CASSAVA_YIELD,
                adviceStatus = checkStatus(EnumTask.CURRENT_CASSAVA_YIELD)
            )
        )

        return myItems
    }
}
