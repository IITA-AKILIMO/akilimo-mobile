package com.akilimo.mobile.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

fun animateColorChange(
    target: (Int) -> Unit,
    fromColor: Int,
    toColor: Int,
    duration: Long = 250
) {
    ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
        this.duration = duration
        addUpdateListener { animator ->
            target(animator.animatedValue as Int)
        }
        start()
    }
}

// Extensions
fun MaterialCardView.animateCardBackground(toColor: Int, duration: Long = 250) {
    val current = cardBackgroundColor.defaultColor
    animateColorChange({ setCardBackgroundColor(it) }, current, toColor, duration)
}

fun TextView.animateTextColor(toColor: Int, duration: Long = 250) {
    val current = currentTextColor
    animateColorChange({ setTextColor(it) }, current, toColor, duration)
}

