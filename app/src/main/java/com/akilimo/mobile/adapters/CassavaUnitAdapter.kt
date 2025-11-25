package com.akilimo.mobile.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCassavaUnitBinding
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.enums.EnumUnitOfSale

class CassavaUnitAdapter : ListAdapter<CassavaUnit, CassavaUnitAdapter.UnitViewHolder>(DIFF) {

    var onItemClick: ((CassavaUnit) -> Unit)? = null

    inner class UnitViewHolder(private val binding: ItemCassavaUnitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CassavaUnit) {
            val ctx = binding.root.context
            val uos = EnumUnitOfSale.entries.find { it.name == item.label }
            uos.let { binding.tvUnitLabel.text = it?.label(ctx) }


            val bgColor = if (item.isSelected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                Color.TRANSPARENT

            binding.root.setBackgroundColor(bgColor)

            binding.root.setOnClickListener { onItemClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UnitViewHolder(
        ItemCassavaUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CassavaUnit>() {
            override fun areItemsTheSame(oldItem: CassavaUnit, newItem: CassavaUnit): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CassavaUnit, newItem: CassavaUnit): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.label == newItem.label &&
                        oldItem.description == newItem.description &&
                        oldItem.isSelected == newItem.isSelected
            }
        }
    }
}
