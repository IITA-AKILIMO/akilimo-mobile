package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
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
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import com.akilimo.mobile.repos.CassavaUnitRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.StarchFactoryRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.dialogs.CassavaPriceSelectionBottomSheet
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.workers.CassavaUnitWorker
import com.akilimo.mobile.workers.StarchFactoryWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CassavaMarketActivity : BaseActivity<ActivityCassavaMarketBinding>() {

    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var factoryRepo: StarchFactoryRepo
    private lateinit var selectedCassavaMarketRepo: SelectedCassavaMarketRepo
    private lateinit var cassavaMarketPriceRepo: CassavaMarketPriceRepo
    private lateinit var cassavaUnitRepo: CassavaUnitRepo

    private lateinit var factoryAdapter: StarchFactoryAdapter
    private lateinit var cassavaUnitAdapter: CassavaUnitAdapter

    private var isGridLayout = false
    private val gridSpanCount by lazy { resources.getInteger(R.integer.grid_span_count_default) }

    override fun inflateBinding() = ActivityCassavaMarketBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initRepositories()
        setupToolbar()
        setupRecyclerViews()
        setupListeners()
        observeData()
    }

    private fun initRepositories() {
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        factoryRepo = StarchFactoryRepo(database.starchFactoryDao())
        selectedCassavaMarketRepo = SelectedCassavaMarketRepo(database.selectedCassavaMarketDao())
        cassavaMarketPriceRepo = CassavaMarketPriceRepo(database.cassavaMarketPriceDao())
        cassavaUnitRepo = CassavaUnitRepo(database.cassavaUnitDao())
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
            safeScope.launch {
                val userId = userRepo.getUser(sessionManager.akilimoUser)?.id ?: return@launch
                val selected = SelectedCassavaMarket(userId = userId, starchFactoryId = factory.id)
                selectedCassavaMarketRepo.select(selected)

                val updatedList = factoryAdapter.currentList.map {
                    it.copy().apply {
                        isSelected = it.id == factory.id
                    }
                }
                factoryAdapter.submitList(updatedList)
            }
        }

        cassavaUnitAdapter.onItemClick = { unit ->
            safeScope.launch {
                val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
                val userId = user.id ?: return@launch
                val marketWithDetails =
                    selectedCassavaMarketRepo.getSelectedByUser(userId = userId)
                val marketPrice = marketWithDetails?.marketPrice
                val selectedMarket = marketWithDetails?.selectedCassavaMarket

                val countryCode = user.enumCountry
                val uos = EnumUnitOfSale.entries.find {
                    it.name.equals(unit.label, ignoreCase = true)
                }

                val prices = cassavaMarketPriceRepo.getPricesByCountry(countryCode)
                val updatedPriceList: List<CassavaMarketPrice> = prices.map {
                    val shouldSelect =
                        (it.id == marketPrice?.id) && (selectedMarket?.unitOfSale == uos)
                    it.copy().apply {
                        isSelected = shouldSelect
                    }
                }

                showUnitPriceBottomSheet(unit, selectedMarket, updatedPriceList)
            }
        }

        // Swipe-to-refresh handlers for each container
        swipeRefreshFactories.setOnRefreshListener {
            WorkerScheduler.scheduleOneTimeWorker<StarchFactoryWorker>(
                context = this@CassavaMarketActivity,
                workName = WorkConstants.STARCH_FACTORY_WORK_NAME
            )
        }
        swipeRefreshUnits.setOnRefreshListener {
            WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
                context = this@CassavaMarketActivity,
                workName = WorkConstants.CASSAVA_UNIT_WORK_NAME
            )
        }
    }

    private fun setupListeners() = with(binding) {
        // Toggle the swipe container visibility
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
                        workName = WorkConstants.CASSAVA_UNIT_WORK_NAME
                    )
                }
            }
        }
    }

    private fun observeData() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val userId = user.id ?: return@launch

        // Fetch persisted selected market for this user
        val marketDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
        val selectedMarket = marketDetails?.selectedCassavaMarket

        // Ensure UI reflects the saved selection when the view is loaded
        with(binding) {
            when {
                selectedMarket?.starchFactoryId != null -> {
                    rgMarketChoice.check(R.id.rb_sell_to_factory)
                }

                selectedMarket?.cassavaUnitId != null -> {
                    rgMarketChoice.check(R.id.rb_sell_to_market)
                }
            }
        }

        // Observe factories and update adapter
        launch {
            factoryRepo.observeAll().collectLatest { factories ->
                val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                val mapped = factories.map {
                    StarchFactory(
                        id = it.id,
                        name = it.name,
                        label = it.label
                    ).apply {
                        isSelected =
                            it.id == marketWithDetails?.selectedCassavaMarket?.starchFactoryId
                    }
                }
                factoryAdapter.submitList(mapped)
                binding.swipeRefreshFactories.isRefreshing = false
            }
        }

        // Observe cassava units and update adapter
        launch {
            cassavaUnitRepo.observeAll().collectLatest { cassavaUnits ->
                val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                val mapped = cassavaUnits.map { unit ->
                    CassavaUnit(
                        id = unit.id,
                        label = unit.label,
                        description = unit.description,
                    ).apply {
                        isSelected =
                            unit.id == marketWithDetails?.selectedCassavaMarket?.cassavaUnitId
                    }
                }
                cassavaUnitAdapter.submitList(mapped)
                binding.swipeRefreshUnits.isRefreshing = false
            }
        }
    }


    private fun showSnackBar(string: String) {
        Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG).show()
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
                saveSelectedPrice(uos, unit, selectedPrice)
            }
        ).show(supportFragmentManager, "CassavaPriceSelectionBottomSheet")
    }


    private fun saveSelectedPrice(
        uos: EnumUnitOfSale,
        unit: CassavaUnit,
        selectedPrice: CassavaMarketPrice?
    ) {
        safeScope.launch(Dispatchers.IO) {
            val userId =
                userRepo.getUser(sessionManager.akilimoUser)?.id ?: return@launch

            val exactPrice = selectedPrice?.exactPrice ?: false
            val unitPrice = when {
                exactPrice -> {
                    selectedPrice.averagePrice
                }

                else -> {
                    MathHelper.computeUnitPrice(selectedPrice?.averagePrice ?: 0.0, uos)
                }
            }


            val selected = SelectedCassavaMarket(
                userId = userId,
                cassavaUnitId = unit.id,
                unitOfSale = uos,
                unitPrice = unitPrice,
                marketPriceId = selectedPrice?.id
            )
            selectedCassavaMarketRepo.select(selected)

            withContext(Dispatchers.Main) {
                val updatedList = cassavaUnitAdapter.currentList.map {
                    it.copy().apply {
                        isSelected = it.id == unit.id
                    }
                }
                cassavaUnitAdapter.submitList(updatedList)
            }
        }
    }

}