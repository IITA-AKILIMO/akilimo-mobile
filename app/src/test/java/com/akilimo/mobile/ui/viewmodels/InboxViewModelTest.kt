package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.entities.InboxMessage
import com.akilimo.mobile.notifications.MessageSync
import com.akilimo.mobile.repos.InboxRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val repo: InboxRepo = mockk(relaxed = true)
    private val messageSync: MessageSync = mockk(relaxed = true)
    private val messages = MutableStateFlow<List<InboxMessage>>(emptyList())
    private val unread = MutableStateFlow(0)

    private fun message(id: String, read: Boolean = false) =
        InboxMessage(id = id, type = "advice", title = "T$id", body = "B$id", target = null, receivedAt = 1L, isRead = read)

    @Before
    fun setUp() {
        clearAllMocks()
        every { repo.messages() } returns messages
        every { repo.unreadCount() } returns unread
    }

    // Built per test, after the flows are set up, because init collects them straight away
    private fun buildViewModel() = InboxViewModel(repo, messageSync)

    @Test
    fun `state follows the stored messages and unread count`() = runTest {
        val viewModel = buildViewModel()
        messages.value = listOf(message("1"), message("2", read = true))
        unread.value = 1
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.messages?.size)
        assertEquals(1, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `creating the screen state fetches what the phone missed`() = runTest {
        buildViewModel()
        advanceUntilIdle()

        coVerify { messageSync.sync() }
    }

    @Test
    fun `opening a message expands it and marks it read`() = runTest {
        val viewModel = buildViewModel()
        viewModel.toggle("1")
        advanceUntilIdle()

        assertEquals("1", viewModel.uiState.value.expandedId)
        coVerify { repo.markRead("1") }
    }

    @Test
    fun `tapping an open message closes it without marking again`() = runTest {
        val viewModel = buildViewModel()
        viewModel.toggle("1")
        viewModel.toggle("1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.expandedId)
        coVerify(exactly = 1) { repo.markRead("1") }
    }

    @Test
    fun `deleting the open message closes it and removes it`() = runTest {
        val viewModel = buildViewModel()
        viewModel.toggle("1")
        viewModel.delete("1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.expandedId)
        coVerify { repo.delete("1") }
    }

    @Test
    fun `mark all read and delete all go to the repository`() = runTest {
        val viewModel = buildViewModel()
        viewModel.markAllRead()
        viewModel.deleteAll()
        advanceUntilIdle()

        coVerify { repo.markAllRead() }
        coVerify { repo.deleteAll() }
    }
}
