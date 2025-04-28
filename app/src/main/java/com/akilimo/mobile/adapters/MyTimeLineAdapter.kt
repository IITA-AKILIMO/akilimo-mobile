package com.akilimo.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
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
    private val mFeedList: List<TimeLineModel>,
    private var mAttributes: TimelineAttributes,
    private var context: Context,
    private val animationType: Int
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
            TheItemAnimation.animate(view, if (onAttach) position else -1, animationType)
            lastPosition = position
        }
    }

    inner class TimeLineViewHolder(itemView: ItemTimelineBinding, viewType: Int) :
        RecyclerView.ViewHolder(itemView.root) {

        private val title: AppCompatTextView = itemView.timelineTitle
        private val message: AppCompatTextView = itemView.timelineContent
        private val timelineView: TimelineView = itemView.timeline

        init {
            timelineView.apply {
                initLine(viewType)
                markerSize = mAttributes.markerSize

                setMarkerColor(mAttributes.markerCompleteColor)
                isMarkerInCenter = mAttributes.markerInCenter
                markerPaddingLeft = mAttributes.markerLeftPadding
                markerPaddingTop = mAttributes.markerTopPadding
                markerPaddingRight = mAttributes.markerRightPadding
                markerPaddingBottom = mAttributes.markerBottomPadding

                linePadding = mAttributes.linePadding
                lineWidth = mAttributes.lineWidth

                setStartLineColor(mAttributes.startLineColor, viewType)
                setEndLineColor(mAttributes.endLineColor, viewType)

                lineStyle = mAttributes.lineStyle
                lineStyleDashLength = mAttributes.lineDashWidth
                lineStyleDashGap = mAttributes.lineDashGap
            }
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
                        R.drawable.ic_clear,
                        mAttributes.markerCompleteColor
                    )
                }
            }

            title.text = context.getString(R.string.lbl_timeline_title, timeLineModel.stepTitle)

            if (timeLineModel.message.isNullOrEmpty()) {
                message.text = context.getString(R.string.lbl_not_provided)
            } else {
                message.visibility = VISIBLE
                message.text = timeLineModel.message
            }
        }
    }

}
