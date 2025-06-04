package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.AdapterListAnimation
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityRecommendationsActivityBinding
import com.akilimo.mobile.entities.AkilimoCurrency
import com.akilimo.mobile.entities.AkilimoCurrencyResponse
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.MyBaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.models.Recommendation
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

class RecommendationsActivity : MyBaseActivity() {

    private var _binding: ActivityRecommendationsActivityBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRecommendationsActivityBinding.inflate(
            layoutInflater
        )

        setContentView(binding.root)


        setupToolbar(binding.toolbar, R.string.lbl_recommendations) {
            closeActivity(false)
        }

        val frString = getString(R.string.lbl_fertilizer_recommendations)
        val icMaizeString = getString(R.string.lbl_intercropping_maize)
        val icSweetPotatoString = getString(R.string.lbl_intercropping_sweet_potato)
        val sphString = getString(R.string.lbl_scheduled_planting_and_harvest)
        val bppString = getString(R.string.lbl_best_planting_practices)

        val database = getDatabase(this@RecommendationsActivity)
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        val mAdapter = AdapterListAnimation()
        val items: MutableList<Recommendation> = ArrayList()
        binding.recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            setHasFixedSize(true)
            adapter = mAdapter
        }


        val frRecommendation = Recommendation(
            recCode = EnumAdvice.FR,
            recommendationName = frString,
            background = ContextCompat.getDrawable(
                this@RecommendationsActivity,
                R.drawable.bg_gradient_very_soft
            )
        )
        items.add(frRecommendation)

        val sphRecommendation = Recommendation(
            recCode = EnumAdvice.SPH,
            recommendationName = sphString,
            background = ContextCompat.getDrawable(
                this@RecommendationsActivity,
                R.drawable.bg_gradient_very_soft
            )
        )
        items.add(sphRecommendation)

        if (countryCode != EnumCountry.Ghana.countryCode()) {
            val bppRecommendation = Recommendation(
                recCode = EnumAdvice.BPP,
                recommendationName = bppString,
                background = ContextCompat.getDrawable(
                    this@RecommendationsActivity,
                    R.drawable.bg_gradient_very_soft
                )
            )
            items.add(bppRecommendation)
        }

        if (countryCode == EnumCountry.Nigeria.countryCode()) {
            val icMaizeRecommendation = Recommendation(
                recCode = EnumAdvice.IC_MAIZE,
                recommendationName = icMaizeString,
                background =
                    ContextCompat.getDrawable(
                        this@RecommendationsActivity,
                        R.drawable.bg_gradient_very_soft
                    )
            )
            items.add(icMaizeRecommendation)
        } else if (countryCode == EnumCountry.Tanzania.countryCode()) {
            val icSweetPotatoRecommendation = Recommendation(
                recCode = EnumAdvice.IC_SWEET_POTATO,
                recommendationName = icSweetPotatoString,
                background =
                    ContextCompat.getDrawable(
                        this@RecommendationsActivity,
                        R.drawable.bg_gradient_very_soft
                    )
            )
            items.add(icSweetPotatoRecommendation)
        }

        mAdapter.setAnimationType(TheItemAnimation.BOTTOM_UP)
        mAdapter.submitList(items)
        mAdapter.setOnItemClickListener { _: View?, recommendation: Recommendation, _: Int ->
            //let us process the data
            var intent: Intent? = null
            var advice = recommendation.recCode
            when (advice) {
                EnumAdvice.FR -> intent =
                    Intent(this@RecommendationsActivity, FertilizerRecActivity::class.java)

                EnumAdvice.BPP -> intent =
                    Intent(this@RecommendationsActivity, PlantingPracticesActivity::class.java)

                EnumAdvice.IC_MAIZE, EnumAdvice.IC_SWEET_POTATO -> intent =
                    Intent(
                        this@RecommendationsActivity,
                        InterCropRecActivity::class.java
                    )

                EnumAdvice.SPH -> intent =
                    Intent(this@RecommendationsActivity, ScheduledPlantingActivity::class.java)

                else -> {}
            }

            if (intent != null) {
                val useCase = database.useCaseDao().findOne() ?: UseCase()
                useCase.useCaseName = advice.name
                database.useCaseDao().insertAll(useCase)
                openActivity(intent)
            }
        }

        updateCurrencyList()
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
