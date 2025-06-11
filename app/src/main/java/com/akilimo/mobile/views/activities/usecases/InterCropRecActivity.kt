package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityInterCropRecBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseRecommendationActivity
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTask
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

class InterCropRecActivity : BaseRecommendationActivity<ActivityInterCropRecBinding>() {

    private var icMaize = false
    private var icPotato = false

    override fun inflateBinding(): ActivityInterCropRecBinding {
        return ActivityInterCropRecBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = getDatabase(this)
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
        mAdapter.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, recommendation: RecommendationOptions, position: Int) {
                dataPositionChanged = position
                val intent = when (recommendation.adviceName) {
                    EnumAdviceTask.PLANTING_AND_HARVEST -> Intent(
                        context,
                        DatesActivity::class.java
                    )

                    EnumAdviceTask.MARKET_OUTLET_CASSAVA -> Intent(
                        context,
                        CassavaMarketActivity::class.java
                    ).apply {
                        putExtra(CassavaMarketActivity.useCaseTag, useCase)
                    }

                    EnumAdviceTask.MARKET_OUTLET_SWEET_POTATO -> Intent(
                        context,
                        SweetPotatoMarketActivity::class.java
                    )

                    EnumAdviceTask.MARKET_OUTLET_MAIZE -> Intent(
                        context,
                        MaizeMarketActivity::class.java
                    )

                    EnumAdviceTask.CURRENT_CASSAVA_YIELD -> Intent(
                        context,
                        RootYieldActivity::class.java
                    )

                    EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM,
                    EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS -> Intent(
                        context,
                        InterCropFertilizersActivity::class.java
                    ).apply {
                        putExtra(
                            BaseFertilizersActivity.useCaseTag,
                            useCase
                        )
                    }

                    EnumAdviceTask.MAIZE_PERFORMANCE -> Intent(
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
                    useCaseName = useCase
                }

                database.useCaseDao().insertAll(useCases)
                processRecommendations(this@InterCropRecActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@InterCropRecActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }
    }


    override fun getRecommendationOptions(): List<RecommendationOptions> {
        val items = mutableListOf<RecommendationOptions>()
        if (countryCode.equals(EnumCountry.Nigeria.countryCode(), ignoreCase = true)) {
            icMaize = true
            items.addAll(
                listOf(
                    RecommendationOptions(
                        getString(R.string.lbl_available_fertilizers),
                        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM,
                        checkStatus(EnumAdviceTask.AVAILABLE_FERTILIZERS_CIM)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_maize_performance),
                        EnumAdviceTask.MAIZE_PERFORMANCE,
                        checkStatus(EnumAdviceTask.MAIZE_PERFORMANCE)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_market_outlet_maize),
                        EnumAdviceTask.MARKET_OUTLET_MAIZE,
                        checkStatus(EnumAdviceTask.MARKET_OUTLET_MAIZE)
                    )
                )
            )
        } else if (countryCode.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
            icPotato = true
            items.addAll(
                listOf(
                    RecommendationOptions(
                        getString(R.string.lbl_available_fertilizers),
                        EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS,
                        checkStatus(EnumAdviceTask.AVAILABLE_FERTILIZERS_CIS)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_market_outlet),
                        EnumAdviceTask.MARKET_OUTLET_CASSAVA,
                        checkStatus(EnumAdviceTask.MARKET_OUTLET_CASSAVA)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_typical_yield),
                        EnumAdviceTask.CURRENT_CASSAVA_YIELD,
                        checkStatus(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_sweet_potato_prices),
                        EnumAdviceTask.MARKET_OUTLET_SWEET_POTATO,
                        checkStatus(EnumAdviceTask.MARKET_OUTLET_SWEET_POTATO)
                    )
                )
            )
        }

        return items
    }
}

