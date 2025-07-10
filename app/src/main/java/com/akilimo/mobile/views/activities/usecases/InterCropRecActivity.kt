package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityInterCropRecBinding
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.BaseFertilizersActivity
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.InterCropFertilizersActivity
import com.akilimo.mobile.views.activities.MaizeMarketActivity
import com.akilimo.mobile.views.activities.MaizePerformanceActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import com.akilimo.mobile.views.activities.SweetPotatoMarketActivity
import io.sentry.Sentry

class InterCropRecActivity : BaseUseCaseTaskActivity<ActivityInterCropRecBinding>() {

    private var icMaize = false
    private var icPotato = false

    override fun inflateBinding(): ActivityInterCropRecBinding {
        return ActivityInterCropRecBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val useCase = when (countryCode) {
            "NG" -> EnumUseCase.CIM
            "TZ" -> EnumUseCase.CIS
            else -> EnumUseCase.NA
        }

        val title = when (countryCode) {
            "NG" -> R.string.title_maize_intercropping
            "TZ" -> R.string.title_sweet_potato_intercropping
            else -> R.string.lbl_intercropping
        }
        setupToolbar(binding.toolbarLayout.toolbar, title) {
            closeActivity(false)
        }

        setupRecyclerView(binding.recyclerView)

        binding.singleButton.btnAction.setOnClickListener {
            try {
                viewModel.insertUseCaseWithTasks(
                    useCase = useCase, useCaseTasks = loadUseCaseTasks()
                )
                launchRecommendationActivity(this@InterCropRecActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@InterCropRecActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }
    }


    override fun loadUseCaseTasks(): List<UseCaseTask> {
        val items = mutableListOf<UseCaseTask>()
        if (countryCode.equals(EnumCountry.Nigeria.countryCode(), ignoreCase = true)) {
            icMaize = true
            items.addAll(
                listOf(
                    UseCaseTask(
                        taskLabel = R.string.lbl_available_fertilizers,
                        taskName = EnumTask.AVAILABLE_FERTILIZERS_CIM
                    ),
                    UseCaseTask(
                        taskLabel = R.string.lbl_maize_performance,
                        taskName = EnumTask.MAIZE_PERFORMANCE
                    ), UseCaseTask(
                        taskLabel = R.string.lbl_market_outlet_maize,
                        taskName = EnumTask.MARKET_OUTLET_MAIZE
                    )
                )
            )
        } else if (countryCode.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
            icPotato = true
            items.addAll(
                listOf(
                    UseCaseTask(
                        taskLabel = R.string.lbl_available_fertilizers,
                        taskName = EnumTask.AVAILABLE_FERTILIZERS_CIS
                    ),
                    UseCaseTask(
                        taskLabel = R.string.lbl_market_outlet,
                        taskName = EnumTask.MARKET_OUTLET_CASSAVA
                    ),
                    UseCaseTask(
                        taskLabel = R.string.lbl_typical_yield,
                        taskName = EnumTask.CURRENT_CASSAVA_YIELD
                    ),
                    UseCaseTask(
                        taskLabel = R.string.lbl_sweet_potato_prices,
                        taskName = EnumTask.MARKET_OUTLET_SWEET_POTATO
                    )
                )
            )
        }

        return items
    }

    override fun handleNavigation(useCaseTask: UseCaseTask) {
        val context = this@InterCropRecActivity
        val intent = when (useCaseTask.taskName) {
            EnumTask.PLANTING_AND_HARVEST -> Intent(
                context,
                DatesActivity::class.java
            )

            EnumTask.MARKET_OUTLET_CASSAVA -> Intent(
                context,
                CassavaMarketActivity::class.java
            ).apply {
                putExtra(CassavaMarketActivity.useCaseTag, useCaseTask.useCaseId)
            }

            EnumTask.MARKET_OUTLET_SWEET_POTATO -> Intent(
                context,
                SweetPotatoMarketActivity::class.java
            )

            EnumTask.MARKET_OUTLET_MAIZE -> Intent(
                context,
                MaizeMarketActivity::class.java
            )

            EnumTask.CURRENT_CASSAVA_YIELD -> Intent(
                context,
                RootYieldActivity::class.java
            )

            EnumTask.AVAILABLE_FERTILIZERS_CIM,
            EnumTask.AVAILABLE_FERTILIZERS_CIS -> Intent(
                context,
                InterCropFertilizersActivity::class.java
            ).apply {
                putExtra(
                    BaseFertilizersActivity.useCaseTag,
                    EnumUseCase.FR.name
                )
            }

            EnumTask.MAIZE_PERFORMANCE -> Intent(
                context,
                MaizePerformanceActivity::class.java
            )

            else -> null
        }
        openActivity(intent)
    }
}

