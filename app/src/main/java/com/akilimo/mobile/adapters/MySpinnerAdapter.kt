package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.akilimo.mobile.R
import io.sentry.Sentry

class MySpinnerAdapter(applicationContext: Context, localeStrings: List<String>) : BaseAdapter() {
    private val context: Context = applicationContext
    private val spinnerItems: List<String> = localeStrings
    private var spinnerImages: List<Int>? = null
    private val layoutInflater: LayoutInflater = (LayoutInflater.from(applicationContext))

    override fun getCount(): Int {
        return spinnerItems.size
    }

    override fun getItem(position: Int): String {
        return spinnerItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val spinnerView =
            convertView ?: layoutInflater.inflate(R.layout.custom_spinner, parent, false)
        val names = spinnerView.findViewById<TextView>(R.id.spinnerText)
        try {
            names.text = spinnerItems[position]
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
        return spinnerView
    }
}
