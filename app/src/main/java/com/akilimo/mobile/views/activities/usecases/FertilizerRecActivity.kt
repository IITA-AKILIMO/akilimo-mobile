package com.akilimo.mobile.views.activities.usecases

import android.app.Activity
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
    var recyclerView: RecyclerView? = null
    var myToolbar: Toolbar? = null
    var btnGetRec: AppCompatButton? = null

    private var _binding: ActivityFertilizerRecBinding? = null
    private val binding get() = _binding!!


    var plantingString: String? = null
    var fertilizerString: String? = null
    var investmentString: String? = null
    var rootYieldString: String? = null
    var marketOutletString: String? = null

    private var activity: Activity? = null
    private var mAdapter: RecOptionsAdapter? = null
    private var items: List<RecommendationOptions> = ArrayList()
    private var useCase: UseCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFertilizerRecBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        myToolbar = binding.toolbarLayout.toolbar
        btnGetRec = binding.singleButton.btnGetRecommendation

        mAdapter = RecOptionsAdapter()
        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        myToolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(myToolbar)
        supportActionBar!!.title = getString(R.string.lbl_fertilizer_recommendations)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        myToolbar!!.setNavigationOnClickListener { v: View? -> closeActivity(false) }
    }

    override fun initComponent() {
        plantingString = getString(R.string.lbl_planting_harvest)
        fertilizerString = getString(R.string.lbl_available_fertilizers)
        investmentString = getString(R.string.lbl_investment_amount)
        rootYieldString = getString(R.string.lbl_typical_yield)
        marketOutletString = getString(R.string.lbl_market_outlet)

        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.adapter = mAdapter
        val database = getDatabase(this@FertilizerRecActivity)
        btnGetRec!!.setOnClickListener { view: View? ->
            //launch the recommendation view
            useCase = database.useCaseDao().findOne()
            try {
                if (useCase == null) {
                    useCase = UseCase()
                }
                useCase!!.fertilizerRecommendation = true
                useCase!!.maizeInterCropping = false
                useCase!!.sweetPotatoInterCropping = false
                useCase!!.scheduledPlantingHighStarch = false
                useCase!!.scheduledPlanting = false
                useCase!!.bestPlantingPractices = false
                useCase!!.useCaseName = EnumUseCase.FR.name

                database.useCaseDao().insertAll(useCase!!)
                processRecommendations(activity!!)
            } catch (ex: Exception) {
                Toast.makeText(this@FertilizerRecActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
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
        //set data and list adapter
        items = recItems
        mAdapter!!.setData(items)
        recyclerView!!.adapter = mAdapter
    }

    private val recItems: List<RecommendationOptions>
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
