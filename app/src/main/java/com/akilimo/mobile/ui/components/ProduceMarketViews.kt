package com.akilimo.mobile.ui.components

import android.view.View
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Represents all the UI components required by AbstractProduceMarketActivity
 */
sealed class ProduceMarketViews {

    abstract val unitLabel: View
    abstract val unitInputLayout: TextInputLayout
    abstract val unitSpinner: MaterialAutoCompleteTextView
    abstract val inputPrice: TextInputEditText
    abstract val priceInputLayout: TextInputLayout
    abstract val fabSave: ExtendedFloatingActionButton
    abstract val marketHintCard: MaterialCardView
    abstract val marketHintText: TextView
    abstract val toolbar: MaterialToolbar

    /**
     * Concrete implementation using ViewBinding
     */
    data class Binding(
        override val unitLabel: View,
        override val unitInputLayout: TextInputLayout,
        override val unitSpinner: MaterialAutoCompleteTextView,
        override val inputPrice: TextInputEditText,
        override val priceInputLayout: TextInputLayout,
        override val fabSave: ExtendedFloatingActionButton,
        override val marketHintCard: MaterialCardView,
        override val marketHintText: TextView,
        override val toolbar: MaterialToolbar
    ) : ProduceMarketViews()
}
