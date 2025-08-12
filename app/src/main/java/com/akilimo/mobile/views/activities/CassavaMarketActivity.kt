package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.enums.EnumCassavaProduceType
import com.akilimo.mobile.utils.enums.EnumContext
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.CassavaMarketViewModel
import com.akilimo.mobile.viewmodels.factory.CassavaMarketViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.CassavaPriceDialogFragment

class CassavaMarketActivity : BindBaseActivity<ActivityCassavaMarketBinding>() {

    private val viewModel: CassavaMarketViewModel by viewModels {
        CassavaMarketViewModelFactory(this.application)
    }

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun inflateBinding() = ActivityCassavaMarketBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupObservers()

        val useCase = intent.getStringExtra("useCase") ?: "NA"
        val enumUseCase = EnumUseCase.valueOf(useCase)
        viewModel.loadInitialData(countryCode)
        viewModel.fetchStarchFactories(countryCode)
        viewModel.fetchCassavaPrices(countryCode)

        setupToolbar(binding.toolbar, R.string.title_activity_cassava_market_outlet) {
            validate(false)
        }

        onBackPressedDispatcher.addCallback(this) {
            validate(true)
        }

        setupUIControls(enumUseCase)
    }

    override fun setupObservers() {
        viewModel.errorMessage.observe(this) {
            it?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.cassavaMarket.observe(this) { market ->
            market?.let {
                viewModel.selectedFactory.value = it.starchFactory
                viewModel.produceType.value = it.produceType
                viewModel.unitOfSale.value = it.unitOfSale ?: "NA"
                viewModel.unitPrice.value = it.unitPrice
                viewModel.unitPriceP1.value = it.unitPriceP1
                viewModel.unitPriceP2.value = it.unitPriceP2
                viewModel.unitPriceM1.value = it.unitPriceM1
                viewModel.unitPriceM2.value = it.unitPriceM2
            }
        }

        viewModel.starchFactories.observe(this) { factories ->
            addFactoriesRadioButtons(factories)
        }
    }

    private fun setUnitOfSale(unit: EnumUnitOfSale) {
        viewModel.unitOfSale.value = unit.name
        viewModel.unitWeight.value = unit.unitWeight()
    }

    private fun setupUIControls(enumUseCase: EnumUseCase) = with(binding.contentCassavaMarket) {
        btnUpP1.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_P1) }
        btnUpP2.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_P2) }
        btnUpM1.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_M1) }
        btnUpM2.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_M2) }

        rdgStarchFactories.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            if (checkedRadioButtonId == -1) return@setOnCheckedChangeListener

            val radioButton = findViewById<RadioButton>(checkedRadioButtonId)
                ?: return@setOnCheckedChangeListener

            val factoryNameCountry = radioButton.tag as? String ?: return@setOnCheckedChangeListener
            viewModel.saveStarchFactory(factoryNameCountry)
        }

        rdgUnitOfSale.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rd_per_kg -> setUnitOfSale(EnumUnitOfSale.ONE_KG)
                R.id.rd_50_kg_bag -> setUnitOfSale(EnumUnitOfSale.FIFTY_KG)
                R.id.rd_100_kg_bag -> setUnitOfSale(EnumUnitOfSale.HUNDRED_KG)
                R.id.rd_per_tonne -> setUnitOfSale(EnumUnitOfSale.TONNE)
            }
        }

        twoButtons.btnFinish.setOnClickListener { validate(false) }
        twoButtons.btnCancel.setOnClickListener { finish() }

        if (enumUseCase == EnumUseCase.CIM) {
            marketOutLetTitle.visibility = View.GONE
            marketOutletCard.visibility = View.GONE
            unitOfSaleCard.visibility = View.VISIBLE
            viewModel.produceType.value = EnumCassavaProduceType.ROOTS.name
        }
    }

    fun onRadioButtonClicked(radioButton: View?) {
        if (radioButton != null && radioButton.isPressed) {
            showUnitPriceDialog(EnumContext.UNIT_PRICE)
        }
    }

    override fun validate(backPressed: Boolean) {
        // Validation can be added based on LiveData state
        viewModel.saveCassavaMarket(
            factoryRequired = true,
            produce = EnumCassavaProduceType.ROOTS.name
        )
        if (backPressed) finish()
    }

    private fun addFactoriesRadioButtons(starchFactoryList: List<StarchFactory>) {
        binding.contentCassavaMarket.rdgStarchFactories.removeAllViews()

        val params = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val dimension = resources.getDimension(R.dimen.spacing_medium).toInt()

        for (factory in starchFactoryList) {
            if (factory.factoryLabel.equals("NA", ignoreCase = true)) continue

            val radioButton = RadioButton(this)
            radioButton.id = View.generateViewId()
            radioButton.tag = factory.factoryNameCountry
            radioButton.text = factory.factoryLabel

            params.setMargins(0, 0, 0, dimension)
            radioButton.layoutParams = params

            if (factory.factoryName == viewModel.selectedFactory.value) {
                radioButton.isChecked = true
            }

            radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    viewModel.selectedFactory.value = factory.factoryName
                }
            }

            binding.contentCassavaMarket.rdgStarchFactories.addView(radioButton)
        }
    }

    private fun showUnitPriceDialog(userContext: EnumContext) {
        val myArgs = Bundle().apply {
            putString(CassavaPriceDialogFragment.CURRENCY_CODE, currencyCode)
            putString(CassavaPriceDialogFragment.COUNTRY_CODE, countryCode)
            putDouble(CassavaPriceDialogFragment.SELECTED_PRICE, viewModel.unitPrice.value ?: 0.0)
            putDouble(CassavaPriceDialogFragment.AVERAGE_PRICE, viewModel.unitPrice.value ?: 0.0)
            putString(CassavaPriceDialogFragment.UNIT_OF_SALE, viewModel.unitOfSale.value ?: "NA")
        }

        val priceDialogFragment = CassavaPriceDialogFragment().apply {
            setOnDismissListener { selectedPrice: Double, isExactPrice: Boolean ->
                val setPrice =
                    if (isExactPrice) selectedPrice else mathHelper.convertToUnitWeightPrice(
                        selectedPrice, viewModel.unitWeight.value ?: 1.0
                    )
                when (userContext) {
                    EnumContext.UNIT_PRICE -> viewModel.unitPrice.value = setPrice
                    EnumContext.UNIT_PRICE_P1 -> viewModel.unitPriceP1.value = setPrice
                    EnumContext.UNIT_PRICE_P2 -> viewModel.unitPriceP2.value = setPrice
                    EnumContext.UNIT_PRICE_M1 -> viewModel.unitPriceM1.value = setPrice
                    EnumContext.UNIT_PRICE_M2 -> viewModel.unitPriceM2.value = setPrice
                }
            }
            arguments = myArgs
        }

        showDialogFragmentSafely(
            supportFragmentManager,
            priceDialogFragment,
            CassavaPriceDialogFragment.ARG_ITEM_ID
        )
    }

}
