package com.akilimo.mobile.ui.components

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.akilimo.mobile.R

/**
 * A fluent helper for configuring a standard non-collapsible [Toolbar].
 *
 * Supports title, back button, navigation icon, and menu inflation.
 * Call [build] as the final step to apply all configurations.
 *
 * ### Usage Example:
 * ```
 * ToolbarHelper(this, binding.toolbar)
 *     .setTitle("Akilimo Dashboard")
 *     .setNavigationIcon(R.drawable.ic_back)
 *     .showBackButton(true)
 *     .inflateMenu(R.menu.toolbar_menu) { item ->
 *         when (item.itemId) {
 *             R.id.action_refresh -> refreshData()
 *             R.id.action_settings -> openSettings()
 *         }
 *     }
 *     .onNavigationClick { finish() }
 *     .build()
 * ```
 *
 * @param activity The hosting [AppCompatActivity]
 * @param toolbar The [Toolbar] to configure
 */
class ToolbarHelper(
    private val activity: AppCompatActivity,
    private val toolbar: Toolbar
) {

    private var title: String? = null
    private var navigationIconRes: Int? = null
    private var showBack: Boolean = true
    private var onNavClick: (() -> Unit)? = null
    private var menuResId: Int? = null
    private var onMenuItemClick: ((MenuItem) -> Unit)? = null

    /** Sets the toolbar title. */
    fun setTitle(title: String): ToolbarHelper {
        this.title = title
        return this
    }

    /** Sets a custom navigation icon. */
    fun setNavigationIcon(iconResId: Int): ToolbarHelper {
        this.navigationIconRes = iconResId
        return this
    }

    /** Enables or disables the default back button. */
    fun showBackButton(enabled: Boolean): ToolbarHelper {
        this.showBack = enabled
        return this
    }

    /** Registers a custom navigation click listener. */
    fun onNavigationClick(listener: () -> Unit): ToolbarHelper {
        this.onNavClick = listener
        return this
    }

    /** Inflates a menu resource and handles item clicks. */
    fun inflateMenu(@MenuRes menuResId: Int, onClick: (MenuItem) -> Unit): ToolbarHelper {
        this.menuResId = menuResId
        this.onMenuItemClick = onClick
        return this
    }

    /**
     * Applies all configurations to the toolbar.
     * Must be called last.
     */
    fun build(): ToolbarHelper {
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        // Title
        title?.let { actionBar?.title = it }
        // Back button or custom nav
        if (showBack) {
            actionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationIcon(navigationIconRes ?: R.drawable.ic_arrow_back)
            toolbar.setNavigationOnClickListener { onNavClick?.invoke() }
        }
        navigationIconRes?.let { toolbar.setNavigationIcon(it) }
        // Inflate menu *after* navigation setup
        menuResId?.let { resId ->
            toolbar.post {
                toolbar.inflateMenu(resId)
                toolbar.setOnMenuItemClickListener { item ->
                    onMenuItemClick?.invoke(item)
                    true
                }
            }
        }
        return this
    }


    /** Returns the configured [Toolbar]. */
    fun getToolbar(): Toolbar = toolbar
}
