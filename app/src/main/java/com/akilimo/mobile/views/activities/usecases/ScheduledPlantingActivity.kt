package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityScheduledPlantingBinding
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import io.sentry.Sentry

class ScheduledPlantingActivity : BaseUseCaseTaskActivity<ActivityScheduledPlantingBinding>() {
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null

    override fun inflateBinding(): ActivityScheduledPlantingBinding {
        return ActivityScheduledPlantingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(binding.toolbarLayout.toolbar, R.string.lbl_scheduled_planting_and_harvest) {
            closeActivity(false)
        }

        setupRecyclerView(binding.recyclerView)


        binding.singleButton.btnAction.setOnClickListener {
            try {
                viewModel.insertUseCaseWithTasks(
                    useCase = EnumUseCase.SP, useCaseTasks = loadUseCaseTasks()
                )
                launchRecommendationActivity(this@ScheduledPlantingActivity)
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }
    }

    override fun handleNavigation(useCaseTask: UseCaseTask) {
        when (useCaseTask.taskName) {
            EnumTask.PLANTING_AND_HARVEST -> intent =
                Intent(this@ScheduledPlantingActivity, DatesActivity::class.java)

            EnumTask.MARKET_OUTLET_CASSAVA -> intent =
                Intent(this@ScheduledPlantingActivity, CassavaMarketActivity::class.java)

            EnumTask.CURRENT_CASSAVA_YIELD -> intent =
                Intent(this@ScheduledPlantingActivity, RootYieldActivity::class.java)

            else -> {}
        }
        openActivity(intent)
    }

    override fun loadUseCaseTasks(): List<UseCaseTask> = listOf(
        UseCaseTask(
            taskLabel = R.string.lbl_planting_harvest,
            taskName = EnumTask.PLANTING_AND_HARVEST
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
