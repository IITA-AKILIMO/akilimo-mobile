package com.akilimo.mobile.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityRecommendationUseCaseBinding
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.dto.WrappedValueOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.usecases.CassavaMarketActivity
import com.akilimo.mobile.ui.usecases.CassavaYieldActivity
import com.akilimo.mobile.ui.usecases.DatesActivity
import com.akilimo.mobile.ui.usecases.ManualTillageCostActivity
import com.akilimo.mobile.ui.usecases.TractorAccessActivity
import com.akilimo.mobile.ui.usecases.WeedControlCostsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BppActivity : BaseActivity<ActivityRecommendationUseCaseBinding>() {
    private var lastStartedTask: EnumAdviceTask? = null

    private val repo by lazy { AdviceCompletionRepo(database.adviceCompletionDao()) }

    override fun inflateBinding() = ActivityRecommendationUseCaseBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        val adviceOptions = listOf(
            UseCaseOption(EnumAdviceTask.PLANTING_AND_HARVEST),
            UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
            UseCaseOption(EnumAdviceTask.CURRENT_CASSAVA_YIELD),
            UseCaseOption(EnumAdviceTask.MANUAL_TILLAGE_COST),
            UseCaseOption(EnumAdviceTask.TRACTOR_ACCESS),
            UseCaseOption(EnumAdviceTask.COST_OF_WEED_CONTROL),
        )

        val recAdapter = RecommendationAdapter<UseCaseOption>(
            context = this,
            getLabel = { it.valueOption.label(this) },
            getId = { it.valueOption.name },
            stepStatus = { it.stepStatus },
            onClick = { item ->
                lastStartedTask = item.valueOption.valueOption
                val intent = when (item.valueOption.valueOption) {
                    EnumAdviceTask.PLANTING_AND_HARVEST -> Intent(
                        this, DatesActivity::class.java
                    )

                    EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(
                        this, CassavaMarketActivity::class.java
                    )

                    EnumAdviceTask.CURRENT_CASSAVA_YIELD -> Intent(
                        this, CassavaYieldActivity::class.java
                    )

                    EnumAdviceTask.MANUAL_TILLAGE_COST -> Intent(
                        this, ManualTillageCostActivity::class.java
                    )

                    EnumAdviceTask.TRACTOR_ACCESS -> Intent(
                        this, TractorAccessActivity::class.java
                    )


                    EnumAdviceTask.COST_OF_WEED_CONTROL -> Intent(
                        this, WeedControlCostsActivity::class.java
                    )

                    else -> null
                }
                intent?.let {
                    lastStartedTask = item.valueOption.valueOption
                    launcher.launch(it)
                }
            })

        binding.fertilizerRecList.apply {
            layoutManager = LinearLayoutManager(this@BppActivity)
            adapter = recAdapter
        }

        binding.frButton.btnAction.setOnClickListener {
            val intent = Intent(this, GetRecommendationActivity::class.java)
            intent.putExtra(GetRecommendationActivity.EXTRA_USE_CASE, EnumUseCase.PP as Parcelable)
            startActivity(intent)
        }
        // Collect completion statuses from Room and update adapter
        safeScope.launch {
            repo.getAllCompletions().collectLatest { completionsMap ->
                val updatedList = adviceOptions.map { option ->
                    val completed = completionsMap[option.valueOption.name]?.stepStatus
                        ?: EnumStepStatus.NOT_STARTED
                    option.copy(stepStatus = completed)
                }.map { WrappedValueOption(it) }

                recAdapter.submitList(updatedList)
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val dataIntent = result.data
            val resultDto = dataIntent?.let {
                IntentCompat.getParcelableExtra(it, COMPLETED_TASK, AdviceCompletionDto::class.java)
            }

            resultDto?.let {
                safeScope.launch {
                    repo.updateStatus(it)
                }
            }
        }
}