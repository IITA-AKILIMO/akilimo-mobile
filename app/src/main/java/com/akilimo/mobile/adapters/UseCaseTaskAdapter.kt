package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemCardRecommendationArrowBinding
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.VectorDrawableUtils

class UseCaseTaskAdapter(
    private val context: Context,
    private val displayArrow: Boolean = true
) : ListAdapter<UseCaseTask, UseCaseTaskAdapter.OriginalViewHolder>(DIFF_CALLBACK) {

    private var _itemClickListener: OnItemClickListener? = null
    private var lastPosition = -1
    private var onAttach = true
    private var _animationType: Int = TheItemAnimation.FADE_IN

    fun interface OnItemClickListener {
        fun onItemClick(view: View?, useCaseTask: UseCaseTask, position: Int)
    }

    fun setAnimationType(animationType: Int) {
        _animationType = animationType
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        _itemClickListener = onItemClickListener
    }

    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
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
        val useCaseTask = getItem(position)

        with(holder.binding) {
            //TODO: Extract label from string resource
            recTitle.text = useCaseTask.taskName.name

            if (displayArrow) {
                val isCompleted = useCaseTask.completed
                val iconRes = if (isCompleted) R.drawable.ic_done else R.drawable.ic_pending
                val colorRes = if (isCompleted) R.color.green_600 else R.color.red_400
                val drawable = VectorDrawableUtils.getDrawable(
                    context,
                    iconRes,
                    ContextCompat.getColor(context, colorRes)
                )
                recIcon.setImageDrawable(drawable)
            } else {
                recIconSpacer.visibility = View.GONE
                recIconContainer.visibility = View.GONE
            }

            recCard.setOnClickListener { view ->
                _itemClickListener?.onItemClick(view, useCaseTask, position)
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
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UseCaseTask>() {
            override fun areItemsTheSame(
                oldItem: UseCaseTask,
                newItem: UseCaseTask
            ): Boolean {
                return oldItem.taskName == newItem.taskName
            }

            override fun areContentsTheSame(
                oldItem: UseCaseTask,
                newItem: UseCaseTask
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}