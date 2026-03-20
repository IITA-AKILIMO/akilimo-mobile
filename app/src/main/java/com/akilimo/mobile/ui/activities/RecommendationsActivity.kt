package com.akilimo.mobile.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.akilimo.mobile.R
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityRecommendationsBinding
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.ui.components.CollapsibleToolbarHelper
import com.akilimo.mobile.ui.viewmodels.RecommendationsViewModel
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendationsActivity : BaseActivity<ActivityRecommendationsBinding>() {

    private val viewModel: RecommendationsViewModel by viewModels()

    private lateinit var recAdapter: RecommendationAdapter<EnumAdvice>

    override fun inflateBinding() = ActivityRecommendationsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        CollapsibleToolbarHelper(this, binding.lytToolbar).build()

        setupAdapter()
        observeViewModel()
        viewModel.loadAdviceOptions(sessionManager.akilimoUser)
    }

    private fun setupAdapter() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_recommendations) as NavHostFragment
        val navController = navHostFragment.navController

        recAdapter = RecommendationAdapter<EnumAdvice>(
            context = this,
            showIcon = false,
            getLabel = { it.label(this) },
            getId = { it.name },
            onClick = { selected ->
                val destId = when (selected.valueOption) {
                    EnumAdvice.FERTILIZER_RECOMMENDATIONS -> R.id.frActivity
                    EnumAdvice.BEST_PLANTING_PRACTICES -> R.id.bppActivity
                    EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> R.id.sphActivity
                    EnumAdvice.INTERCROPPING_MAIZE -> R.id.icMaizeActivity
                    EnumAdvice.INTERCROPPING_SWEET_POTATO -> R.id.icSweetPotatoActivity
                }
                viewModel.trackActiveAdvice(sessionManager.akilimoUser, selected.valueOption)
                navController.navigate(destId)
            }
        )
        binding.recommendationList.apply {
            layoutManager = LinearLayoutManager(this@RecommendationsActivity)
            adapter = recAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    recAdapter.submitList(state.adviceOptions)
                }
            }
        }
    }
}
