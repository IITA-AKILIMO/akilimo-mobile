package com.akilimo.mobile.models

import android.os.Parcelable
import com.akilimo.mobile.utils.enums.Orientation
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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
}
