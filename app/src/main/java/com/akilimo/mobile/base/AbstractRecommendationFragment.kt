package com.akilimo.mobile.base

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.fragments.usecases.GetRecommendationFragment
import com.akilimo.mobile.ui.fragments.usecases.UseCaseResults
import com.akilimo.mobile.ui.viewmodels.AdviceCompletionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base fragment for recommendation use-case screens.
 *
 * Mirrors [AbstractRecommendationActivity] but hosted inside the NavGraph.
 * Use-case screens are launched as Fragments via [findNavController]; results
 * are received via the Fragment Result API and forwarded to [AdviceCompletionViewModel].
 *
 * Subclasses MUST:
 *  [1] Provide advice options via [getAdviceOptions]
 *  [2] Map tasks to nav destination IDs via [mapTaskToDestination]
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
            findNavController().navigate(
                R.id.getRecommendationFragment,
                bundleOf(GetRecommendationFragment.ARG_USE_CASE to enumUseCase as Parcelable)
            )
        }

        // Receive AdviceCompletionDto results from use-case fragments
        parentFragmentManager.setFragmentResultListener(
            UseCaseResults.ADVICE_COMPLETION,
            viewLifecycleOwner
        ) { _, bundle ->
            @Suppress("DEPRECATION")
            (bundle.getParcelable<AdviceCompletionDto>(UseCaseResults.ADVICE_COMPLETION_DTO))
                ?.let { adviceViewModel.updateStatus(it) }
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

    protected open fun createRecommendationAdapter(
        layout: EnumRecyclerLayout = currentLayout
    ): RecommendationAdapter<UseCaseOption> {
        val adapter = RecommendationAdapter<UseCaseOption>(
            context = requireContext(),
            getLabel = { it.valueOption.label(requireContext()) },
            getId = { it.valueOption.name },
            stepStatus = { it.stepStatus },
            onClick = { item ->
                mapTaskToDestination(item.valueOption.valueOption)?.let { destId ->
                    findNavController().navigate(destId)
                }
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

    /**
     * Map [task] to a navigation destination ID in nav_recommendations.xml.
     * Return null for tasks that have no navigation target.
     */
    abstract fun mapTaskToDestination(task: EnumAdviceTask): Int?
}
