package com.akilimo.mobile.views.activities.usecases

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityPlantingPracticesBinding
import com.akilimo.mobile.entities.UseCases
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.views.activities.CassavaMarketActivity
import com.akilimo.mobile.views.activities.DatesActivity
import com.akilimo.mobile.views.activities.ManualTillageCostActivity
import com.akilimo.mobile.views.activities.RootYieldActivity
import com.akilimo.mobile.views.activities.TractorAccessActivity
import com.akilimo.mobile.views.activities.WeedControlCostsActivity
import io.sentry.Sentry

class PlantingPracticesActivity : BaseActivity() {
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var btnGetRec: AppCompatButton? = null


    var recommendations: String? = null
    var plantingString: String? = null
    var marketOutletString: String? = null
    var rootYieldString: String? = null
    var tillageOperationsString: String? = null
    var manualTillageCostsString: String? = null
    var tractorAccessString: String? = null
    var weedControlCostString: String? = null

    private var _binding: ActivityPlantingPracticesBinding? = null
    private val binding get() = _binding!!


    private var mAdapter: RecOptionsAdapter? = null
    private var items: List<RecommendationOptions> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlantingPracticesBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)



        toolbar = binding.toolbarLayout.toolbar
        recyclerView = binding.recyclerView
        btnGetRec = binding.singleButton.btnGetRecommendation

        mAdapter = RecOptionsAdapter()
        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = recommendations
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar!!.setNavigationOnClickListener { v: View? -> closeActivity(false) }
    }

    override fun initComponent() {
        recommendations = getString(R.string.lbl_best_planting_practices)
        plantingString = getString(R.string.lbl_planting_harvest)
        marketOutletString = getString(R.string.lbl_market_outlet)
        weedControlCostString = getString(R.string.lbl_cost_of_weed_control)
        rootYieldString = getString(R.string.lbl_typical_yield)
        tillageOperationsString = getString(R.string.lbl_tillage_operations)
        manualTillageCostsString = getString(R.string.lbl_cost_of_manual_tillage)
        tractorAccessString = getString(R.string.lbl_tractor_access)


        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.adapter = mAdapter

        val database = getDatabase(this@PlantingPracticesActivity)

        var useCases = database.useCaseDao().findOne()
        if (useCases == null) {
            useCases = UseCases()
        }
        btnGetRec!!.setOnClickListener { view: View? ->

            try {
                useCases.apply {
                    FR = false
                    CIM = false
                    CIS = false
                    SPH = false
                    SPP = false
                    BPP = true
                    name = EnumUseCase.PP.name
                }
                database.useCaseDao().insert(useCases)
                processRecommendations(this@PlantingPracticesActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@PlantingPracticesActivity, ex.message, Toast.LENGTH_SHORT)
                    .show()
                Sentry.captureException(ex)
            }
        }


        mAdapter!!.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int) {
                var intent: Intent? = null
                val advice = obj?.adviceName
                when (advice) {
                    EnumAdviceTasks.PLANTING_AND_HARVEST -> intent =
                        Intent(this@PlantingPracticesActivity, DatesActivity::class.java)

                    EnumAdviceTasks.MARKET_OUTLET_CASSAVA -> intent =
                        Intent(this@PlantingPracticesActivity, CassavaMarketActivity::class.java)

                    EnumAdviceTasks.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@PlantingPracticesActivity, RootYieldActivity::class.java)

                    EnumAdviceTasks.MANUAL_TILLAGE_COST -> intent = Intent(
                        this@PlantingPracticesActivity,
                        ManualTillageCostActivity::class.java
                    )

                    EnumAdviceTasks.TRACTOR_ACCESS -> intent =
                        Intent(this@PlantingPracticesActivity, TractorAccessActivity::class.java)

                    EnumAdviceTasks.COST_OF_WEED_CONTROL -> intent = Intent(
                        this@PlantingPracticesActivity,
                        WeedControlCostsActivity::class.java
                    )

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
            val myItems: MutableList<RecommendationOptions> = ArrayList()
            myItems.add(
                RecommendationOptions(
                    manualTillageCostsString!!,
                    EnumAdviceTasks.MANUAL_TILLAGE_COST,
                    checkStatus(EnumAdviceTasks.MANUAL_TILLAGE_COST)
                )
            )
            myItems.add(
                RecommendationOptions(
                    tractorAccessString!!,
                    EnumAdviceTasks.TRACTOR_ACCESS,
                    checkStatus(EnumAdviceTasks.TRACTOR_ACCESS)
                )
            )
            myItems.add(
                RecommendationOptions(
                    weedControlCostString!!,
                    EnumAdviceTasks.COST_OF_WEED_CONTROL,
                    checkStatus(EnumAdviceTasks.COST_OF_WEED_CONTROL)
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
