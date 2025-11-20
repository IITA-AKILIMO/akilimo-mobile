package com.akilimo.mobile.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemRecommendationBinding
import com.akilimo.mobile.dto.BaseValueOption
import com.akilimo.mobile.enums.EnumStepStatus

/**
 * A generic adapter for displaying recommendation options that extend [BaseValueOption].
 *
 * @param context The context used for label rendering
 * @param getLabel Function to get the label text from BV
 * @param getId Function to get a unique ID from BV
 * @param onClick Callback when an item is clicked
 */
class RecommendationAdapter<BV : Any>(
    private val context: Context,
    private val hideIcon: Boolean = false,
    private val getLabel: (BV) -> String,
    private val getId: (BV) -> Any,
    private val stepStatus: (BV) -> EnumStepStatus = { EnumStepStatus.NOT_STARTED },
    private val onClick: (BaseValueOption<BV>) -> Unit
) : ListAdapter<BaseValueOption<BV>, RecommendationAdapter<BV>.ViewHolder>(
    GenericDiffCallback(getId, getLabel)
) {

    inner class ViewHolder(val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaseValueOption<BV>) {
            val value = item.valueOption
            val stepStatus = stepStatus(value)

            binding.recommendationTitle.text = getLabel(value)
            binding.root.setOnClickListener { onClick(item) }
            updateCompletionBadge(binding.completionBadgeIcon, stepStatus)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Generic DiffUtil callback for BaseValueOption<BV>
     */
    class GenericDiffCallback<BV : Any>(
        private val getId: (BV) -> Any, private val getLabel: (BV) -> String
    ) : DiffUtil.ItemCallback<BaseValueOption<BV>>() {

        override fun areItemsTheSame(
            oldItem: BaseValueOption<BV>, newItem: BaseValueOption<BV>
        ): Boolean = getId(oldItem.valueOption) == getId(newItem.valueOption)

        override fun areContentsTheSame(
            oldItem: BaseValueOption<BV>, newItem: BaseValueOption<BV>
        ): Boolean = getLabel(oldItem.valueOption) == getLabel(newItem.valueOption)
    }

    fun updateCompletionBadge(iconView: ImageView, status: EnumStepStatus) {
        val context = iconView.context

        val (iconRes, tintColor) = when (status) {
            EnumStepStatus.COMPLETED -> Triple(R.drawable.ic_check, R.color.color_accent_2, true)
            EnumStepStatus.IN_PROGRESS -> Triple(R.drawable.ic_alert, R.color.color_accent_4, true)
            EnumStepStatus.NOT_STARTED -> Triple(R.drawable.ic_schedule, R.color.transparent, false)
        }

        iconView.setImageResource(iconRes)
        iconView.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, tintColor))
    }


}
