package com.akilimo.mobile.ui.fragments.usecases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CassavaUnitAdapter
import com.akilimo.mobile.adapters.StarchFactoryAdapter
import com.akilimo.mobile.base.BaseFragment
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.enums.EnumUnitOfSale
import androidx.navigation.fragment.findNavController
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.dialogs.CassavaPriceSelectionBottomSheet
import com.akilimo.mobile.ui.viewmodels.CassavaMarketViewModel
import com.akilimo.mobile.workers.CassavaUnitWorker
import com.akilimo.mobile.workers.StarchFactoryWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CassavaMarketFragment : BaseFragment<ActivityCassavaMarketBinding>() {

    private val viewModel: CassavaMarketViewModel by viewModels()

    private lateinit var factoryAdapter: StarchFactoryAdapter
    private lateinit var cassavaUnitAdapter: CassavaUnitAdapter

    private var isGridLayout = false
    private var initialChoiceApplied = false
    private val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityCassavaMarketBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        ToolbarHelper(requireActivity() as androidx.appcompat.app.AppCompatActivity, binding.lytToolbar.toolbar)
            .showBackButton(true)
            .setTitle(getString(R.string.lbl_cassava_price))
            .inflateMenu(R.menu.menu_fertilizers) { item -> onMenuItemClicked(item) }
            .onNavigationClick { findNavController().popBackStack() }
            .build()

        setupRecyclerViews()
        setupListeners()
        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
    }

    private fun onMenuItemClicked(item: MenuItem) {
        if (item.itemId == R.id.action_toggle_layout) {
            isGridLayout = !isGridLayout
            binding.rvStarchFactories.layoutManager =
                if (isGridLayout) GridLayoutManager(requireContext(), gridSpanCount)
                else LinearLayoutManager(requireContext())
            factoryAdapter.setLayoutMode(isGridLayout)
            item.setIcon(if (isGridLayout) R.drawable.ic_list else R.drawable.ic_grid)
        }
    }

    private fun setupRecyclerViews() = with(binding) {
        factoryAdapter = StarchFactoryAdapter()
        rvStarchFactories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = factoryAdapter
            setHasFixedSize(true)
        }

        cassavaUnitAdapter = CassavaUnitAdapter()
        rvCassavaUnit.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cassavaUnitAdapter
            setHasFixedSize(true)
        }

        factoryAdapter.onItemClick = { factory -> viewModel.selectFactory(factory) }

        cassavaUnitAdapter.onItemClick = { unit ->
            safeScope.launch {
                val userId = viewModel.uiState.value.userId
                val country = viewModel.uiState.value.userCountry
                val marketWithDetails = viewModel.selectedRepo.getSelectedByUser(userId)
                val marketPrice = marketWithDetails?.marketPrice
                val selectedMarket = marketWithDetails?.selectedCassavaMarket
                val uos = EnumUnitOfSale.entries.find { it.name.equals(unit.label, ignoreCase = true) }
                val prices = viewModel.priceRepo.getPricesByCountry(country)
                val updatedPriceList = prices.map {
                    val shouldSelect = (it.id == marketPrice?.id) && (selectedMarket?.unitOfSale == uos)
                    it.copy().apply { isSelected = shouldSelect }
                }
                showUnitPriceBottomSheet(unit, selectedMarket, updatedPriceList)
            }
        }

        swipeRefreshFactories.setOnRefreshListener {
            WorkerScheduler.scheduleOneTimeWorker<StarchFactoryWorker>(
                context = requireContext(),
                workName = WorkConstants.STARCH_FACTORY_WORK_NAME
            )
        }
        swipeRefreshUnits.setOnRefreshListener {
            WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
                context = requireContext(),
                workName = WorkConstants.CASSAVA_UNITS_WORK_NAME
            )
        }
    }

    private fun setupListeners() = with(binding) {
        rgMarketChoice.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_sell_to_factory -> {
                    swipeRefreshFactories.isVisible = true
                    swipeRefreshUnits.isVisible = false
                    WorkerScheduler.scheduleOneTimeWorker<StarchFactoryWorker>(
                        context = requireContext(),
                        workName = WorkConstants.STARCH_FACTORY_WORK_NAME
                    )
                }
                R.id.rb_sell_to_market -> {
                    swipeRefreshFactories.isVisible = false
                    swipeRefreshUnits.isVisible = true
                    WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
                        context = requireContext(),
                        workName = WorkConstants.CASSAVA_UNITS_WORK_NAME
                    )
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val factories = state.factories.map { f ->
                        f.copy().apply { isSelected = f.id == state.selectedFactoryId }
                    }
                    factoryAdapter.submitList(factories)

                    val units = state.cassavaUnits.map { u ->
                        u.copy().apply { isSelected = u.id == state.selectedUnitId }
                    }
                    cassavaUnitAdapter.submitList(units)

                    binding.swipeRefreshFactories.isRefreshing = state.factoriesRefreshing
                    binding.swipeRefreshUnits.isRefreshing = state.unitsRefreshing

                    if (!initialChoiceApplied) {
                        when (state.initialMarketChoice) {
                            CassavaMarketViewModel.MarketChoice.FACTORY -> {
                                binding.rgMarketChoice.check(R.id.rb_sell_to_factory)
                                initialChoiceApplied = true
                            }
                            CassavaMarketViewModel.MarketChoice.MARKET -> {
                                binding.rgMarketChoice.check(R.id.rb_sell_to_market)
                                initialChoiceApplied = true
                            }
                            CassavaMarketViewModel.MarketChoice.NONE -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun showUnitPriceBottomSheet(
        unit: CassavaUnit,
        selectedMarket: SelectedCassavaMarket?,
        prices: List<CassavaMarketPrice>
    ) {
        CassavaPriceSelectionBottomSheet(
            unit = unit,
            selectedMarket = selectedMarket,
            prices = prices,
            onPriceSelected = { selectedPrice, uos ->
                viewModel.saveSelectedPrice(sessionManager.akilimoUser, unit, uos, selectedPrice)
            }
        ).show(childFragmentManager, "CassavaPriceSelectionBottomSheet")
    }
}
