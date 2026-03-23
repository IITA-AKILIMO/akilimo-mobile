package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.InvestmentAmountAdapter
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding
import com.akilimo.mobile.ui.viewmodels.InvestmentAmountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InvestmentAmountFragment : BaseFragment<ActivityInvestmentAmountBinding>() {

    private val viewModel: InvestmentAmountViewModel by viewModels()
    private lateinit var adapter: InvestmentAmountAdapter

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityInvestmentAmountBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        adapter = InvestmentAmountAdapter { selected, amount ->
            viewModel.saveInvestment(selected, amount)
        }

        binding.rvInvestments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@InvestmentAmountFragment.adapter
            setHasFixedSize(true)
        }

        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
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
