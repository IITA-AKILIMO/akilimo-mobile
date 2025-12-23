package com.akilimo.mobile.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.CustomSpinnerBinding
import com.akilimo.mobile.dto.BaseValueOption

abstract class BaseSpinnerAdapter<T, O : BaseValueOption<T>>(
    context: Context,
    private val options: List<O>,
    private val getDisplayText: (O) -> String
) : ArrayAdapter<O>(context, R.layout.custom_spinner, options) {

    private val inflater = LayoutInflater.from(context)
    private var selectedValue: T? = null

    fun setSelectedValue(value: T?) {
        selectedValue = value
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): O? = options.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            val binding = CustomSpinnerBinding.inflate(inflater, parent, false)
            view = binding.root
            holder = ViewHolder(binding)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = options[position]
        holder.binding.spinnerText.text = getDisplayText(item)
        holder.binding.spinnerText.setTypeface(null, Typeface.NORMAL)
        holder.binding.spinnerText.setBackgroundColor(
            ContextCompat.getColor(context, android.R.color.transparent)
        )

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            val binding = CustomSpinnerBinding.inflate(inflater, parent, false)
            view = binding.root
            holder = ViewHolder(binding)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = options[position]
        holder.binding.spinnerText.text = getDisplayText(item)

        if (item.valueOption == selectedValue) {
            holder.binding.spinnerText.setTypeface(null, Typeface.BOLD)
            holder.binding.spinnerText.setBackgroundColor(
                ContextCompat.getColor(context, R.color.color_text_primary)
            )
        } else {
            holder.binding.spinnerText.setTypeface(null, Typeface.NORMAL)
            holder.binding.spinnerText.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.transparent)
            )
        }

        return view
    }

    private class ViewHolder(val binding: CustomSpinnerBinding)
}
