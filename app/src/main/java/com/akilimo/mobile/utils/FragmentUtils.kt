package com.akilimo.mobile.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Safely shows a DialogFragment, preventing duplicate instances with the same tag.
 *
 * If a fragment with the given tag already exists, it's removed before showing the new one.
 * Adds the transaction to the back stack for proper dismissal with the back button.
 *
 * @param fragmentManager The FragmentManager for managing fragments.
 * @param dialogFragment The DialogFragment to display.
 * @param tag A unique tag to identify the DialogFragment.
 *
 * Example:
 * `showDialogFragmentSafely(supportFragmentManager, MyDialogFragment(), "MyDialogTag")`
 */
fun showDialogFragmentSafely(
    fragmentManager: FragmentManager,
    dialogFragment: DialogFragment,
    tag: String
) {
    fragmentManager.beginTransaction().apply {
        fragmentManager.findFragmentByTag(tag)?.let { remove(it) }
        addToBackStack(null)
    }.commit()

    dialogFragment.show(fragmentManager, tag)
}