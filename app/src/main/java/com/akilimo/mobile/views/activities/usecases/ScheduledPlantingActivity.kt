package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.databinding.ActivityScheduledPlantingBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import io.sentry.Sentry

class ScheduledPlantingActivity : BaseActivity() {
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null

    private var _binding: ActivityScheduledPlantingBinding? = null
    private val binding get() = _binding!!


    var plantingString: String? = null
    var marketOutletString: String? = null
    var rootYieldString: String? = null

    private var mAdapter: RecOptionsAdapter? = null
    private var items: List<RecommendationOptions> = ArrayList()
//    private var useCases: UseCases? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityScheduledPlantingBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)


        mAdapter = RecOptionsAdapter()
        toolbar = binding.toolbarLayout.toolbar
        recyclerView = binding.recyclerView

        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.lbl_scheduled_planting_and_harvest)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar!!.setNavigationOnClickListener { v: View? -> closeActivity(false) }
    }

    override fun initComponent() {
        plantingString = getString(R.string.lbl_planting_harvest)
        marketOutletString = getString(R.string.lbl_market_outlet)
        rootYieldString = getString(R.string.lbl_typical_yield)


        binding.recyclerView.apply {
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
        mAdapter!!.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int) {
                var intent: Intent? = null
                val advice = obj?.adviceName
                when (advice) {
                    EnumAdviceTasks.PLANTING_AND_HARVEST -> intent =
                        Intent(this@ScheduledPlantingActivity, DatesActivity::class.java)

                    EnumAdviceTasks.MARKET_OUTLET_CASSAVA -> intent =
                        Intent(this@ScheduledPlantingActivity, CassavaMarketActivity::class.java)

                    EnumAdviceTasks.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@ScheduledPlantingActivity, RootYieldActivity::class.java)

                    else -> {}
                }
                if (intent != null) {
                    startActivity(intent)
                    openActivity()
                }
            }
        })
        setAdapter()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }


    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun setAdapter() {
        items = recItems
        mAdapter!!.setData(items)
    }

    private val recItems: List<RecommendationOptions>
        get() {
            val myItems: MutableList<RecommendationOptions> =
                ArrayList()
            myItems.add(
                RecommendationOptions(
                    plantingString!!,
                    EnumAdviceTasks.PLANTING_AND_HARVEST,
                    checkStatus(EnumAdviceTasks.PLANTING_AND_HARVEST)
                )
            )
            myItems.add(
                RecommendationOptions(
                    rootYieldString!!,
                    EnumAdviceTasks.CURRENT_CASSAVA_YIELD,
                    checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)
                )
            )
            myItems.add(
                RecommendationOptions(
                    marketOutletString!!,
                    EnumAdviceTasks.MARKET_OUTLET_CASSAVA,
                    checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)
                )
            )
            return myItems
        }
}
