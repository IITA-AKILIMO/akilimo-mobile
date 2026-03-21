package com.akilimo.mobile.ui.fragments.usecases

import android.view.LayoutInflater
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.base.AbstractProduceMarketFragment
import com.akilimo.mobile.databinding.ActivitySweetPotatoMarketBinding
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUnitOfSale
import com.akilimo.mobile.ui.components.ProduceMarketViews
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SweetPotatoMarketFragment : AbstractProduceMarketFragment<ActivitySweetPotatoMarketBinding>(
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

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivitySweetPotatoMarketBinding.inflate(inflater, container, false)

    override fun resolveProduceType() = EnumProduceType.SWEET_POTATO_TUBERS

    override fun setupProduceTypeSelection() {
        updateUnits { it.isUniversal }
    }
}
