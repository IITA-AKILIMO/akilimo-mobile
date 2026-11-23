package com.akilimo.mobile.ui.viewmodels

import app.cash.turbine.test
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.notifications.PushTopicSync
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val topicSync: PushTopicSync = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        every { appSettings.pushEnabledFlow } returns flowOf(false)
        every { appSettings.pushPromptAnsweredFlow } returns flowOf(false)
    }

    private fun buildViewModel() = NotificationSettingsViewModel(appSettings, topicSync)

    @Test
    fun `fresh install is off and shows the prompt once`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertFalse(enabled)
            assertTrue(showPrompt)
        }
    }

    @Test
    fun `prompt is hidden once answered or once notifications are on`() = runTest {
        every { appSettings.pushPromptAnsweredFlow } returns flowOf(true)
        assertFalse(buildViewModel().uiState.value.showPrompt)

        every { appSettings.pushPromptAnsweredFlow } returns flowOf(false)
        every { appSettings.pushEnabledFlow } returns flowOf(true)
        assertFalse(buildViewModel().uiState.value.showPrompt)
    }

    @Test
    fun `accepting the prompt marks it answered and requests the permission`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onPromptAccepted()
            advanceUntilIdle()

            coVerify { appSettings.setPushPromptAnswered(true) }
            assertTrue(awaitItem() == NotificationSettingsViewModel.Effect.RequestPermission)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissing the prompt marks it answered and does not enable`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onPromptDismissed()
        advanceUntilIdle()

        coVerify { appSettings.setPushPromptAnswered(true) }
        coVerify(exactly = 0) { appSettings.setPushEnabled(true) }
    }

    @Test
    fun `granted permission enables and syncs topics`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        coVerifyOrder {
            appSettings.setPushEnabled(true)
            topicSync.sync()
        }
    }

    @Test
    fun `denied permission leaves notifications off`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onPermissionResult(false)
        advanceUntilIdle()

        coVerify { appSettings.setPushEnabled(false) }
        coVerify(exactly = 0) { appSettings.setPushEnabled(true) }
    }

    @Test
    fun `turning the switch on asks for permission before enabling`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.setEnabled(true)
            advanceUntilIdle()

            assertTrue(awaitItem() == NotificationSettingsViewModel.Effect.RequestPermission)
            coVerify(exactly = 0) { appSettings.setPushEnabled(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `turning the switch off disables and unsubscribes`() = runTest {
        val viewModel = buildViewModel()
        viewModel.setEnabled(false)
        advanceUntilIdle()

        coVerifyOrder {
            appSettings.setPushEnabled(false)
            topicSync.sync()
        }
    }

    @Test
    fun `settings row is hidden while android allows notifications`() = runTest {
        every { appSettings.pushEnabledFlow } returns flowOf(true)
        val viewModel = buildViewModel()
        viewModel.onSystemAllowedChanged(true)

        assertFalse(viewModel.uiState.value.showAllowInSettings)
    }

    @Test
    fun `settings row is shown when notifications are on but android blocks them`() = runTest {
        every { appSettings.pushEnabledFlow } returns flowOf(true)
        val viewModel = buildViewModel()
        viewModel.onSystemAllowedChanged(false)

        assertTrue(viewModel.uiState.value.showAllowInSettings)
    }

    @Test
    fun `settings row is shown after the permission was refused`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onSystemAllowedChanged(false)
        assertFalse(viewModel.uiState.value.showAllowInSettings)

        viewModel.onPermissionResult(false)

        assertTrue(viewModel.uiState.value.showAllowInSettings)
    }

    @Test
    fun `settings row goes away once the farmer allows notifications in phone settings`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onSystemAllowedChanged(false)
        viewModel.onPermissionResult(false)

        viewModel.onSystemAllowedChanged(true)

        assertFalse(viewModel.uiState.value.showAllowInSettings)
        assertFalse(viewModel.uiState.value.permissionDenied)
    }
}
