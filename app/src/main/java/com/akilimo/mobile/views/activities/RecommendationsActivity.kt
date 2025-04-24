package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.AdapterListAnimation
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityRecommendationsActivityBinding
import com.akilimo.mobile.entities.AkilimoCurrency
import com.akilimo.mobile.entities.AkilimoCurrencyResponse
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.UseCases
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.models.Recommendations
import com.akilimo.mobile.utils.SessionManager
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.enums.EnumAdvice
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.views.activities.usecases.FertilizerRecActivity
import com.akilimo.mobile.views.activities.usecases.InterCropRecActivity
import com.akilimo.mobile.views.activities.usecases.PlantingPracticesActivity
import com.akilimo.mobile.views.activities.usecases.ScheduledPlantingActivity
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecommendationsActivity : BaseActivity() {
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null

    var binding: ActivityRecommendationsActivityBinding? = null


    private var frString: String? = null
    private var icMaizeString: String? = null
    private var icSweetPotatoString: String? = null
    private var sphString: String? = null
    private var bppString: String? = null

    private val mandatoryInfo: MandatoryInfo? = null
    private var useCase: UseCases? = null
    private var mAdapter: AdapterListAnimation? = null
    private var items: MutableList<Recommendations> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationsActivityBinding.inflate(
            layoutInflater
        )

        setContentView(binding!!.root)


        if (sessionManager == null) {
            sessionManager = SessionManager(this@RecommendationsActivity)
        }
        toolbar = binding!!.toolbar
        recyclerView = binding!!.recyclerView
        initToolbar()
        initComponent()

        updateCurrencyList()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_home)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.lbl_recommendations)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> closeActivity(false) }
    }


    override fun initComponent() {
        frString = getString(R.string.lbl_fertilizer_recommendations)
        icMaizeString = getString(R.string.lbl_intercropping_maize)
        icSweetPotatoString = getString(R.string.lbl_intercropping_sweet_potato)
        sphString = getString(R.string.lbl_scheduled_planting_and_harvest)
        bppString = getString(R.string.lbl_best_planting_practices)


        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        //set data and list adapter
        mAdapter = AdapterListAnimation(this, R.layout.item_card_recommendation_no_arrow)
        recyclerView!!.adapter = mAdapter
        items = ArrayList()

        val database = getDatabase(this@RecommendationsActivity)
        val profileInfo = database.profileInfoDao().findOne()
        useCase = database.useCaseDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode!!
            currency = profileInfo.currencyCode!!
        }

        val FR = Recommendations()
        FR.recCode = EnumAdvice.FR
        FR.recommendationName = frString
        FR.background = ContextCompat.getDrawable(
            this@RecommendationsActivity,
            R.drawable.bg_gradient_very_soft
        )
        items.add(FR)

        val SPH = Recommendations()
        SPH.recCode = EnumAdvice.SPH
        SPH.recommendationName = sphString
        SPH.background = ContextCompat.getDrawable(
            this@RecommendationsActivity,
            R.drawable.bg_gradient_very_soft
        )
        items.add(SPH)

        if (countryCode != EnumCountry.Ghana.countryCode()) {
            val BPP = Recommendations()
            BPP.recCode = EnumAdvice.BPP
            BPP.recommendationName = bppString
            BPP.background = ContextCompat.getDrawable(
                this@RecommendationsActivity,
                R.drawable.bg_gradient_very_soft
            )
            items.add(BPP)
        }

        if (countryCode == EnumCountry.Nigeria.countryCode()) {
            val IC_MAIZE = Recommendations()
            IC_MAIZE.recCode = EnumAdvice.IC_MAIZE
            IC_MAIZE.recommendationName = icMaizeString
            IC_MAIZE.background =
                ContextCompat.getDrawable(
                    this@RecommendationsActivity,
                    R.drawable.bg_gradient_very_soft
                )
            items.add(IC_MAIZE)
        } else if (countryCode == EnumCountry.Tanzania.countryCode()) {
            val IC_SWEET_POTATO = Recommendations()
            IC_SWEET_POTATO.recCode = EnumAdvice.IC_SWEET_POTATO
            IC_SWEET_POTATO.recommendationName = icSweetPotatoString
            IC_SWEET_POTATO.background =
                ContextCompat.getDrawable(
                    this@RecommendationsActivity,
                    R.drawable.bg_gradient_very_soft
                )
            items.add(IC_SWEET_POTATO)
        }


        setAdapter()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun onSupportNavigateUp(): Boolean {
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(
            this@RecommendationsActivity,
            R.string.lbl_back_instructions,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setAdapter() {
        val database = getDatabase(this@RecommendationsActivity)
        mAdapter!!.setItems(items, TheItemAnimation.BOTTOM_UP)
        // on item list clicked
        mAdapter!!.setOnItemClickListener { view: View?, obj: Recommendations, position: Int ->
            //let us process the data
            var intent: Intent? = null
            var advice = obj.recCode
            if (advice == null) {
                advice = EnumAdvice.WM
            }
            when (advice) {
                EnumAdvice.FR -> intent =
                    Intent(this, FertilizerRecActivity::class.java)

                EnumAdvice.BPP -> intent =
                    Intent(this, PlantingPracticesActivity::class.java)

                EnumAdvice.IC_MAIZE, EnumAdvice.IC_SWEET_POTATO -> intent =
                    Intent(
                        this,
                        InterCropRecActivity::class.java
                    )

                EnumAdvice.SPH -> intent =
                    Intent(this, ScheduledPlantingActivity::class.java)

                else -> {}
            }
            if (intent != null) {
                if (useCase == null) {
                    useCase = UseCases()
                }
                useCase!!.name = advice.name
                database.useCaseDao().insert(useCase!!)
                startActivity(intent)
                openActivity()
            }
        }
    }


    private fun updateCurrencyList() {
        val database = getDatabase(this@RecommendationsActivity)
        val currencyCall = AkilimoApi.apiService.listCurrencies()
        currencyCall.enqueue(object : Callback<AkilimoCurrencyResponse> {
            override fun onResponse(
                call: Call<AkilimoCurrencyResponse>,
                response: Response<AkilimoCurrencyResponse>
            ) {
                if (response.isSuccessful) {
                    val akilimoCurrencyList: List<AkilimoCurrency> = response.body()!!.data
                    database.currencyDao().insertAll(akilimoCurrencyList)
                }
            }

            override fun onFailure(call: Call<AkilimoCurrencyResponse>, t: Throwable) {
                Toast.makeText(this@RecommendationsActivity, t.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(t)
            }
        })
    }
}
