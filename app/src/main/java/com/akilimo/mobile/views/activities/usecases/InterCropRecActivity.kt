package com.akilimo.mobile.views.activities.usecases

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityInterCropRecBinding
import com.akilimo.mobile.entities.UseCases
import com.akilimo.mobile.inherit.BaseActivity
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

class InterCropRecActivity : BaseActivity() {
    var recyclerView: RecyclerView? = null
    var myToolbar: Toolbar? = null
    var btnGetRec: AppCompatButton? = null

    private var _binding: ActivityInterCropRecBinding? = null
    private val binding get() = _binding!!


    var recommendations: String? = null
    var plantingString: String? = null
    var fertilizerString: String? = null
    var marketOutletString: String? = null
    var marketOutletMaizeString: String? = null
    var rootYieldString: String? = null
    var maizeHeightString: String? = null
    var sweetPotatoPricesString: String? = null

    private var activity: Activity? = null
    private var mAdapter: RecOptionsAdapter? = null
    private var items: List<RecommendationOptions> = ArrayList()
    private var useCases: UseCases? = null
    private var useCase: EnumUseCase? = null
    private var icMaize = false
    private var icPotato = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityInterCropRecBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        myToolbar = binding.toolbarLayout.toolbar
        btnGetRec = binding.singleButton.btnGetRecommendation

        val database = getDatabase(this@InterCropRecActivity)

        mAdapter = RecOptionsAdapter()
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode!!
            currency = profileInfo.currency!!
        }

        when (countryCode) {
            "NG" -> {
                recommendations = getString(R.string.title_maize_intercropping)
                useCase = EnumUseCase.CIM
            }

            "TZ" -> {
                recommendations = getString(R.string.title_sweet_potato_intercropping)
                useCase = EnumUseCase.CIS
            }
        }

        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        myToolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(myToolbar)
        supportActionBar!!.title = recommendations
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        myToolbar!!.setNavigationOnClickListener { v: View? -> closeActivity(false) }
    }

    override fun initComponent() {
        recommendations = getString(R.string.lbl_intercropping)
        plantingString = getString(R.string.lbl_planting_harvest)
        fertilizerString = getString(R.string.lbl_available_fertilizers)
        marketOutletString = getString(R.string.lbl_market_outlet)
        marketOutletMaizeString = getString(R.string.lbl_market_outlet_maize)
        rootYieldString = getString(R.string.lbl_typical_yield)
        maizeHeightString = getString(R.string.lbl_maize_performance)
        sweetPotatoPricesString = getString(R.string.lbl_sweet_potato_prices)

        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.adapter = mAdapter

        btnGetRec!!.setOnClickListener { view: View? ->
            //launch the recommendation view
            val database = getDatabase(this@InterCropRecActivity)
            var useCases = database.useCaseDao().findOne()
            try {
                if (useCases == null) {
                    useCases = UseCases()
                }
                useCases.FR = false
                useCases.CIM = icMaize
                useCases.CIS = icPotato
                useCases.SPH = false
                useCases.SPP = false
                useCases.BPP = false
                useCases.name = useCase!!.name

                database.useCaseDao().insert(useCases)
                processRecommendations(this@InterCropRecActivity)
            } catch (ex: Exception) {
                Toast.makeText(this@InterCropRecActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }


        mAdapter!!.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int) {
                var intent: Intent? = null
                val advice = obj?.adviceName
                when (advice) {
                    EnumAdviceTasks.PLANTING_AND_HARVEST -> intent =
                        Intent(this@InterCropRecActivity, DatesActivity::class.java)

                    EnumAdviceTasks.MARKET_OUTLET_CASSAVA -> {
                        intent =
                            Intent(this@InterCropRecActivity, CassavaMarketActivity::class.java)
                        intent.putExtra(CassavaMarketActivity.useCaseTag, useCase as Parcelable?)
                    }

                    EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO -> intent = Intent(
                        this@InterCropRecActivity,
                        SweetPotatoMarketActivity::class.java
                    )

                    EnumAdviceTasks.MARKET_OUTLET_MAIZE -> intent =
                        Intent(this@InterCropRecActivity, MaizeMarketActivity::class.java)

                    EnumAdviceTasks.CURRENT_CASSAVA_YIELD -> intent =
                        Intent(this@InterCropRecActivity, RootYieldActivity::class.java)

                    EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS, EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM -> {
                        intent =
                            Intent(
                                this@InterCropRecActivity,
                                InterCropFertilizersActivity::class.java
                            )
                        intent.putExtra(
                            InterCropFertilizersActivity.useCaseTag,
                            useCase as Parcelable?
                        )
                    }

                    EnumAdviceTasks.MAIZE_PERFORMANCE -> intent =
                        Intent(this@InterCropRecActivity, MaizePerformanceActivity::class.java)

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
            val myItems: MutableList<RecommendationOptions> =
                ArrayList()
            if (countryCode.equals(EnumCountry.Nigeria.countryCode(), ignoreCase = true)) {
                icMaize = true
                myItems.add(
                    RecommendationOptions(
                        fertilizerString!!,
                        EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM,
                        checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM)
                    )
                )
                myItems.add(
                    RecommendationOptions(
                        maizeHeightString!!,
                        EnumAdviceTasks.MAIZE_PERFORMANCE,
                        checkStatus(EnumAdviceTasks.MAIZE_PERFORMANCE)
                    )
                )
                myItems.add(
                    RecommendationOptions(
                        marketOutletMaizeString!!,
                        EnumAdviceTasks.MARKET_OUTLET_MAIZE,
                        checkStatus(EnumAdviceTasks.MARKET_OUTLET_MAIZE)
                    )
                )
            } else if (countryCode.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
                icPotato = true
                myItems.add(
                    RecommendationOptions(
                        fertilizerString!!,
                        EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS,
                        checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS)
                    )
                )
                myItems.add(
                    RecommendationOptions(
                        marketOutletString!!,
                        EnumAdviceTasks.MARKET_OUTLET_CASSAVA,
                        checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)
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
                        sweetPotatoPricesString!!,
                        EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO,
                        checkStatus(EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO)
                    )
                )
            }
            return myItems
        }
}
