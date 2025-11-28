package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CassavaYieldAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityCassavaYieldBinding
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CassavaYieldActivity : BaseActivity<ActivityCassavaYieldBinding>() {

    private val yieldImages = arrayOf(
        R.drawable.yield_less_than_7point5,
        R.drawable.yield_7point5_to_15,
        R.drawable.yield_15_to_22point5,
        R.drawable.yield_22_to_30,
        R.drawable.yield_more_than_30,
    )

    private lateinit var cassavaYieldRepo: CassavaYieldRepo
    private lateinit var selectedCassavaMarketRepo: SelectedCassavaMarketRepo
    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var cassavaYieldAdapter: CassavaYieldAdapter


    override fun inflateBinding() = ActivityCassavaYieldBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initRepositories()
        setupAdapter()
    }

    private fun initRepositories() {
        cassavaYieldRepo = CassavaYieldRepo(database.cassavaYieldDao())
        selectedCassavaMarketRepo = SelectedCassavaMarketRepo(database.selectedCassavaMarketDao())
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
    }

    private fun setupAdapter() = with(binding) {
        cassavaYieldAdapter = CassavaYieldAdapter()
        rvCassavaYield.apply {
            layoutManager = GridLayoutManager(this@CassavaYieldActivity, 2)
            adapter = cassavaYieldAdapter
            setHasFixedSize(true)
        }

        cassavaYieldAdapter.onItemClick = { cassavaYield ->
            safeScope.launch {
                val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
                val userId = user.id ?: 0
                val selected = SelectedCassavaMarket(userId = userId, yieldId = cassavaYield.id)

                selectedCassavaMarketRepo.select(selected)
                val updatedList = cassavaYieldAdapter.currentList.map {
                    it.copy().apply {
                        isSelected = it.id == cassavaYield.id
                    }
                }
                cassavaYieldAdapter.submitList(updatedList)
            }
        }
    }

    override fun observeSyncWorker() {

        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: 0

            val marketDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
            val selectedMarket = marketDetails?.selectedCassavaMarket

            val areaUnit = user.enumAreaUnit

            val enumAreaUnit = EnumAreaUnit.entries.firstOrNull {
                it == areaUnit
            } ?: EnumAreaUnit.ACRE

            val useCase = user.activeAdvise ?: EnumAdvice.FERTILIZER_RECOMMENDATIONS

            val tonnage = when (enumAreaUnit) {
                EnumAreaUnit.ARE -> getString(R.string.lbl_are_yield)
                EnumAreaUnit.HA -> getString(R.string.lbl_ha_yield)
                else -> getString(R.string.lbl_acre_yield)
            }

            var title = getString(R.string.lbl_typical_yield_question, tonnage)
            if (useCase == EnumAdvice.FERTILIZER_RECOMMENDATIONS) {
                title = getString(R.string.lbl_typical_yield_question_fr, tonnage)
            }

            binding.rootYieldTitle.text = title

            cassavaYieldRepo.observeAll().collectLatest { repoList: List<CassavaYield> ->
                val items = if (repoList.isEmpty()) {
                    seedDefaultYieldsFromResources(enumAreaUnit)
                } else {
                    repoList.map { it.apply { isSelected = (it.id == selectedMarket?.yieldId) } }
                }

                withContext(Dispatchers.Main) {
                    cassavaYieldAdapter.submitList(items)
                }
            }
        }
    }

    private fun seedDefaultYieldsFromResources(areaUnit: EnumAreaUnit): List<CassavaYield> {
        fun unit(
            @StringRes a: Int,
            @StringRes b: Int,
            @StringRes c: Int,
            @StringRes d: Int,
            @StringRes e: Int
        ) = intArrayOf(a, b, c, d, e)

        val amountLabelResByUnit: Map<EnumAreaUnit, IntArray> = mapOf(
            EnumAreaUnit.ACRE to unit(
                R.string.yield_less_than_3_tonnes_per_acre,
                R.string.yield_3_to_6_tonnes_per_acre,
                R.string.yield_6_to_9_tonnes_per_acre,
                R.string.yield_9_to_12_tonnes_per_acre,
                R.string.yield_more_than_12_tonnes_per_acre
            ), EnumAreaUnit.HA to unit(
                R.string.yield_less_than_3_tonnes_per_hectare,
                R.string.yield_3_to_6_tonnes_per_hectare,
                R.string.yield_6_to_9_tonnes_per_hectare,
                R.string.yield_9_to_12_tonnes_per_hectare,
                R.string.yield_more_than_12_tonnes_per_hectare
            ), EnumAreaUnit.ARE to unit(
                R.string.yield_less_than_3_tonnes_per_are,
                R.string.yield_3_to_6_tonnes_per_are,
                R.string.yield_6_to_9_tonnes_per_are,
                R.string.yield_9_to_12_tonnes_per_are,
                R.string.yield_more_than_12_tonnes_per_are
            ), EnumAreaUnit.M2 to unit(
                R.string.yield_less_than_3_tonnes_per_meter,
                R.string.yield_3_to_6_tonnes_per_meter,
                R.string.yield_6_to_9_tonnes_per_meter,
                R.string.yield_9_to_12_tonnes_per_meter,
                R.string.yield_more_than_12_tonnes_per_meter
            )
        )

        val amountRes = amountLabelResByUnit[areaUnit] ?: amountLabelResByUnit[EnumAreaUnit.ACRE]!!

        data class YieldDef(
            @param:DrawableRes val imageId: Int,
            @param:StringRes val labelRes: Int,
            val amountValue: Double,
            @param:StringRes val descRes: Int
        )

        val yieldDefList = listOf(
            YieldDef(yieldImages[0], R.string.fcy_lower, 3.75, R.string.lbl_low_yield),
            YieldDef(yieldImages[1], R.string.fcy_about_the_same, 11.25, R.string.lbl_normal_yield),
            YieldDef(yieldImages[2], R.string.fcy_somewhat_higher, 18.75, R.string.lbl_high_yield),
            YieldDef(
                yieldImages[3], R.string.fcy_2_3_times_higher, 26.25, R.string.lbl_very_high_yield
            ),
            YieldDef(
                yieldImages[4],
                R.string.fcy_more_than_3_times_higher,
                33.75,
                R.string.lbl_very_high_yield
            )
        )

        val resolvedAmountLabels = amountRes.map { getString(it) }

        val list = yieldDefList.mapIndexed { index, def ->
            // inline creation, no helper function
            val yield = CassavaYield.create(
                def.amountValue, getString(def.labelRes), def.imageId, getString(def.descRes)
            )
            yield.amountLabel = resolvedAmountLabels[index]
            yield
        }

        safeScope.launch {
            cassavaYieldRepo.saveAll(list)
        }

        return list
    }
}