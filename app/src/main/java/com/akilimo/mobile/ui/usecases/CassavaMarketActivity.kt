package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CassavaUnitAdapter
import com.akilimo.mobile.adapters.StarchFactoryAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.dialogs.CassavaPriceSelectionBottomSheet
import com.akilimo.mobile.ui.viewmodels.CassavaMarketViewModel
import com.akilimo.mobile.workers.CassavaUnitWorker
import com.akilimo.mobile.workers.StarchFactoryWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CassavaMarketActivity : BaseActivity<ActivityCassavaMarketBinding>() {

    private val viewModel: CassavaMarketViewModel by viewModels()

    private lateinit var factoryAdapter: StarchFactoryAdapter
    private lateinit var cassavaUnitAdapter: CassavaUnitAdapter

    private var isGridLayout = false
    private var initialChoiceApplied = false
    private val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    override fun inflateBinding() = ActivityCassavaMarketBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupToolbar()
        setupRecyclerViews()
        setupListeners()
        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
    }

    private fun setupToolbar() {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .showBackButton(true)
            .setTitle(getString(R.string.lbl_cassava_price))
            .inflateMenu(R.menu.menu_fertilizers) { item ->
                if (item.itemId == R.id.action_toggle_layout) toggleLayout(item)
            }
            .onNavigationClick { finish() }
            .build()
    }

    private fun toggleLayout(item: android.view.MenuItem) {
        isGridLayout = !isGridLayout
        binding.rvStarchFactories.layoutManager =
            if (isGridLayout) GridLayoutManager(this, gridSpanCount) else LinearLayoutManager(this)
        factoryAdapter.setLayoutMode(isGridLayout)
        item.setIcon(if (isGridLayout) R.drawable.ic_list else R.drawable.ic_grid)
    }

    private fun setupRecyclerViews() = with(binding) {
        factoryAdapter = StarchFactoryAdapter()
        rvStarchFactories.apply {
            layoutManager = LinearLayoutManager(this@CassavaMarketActivity)
            adapter = factoryAdapter
            setHasFixedSize(true)
        }

        cassavaUnitAdapter = CassavaUnitAdapter()
        rvCassavaUnit.apply {
            layoutManager = LinearLayoutManager(this@CassavaMarketActivity)
            adapter = cassavaUnitAdapter
            setHasFixedSize(true)
        }

        factoryAdapter.onItemClick = { factory ->
            viewModel.selectFactory(factory)
            val updatedList = factoryAdapter.currentList.map { item ->
                item.copy().apply { isSelected = item.id == factory.id }
            }
            factoryAdapter.submitList(updatedList)
        }

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
                context = this@CassavaMarketActivity,
                workName = WorkConstants.STARCH_FACTORY_WORK_NAME
            )
        }
        swipeRefreshUnits.setOnRefreshListener {
            WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
                context = this@CassavaMarketActivity,
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
                        context = this@CassavaMarketActivity,
                        workName = WorkConstants.STARCH_FACTORY_WORK_NAME
                    )
                }
                R.id.rb_sell_to_market -> {
                    swipeRefreshFactories.isVisible = false
                    swipeRefreshUnits.isVisible = true
                    WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
                        context = this@CassavaMarketActivity,
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
                    factoryAdapter.submitList(state.factories)
                    cassavaUnitAdapter.submitList(state.cassavaUnits)
                    binding.swipeRefreshFactories.isRefreshing = state.factoriesRefreshing
                    binding.swipeRefreshUnits.isRefreshing = state.unitsRefreshing

                    // Apply initial radio selection exactly once
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
                val updatedList = cassavaUnitAdapter.currentList.map {
                    it.copy().apply { isSelected = it.id == unit.id }
                }
                cassavaUnitAdapter.submitList(updatedList)
            }
        ).show(supportFragmentManager, "CassavaPriceSelectionBottomSheet")
    }
}
