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
import com.akilimo.mobile.ui.usecases.FertilizersActivity
import com.akilimo.mobile.ui.usecases.InvestmentAmountActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FrActivity : BaseActivity<ActivityRecommendationUseCaseBinding>() {

    private var lastStartedTask: EnumAdviceTask? = null
    private val repo by lazy { AdviceCompletionRepo(database.adviceCompletionDao()) }

    override fun inflateBinding() = ActivityRecommendationUseCaseBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // Setup toolbar
        ToolbarHelper(this, binding.lytToolbar.toolbar).onNavigationClick { finish() }.build()

        // Define all tasks
        val adviceOptions = listOf(
            UseCaseOption(EnumAdviceTask.AVAILABLE_FERTILIZERS),
            UseCaseOption(EnumAdviceTask.INVESTMENT_AMOUNT),
            UseCaseOption(EnumAdviceTask.CASSAVA_MARKET_OUTLET),
            UseCaseOption(EnumAdviceTask.CURRENT_CASSAVA_YIELD)
        )

        // Setup adapter
        val recAdapter = RecommendationAdapter<UseCaseOption>(
            context = this,
            getLabel = { it.valueOption.label(this) },
            getId = { it.valueOption.name },
            stepStatus = { it.stepStatus },
            onClick = { item ->
                lastStartedTask = item.valueOption.valueOption
                val intent = when (item.valueOption.valueOption) {
                    EnumAdviceTask.AVAILABLE_FERTILIZERS -> Intent(
                        this, FertilizersActivity::class.java
                    )

                    EnumAdviceTask.INVESTMENT_AMOUNT -> Intent(
                        this, InvestmentAmountActivity::class.java
                    )

                    EnumAdviceTask.CASSAVA_MARKET_OUTLET -> Intent(
                        this, CassavaMarketActivity::class.java
                    )

                    EnumAdviceTask.CURRENT_CASSAVA_YIELD -> Intent(
                        this, CassavaYieldActivity::class.java
                    )

                    else -> null
                }
                intent?.let {
                    lastStartedTask = item.valueOption.valueOption
                    launcher.launch(it)
                }
            })

        // RecyclerView setup
        binding.fertilizerRecList.apply {
            layoutManager = LinearLayoutManager(this@FrActivity)
            adapter = recAdapter
        }

        binding.frButton.btnAction.setOnClickListener {
            val intent = Intent(this@FrActivity, GetRecommendationActivity::class.java)
            intent.putExtra(GetRecommendationActivity.EXTRA_USE_CASE, EnumUseCase.FR as Parcelable)
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


    // Activity result launcher
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
