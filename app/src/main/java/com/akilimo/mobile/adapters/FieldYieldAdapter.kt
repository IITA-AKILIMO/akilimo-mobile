package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationImageBinding
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.utils.TheItemAnimation.animate
import com.akilimo.mobile.utils.Tools.displayImageOriginal

class FieldYieldAdapter(
    private val ctx: Context,
    private var items: List<FieldYield>,
    private val animationType: Int,
    private val showImage: Boolean = true
) : RecyclerView.Adapter<FieldYieldAdapter.OriginalViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var rowIndex = -1
    private val onAttach = true
    private var selectedYieldAmount = 0.0

    fun interface OnItemClickListener {
        fun onItemClick(view: View?, fieldYield: FieldYield?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mItemClickListener
    }

    fun setItems(selectedYieldAmount: Double, items: List<FieldYield>) {
        this.items = items
        this.selectedYieldAmount = selectedYieldAmount
        notifyDataSetChanged()
    }

    inner class OriginalViewHolder(val binding: ItemCardRecommendationImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val binding = ItemCardRecommendationImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val fieldYield = items[position]
        val currentYieldAmount = fieldYield.yieldAmount

        with(holder.binding) {
            recImgTitle.text = fieldYield.fieldYieldLabel
            if (showImage) {
                displayImageOriginal(ctx, recImgImage, fieldYield.imageId)

                recImgCard.setOnClickListener { view1 ->
                    mOnItemClickListener?.onItemClick(view1, fieldYield, position)
                }

                val cardColor =
                    if (rowIndex == position || currentYieldAmount == selectedYieldAmount) {
                        ContextCompat.getColor(ctx, R.color.green_100)
                    } else {
                        ContextCompat.getColor(ctx, R.color.grey_3)
                    }
                recImgCard.setCardBackgroundColor(cardColor)
                recImgImageContainer.visibility = View.VISIBLE
            } else {
                recImgImageContainer.visibility = View.GONE
            }
            setAnimation(root, position)
        }
    }

    fun setActiveRowIndex(position: Int) {
        rowIndex = position
    }

    override fun getItemCount(): Int = items.size

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }
}
