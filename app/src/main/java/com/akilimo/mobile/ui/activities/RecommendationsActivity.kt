package com.akilimo.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityRecommendationsBinding
import com.akilimo.mobile.dto.AdviceOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumAdvice
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
        val adviceOptions = listOf(
//            AdviceOption(EnumAdvice.FR),
//            AdviceOption(EnumAdvice.BPP),
            AdviceOption(EnumAdvice.SPH),
            AdviceOption(EnumAdvice.IC_MAIZE),
            AdviceOption(EnumAdvice.IC_SWEET_POTATO),
        )

        // Setup RecyclerView
        val recAdapter = RecommendationAdapter<EnumAdvice>(
            context = this,
            hideIcon = true,
            getLabel = { it.label(this) },
            getId = { it.name },
            onClick = { selected ->
                Toast.makeText(this, "Selected: ${selected.valueOption.name}", Toast.LENGTH_SHORT)
                    .show()

                val intent = when (selected.valueOption) {
                    EnumAdvice.FR -> Intent(this, FrActivity::class.java)
                    EnumAdvice.BPP -> Intent(this, BppActivity::class.java)
                    EnumAdvice.SPH -> Intent(this, SphActivity::class.java)
                    EnumAdvice.IC_MAIZE -> Intent(this, IcMaizeActivity::class.java)
                    EnumAdvice.IC_SWEET_POTATO -> Intent(this, IcSweetPotatoActivity::class.java)
                    else -> null
                }

                //track the active use case
                safeScope.launch {
                    val user = userRepo.getUser(sessionManager.akilimoUser) ?: AkilimoUser()
                    user.activeAdvise = selected.valueOption
                    userRepo.saveOrUpdateUser(user, sessionManager.akilimoUser)
                }

                intent?.let { openActivity(it) }

            }
        )
        binding.recommendationList.apply {
            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            adapter = recAdapter
        }
        recAdapter.submitList(adviceOptions)

    }


}