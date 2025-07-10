package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityPlantingPracticesBinding
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.ManualTillageCostActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import com.akilimo.mobile.views.activities.TractorAccessActivity
import com.akilimo.mobile.views.activities.WeedControlCostsActivity
import io.sentry.Sentry

class PlantingPracticesActivity : BaseUseCaseTaskActivity<ActivityPlantingPracticesBinding>() {


    override fun inflateBinding(): ActivityPlantingPracticesBinding {
        return ActivityPlantingPracticesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbarLayout.toolbar, R.string.lbl_best_planting_practices) {
            closeActivity(false)
        }

        setupRecyclerView(binding.recyclerView)


        binding.singleButton.btnAction.setOnClickListener {
            try {
                viewModel.insertUseCaseWithTasks(
                    useCase = EnumUseCase.PP, useCaseTasks = loadUseCaseTasks()
                )
                launchRecommendationActivity(this@PlantingPracticesActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@PlantingPracticesActivity, ex.message, Toast.LENGTH_SHORT)
                    .show()
                Sentry.captureException(ex)
            }
        }
    }

    override fun handleNavigation(useCaseTask: UseCaseTask) {
        var intent: Intent? = null
        when (useCaseTask.taskName) {
            EnumTask.PLANTING_AND_HARVEST -> intent =
                Intent(this@PlantingPracticesActivity, DatesActivity::class.java)

            EnumTask.MARKET_OUTLET_CASSAVA -> intent =
                Intent(this@PlantingPracticesActivity, CassavaMarketActivity::class.java)

            EnumTask.CURRENT_CASSAVA_YIELD -> intent =
                Intent(this@PlantingPracticesActivity, RootYieldActivity::class.java)

            EnumTask.MANUAL_TILLAGE_COST -> intent = Intent(
                this@PlantingPracticesActivity, ManualTillageCostActivity::class.java
            )

            EnumTask.TRACTOR_ACCESS -> intent =
                Intent(this@PlantingPracticesActivity, TractorAccessActivity::class.java)

            EnumTask.COST_OF_WEED_CONTROL -> intent = Intent(
                this@PlantingPracticesActivity, WeedControlCostsActivity::class.java
            )

            else -> EnumTask.NOT_SELECTED
        }
        openActivity(intent)
    }


    override fun loadUseCaseTasks(): List<UseCaseTask> = listOf(
        UseCaseTask(
            taskLabel = R.string.lbl_cost_of_manual_tillage,
            taskName = EnumTask.MANUAL_TILLAGE_COST
        ),
        UseCaseTask(taskLabel = R.string.lbl_tractor_access, taskName = EnumTask.TRACTOR_ACCESS),
        UseCaseTask(
            taskLabel = R.string.lbl_cost_of_weed_control,
            taskName = EnumTask.COST_OF_WEED_CONTROL
        ),
        UseCaseTask(
            taskLabel = R.string.lbl_typical_yield,
            taskName = EnumTask.CURRENT_CASSAVA_YIELD
        ),
        UseCaseTask(
            taskLabel = R.string.lbl_market_outlet,
            taskName = EnumTask.MARKET_OUTLET_CASSAVA
        )
    )

}
