// CassavaMarketActivity.kt
package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CassavaUnitAdapter
import com.akilimo.mobile.adapters.StarchFactoryAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.entities.AkilimoUser
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
import com.akilimo.mobile.ui.dialogs.CassavaPriceSelectionBottomSheet
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.workers.CassavaUnitWorker
import com.akilimo.mobile.workers.StarchFactoryWorker
import com.akilimo.mobile.workers.WorkConstants
import com.akilimo.mobile.workers.WorkerScheduler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
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

    private var currentUser: AkilimoUser? = null
    private var isSwipingFactories = false
    private var isSwipingUnits = false

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
            .onNavigationClick { finish() }
            .build()
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

            // Update UI immediately for instant feedback
            val updatedList = factoryAdapter.currentList.map {
                it.copy().apply { isSelected = it.id == factory.id }
            }
            factoryAdapter.submitList(updatedList)

            safeScope.launch(Dispatchers.IO) {
                try {
                    val userId = currentUser?.id ?: return@launch
                    val selected =
                        SelectedCassavaMarket(userId = userId, starchFactoryId = factory.id)
                    selectedCassavaMarketRepo.select(selected)
                    // Observer will reactively update the adapter
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showSnackBar(
                            e.localizedMessage ?: getString(R.string.error_loading_factories)
                        )
                    }
                }
            }
        }

        cassavaUnitAdapter.onItemClick = { unit ->
            safeScope.launch(Dispatchers.IO) {
                try {
                    val user = currentUser ?: return@launch
                    val userId = user.id ?: return@launch
                    val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                    val marketPrice = marketWithDetails?.marketPrice
                    val selectedMarket = marketWithDetails?.selectedCassavaMarket
                    val uos = EnumUnitOfSale.entries.find {
                        it.name.equals(unit.label, ignoreCase = true)
                    }
                    val prices = cassavaMarketPriceRepo.getPricesByCountry(user.enumCountry)
                    val updatedPriceList = prices.map {
                        val shouldSelect =
                            (it.id == marketPrice?.id) && (selectedMarket?.unitOfSale == uos)
                        it.copy().apply { isSelected = shouldSelect }
                    }
                    withContext(Dispatchers.Main) {
                        showUnitPriceBottomSheet(unit, selectedMarket, updatedPriceList)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showSnackBar(e.localizedMessage ?: getString(R.string.error_loading_units))
                    }
                }
            }
        }

        swipeRefreshFactories.setOnRefreshListener {
            isSwipingFactories = true
            loadFactories()
        }
        swipeRefreshUnits.setOnRefreshListener {
            isSwipingUnits = true
            loadCassavaUnits()
        }
    }

    private fun setupListeners() = with(binding) {
        rgMarketChoice.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_sell_to_factory -> {
                    swipeRefreshFactories.visibility = View.VISIBLE
                    swipeRefreshUnits.visibility = View.GONE
                    loadFactories()
                }

                R.id.rb_sell_to_market -> {
                    swipeRefreshFactories.visibility = View.GONE
                    swipeRefreshUnits.visibility = View.VISIBLE
                    loadCassavaUnits()
                }
            }
        }
    }

    private fun observeData() = safeScope.launch(Dispatchers.IO) {
        try {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: return@launch
            currentUser = user

            val selectedMarket = selectedCassavaMarketRepo
                .getSelectedByUser(userId)?.selectedCassavaMarket

            withContext(Dispatchers.Main) {
                with(binding) {
                    when {
                        selectedMarket?.starchFactoryId != null -> {
                            rgMarketChoice.check(R.id.rb_sell_to_factory)
                            swipeRefreshFactories.visibility = View.VISIBLE
                            swipeRefreshUnits.visibility = View.GONE
                        }

                        selectedMarket?.cassavaUnitId != null -> {
                            rgMarketChoice.check(R.id.rb_sell_to_market)
                            swipeRefreshFactories.visibility = View.GONE
                            swipeRefreshUnits.visibility = View.VISIBLE
                        }

                        else -> {
                            rgMarketChoice.check(R.id.rb_sell_to_factory)
                            swipeRefreshFactories.visibility = View.VISIBLE
                            swipeRefreshUnits.visibility = View.GONE
                        }
                    }
                }
            }

            // Observe factories for live DB changes
            launch {
                factoryRepo.observeByCountry(user.enumCountry.name)
                    .catch { e ->
                        withContext(Dispatchers.Main) {
                            showSnackBar(
                                e.localizedMessage ?: getString(R.string.error_loading_factories)
                            )
                        }
                    }
                    .collectLatest { factories ->
                        val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                        val mapped = mapFactories(
                            factories,
                            marketWithDetails?.selectedCassavaMarket?.starchFactoryId
                        )
                        withContext(Dispatchers.Main) {
                            factoryAdapter.submitList(mapped)
                            dismissLoading(isFactories = true)
                        }
                    }
            }

            // Observe cassava units for live DB changes
            launch {
                cassavaUnitRepo.observeAll()
                    .catch { e ->
                        withContext(Dispatchers.Main) {
                            showSnackBar(
                                e.localizedMessage ?: getString(R.string.error_loading_units)
                            )
                        }
                    }
                    .collectLatest { units ->
                        val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                        val mapped = mapCassavaUnits(
                            units,
                            marketWithDetails?.selectedCassavaMarket?.cassavaUnitId
                        )
                        withContext(Dispatchers.Main) {
                            cassavaUnitAdapter.submitList(mapped)
                            dismissLoading(isFactories = false)
                        }
                    }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showSnackBar(e.localizedMessage ?: getString(R.string.error_generic))
            }
        }
    }

    private fun loadFactories() {
        val isSwipe = isSwipingFactories
        showLoading(true, isSwipe, isFactories = true)
        safeScope.launch(Dispatchers.IO) {
            try {
                val user = currentUser ?: return@launch
                val userId = user.id ?: return@launch
                val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                val factories = mapFactories(
                    factoryRepo.findAllByCountry(user.enumCountry.name),
                    marketWithDetails?.selectedCassavaMarket?.starchFactoryId
                )
                withContext(Dispatchers.Main) {
                    factoryAdapter.submitList(factories)
                    showLoading(false, isSwipe, isFactories = true)
                    isSwipingFactories = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false, isSwipe, isFactories = true)
                    isSwipingFactories = false
                    showSnackBar(e.localizedMessage ?: getString(R.string.error_loading_factories))
                }
            }
        }
    }

    private fun loadCassavaUnits() {
        val isSwipe = isSwipingUnits
        showLoading(true, isSwipe, isFactories = false)
        safeScope.launch(Dispatchers.IO) {
            try {
                val user = currentUser ?: return@launch
                val userId = user.id ?: return@launch
                val marketWithDetails = selectedCassavaMarketRepo.getSelectedByUser(userId)
                val units = mapCassavaUnits(
                    cassavaUnitRepo.getAll(),
                    marketWithDetails?.selectedCassavaMarket?.cassavaUnitId
                )
                withContext(Dispatchers.Main) {
                    cassavaUnitAdapter.submitList(units)
                    showLoading(false, isSwipe, isFactories = false)
                    isSwipingUnits = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false, isSwipe, isFactories = false)
                    isSwipingUnits = false
                    showSnackBar(e.localizedMessage ?: getString(R.string.error_loading_units))
                }
            }
        }
    }

    // --- Mappers ---

    private fun mapFactories(factories: List<StarchFactory>, selectedId: Int?) = factories.map {
        StarchFactory(id = it.id, name = it.name, label = it.label).apply {
            isSelected = it.id == selectedId
        }
    }

    private fun mapCassavaUnits(units: List<CassavaUnit>, selectedId: Int?) = units.map {
        CassavaUnit(id = it.id, label = it.label, description = it.description).apply {
            isSelected = it.id == selectedId
        }
    }

    // --- Helpers ---

    private fun dismissLoading(isFactories: Boolean) {
        showLoading(
            visible = false,
            isSwipe = if (isFactories) isSwipingFactories else isSwipingUnits,
            isFactories = isFactories
        )
    }

    private fun showLoading(visible: Boolean, isSwipe: Boolean, isFactories: Boolean) =
        with(binding) {
            if (isSwipe) {
                if (isFactories) swipeRefreshFactories.isRefreshing = visible
                else swipeRefreshUnits.isRefreshing = visible
            } else {
                inlineProgress.visibility = if (visible) View.VISIBLE else View.GONE
            }
        }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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
            try {
                val userId = currentUser?.id ?: return@launch
                val unitPrice = if (selectedPrice?.exactPrice == true) {
                    selectedPrice.averagePrice
                } else {
                    MathHelper.computeUnitPrice(selectedPrice?.averagePrice ?: 0.0, uos)
                }
                val selected = SelectedCassavaMarket(
                    userId = userId,
                    cassavaUnitId = unit.id,
                    unitOfSale = uos,
                    unitPrice = unitPrice,
                    marketPriceId = selectedPrice?.id
                )
                selectedCassavaMarketRepo.select(selected)
                // No manual adapter update — the observer will react automatically
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showSnackBar(e.localizedMessage ?: getString(R.string.error_generic))
                }
            }
        }
    }
}