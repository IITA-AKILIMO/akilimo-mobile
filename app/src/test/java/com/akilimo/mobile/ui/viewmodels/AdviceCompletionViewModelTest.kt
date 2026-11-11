package com.akilimo.mobile.ui.viewmodels

import app.cash.turbine.test
import com.akilimo.mobile.entities.AdviceCompletion
import com.akilimo.mobile.dto.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdviceCompletionViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val repo: AdviceCompletionRepo = mockk(relaxed = true)
    private lateinit var viewModel: AdviceCompletionViewModel

    @Before
    fun setUp() {
        every { repo.getAllCompletions() } returns flowOf(emptyMap())
        viewModel = AdviceCompletionViewModel(repo)
    }

    @Test
    fun `completions emits empty map when repo has no data`() = runTest {
        viewModel.completions.test {
            assertEquals(emptyMap<String, AdviceCompletion>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `completions exposes the flow from repo`() = runTest {
        val completion = AdviceCompletion(
            taskName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
            stepStatus = EnumStepStatus.COMPLETED
        )
        val expected = mapOf(EnumAdviceTask.AVAILABLE_FERTILIZERS.name to completion)
        every { repo.getAllCompletions() } returns flowOf(expected)

        val vm = AdviceCompletionViewModel(repo)
        vm.completions.test {
            assertEquals(expected, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateStatus delegates to repository`() = runTest {
        val dto = AdviceCompletionDto(
            taskName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
            stepStatus = EnumStepStatus.COMPLETED
        )

        viewModel.updateStatus(dto)
        advanceUntilIdle()

        coVerify { repo.updateStatus(dto) }
    }

    @Test
    fun `updateStatus with IN_PROGRESS status delegates to repository`() = runTest {
        val dto = AdviceCompletionDto(
            taskName = EnumAdviceTask.PLANTING_AND_HARVEST,
            stepStatus = EnumStepStatus.IN_PROGRESS
        )

        viewModel.updateStatus(dto)
        advanceUntilIdle()

        coVerify { repo.updateStatus(dto) }
    }

    @Test
    fun `incompleteTasks lists every task not COMPLETED in order`() {
        val items = listOf(
            EnumAdviceTask.PLANTING_AND_HARVEST to EnumStepStatus.COMPLETED,
            EnumAdviceTask.AVAILABLE_FERTILIZERS to EnumStepStatus.IN_PROGRESS,
            EnumAdviceTask.INVESTMENT_AMOUNT to EnumStepStatus.NOT_STARTED,
        )

        assertEquals(
            listOf(EnumAdviceTask.AVAILABLE_FERTILIZERS, EnumAdviceTask.INVESTMENT_AMOUNT),
            AdviceCompletionViewModel.incompleteTasks(items)
        )
    }

    @Test
    fun `incompleteTasks is empty when all tasks are completed`() {
        val items = listOf(EnumAdviceTask.PLANTING_AND_HARVEST to EnumStepStatus.COMPLETED)

        assertEquals(emptyList<EnumAdviceTask>(), AdviceCompletionViewModel.incompleteTasks(items))
    }

    @Test
    fun `progress is the completed share of tasks and zero when there are none`() {
        val items = listOf(
            EnumAdviceTask.PLANTING_AND_HARVEST to EnumStepStatus.COMPLETED,
            EnumAdviceTask.AVAILABLE_FERTILIZERS to EnumStepStatus.IN_PROGRESS,
            EnumAdviceTask.INVESTMENT_AMOUNT to EnumStepStatus.NOT_STARTED,
            EnumAdviceTask.CURRENT_CASSAVA_YIELD to EnumStepStatus.COMPLETED,
        )

        assertEquals(0.5f, AdviceCompletionViewModel.progress(items), 0.0001f)
        assertEquals(0f, AdviceCompletionViewModel.progress(emptyList()), 0f)
    }

    @Test
    fun `multiple updateStatus calls are each forwarded to the repo`() = runTest {
        val tasks = listOf(
            EnumAdviceTask.AVAILABLE_FERTILIZERS to EnumStepStatus.COMPLETED,
            EnumAdviceTask.CASSAVA_MARKET_OUTLET to EnumStepStatus.IN_PROGRESS,
            EnumAdviceTask.PLANTING_AND_HARVEST to EnumStepStatus.NOT_STARTED
        )

        tasks.forEach { (task, status) ->
            viewModel.updateStatus(AdviceCompletionDto(taskName = task, stepStatus = status))
        }
        advanceUntilIdle()

        tasks.forEach { (task, status) ->
            coVerify { repo.updateStatus(AdviceCompletionDto(taskName = task, stepStatus = status)) }
        }
    }
}
