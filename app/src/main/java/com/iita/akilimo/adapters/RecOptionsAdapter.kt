package com.iita.akilimo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iita.akilimo.databinding.ItemCardRecommendationArrowBinding
import com.iita.akilimo.models.RecommendationOptions
import com.iita.akilimo.utils.ItemAnimation

class RecOptionsAdapter(
    private val ctx: Context,
    private val items: List<RecommendationOptions>,
    private val animation_type: Int
) : RecyclerView.Adapter<RecOptionsAdapter.OriginalViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var on_attach = true
    private lateinit var mLayoutInflater: LayoutInflater

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: RecommendationOptions?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
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
            cardView.setOnClickListener { view: View? ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, recModel, position)
                }
            }
        }
    }

}