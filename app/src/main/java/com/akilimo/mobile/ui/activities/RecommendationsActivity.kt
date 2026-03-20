package com.akilimo.mobile.ui.activities

import android.os.Bundle
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityRecommendationsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shell activity that hosts the recommendations NavHostFragment (nav_recommendations.xml).
 * RecommendationsFragment is the startDestination and handles all UI.
 */
@AndroidEntryPoint
class RecommendationsActivity : BaseActivity<ActivityRecommendationsBinding>() {

    override fun inflateBinding() = ActivityRecommendationsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // NavHostFragment in the layout auto-starts RecommendationsFragment
    }
}
