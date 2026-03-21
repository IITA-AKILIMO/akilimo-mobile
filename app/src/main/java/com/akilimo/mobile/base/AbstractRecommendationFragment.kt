package com.akilimo.mobile.base

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.IntentCompat
import androidx.fragment.app.viewModels
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
import com.akilimo.mobile.ui.activities.GetRecommendationActivity
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.AdviceCompletionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base fragment for recommendation use-case screens.
 *
 * Mirrors [AbstractRecommendationActivity] but hosted inside the NavGraph.
 * Use-case activities are still launched via [registerForActivityResult]; results
 * are forwarded to [AdviceCompletionViewModel].
 *
 * Subclasses MUST:
 *  [1] Provide advice options via [getAdviceOptions]
 *  [2] Map tasks to Intents via [mapTaskToIntent]
 *  [3] Provide the [EnumUseCase] for the "Get Recommendation" action via [enumUseCase]
 */
@AndroidEntryPoint
abstract class AbstractRecommendationFragment :
    BaseFragment<ActivityRecommendationUseCaseBinding>() {

    protected var currentLayout: EnumRecyclerLayout = EnumRecyclerLayout.LIST

    protected val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    protected val adviceViewModel: AdviceCompletionViewModel by viewModels()

    abstract val enumUseCase: EnumUseCase

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityRecommendationUseCaseBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(requireActivity() as androidx.appcompat.app.AppCompatActivity, binding.lytToolbar.toolbar)
            .onNavigationClick { requireActivity().onBackPressedDispatcher.onBackPressed() }
            .build()

        val recAdapter = createRecommendationAdapter()
        binding.fertilizerRecList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recAdapter
        }

        binding.frButton.btnAction.setOnClickListener {
            startActivity(
                Intent(requireContext(), GetRecommendationActivity::class.java).apply {
                    putExtra(GetRecommendationActivity.EXTRA_USE_CASE, enumUseCase as Parcelable)
                }
            )
        }

        safeScope.launch {
            adviceViewModel.completions.collectLatest { completions ->
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

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let {
                IntentCompat.getParcelableExtra(it, BaseActivity.COMPLETED_TASK, AdviceCompletionDto::class.java)
            }?.let { dto ->
                adviceViewModel.updateStatus(dto)
            }
        }

    protected open fun createRecommendationAdapter(
        layout: EnumRecyclerLayout = currentLayout
    ): RecommendationAdapter<UseCaseOption> {
        val adapter = RecommendationAdapter<UseCaseOption>(
            context = requireContext(),
            getLabel = { it.valueOption.label(requireContext()) },
            getId = { it.valueOption.name },
            stepStatus = { it.stepStatus },
            onClick = { item ->
                mapTaskToIntent(item.valueOption.valueOption)?.let { launcher.launch(it) }
            }
        )
        binding.fertilizerRecList.layoutManager = when (layout) {
            EnumRecyclerLayout.GRID ->
                androidx.recyclerview.widget.GridLayoutManager(requireContext(), gridSpanCount)
            EnumRecyclerLayout.LIST -> LinearLayoutManager(requireContext())
        }
        return adapter
    }

    abstract fun getAdviceOptions(): List<UseCaseOption>
    abstract fun mapTaskToIntent(task: EnumAdviceTask): Intent?
}
