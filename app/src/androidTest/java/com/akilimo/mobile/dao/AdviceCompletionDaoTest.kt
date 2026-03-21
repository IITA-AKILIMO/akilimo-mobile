package com.akilimo.mobile.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.akilimo.mobile.database.AppDatabase
import com.akilimo.mobile.entities.AdviceCompletion
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented DAO tests using an in-memory Room database.
 *
 * Guards the [AdviceCompletionDao] contract which drives the step-status
 * completion indicators shown on recommendation sub-screens.
 */
@RunWith(AndroidJUnit4::class)
class AdviceCompletionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AdviceCompletionDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.adviceCompletionDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun getAllFlow_emitsEmptyListInitially() = runBlocking {
        val results = dao.getAllFlow().first()
        assertEquals(0, results.size)
    }

    @Test
    fun upsert_insertsRecord_visibleViaFlow() = runBlocking {
        val entity = AdviceCompletion(
            taskName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
            stepStatus = EnumStepStatus.COMPLETED
        )

        dao.upsert(entity)

        val results = dao.getAllFlow().first()
        assertEquals(1, results.size)
        assertEquals(EnumStepStatus.COMPLETED, results.first().stepStatus)
    }

    @Test
    fun upsert_updatesExistingRecord() = runBlocking {
        dao.upsert(
            AdviceCompletion(
                taskName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
                stepStatus = EnumStepStatus.IN_PROGRESS
            )
        )

        dao.upsert(
            AdviceCompletion(
                taskName = EnumAdviceTask.AVAILABLE_FERTILIZERS,
                stepStatus = EnumStepStatus.COMPLETED
            )
        )

        val results = dao.getAllFlow().first()
        assertEquals(1, results.size)                          // still one row
        assertEquals(EnumStepStatus.COMPLETED, results.first().stepStatus)
    }

    @Test
    fun upsert_multipleDistinctTasks_allPersisted() = runBlocking {
        dao.upsert(AdviceCompletion(EnumAdviceTask.AVAILABLE_FERTILIZERS, EnumStepStatus.COMPLETED))
        dao.upsert(AdviceCompletion(EnumAdviceTask.PLANTING_AND_HARVEST, EnumStepStatus.IN_PROGRESS))
        dao.upsert(AdviceCompletion(EnumAdviceTask.CASSAVA_MARKET_OUTLET, EnumStepStatus.NOT_STARTED))

        val results = dao.getAllFlow().first()
        assertEquals(3, results.size)
    }

    @Test
    fun getAdviceByTask_returnsCorrectRecord() = runBlocking {
        dao.upsert(AdviceCompletion(EnumAdviceTask.AVAILABLE_FERTILIZERS, EnumStepStatus.COMPLETED))
        dao.upsert(AdviceCompletion(EnumAdviceTask.PLANTING_AND_HARVEST, EnumStepStatus.IN_PROGRESS))

        val result = dao.getAdviceByTask(EnumAdviceTask.PLANTING_AND_HARVEST)

        assertEquals(EnumAdviceTask.PLANTING_AND_HARVEST, result?.taskName)
        assertEquals(EnumStepStatus.IN_PROGRESS, result?.stepStatus)
    }

    @Test
    fun getAdviceByTask_returnsNullForMissingTask() = runBlocking {
        val result = dao.getAdviceByTask(EnumAdviceTask.AVAILABLE_FERTILIZERS)
        assertNull(result)
    }

    @Test
    fun delete_removesOnlyTargetTask() = runBlocking {
        dao.upsert(AdviceCompletion(EnumAdviceTask.AVAILABLE_FERTILIZERS, EnumStepStatus.COMPLETED))
        dao.upsert(AdviceCompletion(EnumAdviceTask.PLANTING_AND_HARVEST, EnumStepStatus.COMPLETED))

        dao.delete(EnumAdviceTask.AVAILABLE_FERTILIZERS)

        val results = dao.getAllFlow().first()
        assertEquals(1, results.size)
        assertEquals(EnumAdviceTask.PLANTING_AND_HARVEST, results.first().taskName)
    }

    @Test
    fun delete_nonExistentTask_doesNotThrow() = runBlocking {
        dao.delete(EnumAdviceTask.AVAILABLE_FERTILIZERS) // no-op, must not crash

        val results = dao.getAllFlow().first()
        assertEquals(0, results.size)
    }
}
