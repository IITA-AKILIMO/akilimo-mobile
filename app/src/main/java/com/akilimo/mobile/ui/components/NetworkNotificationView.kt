package com.akilimo.mobile.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.akilimo.mobile.R

class NetworkNotificationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val messageText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_network_notification, this, true)
        messageText = findViewById(R.id.tvNetworkMessage)
        elevation = resources.getDimension(R.dimen.dimen_4)
        visibility = View.GONE
    }

    fun showNoConnection() {
        messageText.text = context.getString(R.string.lbl_no_internet)
        messageText.setTextColor(ContextCompat.getColor(context, R.color.color_on_error))
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_error))
        visibility = View.VISIBLE
        removeCallbacks(hideRunnable)
    }

    fun showConnected() {
        messageText.text = context.getString(R.string.lbl_network_connected)
        messageText.setTextColor(ContextCompat.getColor(context, R.color.color_on_primary))
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary))
        visibility = View.VISIBLE
        postDelayed(hideRunnable, 2000)
    }

    fun hide() {
        removeCallbacks(hideRunnable)
        visibility = View.GONE
    }

    private val hideRunnable = Runnable { hide() }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(hideRunnable)
    }
}
