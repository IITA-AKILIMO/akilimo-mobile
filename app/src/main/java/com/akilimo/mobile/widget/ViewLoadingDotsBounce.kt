package com.akilimo.mobile.widget

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
 * To change dot color you can use :
 * android:background="@color/exampleColor"
 */
class ViewLoadingDotsBounce : LinearLayout {

    private var context: Context? = null
    private lateinit var img: Array<ImageView?>
    private val circle = GradientDrawable()
    private var animator: Array<ObjectAnimator?>? = null

    private var onLayoutReach: Boolean = false

    companion object {
        private const val OBJECT_SIZE = 4
        private const val POST_DIV = 6
        private const val DURATION = 500
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context

        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

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
        for (i in 0 until OBJECT_SIZE) {
            rel[i] = LinearLayout(context)
            rel[i]!!.gravity = Gravity.CENTER
            rel[i]!!.layoutParams = layoutParams2
            img[i] = ImageView(context)
            img[i]!!.background = circle
            rel[i]!!.addView(img[i])
            addView(rel[i])
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!onLayoutReach) {
            onLayoutReach = true
            val lp = LayoutParams(width / 5, width / 5)
            for (i in 0 until OBJECT_SIZE) {
                img[i]?.layoutParams = lp
            }
            animateView()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.forEach { anim ->
            anim?.let {
                if (it.isRunning) {
                    it.removeAllListeners()
                    it.end()
                    it.cancel()
                }
            }
        }
    }

    private fun animateView() {
        animator = arrayOfNulls(OBJECT_SIZE)
        for (i in 0 until OBJECT_SIZE) {
            img[i]?.translationY = (height / POST_DIV).toFloat()
            val yCord = PropertyValuesHolder.ofFloat(TRANSLATION_Y, (-height / POST_DIV).toFloat())
            val xCord = PropertyValuesHolder.ofFloat(TRANSLATION_X, 0f)
            animator!![i] = ObjectAnimator.ofPropertyValuesHolder(img[i], xCord, yCord).apply {
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                duration = DURATION.toLong()
                startDelay = ((DURATION / 3) * i).toLong()
                start()
            }
        }
    }
}
