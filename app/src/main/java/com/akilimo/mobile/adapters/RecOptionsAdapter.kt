package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationArrowBinding
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.VectorDrawableUtils

class RecOptionsAdapter(private var items: List<RecommendationOptions>) :
    RecyclerView.Adapter<RecOptionsAdapter.OriginalViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var onAttach = true

    private var animationType: Int = TheItemAnimation.FADE_IN
    private lateinit var mLayoutInflater: LayoutInflater

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    fun setData(items: List<RecommendationOptions>, itemPosition: Int) {
        this.items = items
        notifyItemChanged(itemPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        val view = ItemCardRecommendationArrowBinding.inflate(mLayoutInflater, parent, false)
        return OriginalViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val recModel = items[position]
        holder.bind(recModel, position)
        setAnimation(holder.itemView, position)
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


    inner class OriginalViewHolder(itemView: ItemCardRecommendationArrowBinding, viewType: Int) :
        RecyclerView.ViewHolder(itemView.root) {
        var image: ImageView = itemView.statusImage
        var name: TextView = itemView.name
        var cardView: View = itemView.lytParent

        fun bind(recModel: RecommendationOptions, position: Int) {
            name.text = recModel.recName
            var icon = R.drawable.ic_info
            var statusColor = ContextCompat.getColor(itemView.context, R.color.red_400)
            if (recModel.adviceStatus!!.completed) {
                icon = R.drawable.ic_done
                statusColor = ContextCompat.getColor(itemView.context, R.color.green_600)
            }

            val drawable = VectorDrawableUtils.getDrawable(itemView.context, icon, statusColor)

            image.setImageDrawable(drawable)

            cardView.setOnClickListener { view: View? ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, recModel, position)
                }
            }
        }
    }

}
