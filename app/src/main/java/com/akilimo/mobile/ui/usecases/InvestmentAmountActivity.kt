package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.InvestmentAmountAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.InvestmentAmountViewModel
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvestmentAmountActivity : BaseActivity<ActivityInvestmentAmountBinding>() {

    private val viewModel: InvestmentAmountViewModel by viewModels()

    private lateinit var adapter: InvestmentAmountAdapter

    override fun inflateBinding() = ActivityInvestmentAmountBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .showBackButton(true)
            .onNavigationClick { finish() }
            .build()

        setupRecycler()
        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
    }

    private fun setupRecycler() {
        adapter = InvestmentAmountAdapter { selected, amount ->
            viewModel.saveInvestment(sessionManager.akilimoUser, selected, amount)
        }
        binding.rvInvestments.apply {
            layoutManager = LinearLayoutManager(this@InvestmentAmountActivity)
            adapter = this@InvestmentAmountActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.investments)
                    binding.rvInvestments.visibility =
                        if (state.investments.isEmpty()) View.GONE else View.VISIBLE

                    state.selectedInvestment?.let { selected ->
                        adapter.updateSelection(
                            investment = selected,
                            enumAreaUnit = state.enumAreaUnit,
                            farmSize = state.farmSize
                        )
                    }
                }
            }
        }
    }
}
