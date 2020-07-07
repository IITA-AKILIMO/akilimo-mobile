package com.iita.akilimo.models

import android.os.Parcelable
import com.iita.akilimo.utils.enums.Orientation
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlin.properties.Delegates.observable

@Parcelize
class TimelineAttributes(
    var markerSize: Int,
    var markerCompleteColor: Int,
    var markerIncompleteColor: Int,
    var markerInCenter: Boolean,
    var markerLeftPadding: Int,
    var markerTopPadding: Int,
    var markerRightPadding: Int,
    var markerBottomPadding: Int,
    var linePadding: Int,
    var lineWidth: Int,
    var startLineColor: Int,
    var endLineColor: Int,
    var lineStyle: Int,
    var lineDashWidth: Int,
    var lineDashGap: Int
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

    override fun toString(): String {
        return "TimelineAttributes(markerSize=$markerSize, markerCompleteColor=$markerCompleteColor,markerIncompleteColor=$markerIncompleteColor, markerInCenter=$markerInCenter, " +
                "markerTopPadding=$markerTopPadding, markerBottomPadding=$markerBottomPadding, linePadding=$linePadding, " +
                "lineWidth=$lineWidth, startLineColor=$startLineColor, endLineColor=$endLineColor, lineStyle=$lineStyle, " +
                "lineDashWidth=$lineDashWidth, lineDashGap=$lineDashGap, onOrientationChanged=$onOrientationChanged)"
    }
}