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
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.InterCropFertilizersActivity
import com.akilimo.mobile.views.activities.MaizeMarketActivity
import com.akilimo.mobile.views.activities.MaizePerformanceActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import com.akilimo.mobile.views.activities.SweetPotatoMarketActivity
import io.sentry.Sentry

class InterCropRecActivity : BaseRecommendationActivity() {

    private var _binding: ActivityInterCropRecBinding? = null
    private val binding get() = _binding!!

    private var mAdapter: RecOptionsAdapter? = null
    private var icMaize = false
    private var icPotato = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityInterCropRecBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = getDatabase(this)
        val profileInfo = database.profileInfoDao().findOne()
        profileInfo?.let {
            countryCode = it.countryCode
            currencyCode = it.currencyCode
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
        }

        mAdapter = RecOptionsAdapter(getRecItems()).also { adapter ->
            val context = this@InterCropRecActivity
            binding.recyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int) {
                    val intent = when (obj?.adviceName) {
                        EnumAdviceTasks.PLANTING_AND_HARVEST -> Intent(
                            context,
                            DatesActivity::class.java
                        )

                        EnumAdviceTasks.MARKET_OUTLET_CASSAVA -> Intent(
                            context,
                            CassavaMarketActivity::class.java
                        ).apply {
                            putExtra(CassavaMarketActivity.useCaseTag, useCase)
                        }

                        EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO -> Intent(
                            context,
                            SweetPotatoMarketActivity::class.java
                        )

                        EnumAdviceTasks.MARKET_OUTLET_MAIZE -> Intent(
                            context,
                            MaizeMarketActivity::class.java
                        )

                        EnumAdviceTasks.CURRENT_CASSAVA_YIELD -> Intent(
                            context,
                            RootYieldActivity::class.java
                        )

                        EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM,
                        EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS -> Intent(
                            context,
                            InterCropFertilizersActivity::class.java
                        ).apply {
                            putExtra(
                                InterCropFertilizersActivity.useCaseTag,
                                useCase
                            )
                        }

                        EnumAdviceTasks.MAIZE_PERFORMANCE -> Intent(
                            context,
                            MaizePerformanceActivity::class.java
                        )

                        else -> null
                    }

                    intent?.let {
                        startActivity(it)
                        openActivity()
                    }
                }
            })
        }

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


    private fun getRecItems(): List<RecommendationOptions> {
        val items = mutableListOf<RecommendationOptions>()

        if (countryCode.equals(EnumCountry.Nigeria.countryCode(), ignoreCase = true)) {
            icMaize = true
            items.addAll(
                listOf(
                    RecommendationOptions(
                        getString(R.string.lbl_available_fertilizers),
                        EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM,
                        checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_maize_performance),
                        EnumAdviceTasks.MAIZE_PERFORMANCE,
                        checkStatus(EnumAdviceTasks.MAIZE_PERFORMANCE)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_market_outlet_maize),
                        EnumAdviceTasks.MARKET_OUTLET_MAIZE,
                        checkStatus(EnumAdviceTasks.MARKET_OUTLET_MAIZE)
                    )
                )
            )
        } else if (countryCode.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
            icPotato = true
            items.addAll(
                listOf(
                    RecommendationOptions(
                        getString(R.string.lbl_available_fertilizers),
                        EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS,
                        checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_market_outlet),
                        EnumAdviceTasks.MARKET_OUTLET_CASSAVA,
                        checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_typical_yield),
                        EnumAdviceTasks.CURRENT_CASSAVA_YIELD,
                        checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)
                    ),
                    RecommendationOptions(
                        getString(R.string.lbl_sweet_potato_prices),
                        EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO,
                        checkStatus(EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO)
                    )
                )
            )
        }

        return items
    }
}

