package com.akilimo.mobile.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.BaseValueOptionAdapter
import com.akilimo.mobile.dto.UnitOfSaleOption
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.ProduceMarketViews
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.ui.viewmodels.ProduceMarketViewModel
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch

/**
 * Base fragment for produce market screens.
 *
 * Mirrors [AbstractProduceMarketActivity] but hosted inside the NavGraph.
 * On save it pops the back stack — no Fragment Result is sent because the market
 * step status is tracked via the database, not via a result DTO.
 */
abstract class AbstractProduceMarketFragment<VB : ViewBinding>(
    private val marketType: EnumMarketType,
    private val titleRes: Int
) : BaseFragment<VB>() {

    protected val viewModel: ProduceMarketViewModel by viewModels(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<ProduceMarketViewModel.Factory> { factory ->
                factory.create(marketType)
            }
        }
    )

    protected var currencyCode: String = ""

    protected abstract val views: ProduceMarketViews

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupToolbar()
        setupInitialVisibility()
        setupProduceTypeSelection()
        observeViewModel()
        viewModel.loadData(sessionManager.akilimoUser)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.saved) {
                        viewModel.onSaveHandled()
                        findNavController().popBackStack()
                        return@collect
                    }

                    if (state.userId == 0) return@collect

                    currencyCode = state.currencyCode
                    views.priceInputLayout.prefixText = "$currencyCode "

                    state.lastEntry?.let { entry ->
                        updateUnits { unit ->
                            if (entry.produceType == EnumProduceType.MAIZE_FRESH_COB)
                                unit == EnumUnitOfSale.FRESH_COB
                            else
                                unit.isUniversal
                        }
                        views.apply {
                            inputPrice.setText(entry.unitPrice.toString())
                            unitSpinner.setText(
                                entry.unitOfSale.unitOfSale(requireContext()),
                                false
                            )
                            marketHintText.text = getString(R.string.lbl_selling_hint)
                                .replace("{currency}", currencyCode)
                                .replace("{price}", entry.unitPrice.toString())
                                .replace(
                                    "{unit_of_sale}",
                                    entry.unitOfSale.unitOfSale(requireContext())
                                )
                            marketHintCard.isVisible = true
                        }
                        onEntryPrefilled(entry)
                    }
                }
            }
        }
    }

    protected open fun onEntryPrefilled(entry: ProduceMarket) = Unit

    private fun setupToolbar() {
        ToolbarHelper(requireActivity() as AppCompatActivity, views.toolbar)
            .setTitle(getString(titleRes))
            .onNavigationClick { findNavController().popBackStack() }
            .build()
    }

    private fun setupInitialVisibility() = with(views) {
        fabSave.isVisible = false
        unitLabel.isVisible = true
        unitInputLayout.isVisible = true
    }

    protected abstract fun setupProduceTypeSelection()

    protected fun updateUnits(filter: (EnumUnitOfSale) -> Boolean) {
        val filtered = EnumUnitOfSale.entries
            .filter(filter)
            .map { UnitOfSaleOption(valueOption = it) }
        val unitAdapter = BaseValueOptionAdapter(requireContext(), filtered) { it.label(requireContext()) }
        views.apply {
            unitSpinner.setAdapter(unitAdapter)

            inputPrice.addTextChangedListener { validateForm() }

            unitSpinner.setOnItemClickListener { _, _, position, _ ->
                val selected = unitAdapter.getItem(position) ?: return@setOnItemClickListener
                unitSpinner.setText(
                    selected.valueOption.unitOfSale(requireContext()),
                    false
                )
                validateForm()
            }

            fabSave.setOnClickListener { saveMarketEntry() }
        }
    }

    private val enteredPrice: Double?
        get() = views.inputPrice.text?.toString()?.toDoubleOrNull()

    protected fun validateForm() = with(views) {
        fabSave.isVisible =
            enteredPrice?.let { it > 0 } == true && !unitSpinner.text.isNullOrEmpty()
    }

    private fun saveMarketEntry() {
        clearErrors()

        val price = enteredPrice ?: return showPriceError()
        val unit = selectedUnit() ?: return showUnitError()

        val userId = viewModel.uiState.value.userId.takeIf { it != 0 } ?: return
        viewModel.saveMarketEntry(
            ProduceMarket(
                userId = userId,
                unitPrice = price,
                marketType = marketType,
                produceType = resolveProduceType(),
                unitOfSale = unit
            )
        )
    }

    private fun selectedUnit(): EnumUnitOfSale? =
        EnumUnitOfSale.entries.firstOrNull {
            it.unitOfSale(requireContext()) == views.unitSpinner.text.toString()
        }

    private fun showPriceError() = with(views) {
        priceInputLayout.error = getString(R.string.lbl_invalid_grain_price)
    }

    private fun showUnitError() = with(views) {
        unitInputLayout.error = getString(R.string.lbl_invalid_sale_unit)
    }

    private fun clearErrors() = with(views) {
        unitInputLayout.error = null
        priceInputLayout.error = null
    }

    protected abstract fun resolveProduceType(): EnumProduceType
}
