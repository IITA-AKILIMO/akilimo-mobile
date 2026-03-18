package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationImageBinding
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.utils.animateCardBackground
import kotlin.math.roundToInt

class CassavaYieldAdapter() :
    ListAdapter<CassavaYield, CassavaYieldAdapter.OriginalViewHolder>(CassavaYieldDiffCallback()) {

    var onItemClick: ((CassavaYield) -> Unit)? = null

    inner class OriginalViewHolder(private val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CassavaYield) = with(binding) {
            val ctx = root.context

            recImgTitle.text = item.yieldLabel
            recImgImage.setImageDrawable(
                try {
                    ContextCompat.getDrawable(ctx, item.imageRes)
                } catch (_: Exception) {
                    ContextCompat.getDrawable(ctx, R.drawable.ic_akilimo_logo)
                }
            )

            applySelectionState(item.isSelected)
            recImgCard.setOnClickListener { onItemClick?.invoke(item) }
        }

        private fun applySelectionState(selected: Boolean) = with(binding) {
            val ctx = root.context
            val strokePx = (2 * ctx.resources.displayMetrics.density).roundToInt()

            val targetBg = if (selected)
                ContextCompat.getColor(ctx, R.color.color_focus)
            else
                ContextCompat.getColor(ctx, R.color.transparent)

            recImgCard.animateCardBackground(targetBg)
            recImgCard.strokeWidth = if (selected) strokePx else 0

            selectionOverlay.visibility = if (selected) View.VISIBLE else View.GONE
            selectionIndicator.visibility = if (selected) View.VISIBLE else View.GONE
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
        holder.bind(getItem(position))
    }

    private class CassavaYieldDiffCallback : DiffUtil.ItemCallback<CassavaYield>() {
        override fun areItemsTheSame(oldItem: CassavaYield, newItem: CassavaYield): Boolean {
            return oldItem.id == newItem.id
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
