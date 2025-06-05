package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationImageBinding
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.utils.TheItemAnimation.animate
import com.akilimo.mobile.utils.Tools.displayImageOriginal

class CropPerformanceAdapter(
    private val ctx: Context,
    private var items: List<CropPerformance>,
    private val animationType: Int
) :
    RecyclerView.Adapter<CropPerformanceAdapter.OriginalViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var rowIndex = -1
    private val onAttach = true
    private var performanceScore = 0


    interface OnItemClickListener {
        fun onItemClick(view: View?, clickedCropPerformance: CropPerformance?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        this.mOnItemClickListener = mItemClickListener
    }

    fun setItems(
        performanceScoreValue: Int,
        performanceList: List<CropPerformance>,
        positionChanged: Int = 0
    ) {
        items = performanceList
        performanceScore = performanceScoreValue
        notifyItemChanged(positionChanged)
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

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val maizePerformance = items[position]
        val currentPerformanceValue = maizePerformance.performanceScore

        with(holder.binding) {
            recImgTitle.text = maizePerformance.maizePerformanceLabel
            displayImageOriginal(ctx, recImgImage, maizePerformance.imageId)

            recImgCard.setOnClickListener { view1: View? ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view1, items[position], position)
                }
            }

            if ((rowIndex == position) || (currentPerformanceValue == performanceScore)) {
                recImgCard.setCardBackgroundColor(ctx.resources.getColor(R.color.green_100))
            } else {
                recImgCard.setCardBackgroundColor(ctx.resources.getColor(R.color.grey_3))
            }

            setAnimation(holder.itemView, position)
        }
    }

    fun setActiveRowIndex(position: Int) {
        rowIndex = position
    }

    override fun getItemCount(): Int {
        return items.size
    }


    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }
}
