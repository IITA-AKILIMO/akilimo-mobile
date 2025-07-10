package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.databinding.ItemCardRecommendationArrowBinding
import com.akilimo.mobile.models.Recommendation
import com.akilimo.mobile.utils.TheItemAnimation.animate

@Deprecated(
    message = "Consider moving to a RecOptionsAdapter",
    replaceWith = ReplaceWith("RecOptionsAdapter")
)
class AdapterListAnimation :
    ListAdapter<Recommendation, AdapterListAnimation.OriginalViewHolder>(RecommendationDiffCallback()) {

    private var lastPosition = -1
    private var onAttach = true
    private var animationType: Int = 0
    private var onItemClickListener: ((View, Recommendation, Int) -> Unit)? = null

    fun setAnimationType(type: Int) {
        this.animationType = type
    }

    inner class OriginalViewHolder(val binding: ItemCardRecommendationArrowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val binding = ItemCardRecommendationArrowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val recommendation = getItem(position)
        with(holder.binding) {
            recTitle.text = recommendation.recommendationName

            recCard.setOnClickListener { view ->
                onItemClickListener?.invoke(view, recommendation, position)
            }
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


    fun setOnItemClickListener(listener: (View, Recommendation, Int) -> Unit) {
        this.onItemClickListener = listener
    }

    class RecommendationDiffCallback : DiffUtil.ItemCallback<Recommendation>() {
        override fun areItemsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem.recommendationName == newItem.recommendationName
        }

        override fun areContentsTheSame(
            oldItem: Recommendation,
            newItem: Recommendation
        ): Boolean {
            return oldItem.recommendationName == newItem.recommendationName
        }
    }
}

