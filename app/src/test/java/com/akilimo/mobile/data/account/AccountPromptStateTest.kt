package com.akilimo.mobile.data.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class FakePromptStorage(var until: Long = 0L) : PromptStorage {
    override fun snoozedUntil(): Long = until
    override fun setSnoozedUntil(epochMillis: Long) { until = epochMillis }
}

class AccountPromptStateTest {

    private var now = 1_000_000L
    private val storage = FakePromptStorage()

    private fun state() = AccountPromptState(storage) { now }

    @Test
    fun `the prompt is due once per launch while nobody is signed in`() {
        val state = state()

        assertTrue(state.consumeIfDue(signedIn = false))
        assertFalse("a second ask in the same launch", state.consumeIfDue(signedIn = false))
    }

    @Test
    fun `a signed-in farmer is never prompted`() {
        val state = state()

        assertFalse(state.consumeIfDue(signedIn = true))
        assertFalse(state.consumeIfDue(signedIn = true))
    }

    @Test
    fun `a new launch prompts again when nothing was snoozed`() {
        assertTrue(state().consumeIfDue(signedIn = false))
        assertTrue(state().consumeIfDue(signedIn = false))
    }

    @Test
    fun `remind me tomorrow keeps the prompt away for 24 hours, across restarts`() {
        state().snooze(PromptSnooze.DAY)

        now += TimeUnit.HOURS.toMillis(23)
        assertFalse("still snoozed after a restart", state().consumeIfDue(signedIn = false))

        now += TimeUnit.HOURS.toMillis(2)
        assertTrue("due again once the day has passed", state().consumeIfDue(signedIn = false))
    }

    @Test
    fun `remind me in a week keeps it away for seven days`() {
        state().snooze(PromptSnooze.WEEK)

        now += TimeUnit.DAYS.toMillis(6)
        assertFalse(state().consumeIfDue(signedIn = false))

        now += TimeUnit.DAYS.toMillis(2)
        assertTrue(state().consumeIfDue(signedIn = false))
    }

    @Test
    fun `an untouched snooze does not use up the launch`() {
        val state = state()
        state.snooze(PromptSnooze.DAY)

        assertFalse(state.consumeIfDue(signedIn = false))

        now += TimeUnit.HOURS.toMillis(25)
        assertTrue("the first real ask after the snooze still shows", state.consumeIfDue(signedIn = false))
    }
}
