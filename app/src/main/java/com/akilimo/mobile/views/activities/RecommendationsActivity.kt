package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.databinding.ActivityRecommendationsActivityBinding
import com.akilimo.mobile.entities.AkilimoCurrency
import com.akilimo.mobile.entities.AkilimoCurrencyResponse
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseRecommendationActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.models.RecommendationOptions
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

class RecommendationsActivity :
    BaseRecommendationActivity<ActivityRecommendationsActivityBinding>() {

    override val displayArrow: Boolean = false

    override fun inflateBinding(): ActivityRecommendationsActivityBinding {
        return ActivityRecommendationsActivityBinding.inflate(layoutInflater)
    }

    override fun getRecommendationOptions(): List<RecommendationOptions> {
        val frString = getString(R.string.lbl_fertilizer_recommendations)
        val icMaizeString = getString(R.string.lbl_intercropping_maize)
        val icSweetPotatoString = getString(R.string.lbl_intercropping_sweet_potato)
        val sphString = getString(R.string.lbl_scheduled_planting_and_harvest)
        val bppString = getString(R.string.lbl_best_planting_practices)

        val items: MutableList<RecommendationOptions> = ArrayList()
        val frRecommendation = RecommendationOptions(
            recommendationCode = EnumAdvice.FR,
            recommendationName = frString
        )
        items.add(frRecommendation)

        val sphRecommendation = RecommendationOptions(
            recommendationCode = EnumAdvice.SPH,
            recommendationName = sphString
        )
        items.add(sphRecommendation)

        if (countryCode != EnumCountry.Ghana.countryCode()) {
            val bppRecommendation = RecommendationOptions(
                recommendationCode = EnumAdvice.BPP,
                recommendationName = bppString,
            )
            items.add(bppRecommendation)
        }

        if (countryCode == EnumCountry.Nigeria.countryCode()) {
            val icMaizeRecommendation = RecommendationOptions(
                recommendationCode = EnumAdvice.IC_MAIZE,
                recommendationName = icMaizeString,
            )
            items.add(icMaizeRecommendation)
        } else if (countryCode == EnumCountry.Tanzania.countryCode()) {
            val icSweetPotatoRecommendation = RecommendationOptions(
                recommendationCode = EnumAdvice.IC_SWEET_POTATO,
                recommendationName = icSweetPotatoString,
            )
            items.add(icSweetPotatoRecommendation)
        }

        return items
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar, R.string.lbl_recommendations) {
            closeActivity(false)
        }

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        binding.recommendationsRecyclerView.apply {
//            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            layoutManager = GridLayoutManager(this@RecommendationsActivity, 2)
//            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = mAdapter
        }
        mAdapter.setAnimationType(TheItemAnimation.BOTTOM_UP)

        mAdapter.setOnItemClickListener(object : RecOptionsAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View?,
                recommendation: RecommendationOptions,
                position: Int
            ) {
                var intent: Intent? = null
                var advice = recommendation.recommendationCode
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
        })


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
