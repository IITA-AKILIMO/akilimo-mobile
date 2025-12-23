package com.akilimo.mobile.ui.components

import android.content.Context
import android.util.AttributeSet
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
        visibility = View.GONE
    }

    fun showNoConnection() {
        messageText.text = "No internet connection"
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
        visibility = View.VISIBLE
    }

    fun showConnected() {
        messageText.text = "Connected"
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
        visibility = View.VISIBLE

        // Auto-hide after 2 seconds
        postDelayed({ hide() }, 2000)
    }

    fun hide() {
        visibility = View.GONE
    }
}