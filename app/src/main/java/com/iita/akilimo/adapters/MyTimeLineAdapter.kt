package com.iita.akilimo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.gms.common.util.Strings
import com.iita.akilimo.R
import com.iita.akilimo.databinding.ItemTimelineBinding
import com.iita.akilimo.models.TimeLineModel
import com.iita.akilimo.models.TimelineAttributes
import com.iita.akilimo.utils.ItemAnimation
import com.iita.akilimo.utils.VectorDrawableUtils
import com.iita.akilimo.utils.enums.StepStatus
import kotlinx.android.synthetic.main.item_timeline.view.*

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
            ItemAnimation.animate(view, if (onAttach) position else -1, animation_type)
            lastPosition = position
        }
    }

    inner class TimeLineViewHolder(itemView: ItemTimelineBinding, viewType: Int) :
        RecyclerView.ViewHolder(itemView.root) {

        val title: AppCompatTextView = itemView.timelineTitle
        val message: AppCompatTextView = itemView.timelineContent
        val timeline: TimelineView = itemView.timeline

        init {
            timeline.initLine(viewType)
            timeline.markerSize = mAttributes.markerSize
            timeline.setMarkerColor(mAttributes.markerCompleteColor)
            timeline.isMarkerInCenter = mAttributes.markerInCenter
            timeline.markerPaddingLeft = mAttributes.markerLeftPadding
            timeline.markerPaddingTop = mAttributes.markerTopPadding
            timeline.markerPaddingRight = mAttributes.markerRightPadding
            timeline.markerPaddingBottom = mAttributes.markerBottomPadding
            timeline.linePadding = mAttributes.linePadding

            timeline.lineWidth = mAttributes.lineWidth
            timeline.setStartLineColor(mAttributes.startLineColor, viewType)
            timeline.setEndLineColor(mAttributes.endLineColor, viewType)
            timeline.lineStyle = mAttributes.lineStyle
            timeline.lineStyleDashLength = mAttributes.lineDashWidth
            timeline.lineStyleDashGap = mAttributes.lineDashGap
//        }
        }

        fun bind(timeLineModel: TimeLineModel) {
            when (timeLineModel.status) {
                StepStatus.INCOMPLETE -> {
                    timeline.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_highlight_off,
                        mAttributes.markerIncompleteColor
                    )
                }
                StepStatus.COMPLETED -> {
                    timeline.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_done,
                        mAttributes.markerCompleteColor
                    )
                }
                StepStatus.WARNING -> {
                    timeline.marker = VectorDrawableUtils.getDrawable(
                        itemView.context,
                        R.drawable.ic_warn,
                        ResourcesCompat.getColor(context.resources, R.color.yellow_900, null)
                    )
                }
                StepStatus.CANCELLED -> {
                    timeline.marker = VectorDrawableUtils.getDrawable(
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
