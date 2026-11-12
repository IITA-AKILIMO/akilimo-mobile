package com.akilimo.mobile.data.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountPromptStateTest {

    @Test
    fun `the prompt is due once per launch while nobody is signed in`() {
        val state = AccountPromptState()

        assertTrue(state.consumeIfDue(signedIn = false))
        assertFalse("a second ask in the same launch", state.consumeIfDue(signedIn = false))
    }

    @Test
    fun `a signed-in farmer is never prompted`() {
        val state = AccountPromptState()

        assertFalse(state.consumeIfDue(signedIn = true))
        assertFalse(state.consumeIfDue(signedIn = true))
    }

    @Test
    fun `a new launch prompts again`() {
        assertTrue(AccountPromptState().consumeIfDue(signedIn = false))
        assertTrue(AccountPromptState().consumeIfDue(signedIn = false))
    }
}
