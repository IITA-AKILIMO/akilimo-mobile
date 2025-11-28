package com.akilimo.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityRecommendationsBinding
import com.akilimo.mobile.dto.AdviceOption
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.components.CollapsibleToolbarHelper
import kotlinx.coroutines.launch

class RecommendationsActivity : BaseActivity<ActivityRecommendationsBinding>() {

    private lateinit var userRepo: AkilimoUserRepo

    override fun inflateBinding() = ActivityRecommendationsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // @formatter:off
        // Setup collapsing toolbar
        CollapsibleToolbarHelper(this, binding.lytToolbar)
            .build()
        // @formatter:on


        userRepo = AkilimoUserRepo(database.akilimoUserDao())

        // Prepare dynamic recommendation list
        val adviceOptions = mutableListOf(
            AdviceOption(EnumAdvice.FERTILIZER_RECOMMENDATIONS),
            AdviceOption(EnumAdvice.BEST_PLANTING_PRACTICES),
            AdviceOption(EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH),
        )

        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            when (user.enumCountry) {
                EnumCountry.NG -> adviceOptions.add(AdviceOption(EnumAdvice.INTERCROPPING_MAIZE))
                EnumCountry.TZ -> adviceOptions.add(AdviceOption(EnumAdvice.INTERCROPPING_SWEET_POTATO))
                else -> Unit
            }
        }

        // Setup RecyclerView
        val recAdapter = RecommendationAdapter<EnumAdvice>(
            context = this,
            showIcon = false,
            getLabel = { it.label(this) },
            getId = { it.name },
            onClick = { selected ->
                val intent = when (selected.valueOption) {
                    EnumAdvice.FERTILIZER_RECOMMENDATIONS -> Intent(this, FrActivity::class.java)
                    EnumAdvice.BEST_PLANTING_PRACTICES -> Intent(this, BppActivity::class.java)
                    EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> Intent(
                        this,
                        SphActivity::class.java
                    )

                    EnumAdvice.INTERCROPPING_MAIZE -> Intent(this, IcMaizeActivity::class.java)
                    EnumAdvice.INTERCROPPING_SWEET_POTATO -> Intent(
                        this,
                        IcSweetPotatoActivity::class.java
                    )
                }

                //track the active use case
                safeScope.launch {
                    val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
                    val updated = user.copy(
                        activeAdvise = selected.valueOption
                    )
                    userRepo.saveOrUpdateUser(updated, sessionManager.akilimoUser)
                }

                openActivity(intent)
            }
        )
        binding.recommendationList.apply {
            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            adapter = recAdapter
        }
        recAdapter.submitList(adviceOptions.toList())

    }


}