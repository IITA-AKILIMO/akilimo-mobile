package com.akilimo.mobile.models

import android.os.Parcelable
import com.akilimo.mobile.utils.enums.Orientation
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

import kotlin.properties.Delegates.observable

@Parcelize
class TimelineAttributes(
    var markerSize: Int = 48,
    var markerCompleteColor: Int,
    var markerIncompleteColor: Int,
    var markerInCenter: Boolean = true,
    var markerLeftPadding: Int = 0,
    var markerTopPadding: Int = 0,
    var markerRightPadding: Int = 0,
    var markerBottomPadding: Int = 0,
    var linePadding: Int = 0,
    var lineWidth: Int = 0,
    var startLineColor: Int,
    var endLineColor: Int,
    var lineStyle: Int = TimelineView.LineStyle.DASHED,
    var lineDashWidth: Int = 4,
    var lineDashGap: Int = 2
) : Parcelable {

    @IgnoredOnParcel
    var orientation by observable(Orientation.VERTICAL) { _, oldValue, newValue ->
        onOrientationChanged?.invoke(oldValue, newValue)
    }

    @IgnoredOnParcel
    var onOrientationChanged: ((Orientation, Orientation) -> Unit)? = null

    fun copy(): TimelineAttributes {
        val attributes = TimelineAttributes(
            markerSize,
            markerCompleteColor,
            markerIncompleteColor,
            markerInCenter,
            markerLeftPadding,
            markerTopPadding,
            markerRightPadding,
            markerBottomPadding,
            linePadding,
            lineWidth,
            startLineColor,
            endLineColor,
            lineStyle,
            lineDashWidth,
            lineDashGap
        )
        attributes.orientation = orientation
        return attributes
    }
}
