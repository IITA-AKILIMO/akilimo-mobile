package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.models.Recommendations
import com.akilimo.mobile.utils.TheItemAnimation.animate

class AdapterListAnimation(
    private val layoutId: Int = R.layout.item_card_recommendation_arrow,
) : ListAdapter<Recommendations, AdapterListAnimation.OriginalViewHolder>(RecommendationDiffCallback()) {

    private var lastPosition = -1
    private var onAttach = true
    private var animationType: Int = 0
    private var onItemClickListener: ((View, Recommendations, Int) -> Unit)? = null

    fun setAnimationType(type: Int) {
        this.animationType = type
    }

    inner class OriginalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView? = view.findViewById(R.id.image)
        val name: TextView = view.findViewById(R.id.name)
        val cardView: View = view.findViewById(R.id.lyt_parent)
        val contentLayout: View = view.findViewById(R.id.contentLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return OriginalViewHolder(view)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val recommendation = getItem(position)
        holder.name.text = recommendation.recommendationName

        holder.cardView.setOnClickListener { view ->
            onItemClickListener?.invoke(view, recommendation, position)
        }

        setAnimation(holder.itemView, position)
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

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }


    fun setOnItemClickListener(listener: (View, Recommendations, Int) -> Unit) {
        this.onItemClickListener = listener
    }

    class RecommendationDiffCallback : DiffUtil.ItemCallback<Recommendations>() {
        override fun areItemsTheSame(oldItem: Recommendations, newItem: Recommendations): Boolean {
            return oldItem.recommendationName == newItem.recommendationName
        }

        override fun areContentsTheSame(
            oldItem: Recommendations,
            newItem: Recommendations
        ): Boolean {
            return oldItem.recommendationName == newItem.recommendationName
        }
    }
}

