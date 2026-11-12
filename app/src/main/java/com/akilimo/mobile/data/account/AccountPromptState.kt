package com.akilimo.mobile.data.account

import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides when the app offers an account after it starts: once per launch, and only while nobody is signed in.
 * A farmer who is signed in is never asked, and one who dismisses it is not asked again until the app is started again.
 */
@Singleton
class AccountPromptState @Inject constructor() {
    private val consumed = AtomicBoolean(false)

    /** True the first time it is asked in this launch with nobody signed in; false from then on. */
    fun consumeIfDue(signedIn: Boolean): Boolean = !signedIn && consumed.compareAndSet(false, true)
}
