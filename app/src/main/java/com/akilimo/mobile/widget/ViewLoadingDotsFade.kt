package com.akilimo.mobile.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Use this view with width = height or width > height
 * EXAMPLE :
 * android:layout_width="50dp"
 * android:layout_height="30dp"
 *
 *
 * To change dot color you can use :
 * android:background="@color/exampleColor"
 */
class ViewLoadingDotsFade : LinearLayout {
    private var context: Context? = null
    private lateinit var img: Array<ImageView?>
    private val circle = GradientDrawable()
    private lateinit var animator: Array<ObjectAnimator?>
    private var onLayoutReach: Boolean = false

    companion object {
        private const val OBJECT_SIZE = 3
        private const val DURATION = 400
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context

        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setLayoutParams(layoutParams)

        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun initView() {
        var color = Color.GRAY
        val background = background
        if (background is ColorDrawable) {
            color = background.color
        }
        setBackgroundColor(Color.TRANSPARENT)

        removeAllViews()
        img = arrayOfNulls(OBJECT_SIZE)
        circle.shape = GradientDrawable.OVAL
        circle.setColor(color)
        circle.setSize(200, 200)

        val layoutParams2 = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams2.weight = 1f

        val rel = arrayOfNulls<LinearLayout>(OBJECT_SIZE)
        for (i in 0..<OBJECT_SIZE) {
            rel[i] = LinearLayout(context)
            rel[i]!!.gravity = Gravity.CENTER
            rel[i]!!.layoutParams = layoutParams2
            img[i] = ImageView(context)
            img[i]!!.setBackgroundDrawable(circle)
            rel[i]!!.addView(img[i])
            addView(rel[i])
        }
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!onLayoutReach) {
            onLayoutReach = true
            val lp = LayoutParams(width / 5, width / 5)
            for (i in 0..<OBJECT_SIZE) {
                img[i]!!.layoutParams = lp
            }
            animateView(true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        for (i in 0..<OBJECT_SIZE) {
            if (animator[i]!!.isRunning) {
                animator[i]!!.removeAllListeners()
                animator[i]!!.end()
                animator[i]!!.cancel()
            }
        }
    }

    private fun animateView(show: Boolean) {
        animator = arrayOfNulls(OBJECT_SIZE)
        for (i in 0..<OBJECT_SIZE) {
            val A = PropertyValuesHolder.ofFloat("alpha", 0.2f)
            val B = PropertyValuesHolder.ofFloat("alpha", 1.0f)
            val alpha = if (show) A else B
            animator[i] = ObjectAnimator.ofPropertyValuesHolder(img[i], alpha)
            animator[i]!!.repeatCount = 0
            animator[i]!!.repeatMode = ValueAnimator.REVERSE
            animator[i]!!.setDuration(DURATION.toLong())
            animator[i]!!.startDelay = (DURATION * i).toLong()
            animator[i]!!.start()
        }
        animator[OBJECT_SIZE - 1]!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animateView(!show)
            }
        })
    }
}
