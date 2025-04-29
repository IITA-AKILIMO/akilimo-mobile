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
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.FertilizersActivity
import com.akilimo.mobile.views.activities.InvestmentAmountActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import io.sentry.Sentry

class FertilizerRecActivity : BaseActivity() {
    private var _binding: ActivityFertilizerRecBinding? = null
    private val binding get() = _binding!!


    var plantingString: String? = null
    var fertilizerString: String? = null
    var investmentString: String? = null
    var rootYieldString: String? = null
    var marketOutletString: String? = null

    private var mAdapter: RecOptionsAdapter? = null
    private var items: List<RecommendationOptions> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFertilizerRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAdapter = RecOptionsAdapter()
        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        binding.toolbarLayout.toolbar.apply {
            setNavigationIcon(R.drawable.ic_left_arrow)
            setSupportActionBar(binding.toolbarLayout.toolbar)
            supportActionBar!!.title = getString(R.string.lbl_fertilizer_recommendations)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            setNavigationOnClickListener { v: View? -> closeActivity(false) }
        }
    }

    override fun initComponent() {
        plantingString = getString(R.string.lbl_planting_harvest)
        fertilizerString = getString(R.string.lbl_available_fertilizers)
        investmentString = getString(R.string.lbl_investment_amount)
        rootYieldString = getString(R.string.lbl_typical_yield)
        marketOutletString = getString(R.string.lbl_market_outlet)

        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(this@FertilizerRecActivity)
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = mAdapter

            singleButton.btnAction.setOnClickListener {
                var useCase = database.useCaseDao().findOne()
                try {
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

        mAdapter!!.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int) {
                var intent: Intent? = null
                val advice = obj?.adviceName
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

        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun setAdapter() {
        mAdapter!!.setData(recommendationOptions)
        binding.recyclerView.adapter = mAdapter
    }

    private val recommendationOptions: List<RecommendationOptions>
        get() {
            val myItems: MutableList<RecommendationOptions> =
                ArrayList()
            myItems.add(
                RecommendationOptions(
                    marketOutletString!!,
                    EnumAdviceTasks.MARKET_OUTLET_CASSAVA,
                    checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)
                )
            )
            myItems.add(
                RecommendationOptions(
                    fertilizerString!!,
                    EnumAdviceTasks.AVAILABLE_FERTILIZERS,
                    checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS)
                )
            )
            myItems.add(
                RecommendationOptions(
                    investmentString!!,
                    EnumAdviceTasks.INVESTMENT_AMOUNT,
                    checkStatus(EnumAdviceTasks.INVESTMENT_AMOUNT)
                )
            )
            myItems.add(
                RecommendationOptions(
                    rootYieldString!!,
                    EnumAdviceTasks.CURRENT_CASSAVA_YIELD,
                    checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)
                )
            )

            return myItems
        }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }
}
