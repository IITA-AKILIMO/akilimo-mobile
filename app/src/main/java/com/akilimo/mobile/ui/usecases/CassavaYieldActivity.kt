package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CassavaYieldAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityCassavaYieldBinding
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.ui.viewmodels.CassavaYieldViewModel
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CassavaYieldActivity : BaseActivity<ActivityCassavaYieldBinding>() {

    private val yieldImages = arrayOf(
        R.drawable.yield_less_than_7point5,
        R.drawable.yield_7point5_to_15,
        R.drawable.yield_15_to_22point5,
        R.drawable.yield_22_to_30,
        R.drawable.yield_more_than_30,
    )

    private val viewModel: CassavaYieldViewModel by viewModels()

    private lateinit var cassavaYieldAdapter: CassavaYieldAdapter

    override fun inflateBinding() = ActivityCassavaYieldBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        cassavaYieldAdapter = CassavaYieldAdapter().apply {
            onItemClick = { cassavaYield ->
                viewModel.selectYield(sessionManager.akilimoUser, cassavaYield)
            }
        }

        binding.rvCassavaYield.apply {
            layoutManager = GridLayoutManager(this@CassavaYieldActivity, 2)
            adapter = cassavaYieldAdapter
            setHasFixedSize(true)
        }

        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update toolbar title based on use case and area unit
                    val tonnage = when (state.areaUnit) {
                        EnumAreaUnit.ARE -> getString(R.string.lbl_are_yield)
                        EnumAreaUnit.HA -> getString(R.string.lbl_ha_yield)
                        else -> getString(R.string.lbl_acre_yield)
                    }
                    binding.rootYieldTitle.text = if (state.useCase == EnumAdvice.FERTILIZER_RECOMMENDATIONS) {
                        getString(R.string.lbl_typical_yield_question_fr, tonnage)
                    } else {
                        getString(R.string.lbl_typical_yield_question, tonnage)
                    }

                    // Activity supplies seed data when DB is empty (requires getString context)
                    state.seedRequest?.let { areaUnit ->
                        viewModel.seedYields(seedDefaultYields(areaUnit))
                    }

                    if (state.yields.isNotEmpty()) {
                        cassavaYieldAdapter.submitList(state.yields)
                    }
                }
            }
        }
    }

    private fun seedDefaultYields(areaUnit: EnumAreaUnit): List<CassavaYield> {
        fun unit(
            @StringRes a: Int, @StringRes b: Int, @StringRes c: Int,
            @StringRes d: Int, @StringRes e: Int
        ) = intArrayOf(a, b, c, d, e)

        val amountLabelResByUnit: Map<EnumAreaUnit, IntArray> = mapOf(
            EnumAreaUnit.ACRE to unit(
                R.string.yield_less_than_3_tonnes_per_acre, R.string.yield_3_to_6_tonnes_per_acre,
                R.string.yield_6_to_9_tonnes_per_acre, R.string.yield_9_to_12_tonnes_per_acre,
                R.string.yield_more_than_12_tonnes_per_acre
            ),
            EnumAreaUnit.HA to unit(
                R.string.yield_less_than_3_tonnes_per_hectare, R.string.yield_3_to_6_tonnes_per_hectare,
                R.string.yield_6_to_9_tonnes_per_hectare, R.string.yield_9_to_12_tonnes_per_hectare,
                R.string.yield_more_than_12_tonnes_per_hectare
            ),
            EnumAreaUnit.ARE to unit(
                R.string.yield_less_than_3_tonnes_per_are, R.string.yield_3_to_6_tonnes_per_are,
                R.string.yield_6_to_9_tonnes_per_are, R.string.yield_9_to_12_tonnes_per_are,
                R.string.yield_more_than_12_tonnes_per_are
            ),
            EnumAreaUnit.M2 to unit(
                R.string.yield_less_than_3_tonnes_per_meter, R.string.yield_3_to_6_tonnes_per_meter,
                R.string.yield_6_to_9_tonnes_per_meter, R.string.yield_9_to_12_tonnes_per_meter,
                R.string.yield_more_than_12_tonnes_per_meter
            )
        )

        data class YieldDef(
            @param:DrawableRes val imageId: Int,
            @param:StringRes val labelRes: Int,
            val amountValue: Double,
            @param:StringRes val descRes: Int
        )

        val yieldDefs = listOf(
            YieldDef(yieldImages[0], R.string.fcy_lower, 3.75, R.string.lbl_low_yield),
            YieldDef(yieldImages[1], R.string.fcy_about_the_same, 11.25, R.string.lbl_normal_yield),
            YieldDef(yieldImages[2], R.string.fcy_somewhat_higher, 18.75, R.string.lbl_high_yield),
            YieldDef(yieldImages[3], R.string.fcy_2_3_times_higher, 26.25, R.string.lbl_very_high_yield),
            YieldDef(yieldImages[4], R.string.fcy_more_than_3_times_higher, 33.75, R.string.lbl_very_high_yield)
        )

        val amountRes = amountLabelResByUnit[areaUnit] ?: amountLabelResByUnit[EnumAreaUnit.ACRE]!!
        val resolvedLabels = amountRes.map { getString(it) }

        return yieldDefs.mapIndexed { index, def ->
            CassavaYield.create(
                def.amountValue, getString(def.labelRes), def.imageId, getString(def.descRes)
            ).also { it.amountLabel = resolvedLabels[index] }
        }
    }
}
