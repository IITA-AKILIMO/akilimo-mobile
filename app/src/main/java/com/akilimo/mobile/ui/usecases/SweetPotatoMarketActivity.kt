package com.akilimo.mobile.ui.usecases

import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractProduceMarketActivity
import com.akilimo.mobile.databinding.ActivitySweetPotatoMarketBinding
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.ui.components.ProduceMarketViews

class SweetPotatoMarketActivity : AbstractProduceMarketActivity<ActivitySweetPotatoMarketBinding>(
    marketType = EnumMarketType.SWEET_POTATO_MARKET,
    titleRes = R.string.lbl_sweet_potato_market
) {

    override val views: ProduceMarketViews
        get() = ProduceMarketViews.Binding(
            unitLabel = binding.unitLabel,
            unitInputLayout = binding.unitInputLayout,
            unitSpinner = binding.unitSpinner,
            inputPrice = binding.inputPrice,
            priceInputLayout = binding.priceInputLayout,
            fabSave = binding.lytFab.fabSave,
            marketHintCard = binding.marketHintCard,
            marketHintText = binding.marketHintText,
            toolbar = binding.lytToolbar.toolbar
        )

    override fun inflateBinding() = ActivitySweetPotatoMarketBinding.inflate(layoutInflater)
    override fun resolveProduceType(): EnumProduceType {
        return EnumProduceType.SWEET_POTATO_TUBERS
    }

    override fun setupProduceTypeSelection() {
        updateUnits { it.isUniversal }
    }

}