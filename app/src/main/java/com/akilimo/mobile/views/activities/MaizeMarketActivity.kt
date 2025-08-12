package com.akilimo.mobile.views.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityMaizeMarketBinding
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.enums.EnumMaizeProduceType
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.ui.SnackBarMessage
import com.akilimo.mobile.viewmodels.MaizeMarketViewModel
import com.akilimo.mobile.viewmodels.factory.MaizeMarketViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.MaizePriceDialogFragment
import com.google.android.material.snackbar.Snackbar

class MaizeMarketActivity : BindBaseActivity<ActivityMaizeMarketBinding>() {


    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null

    private val viewModel: MaizeMarketViewModel by viewModels {
        MaizeMarketViewModelFactory(application = this.application)
    }


    override fun inflateBinding(): ActivityMaizeMarketBinding =
        ActivityMaizeMarketBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupObservers()
        setupListeners()

        setupToolbar(binding.toolbar, R.string.title_activity_maize_market_outlet) {
            validate(false)
        }
    }

    override fun setupObservers() {
        viewModel.showSnackBarEvent.observe(this) { message ->
            message?.let {
                val message = when (it) {
                    is SnackBarMessage.Text -> it.message
                    is SnackBarMessage.Resource -> getString(it.resId)
                }
                Snackbar.make(
                    binding.marketContent.twoButtons.lytButton,
                    message,
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.clearSnackBarEvent()
            }
        }

        viewModel.closeScreen.observe(this, Observer { shouldClose ->
            if (shouldClose) finish()
        })

        viewModel.showPriceDialog.observe(this, Observer { args ->
            val dialog = MaizePriceDialogFragment().apply {
                arguments = args
                setOnDismissListener { price, isExact ->
                    viewModel.onDialogPriceSelected(price, isExact)
                }
            }
            dialog.show(supportFragmentManager, MaizePriceDialogFragment.ARG_ITEM_ID)
        })
    }

    private fun setupListeners() {
        binding.marketContent.rdgMaizeProduceType.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rdDryGrain -> viewModel.setProduceType(EnumMaizeProduceType.GRAIN)
                R.id.rdFreshCobs -> {
                    viewModel.setProduceType(EnumMaizeProduceType.FRESH_COB)
                    viewModel.setUnitOfSale(EnumUnitOfSale.NA.unitOfSale(this), EnumUnitOfSale.NA)
                }
            }
        }

        binding.marketContent.rdgUnitOfSaleGrain.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rd_per_kg -> viewModel.setUnitOfSale(
                    EnumUnitOfSale.ONE_KG.unitOfSale(this),
                    EnumUnitOfSale.ONE_KG
                )

                R.id.rd_50_kg_bag -> viewModel.setUnitOfSale(
                    EnumUnitOfSale.FIFTY_KG.unitOfSale(this),
                    EnumUnitOfSale.FIFTY_KG
                )

                R.id.rd_100_kg_bag -> viewModel.setUnitOfSale(
                    EnumUnitOfSale.HUNDRED_KG.unitOfSale(this),
                    EnumUnitOfSale.HUNDRED_KG
                )
            }
        }

        binding.marketContent.twoButtons.btnFinish.setOnClickListener {
            viewModel.validateAndSave(backPressed = false)
        }

        binding.marketContent.twoButtons.btnCancel.setOnClickListener {
            finish()
        }

        binding.marketContent.btnPickCobPrice.setOnClickListener {
            viewModel.requestPriceDialog(
                produceType = EnumMaizeProduceType.FRESH_COB.name.lowercase(),
                unitEnum = EnumUnitOfSale.FRESH_COB
            )
        }
    }

//    fun onGrainUnitRadioButtonClicked(radioButton: View?) {
//        if (radioButton != null && radioButton.isPressed) {
//            showUnitGrainPriceDialog("grain")
//        }
//    }
}
