package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationArrowBinding
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.VectorDrawableUtils

class RecOptionsAdapter(
    private val context: Context,
    private var items: List<RecommendationOptions>,
    private val displayArrow: Boolean
) : RecyclerView.Adapter<RecOptionsAdapter.OriginalViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var onAttach = true
    private var animationType: Int = TheItemAnimation.FADE_IN

    interface OnItemClickListener {
        fun onItemClick(view: View?, recommendation: RecommendationOptions, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    fun setData(newItems: List<RecommendationOptions>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                onAttach = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun getItemCount(): Int = items.size

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            TheItemAnimation.animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }

    inner class OriginalViewHolder(val binding: ItemCardRecommendationArrowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCardRecommendationArrowBinding.inflate(inflater, parent, false)
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val recModel = items[position]

        with(holder.binding) {
            recTitle.text = recModel.recName

            if (displayArrow) {
                val isCompleted = recModel.adviceStatus?.completed == true
                val iconRes = if (isCompleted) R.drawable.ic_done else R.drawable.ic_info
                val colorRes = if (isCompleted) R.color.green_600 else R.color.red_400
                val drawable = VectorDrawableUtils.getDrawable(
                    context,
                    iconRes,
                    ContextCompat.getColor(context, colorRes)
                )
                recIcon.setImageDrawable(drawable)
            } else {
                recIconContainer.visibility = View.GONE
            }

            recCard.setOnClickListener { view ->
                itemClickListener?.onItemClick(view, recModel, position)
            }
        }

        setAnimation(holder.itemView, position)
    }
}