package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.adapters.MaizePerformanceAdapter
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.ActivityMaizePerformanceBinding
import com.akilimo.mobile.ui.viewmodels.MaizePerformanceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MaizePerformanceFragment : BaseFragment<ActivityMaizePerformanceBinding>() {

    private val viewModel: MaizePerformanceViewModel by viewModels()

    private lateinit var maizePerformanceAdapter: MaizePerformanceAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityMaizePerformanceBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        maizePerformanceAdapter = MaizePerformanceAdapter().apply {
            onItemClick = { selected ->
                viewModel.saveSelection(sessionManager.akilimoUser, selected)
            }
        }

        binding.rvMaizePerformance.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = maizePerformanceAdapter
            setHasFixedSize(true)
        }

        observeViewModel()
        viewModel.loadOptions(sessionManager.akilimoUser)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    maizePerformanceAdapter.submitList(state.options)
                }
            }
        }
    }
}
