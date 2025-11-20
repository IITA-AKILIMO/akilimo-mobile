// CassavaMarketPriceAdapter.kt
package com.akilimo.mobile.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCassavaPriceBinding
import com.akilimo.mobile.entities.CassavaMarketPrice

/**
 * Linear list adapter for cassava market prices.
 * Highlights selected price and exposes onItemClick callback.
 */
class CassavaMarketPriceAdapter :
    ListAdapter<CassavaMarketPrice, CassavaMarketPriceAdapter.PriceViewHolder>(PriceDiff()) {

    var selectedPriceId: Int = 0
    var onItemClick: ((CassavaMarketPrice) -> Unit)? = null

    fun setSelectedPrice(id: Int? = null) {
        if (id == null) return
        selectedPriceId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceViewHolder {
        val binding =
            ItemCassavaPriceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PriceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PriceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
    }

    inner class PriceViewHolder(private val binding: ItemCassavaPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(price: CassavaMarketPrice) = with(binding) {
            val ctx = root.context
            tvPriceLabel.text = price.averagePrice.toString()
            tvPriceValue.text = price.averagePrice.toString()
            val isSelected = price.id == selectedPriceId
            val bg = if (isSelected) ContextCompat.getColor(
                ctx,
                R.color.color_focus
            ) else Color.TRANSPARENT
            root.setBackgroundColor(bg)
        }
    }

    private class PriceDiff : DiffUtil.ItemCallback<CassavaMarketPrice>() {
        override fun areItemsTheSame(
            oldItem: CassavaMarketPrice,
            newItem: CassavaMarketPrice
        ): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CassavaMarketPrice,
            newItem: CassavaMarketPrice
        ): Boolean =
            oldItem == newItem
    }
}
