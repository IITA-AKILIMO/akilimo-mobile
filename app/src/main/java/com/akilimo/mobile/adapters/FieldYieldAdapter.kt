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
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.TheItemAnimation.animate
import com.akilimo.mobile.utils.Tools.displayImageOriginal

class FieldYieldAdapter(
    private val animationType: Int = TheItemAnimation.SCALE,
    private val showImage: Boolean = true,
    private val isItemSelected: (FieldYield) -> Boolean,
    private val onItemClick: (View, FieldYield, Int) -> Unit
) : ListAdapter<FieldYield, FieldYieldAdapter.ViewHolder>(FieldYieldDiffCallback()) {


    class ViewHolder(val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardRecommendationImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val fieldYield = getItem(position)

        with(holder.binding) {
            recImgTitle.text = fieldYield.fieldYieldLabel

            if (showImage) {
                displayImageOriginal(context, recImgImage, fieldYield.imageId)
                recImgImageContainer.visibility = View.VISIBLE
            } else {
                recImgImageContainer.visibility = View.GONE
            }

            val isSelected = isItemSelected(fieldYield)
            val colorRes = if (isSelected) R.color.green_100 else R.color.grey_3
            recImgCard.setCardBackgroundColor(ContextCompat.getColor(context, colorRes))

            recImgCard.setOnClickListener { view ->
                onItemClick(view, fieldYield, position)
            }

            animate(holder.itemView, position, animationType)
        }
    }

    private class FieldYieldDiffCallback : DiffUtil.ItemCallback<FieldYield>() {
        override fun areItemsTheSame(oldItem: FieldYield, newItem: FieldYield): Boolean {
            return oldItem.yieldAmount == newItem.yieldAmount
        }

        override fun areContentsTheSame(oldItem: FieldYield, newItem: FieldYield): Boolean {
            return oldItem == newItem
        }
    }
}
