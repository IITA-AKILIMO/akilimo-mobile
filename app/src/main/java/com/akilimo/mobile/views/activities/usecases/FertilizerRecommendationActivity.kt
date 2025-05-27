package com.akilimo.mobile.views.activities.usecases

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityFertilizerRecommendationBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks

class FertilizerRecommendationActivity : AppCompatActivity() {
    private var _binding: ActivityFertilizerRecommendationBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFertilizerRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataSet = getRecommendationOptions()
        val mAdapter = RecAdapter(dataSet)
        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(this@FertilizerRecommendationActivity)
            setHasFixedSize(true)
            adapter = mAdapter
        }
    }

    private fun getRecommendationOptions(): List<RecommendationOptions> {
        val fertilizerString = getString(R.string.lbl_available_fertilizers)
        val investmentString = getString(R.string.lbl_investment_amount)
        val rootYieldString = getString(R.string.lbl_typical_yield)
        val marketOutletString = getString(R.string.lbl_market_outlet)

        val myItems: MutableList<RecommendationOptions> = ArrayList()

        myItems.add(
            RecommendationOptions(
                marketOutletString,
                EnumAdviceTasks.MARKET_OUTLET_CASSAVA,
                checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)
            )
        )
        myItems.add(
            RecommendationOptions(
                fertilizerString,
                EnumAdviceTasks.AVAILABLE_FERTILIZERS,
                checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS)
            )
        )
        myItems.add(
            RecommendationOptions(
                investmentString,
                EnumAdviceTasks.INVESTMENT_AMOUNT,
                checkStatus(EnumAdviceTasks.INVESTMENT_AMOUNT)
            )
        )
        myItems.add(
            RecommendationOptions(
                rootYieldString,
                EnumAdviceTasks.CURRENT_CASSAVA_YIELD,
                checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)
            )
        )

        return myItems
    }

    private fun checkStatus(adviceTasks: EnumAdviceTasks): AdviceStatus {
        val database = getDatabase(this)
        val adviceStatus = database.adviceStatusDao().findOne(adviceTasks.name)
        if (adviceStatus != null) {
            return adviceStatus
        }
        return AdviceStatus(adviceTasks.name, false)
    }
}