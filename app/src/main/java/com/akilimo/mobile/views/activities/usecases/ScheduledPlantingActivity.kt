package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.UseCaseTaskAdapter
import com.akilimo.mobile.databinding.ActivityScheduledPlantingBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
import com.akilimo.mobile.models.UseCaseWithTasks
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
        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(this@ScheduledPlantingActivity)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        binding.singleButton.btnAction.setOnClickListener {
            var useCases = database.useCaseDao().findOne()
            try {
                if (useCases == null) {
                    useCases = UseCase()
                }
                useCases.apply {
                    useCases.fertilizerRecommendation = false
                    useCases.maizeInterCropping = false
                    useCases.sweetPotatoInterCropping = false
                    useCases.scheduledPlantingHighStarch = true
                    useCases.scheduledPlanting = true
                    useCases.bestPlantingPractices = false
                    useCases.useCaseName = EnumUseCase.SP.name
                }

                database.useCaseDao().insertAll(useCases)
                processRecommendations(this@ScheduledPlantingActivity)
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }

        // on item list clicked
        mAdapter.setOnItemClickListener(object : UseCaseTaskAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, recommendation: UseCaseWithTasks, position: Int) {
                var intent: Intent? = null
                dataPositionChanged = position
                val advice = recommendation.adviceName
                when (advice) {
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
        })
    }

    override fun getRecommendationOptions(): List<UseCaseWithTasks> {
        val plantingString = getString(R.string.lbl_planting_harvest)
        val marketOutletString = getString(R.string.lbl_market_outlet)
        val rootYieldString = getString(R.string.lbl_typical_yield)

        val myItems: MutableList<UseCaseWithTasks> =
            ArrayList()
        myItems.add(
            UseCaseWithTasks(
                plantingString,
                EnumTask.PLANTING_AND_HARVEST,
                checkStatus(EnumTask.PLANTING_AND_HARVEST)
            )
        )
        myItems.add(
            UseCaseWithTasks(
                rootYieldString,
                EnumTask.CURRENT_CASSAVA_YIELD,
                checkStatus(EnumTask.CURRENT_CASSAVA_YIELD)
            )
        )
        myItems.add(
            UseCaseWithTasks(
                marketOutletString,
                EnumTask.MARKET_OUTLET_CASSAVA,
                checkStatus(EnumTask.MARKET_OUTLET_CASSAVA)
            )
        )
        return myItems
    }
}
