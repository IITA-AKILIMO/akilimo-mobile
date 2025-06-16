package com.akilimo.mobile.views

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class ObservableWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : WebView(context, attrs) {

    fun isScrolledToBottom(threshold: Int = 10): Boolean {
        val offset = computeVerticalScrollOffset()
        val extent = computeVerticalScrollExtent()
        val range = computeVerticalScrollRange()
        return offset + extent >= range - threshold
    }
}
