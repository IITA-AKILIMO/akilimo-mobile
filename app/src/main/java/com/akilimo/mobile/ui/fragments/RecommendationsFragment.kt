package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.FragmentRecommendationsBinding
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.ui.components.CollapsibleToolbarHelper
import com.akilimo.mobile.ui.viewmodels.RecommendationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecommendationsFragment : BaseFragment<FragmentRecommendationsBinding>() {

    private val viewModel: RecommendationsViewModel by viewModels()

    private lateinit var recAdapter: RecommendationAdapter<EnumAdvice>

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentRecommendationsBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        CollapsibleToolbarHelper(requireActivity() as androidx.appcompat.app.AppCompatActivity, binding.lytToolbar).build()

        recAdapter = RecommendationAdapter<EnumAdvice>(
            context = requireContext(),
            showIcon = false,
            getLabel = { it.label(requireContext()) },
            getId = { it.name },
            onClick = { selected ->
                viewModel.trackActiveAdvice(sessionManager.akilimoUser, selected.valueOption)
                val destId = when (selected.valueOption) {
                    EnumAdvice.FERTILIZER_RECOMMENDATIONS -> R.id.frFragment
                    EnumAdvice.BEST_PLANTING_PRACTICES -> R.id.bppFragment
                    EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> R.id.sphFragment
                    EnumAdvice.INTERCROPPING_MAIZE -> R.id.icMaizeFragment
                    EnumAdvice.INTERCROPPING_SWEET_POTATO -> R.id.icSweetPotatoFragment
                }
                findNavController().navigate(destId)
            }
        )

        binding.recommendationList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recAdapter
        }

        observeViewModel()
        viewModel.loadAdviceOptions(sessionManager.akilimoUser)
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
