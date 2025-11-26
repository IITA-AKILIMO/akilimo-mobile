package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationImageBinding
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.utils.animateCardBackground
import com.akilimo.mobile.utils.animateTextColor

class CassavaYieldAdapter() :
    ListAdapter<CassavaYield, CassavaYieldAdapter.OriginalViewHolder>(CassavaYieldDiffCallback()) {

    var onItemClick: ((CassavaYield) -> Unit)? = null

    inner class OriginalViewHolder(private val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CassavaYield, position: Int) = with(binding) {
            val ctx = binding.root.context
            val drawableRes = try {
                ContextCompat.getDrawable(ctx, item.imageRes)
            } catch (e: Exception) {
                ContextCompat.getDrawable(ctx, R.drawable.ic_akilimo_logo)
            }


            recImgTitle.text = item.yieldLabel
            recImgImage.setImageDrawable(drawableRes)

            val targetBg = if (item.isSelected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                ContextCompat.getColor(ctx, R.color.transparent)

            val targetText = if (item.isSelected)
                ContextCompat.getColor(ctx, R.color.color_on_primary)
            else
                ContextCompat.getColor(ctx, R.color.black)

            recImgCard.animateCardBackground(targetBg)
            recImgTitle.animateTextColor(targetText)

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
