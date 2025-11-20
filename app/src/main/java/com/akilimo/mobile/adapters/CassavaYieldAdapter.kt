package com.akilimo.mobile.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationImageBinding
import com.akilimo.mobile.entities.CassavaYield

class CassavaYieldAdapter() :
    ListAdapter<CassavaYield, CassavaYieldAdapter.OriginalViewHolder>(CassavaYieldDiffCallback()) {

    var onItemClick: ((CassavaYield) -> Unit)? = null

    inner class OriginalViewHolder(private val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CassavaYield, position: Int) = with(binding) {
            val ctx = binding.root.context
            recImgTitle.text = item.yieldLabel

            val drawableRes = try {
                ContextCompat.getDrawable(ctx, item.imageRes)
            } catch (e: Exception) {
                ContextCompat.getDrawable(ctx, R.drawable.ic_akilimo_logo)
            }

            recImgImage.setImageDrawable(drawableRes)

            val bgColor = if (item.isSelected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                Color.TRANSPARENT

            recImgContent.setBackgroundColor(bgColor)
            recImgCard.setOnClickListener { onItemClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OriginalViewHolder(
        ItemCardRecommendationImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    private class CassavaYieldDiffCallback : DiffUtil.ItemCallback<CassavaYield>() {
        override fun areItemsTheSame(oldItem: CassavaYield, newItem: CassavaYield): Boolean {
            return oldItem.imageRes == newItem.imageRes && oldItem.yieldLabel == newItem.yieldLabel
        }

        override fun areContentsTheSame(oldItem: CassavaYield, newItem: CassavaYield): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.imageRes == newItem.imageRes &&
                    oldItem.yieldLabel == newItem.yieldLabel &&
                    oldItem.amountLabel == newItem.amountLabel &&
                    oldItem.isSelected == newItem.isSelected
        }
    }
}
