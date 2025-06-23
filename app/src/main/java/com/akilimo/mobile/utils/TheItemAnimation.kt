package com.akilimo.mobile.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

@Suppress("MagicNumber")
object TheItemAnimation {

    const val NONE = 0
    const val BOTTOM_UP = 1
    const val FADE_IN = 2
    const val LEFT_RIGHT = 3
    const val RIGHT_LEFT = 4
    const val SCALE = 5
    const val TOP_DOWN = 6

    private const val DEFAULT_TRANSLATION_Y = 500f
    private const val LARGE_TRANSLATION_Y = 800f
    private const val DEFAULT_TRANSLATION_X = 400f

    private const val DURATION_BOTTOM_UP = 150L
    private const val DURATION_FADE_IN = 500L
    private const val DURATION_SCALE = 500L
    private const val DURATION_LEFT_RIGHT = 150L
    private const val DURATION_RIGHT_LEFT = 150L

    fun animate(view: View, position: Int, type: Int) {
        when (type) {
            BOTTOM_UP -> animateBottomUp(view, position)
            FADE_IN -> animateFadeIn(view, position)
            SCALE -> animateScale(view, position)
            TOP_DOWN -> animateTopDown(view, position)
            LEFT_RIGHT -> animateSide(view, position, fromLeft = true)
            RIGHT_LEFT -> animateSide(view, position, fromLeft = false)
        }
    }

    private fun animateScale(view: View, position: Int) {
        val isNotFirst = position == -1

        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = if (isNotFirst) 3 else 1 * DURATION_SCALE
            start()
        }
    }

    private fun animateBottomUp(view: View, position: Int) {
        val isNotFirst = position == -1
        val effectivePosition = position + 1
        val translationY = if (isNotFirst) LARGE_TRANSLATION_Y else DEFAULT_TRANSLATION_Y

        view.translationY = translationY
        view.alpha = 0f

        val animatorTranslateY =
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, translationY, 0f).apply {
                startDelay = if (isNotFirst) 0 else effectivePosition * DURATION_BOTTOM_UP
                duration = if (isNotFirst) 3 else 1 * DURATION_BOTTOM_UP
            }

        val animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1f)

        AnimatorSet().apply {
            playTogether(animatorTranslateY, animatorAlpha)
            start()
        }
    }

    private fun animateFadeIn(view: View, position: Int) {
        val isNotFirst = position == -1
        val effectivePosition = position + 1

        view.alpha = 0f

        val animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 0.5f, 1f).apply {
            startDelay =
                if (isNotFirst) DURATION_FADE_IN / 2 else effectivePosition * DURATION_FADE_IN / 3
            duration = DURATION_FADE_IN
        }

        AnimatorSet().apply {
            play(animatorAlpha)
            start()
        }
    }

    private fun animateSide(view: View, position: Int, fromLeft: Boolean) {
        val isNotFirst = position == -1
        val effectivePosition = position + 1

        val translationX = if (fromLeft) -DEFAULT_TRANSLATION_X else DEFAULT_TRANSLATION_X
        val duration = if (fromLeft) DURATION_LEFT_RIGHT else DURATION_RIGHT_LEFT

        view.translationX = translationX
        view.alpha = 0f

        val animatorTranslateX =
            ObjectAnimator.ofFloat(view, View.TRANSLATION_X, translationX, 0f).apply {
                startDelay = if (isNotFirst) duration else effectivePosition * duration
                this.duration = if (isNotFirst) 2 else 1 * duration
            }

        val animatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1f)

        AnimatorSet().apply {
            playTogether(animatorTranslateX, animatorAlpha)
            start()
        }
    }

    private fun animateTopDown(view: View, position: Int) {
        view.translationY = -DEFAULT_TRANSLATION_Y
        view.alpha = 0f

        val translateY = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f)
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1f)

        AnimatorSet().apply {
            playTogether(translateY, alpha)
            duration = 250
            startDelay = position * 50L
            start()
        }
    }
}
