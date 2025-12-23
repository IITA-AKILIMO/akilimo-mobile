package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.adapters.MaizePerformanceAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.dao.MaizePerformanceRepo
import com.akilimo.mobile.databinding.ActivityMaizePerformanceBinding
import com.akilimo.mobile.dto.MaizePerfOption
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.enums.EnumMaizePerformance
import com.akilimo.mobile.repos.AkilimoUserRepo
import kotlinx.coroutines.launch

class MaizePerformanceActivity : BaseActivity<ActivityMaizePerformanceBinding>() {

    private val userRepo by lazy { AkilimoUserRepo(database.akilimoUserDao()) }
    private val maizeRepo by lazy { MaizePerformanceRepo(database.maizePerformanceDao()) }
    private lateinit var maizePerformanceAdapter: MaizePerformanceAdapter

    val maizePerfOptions = EnumMaizePerformance.entries.map { MaizePerfOption(valueOption = it) }

    override fun inflateBinding() = ActivityMaizePerformanceBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        maizePerformanceAdapter = MaizePerformanceAdapter().apply {
            onItemClick = { selected ->
                safeScope.launch {
                    val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
                    val entity = MaizePerformance(
                        userId = user.id ?: 0,
                        maizePerformance = selected,
                    )

                    maizeRepo.saveOrUpdatePerformance(entity)
                }
            }
        }

        binding.rvMaizePerformance.apply {
            layoutManager = GridLayoutManager(this@MaizePerformanceActivity, 2)
            adapter = maizePerformanceAdapter
            setHasFixedSize(true)
        }
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            maizeRepo.observeByUserId(user.id ?: 0).collect { savedPerf ->
                val options = maizePerfOptions.map { option ->
                    option.copy(isSelected = option.valueOption == savedPerf?.maizePerformance)
                }
                maizePerformanceAdapter.submitList(options)
            }
        }
    }
}
