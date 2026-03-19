package com.akilimo.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityRecommendationsBinding
import com.akilimo.mobile.dto.AdviceOption
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
        recAdapter = RecommendationAdapter<EnumAdvice>(
            context = this,
            showIcon = false,
            getLabel = { it.label(this) },
            getId = { it.name },
            onClick = { selected ->
                val intent = when (selected.valueOption) {
                    EnumAdvice.FERTILIZER_RECOMMENDATIONS -> Intent(this, FrActivity::class.java)
                    EnumAdvice.BEST_PLANTING_PRACTICES -> Intent(this, BppActivity::class.java)
                    EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> Intent(this, SphActivity::class.java)
                    EnumAdvice.INTERCROPPING_MAIZE -> Intent(this, IcMaizeActivity::class.java)
                    EnumAdvice.INTERCROPPING_SWEET_POTATO -> Intent(this, IcSweetPotatoActivity::class.java)
                }
                viewModel.trackActiveAdvice(sessionManager.akilimoUser, selected.valueOption)
                openActivity(intent)
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
