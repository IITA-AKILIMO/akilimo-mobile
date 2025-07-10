package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.databinding.ItemCardRecommendationArrowBinding
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.utils.TheItemAnimation

class UseCaseAdapter(
    private val context: Context,
) : ListAdapter<UseCaseWithTasks, UseCaseAdapter.OriginalViewHolder>(DIFF_CALLBACK) {

    private var _itemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var onAttach = true
    private var _animationType: Int = TheItemAnimation.FADE_IN

    fun interface OnItemClickListener {
        fun onItemClick(view: View?, useCase: UseCaseWithTasks, position: Int)
    }

    fun setAnimationType(animationType: Int) {
        _animationType = animationType
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        _itemClickListener = onItemClickListener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                onAttach = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCardRecommendationArrowBinding.inflate(inflater, parent, false)
        return OriginalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OriginalViewHolder, position: Int) {
        val useCase = getItem(position)

        with(holder.binding) {
            recTitle.text = context.getString(useCase.useCase.useCaseLabel)
            recIconSpacer.visibility = View.GONE
            recIconContainer.visibility = View.GONE


            recCard.setOnClickListener { view ->
                _itemClickListener?.onItemClick(view, useCase, position)
            }
        }

        setAnimation(holder.itemView, position)
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            TheItemAnimation.animate(view, if (onAttach) position else -1, _animationType)
            lastPosition = position
        }
    }

    inner class OriginalViewHolder(val binding: ItemCardRecommendationArrowBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UseCaseWithTasks>() {
            override fun areItemsTheSame(
                oldItem: UseCaseWithTasks,
                newItem: UseCaseWithTasks
            ): Boolean {
                return oldItem.useCase == newItem.useCase
            }

            override fun areContentsTheSame(
                oldItem: UseCaseWithTasks,
                newItem: UseCaseWithTasks
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}