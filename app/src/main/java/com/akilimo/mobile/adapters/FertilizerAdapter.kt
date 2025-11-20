package com.akilimo.mobile.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemFertilizerGridBinding
import com.akilimo.mobile.databinding.ItemFertilizerLinearBinding
import com.akilimo.mobile.entities.Fertilizer

class FertilizerAdapter :
    ListAdapter<Fertilizer, RecyclerView.ViewHolder>(FertilizerDiffCallback()) {

    @Suppress("PrivatePropertyName")
    private val PAYLOAD_LAYOUT_MODE = "PAYLOAD_LAYOUT_MODE"
    private var isGridLayout: Boolean = false
    var onItemClick: ((Fertilizer) -> Unit)? = null

    fun setLayoutMode(isGrid: Boolean) {
        if (isGridLayout != isGrid) {
            isGridLayout = isGrid
            notifyItemRangeChanged(0, itemCount, PAYLOAD_LAYOUT_MODE)
        }
    }

    fun updateSelection(selectedIds: Set<Int>) {
        currentList.forEach {
            val selected = selectedIds.contains(it.id)
            it.isSelected = selected
            it.displayPrice = it.displayPrice.orEmpty()
            it.selectedPrice = if (selected) it.selectedPrice else 0.0
        }
        notifyItemRangeChanged(0, itemCount, PAYLOAD_LAYOUT_MODE)
    }


    override fun getItemViewType(position: Int): Int =
        if (isGridLayout) VIEW_TYPE_GRID else VIEW_TYPE_LINEAR

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_GRID) {
            val binding = ItemFertilizerGridBinding.inflate(inflater, parent, false)
            GridViewHolder(binding)
        } else {
            val binding = ItemFertilizerLinearBinding.inflate(inflater, parent, false)
            LinearViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val fertilizer = getItem(position)
        holder.itemView.setOnClickListener { onItemClick?.invoke(fertilizer) }
        when (holder) {
            is GridViewHolder -> holder.bind(fertilizer)
            is LinearViewHolder -> holder.bind(fertilizer)
        }
    }

    class GridViewHolder(private val binding: ItemFertilizerGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fertilizer: Fertilizer) = with(binding.fertilizerContent) {
            val ctx = root.context
            var priceText = fertilizer.displayPrice
            if (fertilizer.isSelected) {
                if (fertilizer.selectedPrice == 0.0) {
                    priceText = ctx.getString(R.string.lbl_do_not_know)
                }
            } else {
                priceText = ctx.getString(R.string.under_score)
            }
            fertilizerName.text = fertilizer.name
            fertilizerPrice.text = priceText

            val bgColor = if (fertilizer.isSelected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                Color.TRANSPARENT
            root.setBackgroundColor(bgColor)
        }
    }

    class LinearViewHolder(private val binding: ItemFertilizerLinearBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fertilizer: Fertilizer) = with(binding.fertilizerContent) {
            val ctx = root.context
            var priceText = fertilizer.displayPrice
            if (fertilizer.isSelected) {
                if (fertilizer.selectedPrice == 0.0) {
                    priceText = ctx.getString(R.string.lbl_do_not_know)
                }
            } else {
                priceText = ctx.getString(R.string.under_score)
            }
            fertilizerName.text = fertilizer.name
            fertilizerPrice.text = priceText

            val bgColor = if (fertilizer.isSelected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                ContextCompat.getColor(ctx, android.R.color.transparent)
            root.setBackgroundColor(bgColor)
        }
    }

    companion object {
        private const val VIEW_TYPE_LINEAR = 0
        private const val VIEW_TYPE_GRID = 1
    }
}

private class FertilizerDiffCallback : DiffUtil.ItemCallback<Fertilizer>() {
    override fun areItemsTheSame(oldItem: Fertilizer, newItem: Fertilizer): Boolean =
        oldItem.key == newItem.key

    override fun areContentsTheSame(oldItem: Fertilizer, newItem: Fertilizer): Boolean =
        oldItem.key == newItem.key
}
