package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.InboxMessageDao
import com.akilimo.mobile.entities.InboxMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InboxRepoTest {

    private val dao: InboxMessageDao = mockk(relaxed = true)
    private val repo = InboxRepo(dao)
    private val message = InboxMessage("1", "advice", "T", "B", null, 1L)

    @Test
    fun `a new message is stored and the history is trimmed`() = runTest {
        coEvery { dao.insert(message) } returns 7L

        repo.save(message)

        coVerify { dao.insert(message) }
        coVerify { dao.prune(InboxRepo.MAX_MESSAGES) }
    }

    @Test
    fun `a duplicate message changes nothing`() = runTest {
        coEvery { dao.insert(message) } returns -1L

        repo.save(message)

        coVerify(exactly = 0) { dao.prune(any()) }
    }
}
