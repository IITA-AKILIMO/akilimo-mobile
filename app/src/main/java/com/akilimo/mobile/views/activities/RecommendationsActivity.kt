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
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.models.Recommendations
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

    private var _binding: ActivityRecommendationsActivityBinding? = null
    private val binding get() = _binding!!


    private var frString: String? = null
    private var icMaizeString: String? = null
    private var icSweetPotatoString: String? = null
    private var sphString: String? = null
    private var bppString: String? = null

    //    private var useCase: UseCase? = null
    private var mAdapter: AdapterListAnimation? = null
    private var items: MutableList<Recommendations> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRecommendationsActivityBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)

        toolbar = binding.toolbar
        recyclerView = binding.recyclerView
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


    @Deprecated("Deprecated remove it completely")
    override fun initComponent() {
        frString = getString(R.string.lbl_fertilizer_recommendations)
        icMaizeString = getString(R.string.lbl_intercropping_maize)
        icSweetPotatoString = getString(R.string.lbl_intercropping_sweet_potato)
        sphString = getString(R.string.lbl_scheduled_planting_and_harvest)
        bppString = getString(R.string.lbl_best_planting_practices)


        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        //set data and list adapter
        mAdapter = AdapterListAnimation()


        recyclerView!!.adapter = mAdapter
        items = ArrayList()

        val database = getDatabase(this@RecommendationsActivity)
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        val frRecommendations = Recommendations().apply {
            recCode = EnumAdvice.FR
            recommendationName = frString
            background = ContextCompat.getDrawable(
                this@RecommendationsActivity,
                R.drawable.bg_gradient_very_soft
            )
        }
        items.add(frRecommendations)

        val sphRecommendations = Recommendations().apply {
            recCode = EnumAdvice.SPH
            recommendationName = sphString
            background = ContextCompat.getDrawable(
                this@RecommendationsActivity,
                R.drawable.bg_gradient_very_soft
            )
        }
        items.add(sphRecommendations)

        if (countryCode != EnumCountry.Ghana.countryCode()) {
            val bppRecommendations = Recommendations().apply {
                recCode = EnumAdvice.BPP
                recommendationName = bppString
                background = ContextCompat.getDrawable(
                    this@RecommendationsActivity,
                    R.drawable.bg_gradient_very_soft
                )
            }
            items.add(bppRecommendations)
        }

        if (countryCode == EnumCountry.Nigeria.countryCode()) {
            val icMaizeRecommendations = Recommendations().apply {
                recCode = EnumAdvice.IC_MAIZE
                recommendationName = icMaizeString
                background =
                    ContextCompat.getDrawable(
                        this@RecommendationsActivity,
                        R.drawable.bg_gradient_very_soft
                    )
            }
            items.add(icMaizeRecommendations)
        } else if (countryCode == EnumCountry.Tanzania.countryCode()) {
            val icSweetPotatoRecommendations = Recommendations().apply {
                recCode = EnumAdvice.IC_SWEET_POTATO
                recommendationName = icSweetPotatoString
                background =
                    ContextCompat.getDrawable(
                        this@RecommendationsActivity,
                        R.drawable.bg_gradient_very_soft
                    )
            }
            items.add(icSweetPotatoRecommendations)
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
        mAdapter!!.setAnimationType(TheItemAnimation.BOTTOM_UP)
        mAdapter!!.submitList(items)
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
                val useCase = database.useCaseDao().findOne() ?: UseCase()
                useCase.useCaseName = advice.name
                database.useCaseDao().insertAll(useCase)
                openActivity(intent)
            }
        }
    }


    private fun updateCurrencyList() {
        val currencies = database.currencyDao().listAll()
        if (currencies.isNotEmpty()) {
            return
        }

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
