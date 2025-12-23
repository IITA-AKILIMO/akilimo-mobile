package com.akilimo.mobile.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemStarchFactoryGridBinding
import com.akilimo.mobile.databinding.ItemStarchFactoryLinearBinding
import com.akilimo.mobile.entities.StarchFactory

class StarchFactoryAdapter :
    ListAdapter<StarchFactory, RecyclerView.ViewHolder>(StarchFactoryDiffCallback()) {

    @Suppress("PrivatePropertyName")
    private val PAYLOAD_LAYOUT_MODE = "PAYLOAD_LAYOUT_MODE"
    
    private var isGridLayout: Boolean = false
    var onItemClick: ((StarchFactory) -> Unit)? = null

    fun setLayoutMode(isGrid: Boolean) {
        if (isGridLayout != isGrid) {
            isGridLayout = isGrid
            notifyItemRangeChanged(0, itemCount, PAYLOAD_LAYOUT_MODE)
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (isGridLayout) VIEW_TYPE_GRID else VIEW_TYPE_LINEAR

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_GRID) {
            val binding = ItemStarchFactoryGridBinding.inflate(inflater, parent, false)
            GridViewHolder(binding)
        } else {
            val binding = ItemStarchFactoryLinearBinding.inflate(inflater, parent, false)
            LinearViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val factory = getItem(position)
        holder.itemView.setOnClickListener { onItemClick?.invoke(factory) }
        when (holder) {
            is GridViewHolder -> holder.bind(factory)
            is LinearViewHolder -> holder.bind(factory)
        }
    }

    class GridViewHolder(private val binding: ItemStarchFactoryGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(factory: StarchFactory) = with(binding.starchFactoryContent) {
            val ctx = root.context
            tvFactoryName.text = factory.name

            val bgColor = if (factory.isSelected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                Color.TRANSPARENT
            root.setBackgroundColor(bgColor)
        }
    }

    class LinearViewHolder(private val binding: ItemStarchFactoryLinearBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(factory: StarchFactory) = with(binding.starchFactoryContent) {
            val ctx = root.context
            tvFactoryName.text = factory.label
            val bgColor = if (factory.isSelected)
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

private class StarchFactoryDiffCallback : DiffUtil.ItemCallback<StarchFactory>() {
    override fun areItemsTheSame(oldItem: StarchFactory, newItem: StarchFactory): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: StarchFactory, newItem: StarchFactory): Boolean {
        return oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.label == newItem.label &&
                oldItem.isSelected == newItem.isSelected
    }
}
