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
    private val ctx: Context,
    private var items: List<RecommendationOptions>
) :
    RecyclerView.Adapter<RecOptionsAdapter.OriginalViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var onAttach = true

    private var animationType: Int = TheItemAnimation.FADE_IN

    interface OnItemClickListener {
        fun onItemClick(view: View?, recommendation: RecommendationOptions, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    fun setData(items: List<RecommendationOptions>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                onAttach = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            TheItemAnimation.animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }

    inner class OriginalViewHolder(val binding: ItemCardRecommendationArrowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val mLayoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCardRecommendationArrowBinding.inflate(mLayoutInflater, parent, false)
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val recModel = items[position]
        with(holder.binding) {
            recTitle.text = recModel.recName
            var icon = R.drawable.ic_info
            var statusColor = ContextCompat.getColor(ctx, R.color.red_400)
            if (recModel.adviceStatus!!.completed) {
                icon = R.drawable.ic_done
                statusColor = ContextCompat.getColor(ctx, R.color.green_600)
            }

            val drawable = VectorDrawableUtils.getDrawable(ctx, icon, statusColor)
            recIcon.setImageDrawable(drawable)
            recCard.setOnClickListener { view: View? ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, recModel, position)
                }
            }
        }
        setAnimation(holder.itemView, position)
    }

}
