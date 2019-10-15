package com.iita.akilimo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.gms.common.util.Strings
import com.iita.akilimo.R
import com.iita.akilimo.entities.TimeLineModel
import com.iita.akilimo.entities.TimelineAttributes
import com.iita.akilimo.utils.ItemAnimation
import com.iita.akilimo.utils.VectorDrawableUtils
import com.iita.akilimo.utils.enums.Orientation
import com.iita.akilimo.utils.enums.StepStatus
import kotlinx.android.synthetic.main.item_timeline.view.*


/**
 * Created by Vipul Asri on 05-12-2015.
 */

class TimeLineAdapter(
    private val mFeedList: List<TimeLineModel>,
    private var mAttributes: TimelineAttributes,
    private var context: Context,
    private val animation_type: Int
) : RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder>() {

    private var lastPosition = -1
    private var on_attach = true
    private lateinit var mLayoutInflater: LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        val view = if (mAttributes.orientation == Orientation.HORIZONTAL) {
            mLayoutInflater.inflate(R.layout.item_timeline_horizontal, parent, false)
        } else {
            mLayoutInflater.inflate(R.layout.item_timeline, parent, false)
        }
        return TimeLineViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: TimeLineViewHolder, position: Int) {

        val timeLineModel = mFeedList[position]

        when {
            timeLineModel.status == StepStatus.INCOMPLETE -> {
                holder.timeline.marker = VectorDrawableUtils.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_highlight_off,
                    mAttributes.markerIncompleteColor
                )
            }
            timeLineModel.status == StepStatus.COMPLETED -> {
                holder.timeline.marker = VectorDrawableUtils.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_done,
                    mAttributes.markerCompleteColor
                )
            }
            else -> {
                holder.timeline.setMarker(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_hourglass_empty
                    ), mAttributes.markerIncompleteColor
                )
            }
        }

        holder.title.text = timeLineModel.stepTitle

        if (Strings.isEmptyOrWhitespace(timeLineModel.message)) {
            holder.message.text = context.getString(R.string.lbl_not_provided)
        } else {
            holder.message.visibility = VISIBLE
            holder.message.text = timeLineModel.message
        }

        setAnimation(holder.itemView, position)


    }

    override fun getItemCount() = mFeedList.size

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, if (on_attach) position else -1, animation_type)
            lastPosition = position
        }
    }

    inner class TimeLineViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView) {

        val title = itemView.timelineTitle
        val message = itemView.timelineContent
        val timeline = itemView.timeline

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
        }
    }

}
