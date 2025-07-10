package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.UseCaseTaskAdapter
import com.akilimo.mobile.databinding.ActivityInterCropRecBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseUseCaseTaskActivity
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.utils.enums.EnumTask
import com.akilimo.mobile.utils.enums.EnumCountry
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
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        val useCase = when (countryCode) {
            "NG" -> EnumUseCase.CIM.name
            "TZ" -> EnumUseCase.CIS.name
            else -> EnumUseCase.NA.name
        }

        val title = when (countryCode) {
            "NG" -> R.string.title_maize_intercropping
            "TZ" -> R.string.title_sweet_potato_intercropping
            else -> R.string.lbl_intercropping
        }
        setupToolbar(binding.toolbarLayout.toolbar, title) {
            closeActivity(false)
        }


        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = mAdapter
        }

        val context = this@InterCropRecActivity
        mAdapter.setOnItemClickListener(object : UseCaseTaskAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, recommendation: UseCaseWithTasks, position: Int) {
                dataPositionChanged = position
                val intent = when (recommendation.adviceName) {
                    EnumTask.PLANTING_AND_HARVEST -> Intent(
                        context,
                        DatesActivity::class.java
                    )

                    EnumTask.MARKET_OUTLET_CASSAVA -> Intent(
                        context,
                        CassavaMarketActivity::class.java
                    ).apply {
                        putExtra(CassavaMarketActivity.useCaseTag, useCase)
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
                            useCase
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
        })


        binding.singleButton.btnAction.setOnClickListener {
            try {
                val useCases = database.useCaseDao().findOne() ?: UseCase()
                useCases.apply {
                    fertilizerRecommendation = false
                    maizeInterCropping = icMaize
                    sweetPotatoInterCropping = icPotato
                    scheduledPlantingHighStarch = false
                    scheduledPlanting = false
                    bestPlantingPractices = false
                    this.useCase = useCase
                }

                database.useCaseDao().insertAll(useCases)
                processRecommendations(this@InterCropRecActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@InterCropRecActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }
    }


    override fun getRecommendationOptions(): List<UseCaseWithTasks> {
        val items = mutableListOf<UseCaseWithTasks>()
        if (countryCode.equals(EnumCountry.Nigeria.countryCode(), ignoreCase = true)) {
            icMaize = true
            items.addAll(
                listOf(
                    UseCaseWithTasks(
                        getString(R.string.lbl_available_fertilizers),
                        EnumTask.AVAILABLE_FERTILIZERS_CIM,
                        checkStatus(EnumTask.AVAILABLE_FERTILIZERS_CIM)
                    ),
                    UseCaseWithTasks(
                        getString(R.string.lbl_maize_performance),
                        EnumTask.MAIZE_PERFORMANCE,
                        checkStatus(EnumTask.MAIZE_PERFORMANCE)
                    ),
                    UseCaseWithTasks(
                        getString(R.string.lbl_market_outlet_maize),
                        EnumTask.MARKET_OUTLET_MAIZE,
                        checkStatus(EnumTask.MARKET_OUTLET_MAIZE)
                    )
                )
            )
        } else if (countryCode.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
            icPotato = true
            items.addAll(
                listOf(
                    UseCaseWithTasks(
                        getString(R.string.lbl_available_fertilizers),
                        EnumTask.AVAILABLE_FERTILIZERS_CIS,
                        checkStatus(EnumTask.AVAILABLE_FERTILIZERS_CIS)
                    ),
                    UseCaseWithTasks(
                        getString(R.string.lbl_market_outlet),
                        EnumTask.MARKET_OUTLET_CASSAVA,
                        checkStatus(EnumTask.MARKET_OUTLET_CASSAVA)
                    ),
                    UseCaseWithTasks(
                        getString(R.string.lbl_typical_yield),
                        EnumTask.CURRENT_CASSAVA_YIELD,
                        checkStatus(EnumTask.CURRENT_CASSAVA_YIELD)
                    ),
                    UseCaseWithTasks(
                        getString(R.string.lbl_sweet_potato_prices),
                        EnumTask.MARKET_OUTLET_SWEET_POTATO,
                        checkStatus(EnumTask.MARKET_OUTLET_SWEET_POTATO)
                    )
                )
            )
        }

        return items
    }
}

