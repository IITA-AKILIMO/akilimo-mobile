package com.akilimo.mobile.base

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.databinding.ActivityRecommendationUseCaseBinding
import com.akilimo.mobile.dto.UseCaseOption
import com.akilimo.mobile.dto.WrappedValueOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumRecyclerLayout
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.ui.activities.GetRecommendationActivity
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base screen for recommendation-related activities.
 *
 * Responsibilities:
 * - Provides toolbar setup
 * - Displays a list of advice options with completion state
 * - Handles launching activities for each task through Activity Result API
 * - Updates repository upon task completion
 *
 * Subclasses MUST:
 *  [1] Provide list of advice options via [getAdviceOptions]
 *  [2] Map tasks to activity Intents via [mapTaskToIntent]
 */
abstract class AbstractRecommendationActivity(private val enumUseCase: EnumUseCase) :
    BaseActivity<ActivityRecommendationUseCaseBinding>() {

    protected var currentLayout: EnumRecyclerLayout = EnumRecyclerLayout.LIST

    protected val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    protected val repo by lazy { AdviceCompletionRepo(database.adviceCompletionDao()) }

    override fun inflateBinding() = ActivityRecommendationUseCaseBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {

        // ---- Toolbar Setup ----
        setupToolbar(binding.lytToolbar.toolbar)

        val recAdapter = createRecommendationAdapter()
        binding.fertilizerRecList.apply {
            layoutManager = LinearLayoutManager(this@AbstractRecommendationActivity)
            adapter = recAdapter
        }

        // ---- Bottom Action Button ----
        binding.frButton.btnAction.setOnClickListener {
            val intent = Intent(this, GetRecommendationActivity::class.java)
            intent.putExtra(GetRecommendationActivity.EXTRA_USE_CASE, enumUseCase as Parcelable)
            startActivity(intent)
        }

        // ---- Observe and Update Recommendation States ----
        safeScope.launch {
            repo.getAllCompletions().collectLatest { completions ->

                val updatedList = getAdviceOptions()
                    .map { option ->
                        val status = completions[option.valueOption.name]?.stepStatus
                            ?: EnumStepStatus.NOT_STARTED
                        option.copy(stepStatus = status)
                    }
                    .map { WrappedValueOption(it) }

                recAdapter.submitList(updatedList)
            }
        }
    }

    // ---- Activity Result Handler ----
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            val completionDto = result.data?.let {
                IntentCompat.getParcelableExtra(
                    it,
                    COMPLETED_TASK,
                    AdviceCompletionDto::class.java
                )
            }

            completionDto?.let { dto ->
                safeScope.launch { repo.updateStatus(dto) }
            }
        }

    protected open fun setupToolbar(toolbar: MaterialToolbar) {
        ToolbarHelper(this, toolbar)
            .onNavigationClick { finish() }
            .build()
    }

    // ---- Default adapter creation logic ----
    protected open fun createRecommendationAdapter(
        layout: EnumRecyclerLayout = currentLayout
    ): RecommendationAdapter<UseCaseOption> {

        val adapter = RecommendationAdapter<UseCaseOption>(
            context = this,
            getLabel = { it.valueOption.label(this) },
            getId = { it.valueOption.name },
            stepStatus = { it.stepStatus },
            onClick = { item ->
                mapTaskToIntent(item.valueOption.valueOption)?.let { launcher.launch(it) }
            }
        )

        // Optionally, configure adapter or item decorators based on layout
        when (layout) {
            EnumRecyclerLayout.LIST -> {
                binding.fertilizerRecList.layoutManager = LinearLayoutManager(this)
            }

            EnumRecyclerLayout.GRID -> {
                binding.fertilizerRecList.layoutManager =
                    androidx.recyclerview.widget.GridLayoutManager(this, gridSpanCount)
            }
        }

        return adapter
    }

    protected fun toggleLayout() {
        currentLayout = when (currentLayout) {
            EnumRecyclerLayout.LIST -> EnumRecyclerLayout.GRID
            EnumRecyclerLayout.GRID -> EnumRecyclerLayout.LIST
        }

        val newAdapter = createRecommendationAdapter(currentLayout)
        binding.fertilizerRecList.adapter = newAdapter

        // Re-submit current list
        safeScope.launch {
            repo.getAllCompletions().collectLatest { completions ->
                val updatedList = getAdviceOptions()
                    .map { option ->
                        val status = completions[option.valueOption.name]?.stepStatus
                            ?: EnumStepStatus.NOT_STARTED
                        option.copy(stepStatus = status)
                    }
                    .map { WrappedValueOption(it) }

                newAdapter.submitList(updatedList)
            }
        }
    }


    /**
     * Subclasses must provide the full list of advice options.
     * Order is preserved as provided.
     */
    protected abstract fun getAdviceOptions(): List<UseCaseOption>

    /**
     * Subclasses must map an EnumAdviceTask to its target activity Intent.
     * Return null to disable launching for a task.
     */
    protected abstract fun mapTaskToIntent(task: EnumAdviceTask): Intent?
}
