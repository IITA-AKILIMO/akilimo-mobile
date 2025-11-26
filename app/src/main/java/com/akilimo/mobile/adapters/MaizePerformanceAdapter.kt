package com.akilimo.mobile.adapters

import android.view.LayoutInflater
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
import com.akilimo.mobile.utils.animateTextColor

class MaizePerformanceAdapter() :
    ListAdapter<MaizePerfOption, MaizePerformanceAdapter.OriginalViewHolder>(MaizePerfDiffCallback()) {

    var onItemClick: ((EnumMaizePerformance) -> Unit)? = null

    inner class OriginalViewHolder(private val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MaizePerfOption, position: Int) = with(binding) {
            val ctx = binding.root.context
            val drawableRes = try {
                ContextCompat.getDrawable(ctx, item.valueOption.imageRes)
            } catch (_: Exception) {
                ContextCompat.getDrawable(ctx, R.drawable.ic_akilimo_logo)
            }
            recImgImage.setImageDrawable(drawableRes)
            recImgTitle.text = ctx.getString(item.valueOption.label)

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

            recImgCard.setOnClickListener { onItemClick?.invoke(item.valueOption) }
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
        val item = getItem(position)
        holder.bind(item, position)
    }

    private class MaizePerfDiffCallback : DiffUtil.ItemCallback<MaizePerfOption>() {
        override fun areItemsTheSame(oldItem: MaizePerfOption, newItem: MaizePerfOption): Boolean {
            return oldItem.valueOption.imageRes == newItem.valueOption.imageRes && oldItem.valueOption.label == newItem.valueOption.label
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