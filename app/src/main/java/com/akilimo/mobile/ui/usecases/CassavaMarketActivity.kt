// CassavaMarketActivity.kt
package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CassavaUnitAdapter
import com.akilimo.mobile.adapters.GenericSelectableAdapter
import com.akilimo.mobile.adapters.StarchFactoryAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.databinding.DialogPriceSelectionBinding
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.extensions.toText
import com.akilimo.mobile.helper.WorkStateMapper
import com.akilimo.mobile.helper.WorkStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import com.akilimo.mobile.repos.CassavaUnitRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.StarchFactoryRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
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
    private lateinit var priceRepo: CassavaMarketPriceRepo
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
        priceRepo = CassavaMarketPriceRepo(database.cassavaMarketPriceDao())
        cassavaUnitRepo = CassavaUnitRepo(database.cassavaUnitDao())
    }

    private fun setupToolbar() {
        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .showBackButton(true)
            .setTitle(getString(R.string.lbl_cassava_price))
//            .inflateMenu(R.menu.menu_fertilizers) { item ->
//                if (item.itemId == R.id.action_toggle_layout) toggleLayout(item)
//            }
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

                val countryCode = user.farmCountry ?: return@launch
                val uos = EnumUnitOfSale.entries.find {
                    it.name.equals(unit.label, ignoreCase = true)
                } ?: EnumUnitOfSale.NA

                val prices = priceRepo.getPricesByCountry(countryCode)
                val updatedPriceList: List<CassavaMarketPrice> = prices.map {
                    val shouldSelect =
                        (it.id == marketPrice?.id) && (selectedMarket?.unitOfSale == uos)
                    it.copy().apply {
                        isSelected = shouldSelect
                    }
                }

                showUnitPriceDialog(unit, selectedMarket, updatedPriceList)
            }
        }

        // Swipe-to-refresh handlers for each container
        swipeRefreshFactories.setOnRefreshListener {
            loadFactories(isSwipe = true)
        }
        swipeRefreshUnits.setOnRefreshListener {
            loadCassavaUnits(isSwipe = true)
        }
    }

    private fun setupListeners() = with(binding) {
        // Toggle the swipe container visibility (not the recyclers)
        rgMarketChoice.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_sell_to_factory -> {
                    swipeRefreshFactories.visibility = View.VISIBLE
                    swipeRefreshUnits.visibility = View.GONE
                    // load factories without swipe indicator (initial/background)
                    loadFactories(isSwipe = false)
                }

                R.id.rb_sell_to_market -> {
                    swipeRefreshFactories.visibility = View.GONE
                    swipeRefreshUnits.visibility = View.VISIBLE
                    // load units without swipe indicator (initial/background)
                    loadCassavaUnits(isSwipe = false)
                }
            }
        }
    }

    private fun observeData() = safeScope.launch {
        val userId = userRepo.getUser(sessionManager.akilimoUser)?.id ?: return@launch

        // Fetch persisted selected market for this user (synchronous DB call in coroutine)
        val marketDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
        val selectedMarket = marketDetails?.selectedCassavaMarket


        // Ensure UI reflects the saved selection when the view is loaded
        with(binding) {
            when {
                selectedMarket?.starchFactoryId != null -> {
                    // select factory radio and show factories container
                    rgMarketChoice.check(R.id.rb_sell_to_factory)
                    swipeRefreshFactories.visibility = View.VISIBLE
                    swipeRefreshUnits.visibility = View.GONE
                    loadFactories(isSwipe = false)
                }

                selectedMarket?.cassavaUnitId != null -> {
                    // select unit radio and show units container
                    rgMarketChoice.check(R.id.rb_sell_to_market)
                    swipeRefreshFactories.visibility = View.GONE
                    swipeRefreshUnits.visibility = View.VISIBLE
                    loadCassavaUnits(isSwipe = false)
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
                        isSelected = it.id == marketWithDetails?.selectedCassavaMarket?.starchFactoryId
                    }
                }
                factoryAdapter.submitList(mapped)
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
                        isSelected = unit.id == marketWithDetails?.selectedCassavaMarket?.cassavaUnitId
                    }
                }
                cassavaUnitAdapter.submitList(mapped)
            }
        }
    }


    /**
     * Load factories.
     * @param isSwipe whether this load was user-initiated via the factories' SwipeRefreshLayout
     */
    private fun loadFactories(isSwipe: Boolean) {
        showLoading(true, isSwipe, isFactories = true)
        WorkerScheduler.scheduleOneTimeWorker<StarchFactoryWorker>(
            context = this@CassavaMarketActivity,
            workName = WorkConstants.STARCH_FACTORY_WORK_NAME
        )
        WorkStateMapper.observeWorkAsStatus(
            this@CassavaMarketActivity,
            this,
            WorkConstants.STARCH_FACTORY_WORK_NAME
        ) { status ->
            when (status) {
                WorkStatus.Running -> showLoading(true, isSwipe, isFactories = true)
                is WorkStatus.Failed -> {
                    showSnackBar(status.payload.toText(this@CassavaMarketActivity))
                    showLoading(false, isSwipe, isFactories = true)
                }

                is WorkStatus.Success -> {
                    showSnackBar(status.payload.toText(this@CassavaMarketActivity))
                    showLoading(false, isSwipe, isFactories = true)
                }

                else -> Unit
            }
        }
    }

    /**
     * Load cassava units.
     * @param isSwipe whether this load was user-initiated via the units' SwipeRefreshLayout
     */
    private fun loadCassavaUnits(isSwipe: Boolean) {
        showLoading(true, isSwipe, isFactories = false)
        WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
            context = this@CassavaMarketActivity,
            workName = WorkConstants.CASSAVA_UNITS_WORK_NAME
        )
        WorkStateMapper.observeWorkAsStatus(
            this@CassavaMarketActivity,
            this,
            WorkConstants.CASSAVA_UNITS_WORK_NAME
        ) { status ->
            when (status) {
                WorkStatus.Running -> showLoading(true, isSwipe, isFactories = false)
                is WorkStatus.Failed -> {
                    showSnackBar(status.payload.toText(this@CassavaMarketActivity))
                    showLoading(false, isSwipe, isFactories = false)
                }

                is WorkStatus.Success -> {
                    showSnackBar(status.payload.toText(this@CassavaMarketActivity))
                    showLoading(false, isSwipe, isFactories = false)
                }

                else -> Unit
            }
        }
    }

    private fun showSnackBar(string: String) {
        Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Show inline progress or the appropriate SwipeRefreshLayout spinner depending on isSwipe and which container.
     * @param visible show/hide
     * @param isSwipe whether to use the swipe spinner
     * @param isFactories true => target swipeRefreshFactories, false => swipeRefreshUnits
     */
    private fun showLoading(visible: Boolean, isSwipe: Boolean, isFactories: Boolean) =
        with(binding) {
            if (isSwipe) {
                if (isFactories) swipeRefreshFactories.isRefreshing = visible
                else swipeRefreshUnits.isRefreshing = visible
            } else {
                inlineProgress.visibility = if (visible) View.VISIBLE else View.GONE
            }
        }

    private fun showUnitPriceDialog(
        unit: CassavaUnit,
        selectedMarket: SelectedCassavaMarket?,
        prices: List<CassavaMarketPrice>
    ) {
        val binding = DialogPriceSelectionBinding.inflate(layoutInflater)
        val recyclerView = binding.priceRecycler.apply {
            layoutManager = LinearLayoutManager(this@CassavaMarketActivity)
        }

        val unitOfSaleEnum = EnumUnitOfSale.entries.find {
            it.name.equals(unit.label, ignoreCase = true)
        } ?: EnumUnitOfSale.NA

        safeScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                var selectedMarketPrice: CassavaMarketPrice? = null
                val unitPriceAdapter = GenericSelectableAdapter<CassavaMarketPrice>(
                    scope = lifecycleScope,
                    getId = { it.id },
                    getLabel = {
                        val computedPrice =
                            MathHelper.computeUnitPrice(it.averagePrice, unitOfSaleEnum)
                        when {
                            it.averagePrice == 0.0 -> getString(R.string.lbl_do_not_know)
                            it.averagePrice < 0.0 -> getString(R.string.lbl_exact_price_x_per_unit_of_sale)
                            else -> "$computedPrice ${it.currencySymbol}"
                        }
                    },
                    isSelected = { it.isSelected },
                    isExactPrice = { it.exactPrice },
                    getExactPrice = {
                        val matches = (selectedMarket?.marketPriceId ?: -1) == it.id
                        when {
                            matches -> selectedMarket?.unitPrice
                            else -> null
                        }
                    },
                    onItemClick = { selectedPrice ->
                        selectedMarketPrice = selectedPrice
                    },
                    onExactAmount = { selectedPrice, amount ->
                        selectedMarketPrice =
                            selectedPrice.copy(exactPrice = true, averagePrice = amount)
                    }
                )
                recyclerView.adapter = unitPriceAdapter
                unitPriceAdapter.updateItems(prices)

                AlertDialog.Builder(this@CassavaMarketActivity)
                    .setTitle("Select Price for ${unitOfSaleEnum.unitOfSale(this@CassavaMarketActivity)}")
                    .setView(binding.root)
                    .setPositiveButton(getString(R.string.lbl_save)) { _, _ ->
                        if (selectedMarketPrice == null) return@setPositiveButton
                        saveSelectedPrice(unitOfSaleEnum, unit, selectedMarketPrice)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show()
            }
        }
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