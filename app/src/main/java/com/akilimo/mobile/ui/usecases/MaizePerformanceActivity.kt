package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.adapters.MaizePerformanceAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityMaizePerformanceBinding
import com.akilimo.mobile.ui.viewmodels.MaizePerformanceViewModel
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MaizePerformanceActivity : BaseActivity<ActivityMaizePerformanceBinding>() {

    private val viewModel: MaizePerformanceViewModel by viewModels()

    private lateinit var maizePerformanceAdapter: MaizePerformanceAdapter

    override fun inflateBinding() = ActivityMaizePerformanceBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        maizePerformanceAdapter = MaizePerformanceAdapter().apply {
            onItemClick = { selected ->
                viewModel.saveSelection(sessionManager.akilimoUser, selected)
            }
        }

        binding.rvMaizePerformance.apply {
            layoutManager = GridLayoutManager(this@MaizePerformanceActivity, 2)
            adapter = maizePerformanceAdapter
            setHasFixedSize(true)
        }

        observeViewModel()
        viewModel.loadOptions(sessionManager.akilimoUser)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    maizePerformanceAdapter.submitList(state.options)
                }
            }
        }
    }
}
