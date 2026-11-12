package com.akilimo.mobile.data.account

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/** How long a farmer who dismisses the start-up offer is left alone. */
enum class PromptSnooze(val millis: Long) {
    DAY(TimeUnit.HOURS.toMillis(24)),
    WEEK(TimeUnit.DAYS.toMillis(7)),
}

/** Where the "ask me again after" time is kept; a seam so the logic is tested without a phone. */
interface PromptStorage {
    fun snoozedUntil(): Long
    fun setSnoozedUntil(epochMillis: Long)
}

class PrefsPromptStorage(private val context: Context) : PromptStorage {
    private val prefs get() = context.getSharedPreferences("account_prompt", Context.MODE_PRIVATE)
    override fun snoozedUntil(): Long = prefs.getLong(KEY, 0L)
    override fun setSnoozedUntil(epochMillis: Long) = prefs.edit().putLong(KEY, epochMillis).apply()

    private companion object {
        const val KEY = "snoozedUntil"
    }
}

/**
 * Decides when the app offers an account after it starts: at most once per launch, only while nobody is signed in, and not
 * while the farmer has asked to be left alone for a while ("remind me tomorrow", "in a week"). A farmer who is signed in
 * is never asked.
 */
@Singleton
class AccountPromptState(
    private val storage: PromptStorage,
    private val clock: () -> Long,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(PrefsPromptStorage(context), System::currentTimeMillis)

    private val consumed = AtomicBoolean(false)

    /** True the first time it is asked in this launch with nobody signed in and no snooze running; false after that. */
    fun consumeIfDue(signedIn: Boolean): Boolean =
        !signedIn && clock() >= storage.snoozedUntil() && consumed.compareAndSet(false, true)

    /** Leaves the farmer alone for [snooze]; the prompt is not shown again, even after a restart, until it has passed. */
    fun snooze(snooze: PromptSnooze) = storage.setSnoozedUntil(clock() + snooze.millis)
}
