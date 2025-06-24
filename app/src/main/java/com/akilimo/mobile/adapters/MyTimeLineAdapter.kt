package com.akilimo.mobile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemTimelineBinding
import com.akilimo.mobile.models.TimeLineModel
import com.akilimo.mobile.models.TimelineAttributes
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.VectorDrawableUtils
import com.akilimo.mobile.utils.enums.StepStatus
import com.github.vipulasri.timelineview.TimelineView

class MyTimeLineAdapter(
    private var items: List<TimeLineModel>,
    private val attributes: TimelineAttributes,
    private val animationType: Int = TheItemAnimation.FADE_IN
) : RecyclerView.Adapter<MyTimeLineAdapter.TimeLineViewHolder>() {

    private var lastPosition = -1
    private var onAttach = true

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        val binding =
            ItemTimelineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeLineViewHolder(binding, viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {
        holder.bind(items[position])
        setAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<TimeLineModel>) {
        items = newList
        notifyDataSetChanged()
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            TheItemAnimation.animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }

    inner class TimeLineViewHolder(
        private val binding: ItemTimelineBinding,
        private val viewType: Int
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context
        private val title: AppCompatTextView = binding.timelineTitle
        private val message: AppCompatTextView = binding.timelineContent
        private val timelineView: TimelineView = binding.timeline

        fun bind(model: TimeLineModel) {
            // Configure TimelineView
            timelineView.apply {
                initLine(viewType)
                markerSize = attributes.markerSize
                isMarkerInCenter = attributes.markerInCenter
                markerPaddingLeft = attributes.markerLeftPadding
                markerPaddingTop = attributes.markerTopPadding
                markerPaddingRight = attributes.markerRightPadding
                markerPaddingBottom = attributes.markerBottomPadding

                linePadding = attributes.linePadding
                lineWidth = attributes.lineWidth
                setStartLineColor(attributes.startLineColor, viewType)
                setEndLineColor(attributes.endLineColor, viewType)
                lineStyle = attributes.lineStyle
                lineStyleDashLength = attributes.lineDashWidth
                lineStyleDashGap = attributes.lineDashGap
            }

            // Set marker icon based on status
            timelineView.marker = when (model.status) {
                StepStatus.INCOMPLETE -> VectorDrawableUtils.getDrawable(
                    context, R.drawable.ic_highlight_off, attributes.markerIncompleteColor
                )

                StepStatus.COMPLETED -> VectorDrawableUtils.getDrawable(
                    context, R.drawable.ic_done, attributes.markerCompleteColor
                )

                StepStatus.WARNING -> VectorDrawableUtils.getDrawable(
                    context, R.drawable.ic_warn,
                    ResourcesCompat.getColor(context.resources, R.color.yellow_900, null)
                )

                StepStatus.CANCELLED -> VectorDrawableUtils.getDrawable(
                    context, R.drawable.ic_clear, attributes.markerCompleteColor
                )
            }

            // Set title
            title.text = context.getString(R.string.lbl_timeline_title, model.stepTitle)

            // Set content or fallback
            message.apply {
                visibility = View.VISIBLE
                text = if (model.message.isNullOrEmpty()) {
                    context.getString(R.string.lbl_not_provided)
                } else {
                    model.message
                }
            }
        }
    }
}
