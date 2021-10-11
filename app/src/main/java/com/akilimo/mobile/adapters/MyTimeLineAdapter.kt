package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.gms.common.util.Strings
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ItemTimelineBinding
import com.akilimo.mobile.models.TimeLineModel
import com.akilimo.mobile.models.TimelineAttributes
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.VectorDrawableUtils
import com.akilimo.mobile.utils.enums.StepStatus

class MyTimeLineAdapter(
    private val mFeedList: List<TimeLineModel>,
    private var mAttributes: TimelineAttributes,
    private var context: Context,
    private val animation_type: Int
) : RecyclerView.Adapter<MyTimeLineAdapter.TimeLineViewHolder>() {

    private var lastPosition = -1
    private var onAttach = true
    private lateinit var mLayoutInflater: LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        val view = ItemTimelineBinding.inflate(mLayoutInflater, parent, false)

        return TimeLineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val timeLineModel = mFeedList[position]
        holder.bind(timeLineModel)

        setAnimation(holder.itemView, position)
    }

    override fun getItemCount() = mFeedList.size

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            TheItemAnimation.animate(view, if (onAttach) position else -1, animation_type)
            lastPosition = position
        }
    }

    inner class TimeLineViewHolder(itemView: ItemTimelineBinding, viewType: Int) :
        RecyclerView.ViewHolder(itemView.root) {

        val title: AppCompatTextView = itemView.timelineTitle
        val message: AppCompatTextView = itemView.timelineContent
        val timelineView: TimelineView = itemView.timeline

        init {
            timelineView.initLine(viewType)
            timelineView.markerSize = mAttributes.markerSize
            timelineView.setMarkerColor(mAttributes.markerCompleteColor)
            timelineView.isMarkerInCenter = mAttributes.markerInCenter
            timelineView.markerPaddingLeft = mAttributes.markerLeftPadding
            timelineView.markerPaddingTop = mAttributes.markerTopPadding
            timelineView.markerPaddingRight = mAttributes.markerRightPadding
            timelineView.markerPaddingBottom = mAttributes.markerBottomPadding
            timelineView.linePadding = mAttributes.linePadding

            timelineView.lineWidth = mAttributes.lineWidth
            timelineView.setStartLineColor(mAttributes.startLineColor, viewType)
            timelineView.setEndLineColor(mAttributes.endLineColor, viewType)
            timelineView.lineStyle = mAttributes.lineStyle
            timelineView.lineStyleDashLength = mAttributes.lineDashWidth
            timelineView.lineStyleDashGap = mAttributes.lineDashGap
        }

        fun bind(timeLineModel: TimeLineModel) {
            when (timeLineModel.status) {
                StepStatus.INCOMPLETE -> {
                    timelineView.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_highlight_off,
                        mAttributes.markerIncompleteColor
                    )
                }
                StepStatus.COMPLETED -> {
                    timelineView.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_done,
                        mAttributes.markerCompleteColor
                    )
                }
                StepStatus.WARNING -> {
                    timelineView.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_warn,
                        ResourcesCompat.getColor(context.resources, R.color.yellow_900, null)
                    )
                }
                StepStatus.CANCELLED -> {
                    timelineView.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_clear_all_white_24dp,
                        mAttributes.markerCompleteColor
                    )
                }
            }

            title.text = context.getString(R.string.lbl_timeline_title, timeLineModel.stepTitle)

            if (Strings.isEmptyOrWhitespace(timeLineModel.message)) {
                message.text = context.getString(R.string.lbl_not_provided)
            } else {
                message.visibility = VISIBLE
                message.text = timeLineModel.message
            }
        }
    }

}
