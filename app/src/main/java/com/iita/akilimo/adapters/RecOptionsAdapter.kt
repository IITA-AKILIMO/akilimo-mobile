package com.iita.akilimo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iita.akilimo.R
import com.iita.akilimo.databinding.ItemCardRecommendationArrowBinding
import com.iita.akilimo.mappers.ComputedResponse
import com.iita.akilimo.models.RecommendationOptions
import com.iita.akilimo.utils.ItemAnimation
import com.iita.akilimo.utils.VectorDrawableUtils

class RecOptionsAdapter : RecyclerView.Adapter<RecOptionsAdapter.OriginalViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var on_attach = true

    private var animation_type: Int = ItemAnimation.FADE_IN
    private lateinit var items: List<RecommendationOptions>
    private lateinit var mLayoutInflater: LayoutInflater

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    fun setData(items: List<RecommendationOptions>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setData(items: List<RecommendationOptions>, animation_type: Int) {
        this.items = items
        this.animation_type = animation_type
        notifyDataSetChanged()
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
                on_attach = false
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
            ItemAnimation.animate(view, if (on_attach) position else -1, animation_type)
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