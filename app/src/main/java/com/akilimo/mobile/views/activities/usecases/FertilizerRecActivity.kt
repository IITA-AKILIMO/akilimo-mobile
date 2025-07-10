package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityFertilizerRecBinding
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
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
            binding.fertilizerRecToolbar.toolbar, R.string.lbl_fertilizer_recommendations
        ) {
            closeActivity(false)
        }

        setupRecyclerView(binding.fertilizerRecList)

        binding.fertilizerRecButton.btnAction.setOnClickListener {
            try {
                viewModel.insertUseCaseWithTasks(
                    useCase = EnumUseCase.FR, useCaseTasks = loadUseCaseTasks()
                )
                launchRecommendationActivity(this@FertilizerRecActivity)

            } catch (ex: Exception) {
                Toast.makeText(this@FertilizerRecActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }

        }
    }

    override fun handleNavigation(useCaseTask: UseCaseTask) {
        when (useCaseTask.taskName) {
            EnumTask.PLANTING_AND_HARVEST -> intent =
                Intent(this@FertilizerRecActivity, DatesActivity::class.java)

            EnumTask.AVAILABLE_FERTILIZERS -> {
                intent = Intent(
                    this@FertilizerRecActivity,
                    FertilizersActivity::class.java
                ).apply {
                    putExtra(
                        BaseFertilizersActivity.useCaseTag, EnumUseCase.FR.name
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

    override fun loadUseCaseTasks(): List<UseCaseTask> {
        return listOf(
            UseCaseTask(
                taskLabel = R.string.lbl_market_outlet,
                taskName = EnumTask.MARKET_OUTLET_CASSAVA,
            ),
            UseCaseTask(
                taskLabel = R.string.lbl_available_fertilizers,
                taskName = EnumTask.AVAILABLE_FERTILIZERS,
            ),
            UseCaseTask(
                taskLabel = R.string.lbl_investment_amount,
                taskName = EnumTask.INVESTMENT_AMOUNT,
            ),
            UseCaseTask(
                taskLabel = R.string.lbl_typical_yield,
                taskName = EnumTask.CURRENT_CASSAVA_YIELD,
            )
        )
    }
}
