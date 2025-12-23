package com.akilimo.mobile.ui.components

import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.CollapsibleToolbarBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlin.math.abs

/**
 * A fluent helper for configuring a reusable CollapsingToolbarLayout.
 *
 * Supports title, banner image, scrim color, back button, collapse listener,
 * and dynamic title visibility based on scroll state.
 *
 * Call [build] as the final step to apply all configurations.
 */
class CollapsibleToolbarHelper(
    private val activity: AppCompatActivity,
    private val binding: CollapsibleToolbarBinding
) {

    // Properties + isSet flags
    private var title: String? = null
    private var isTitleSet = false


    private var bannerResId: Int = R.drawable.bg_header
    private var isBannerSet = false

    private var scrimColor: Int? = null
    private var isScrimColorSet = false

    private var expandedTitleColor: Int? = null
    private var isExpandedTitleColorSet = false

    private var collapsedTitleColor: Int? = null
    private var isCollapsedTitleColorSet = false

    private var showBack: Boolean = false
    private var isBackButtonSet = false

    private var titleEnabled: Boolean = false
    private var isTitleEnabledSet = false

    private var bannerScaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP
    private var isBannerScaleTypeSet = false

    private var scrollFlags: Int? = null
    private var isScrollFlagsSet = false

    private var onCollapse: ((collapsed: Boolean) -> Unit)? = null
    private var isOnCollapseSet = false

    private var autoToggleTitle: Boolean = false

    // Fluent setters
    fun setTitle(title: String): CollapsibleToolbarHelper {
        this.title = title
        this.isTitleSet = true
        return this
    }


    fun setBanner(@DrawableRes bannerResId: Int): CollapsibleToolbarHelper {
        this.bannerResId = bannerResId
        this.isBannerSet = true
        return this
    }

    fun setBannerScaleType(scaleType: ImageView.ScaleType): CollapsibleToolbarHelper {
        this.bannerScaleType = scaleType
        this.isBannerScaleTypeSet = true
        return this
    }

    fun setScrimColor(@ColorInt color: Int): CollapsibleToolbarHelper {
        this.scrimColor = color
        this.isScrimColorSet = true
        return this
    }

    fun setExpandedTitleColor(@ColorInt color: Int): CollapsibleToolbarHelper {
        this.expandedTitleColor = color
        this.isExpandedTitleColorSet = true
        return this
    }

    fun setCollapsedTitleColor(@ColorInt color: Int): CollapsibleToolbarHelper {
        this.collapsedTitleColor = color
        this.isCollapsedTitleColorSet = true
        return this
    }

    fun enableTitle(enabled: Boolean): CollapsibleToolbarHelper {
        this.titleEnabled = enabled
        this.isTitleEnabledSet = true
        return this
    }

    fun showBackButton(enabled: Boolean): CollapsibleToolbarHelper {
        this.showBack = enabled
        this.isBackButtonSet = true
        return this
    }

    fun setScrollFlags(flags: Int): CollapsibleToolbarHelper {
        this.scrollFlags = flags
        this.isScrollFlagsSet = true
        return this
    }

    fun onCollapseChanged(listener: (collapsed: Boolean) -> Unit): CollapsibleToolbarHelper {
        this.onCollapse = listener
        this.isOnCollapseSet = true
        return this
    }

    fun autoToggleTitleOnCollapse(): CollapsibleToolbarHelper {
        this.autoToggleTitle = true
        return this
    }

    // Build method
    fun build(): CollapsibleToolbarHelper {
        // Toolbar setup
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar?.setDisplayShowTitleEnabled(isTitleEnabledSet)
        if (isBackButtonSet) {
            activity.supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
        }

        // Collapse listener
        if (autoToggleTitle || isOnCollapseSet) {
            binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val collapsed = abs(verticalOffset) >= appBarLayout.totalScrollRange
                if (autoToggleTitle) {
                    binding.collapsingToolbar.isTitleEnabled = collapsed
                }
                onCollapse?.invoke(collapsed)
            }
        } else if (isTitleEnabledSet) {
            binding.collapsingToolbar.isTitleEnabled = titleEnabled
        }

        // Title
        if (isTitleSet) {
            binding.collapsingToolbar.title = title
        }
        // Banner
        if (isBannerSet) {
            binding.bannerImage.setImageResource(bannerResId)
        }
        if (isBannerScaleTypeSet) {
            binding.bannerImage.scaleType = bannerScaleType
        }

        // Scrim color
        if (isScrimColorSet) {
            binding.collapsingToolbar.setContentScrimColor(scrimColor!!)
        }

        // Title colors
        if (isExpandedTitleColorSet) {
            binding.collapsingToolbar.setExpandedTitleColor(expandedTitleColor!!)
        }
        if (isCollapsedTitleColorSet) {
            binding.collapsingToolbar.setCollapsedTitleTextColor(collapsedTitleColor!!)
        }

        // Scroll flags
        if (isScrollFlagsSet) {
            val params = binding.collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
            params.scrollFlags = scrollFlags!!
        }

        return this
    }

    // Accessors
    fun getToolbar(): Toolbar = binding.toolbar
    fun getCollapsingLayout(): CollapsingToolbarLayout = binding.collapsingToolbar
    fun getAppBarLayout(): AppBarLayout = binding.appBar
}
