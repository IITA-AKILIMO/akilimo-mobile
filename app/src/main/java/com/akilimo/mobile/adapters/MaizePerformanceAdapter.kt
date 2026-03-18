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
import com.akilimo.mobile.dto.MaizePerfOption
import com.akilimo.mobile.enums.EnumMaizePerformance
import com.akilimo.mobile.utils.animateCardBackground
import kotlin.math.roundToInt

class MaizePerformanceAdapter() :
    ListAdapter<MaizePerfOption, MaizePerformanceAdapter.OriginalViewHolder>(MaizePerfDiffCallback()) {

    var onItemClick: ((EnumMaizePerformance) -> Unit)? = null

    inner class OriginalViewHolder(private val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaizePerfOption) = with(binding) {
            val ctx = root.context
            recImgImage.setImageDrawable(
                try {
                    ContextCompat.getDrawable(ctx, item.valueOption.imageRes)
                } catch (_: Exception) {
                    ContextCompat.getDrawable(ctx, R.drawable.ic_akilimo_logo)
                }
            )
            recImgTitle.text = ctx.getString(item.valueOption.label)

            applySelectionState(item.isSelected)
            recImgCard.setOnClickListener { onItemClick?.invoke(item.valueOption) }
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = OriginalViewHolder(
        ItemCardRecommendationImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(
        holder: OriginalViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    private class MaizePerfDiffCallback : DiffUtil.ItemCallback<MaizePerfOption>() {
        override fun areItemsTheSame(oldItem: MaizePerfOption, newItem: MaizePerfOption): Boolean {
            return oldItem.valueOption == newItem.valueOption
        }

        override fun areContentsTheSame(
            oldItem: MaizePerfOption,
            newItem: MaizePerfOption
        ): Boolean {
            return oldItem.valueOption.performanceValue == newItem.valueOption.performanceValue &&
                    oldItem.isSelected == newItem.isSelected
        }
    }
}