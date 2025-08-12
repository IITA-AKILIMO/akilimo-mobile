package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivitySweetPotatoMarketBinding
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.utils.ui.SnackBarMessage
import com.akilimo.mobile.viewmodels.SweetPotatoMarketViewModel
import com.akilimo.mobile.viewmodels.factory.SweetPotatoMarketViewModelFactory
import com.akilimo.mobile.views.fragments.dialog.SweetPotatoPriceDialogFragment
import com.google.android.material.snackbar.Snackbar

class SweetPotatoMarketActivity : BindBaseActivity<ActivitySweetPotatoMarketBinding>() {

    private val viewModel: SweetPotatoMarketViewModel by viewModels {
        SweetPotatoMarketViewModelFactory(application = this.application, mathHelper = mathHelper)
    }

    override fun inflateBinding() = ActivitySweetPotatoMarketBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }


        setupToolbar(binding.toolbar, R.string.lbl_sweet_potato_prices) {
            viewModel.validateSelection(
                binding.potatoMarket.rdgPotatoProduceType.checkedRadioButtonId,
                viewModel.unitOfSale.value?.name,
                viewModel.unitPrice.value,
                true
            )
        }

        binding.potatoMarket.rdgUnitOfSalePotato.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rd_per_kg -> viewModel.setUnitOfSale(EnumUnitOfSale.ONE_KG)
                R.id.rd_50_kg_bag -> viewModel.setUnitOfSale(EnumUnitOfSale.FIFTY_KG)
                R.id.rd_100_kg_bag -> viewModel.setUnitOfSale(EnumUnitOfSale.HUNDRED_KG)
                R.id.rd_1000_kg_bag -> viewModel.setUnitOfSale(EnumUnitOfSale.TONNE)
            }
        }
        binding.potatoMarket.unitOfSalePotatoCard.setOnClickListener {
            openPriceDialog()
        }

        binding.potatoMarket.twoButtons.apply {
            btnFinish.setOnClickListener { view: View? -> validate(false) }
            btnCancel.setOnClickListener { view: View? -> closeActivity(false) }
        }

        showCustomNotificationDialog()
        setupObservers()
    }

    override fun setupObservers() {
        viewModel.showSnackBarEvent.observe(this) { message ->
            message?.let {
                val message = when (it) {
                    is SnackBarMessage.Text -> it.message
                    is SnackBarMessage.Resource -> getString(it.resId)
                }
                Snackbar.make(
                    binding.potatoMarket.produceTypeCard,
                    message,
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.clearSnackBarEvent()
            }
        }
        viewModel.closeEvent.observe(this) { shouldClose ->
            if (shouldClose) closeActivity(false)
        }
        viewModel.unitPrice.observe(this) { price ->
//            binding.potatoMarket.unitOfSalePotatoTitle.text = price
        }
    }

    fun onPotatoUnitRadioButtonClicked(radioButton: View?) {
        if (radioButton != null && radioButton.isPressed) {
//            openPriceDialog()
        }
    }


    private fun openPriceDialog() {
        val args = Bundle().apply {
            putString(SweetPotatoPriceDialogFragment.CURRENCY_CODE, currencyCode)
            putString(SweetPotatoPriceDialogFragment.COUNTRY_CODE, countryCode)
            putDouble(
                SweetPotatoPriceDialogFragment.SELECTED_PRICE,
                viewModel.unitPrice.value ?: 0.0
            )
            putString(SweetPotatoPriceDialogFragment.UNIT_OF_SALE, viewModel.unitOfSale.value?.name)
            putParcelable(
                SweetPotatoPriceDialogFragment.ENUM_UNIT_OF_SALE,
                EnumUnitOfSale.FIFTY_KG
            )
        }

        val dialog = SweetPotatoPriceDialogFragment()
        dialog.arguments = args
        dialog.setOnDismissListener { selectedPrice: Double, isExactPrice: Boolean ->
            viewModel.setUnitPriceFromDialog(selectedPrice, isExactPrice)
        }
        showDialogFragmentSafely(
            supportFragmentManager,
            dialog,
            SweetPotatoPriceDialogFragment.ARG_ITEM_ID
        )
    }
}
