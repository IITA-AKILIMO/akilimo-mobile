package com.akilimo.mobile.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import kotlin.math.roundToLong

object TheItemAnimation {
    // animation types (kept as Ints for backward compatibility)
    const val NONE = 0
    const val BOTTOM_UP = 1
    const val FADE_IN = 2
    const val LEFT_RIGHT = 3
    const val RIGHT_LEFT = 4

    // durations (ms)
    private const val DURATION_BOTTOM_UP = 150L
    private const val DURATION_FADE_IN = 500L
    private const val DURATION_LEFT_RIGHT = 150L
    private const val DURATION_RIGHT_LEFT = 150L

    // translation distances (px)
    private const val TRANSLATION_BOTTOM_UP_FIRST = 800f
    private const val TRANSLATION_BOTTOM_UP_SUBSEQUENT = 500f
    private const val TRANSLATION_HORIZONTAL = 400f

    @JvmStatic
    fun animate(view: View, position: Int, type: Int) {
        when (type) {
            BOTTOM_UP -> animateBottomUp(view, position)
            FADE_IN -> animateFadeIn(view, position)
            LEFT_RIGHT -> animateLeftRight(view, position)
            RIGHT_LEFT -> animateRightLeft(view, position)
            else -> {/* NONE or unknown — no animation */}
        }
    }

    // helper: compute item index (position == -1 means "not first item / single item")
    private fun itemIndex(position: Int) = if (position == -1) -1 else position + 1

    // helper: compute start delay
    private fun startDelayFor(itemIndex: Int, baseDuration: Long, isSingleOrHeader: Boolean = false): Long {
        return if (isSingleOrHeader) 0L else (itemIndex * baseDuration)
    }

    private fun animateBottomUp(view: View, position: Int) {
        val idx = itemIndex(position)
        val isSingle = position == -1

        val translationStart = if (isSingle) TRANSLATION_BOTTOM_UP_FIRST else TRANSLATION_BOTTOM_UP_SUBSEQUENT
        view.translationY = translationStart
        view.alpha = 0f

        val translate = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, translationStart, 0f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

        translate.startDelay = startDelayFor(idx, DURATION_BOTTOM_UP, isSingle)
        translate.duration = if (isSingle) (3 * DURATION_BOTTOM_UP) else DURATION_BOTTOM_UP
        alpha.startDelay = translate.startDelay
        alpha.duration = translate.duration

        AnimatorSet().apply {
            playTogether(translate, alpha)
            start()
        }
    }

    private fun animateFadeIn(view: View, position: Int) {
        val idx = itemIndex(position)
        val isSingle = position == -1

        view.alpha = 0f

        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 0.5f, 1f)
        alpha.startDelay = if (isSingle) (DURATION_FADE_IN / 2) else ((idx * (DURATION_FADE_IN / 3.0)).roundToLong())
        alpha.duration = DURATION_FADE_IN

        AnimatorSet().apply {
            play(alpha)
            start()
        }
    }

    private fun animateLeftRight(view: View, position: Int) {
        val idx = itemIndex(position)
        val isSingle = position == -1

        view.translationX = -TRANSLATION_HORIZONTAL
        view.alpha = 0f

        val translate = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -TRANSLATION_HORIZONTAL, 0f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

        translate.startDelay = startDelayFor(idx, DURATION_LEFT_RIGHT, isSingle)
        translate.duration = if (isSingle) (2 * DURATION_LEFT_RIGHT) else DURATION_LEFT_RIGHT
        alpha.startDelay = translate.startDelay
        alpha.duration = translate.duration

        AnimatorSet().apply {
            playTogether(translate, alpha)
            start()
        }
    }

    private fun animateRightLeft(view: View, position: Int) {
        val idx = itemIndex(position)
        val isSingle = position == -1

        // Use an absolute offset from current x to avoid unexpected results with view.x at layout time
        view.translationX = TRANSLATION_HORIZONTAL
        view.alpha = 0f

        val translate = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, TRANSLATION_HORIZONTAL, 0f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

        translate.startDelay = startDelayFor(idx, DURATION_RIGHT_LEFT, isSingle)
        translate.duration = if (isSingle) (2 * DURATION_RIGHT_LEFT) else DURATION_RIGHT_LEFT
        alpha.startDelay = translate.startDelay
        alpha.duration = translate.duration

        AnimatorSet().apply {
            playTogether(translate, alpha)
            start()
        }
    }
}
