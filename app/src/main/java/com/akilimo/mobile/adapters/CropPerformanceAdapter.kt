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
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.utils.TheItemAnimation.animate
import com.akilimo.mobile.utils.Tools.displayImageOriginal

class CropPerformanceAdapter(
    private val animationType: Int,
    private val onItemClick: (View, CropPerformance, Int) -> Unit
) : ListAdapter<CropPerformance, CropPerformanceAdapter.OriginalViewHolder>(DiffCallback()) {

    private var lastPosition = -1
    private var selectedIndex = -1
    private var selectedScore = 0

    fun updateItems(newScore: Int, newList: List<CropPerformance>, notifyPosition: Int = 0) {
        selectedScore = newScore
        submitList(newList)
        notifyItemChanged(notifyPosition)
    }

    fun setActiveIndex(index: Int) {
        selectedIndex = index
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val binding = ItemCardRecommendationImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position)
    }

    inner class OriginalViewHolder(
        private val binding: ItemCardRecommendationImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CropPerformance, position: Int) = with(binding) {
            recImgTitle.text = item.maizePerformanceLabel
            displayImageOriginal(root.context, recImgImage, item.imageId)

            val isSelected = position == selectedIndex || item.performanceScore == selectedScore
            val bgColor = if (isSelected) R.color.green_100 else R.color.grey_3
            recImgCard.setCardBackgroundColor(ContextCompat.getColor(root.context, bgColor))

            recImgCard.setOnClickListener {
                onItemClick.invoke(it, item, position)
            }

            setAnimation(itemView, position)
        }
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            animate(view, position, animationType)
            lastPosition = position
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CropPerformance>() {
        override fun areItemsTheSame(oldItem: CropPerformance, newItem: CropPerformance): Boolean {
            return oldItem.performanceScore == newItem.performanceScore // Replace with proper unique identifier
        }

        override fun areContentsTheSame(
            oldItem: CropPerformance,
            newItem: CropPerformance
        ): Boolean {
            return oldItem == newItem
        }
    }
}