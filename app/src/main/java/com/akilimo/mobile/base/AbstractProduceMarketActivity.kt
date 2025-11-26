package com.akilimo.mobile.base

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.BaseValueOptionAdapter
import com.akilimo.mobile.dao.ProduceMarketRepo
import com.akilimo.mobile.dto.UnitOfSaleOption
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.ui.components.ProduceMarketViews
import com.akilimo.mobile.ui.components.ToolbarHelper
import kotlinx.coroutines.launch

abstract class AbstractProduceMarketActivity<T : ViewBinding>(
    private val marketType: EnumMarketType,
    private val titleRes: Int
) : BaseActivity<T>() {

    protected val userRepo by lazy { AkilimoUserRepo(database.akilimoUserDao()) }
    protected val marketRepo by lazy { ProduceMarketRepo(database.produceMarketDao()) }

    protected var currencyCode: String = ""

    protected abstract val views: ProduceMarketViews


    /** --- Lifecycle --- */
    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupToolbar()
        setupInitialVisibility()
        setupProduceTypeSelection()
        prefillFromEntity()
    }

    /** --- UI Setup --- */
    private fun setupToolbar() {
        ToolbarHelper(this, views.toolbar)
            .setTitle(getString(titleRes))
            .onNavigationClick { finish() }
            .build()
    }


    private fun setupInitialVisibility() = with(views) {
        fabSave.isVisible = false
        unitLabel.isVisible = true
        unitInputLayout.isVisible = true
    }

    /** Hook for subclasses to setup produce type selection */
    protected open fun setupProduceTypeSelection() = Unit

    /** --- Prefill --- */
    private fun prefillFromEntity() = safeScope.launch {
        val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
        val lastEntry = marketRepo.getLastEntryForUser(user.id ?: 0, marketType)

        currencyCode = user.enumCountry.currencyCode
        views.priceInputLayout.prefixText = "$currencyCode "

        lastEntry?.let { entry ->
            updateUnits { unit ->
                if (entry.produceType == EnumProduceType.MAIZE_FRESH_COB)
                    unit == EnumUnitOfSale.FRESH_COB
                else
                    unit.isUniversal
            }

            views.apply {
                inputPrice.setText(entry.unitPrice.toString())

                unitSpinner.setText(
                    entry.unitOfSale.unitOfSale(this@AbstractProduceMarketActivity),
                    false
                )

                marketHintText.text = getString(
                    R.string.lbl_selling_hint,
                    currencyCode,
                    entry.unitPrice,
                    entry.unitOfSale.unitOfSale(this@AbstractProduceMarketActivity)
                )
                marketHintCard.isVisible = true
            }
        }
    }

    /** --- Units --- */
    protected fun updateUnits(filter: (EnumUnitOfSale) -> Boolean) {
        val filtered = EnumUnitOfSale.entries
            .filter(filter)
            .map { UnitOfSaleOption(valueOption = it) }
        val unitAdapter = BaseValueOptionAdapter(this, filtered) { it.label(this) }
        views.apply {
            unitSpinner.setAdapter(unitAdapter)

            inputPrice.addTextChangedListener { validateForm() }

            unitSpinner.setOnItemClickListener { _, _, position, _ ->
                val selected = unitAdapter.getItem(position) ?: return@setOnItemClickListener
                unitSpinner.setText(
                    selected.valueOption.unitOfSale(this@AbstractProduceMarketActivity),
                    false
                )
                validateForm()
            }

            fabSave.setOnClickListener { saveMarketEntry() }
        }
    }

    /** --- Validation --- */
    private val enteredPrice: Double?
        get() = views.inputPrice.text?.toString()?.toDoubleOrNull()

    protected fun validateForm() = with(views) {
        fabSave.isVisible =
            enteredPrice?.let { it > 0 } == true && !unitSpinner.text.isNullOrEmpty()
    }

    /** --- Save --- */
    private fun saveMarketEntry() {
        clearErrors()

        val price = enteredPrice ?: return showPriceError()
        val unit = selectedUnit() ?: return showUnitError()

        safeScope.launch {
            val userId = userRepo.getUser(sessionManager.akilimoUser)?.id ?: return@launch
            marketRepo.saveMarketEntry(
                ProduceMarket(
                    userId = userId,
                    unitPrice = price,
                    marketType = marketType,
                    produceType = resolveProduceType(),
                    unitOfSale = unit
                )
            )
            finish()
        }
    }

    private fun selectedUnit(): EnumUnitOfSale? =
        EnumUnitOfSale.entries.firstOrNull { it.unitOfSale(this) == views.unitSpinner.text.toString() }

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

    /** --- Subclass Responsibility --- */
    protected abstract fun resolveProduceType(): EnumProduceType
}
