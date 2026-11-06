package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.repos.CoverageRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import com.akilimo.mobile.utils.CoverageArea
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationPickerViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val coverageRepo: CoverageRepo = mockk(relaxed = true)
    private val context: android.content.Context = mockk(relaxed = true)

    // Square from lon 3..4, lat 7..8
    private val geoJson = """{"type":"Polygon","coordinates":[[[3,7],[4,7],[4,8],[3,8],[3,7]]]}"""
    private val coverage = CoverageRepo.Coverage("NG", geoJson, CoverageArea.parse(geoJson)!!)

    @Before
    fun setUp() {
        clearAllMocks()
    }

    private fun buildViewModel() = LocationPickerViewModel(appSettings, coverageRepo, context)

    @Test
    fun `loading coverage exposes the GeoJSON for the overlay`() = runTest {
        coEvery { coverageRepo.get("NG") } returns coverage

        val viewModel = buildViewModel()
        viewModel.loadCoverage("NG")
        advanceUntilIdle()

        assertEquals(geoJson, viewModel.uiState.value.coverageGeoJson)
        assertNotNull(viewModel.uiState.value.coverageArea)
        assertFalse(viewModel.uiState.value.isLoadingCoverage)
        assertFalse(viewModel.uiState.value.coverageUnavailable)
    }

    @Test
    fun `points inside and outside the area are classified`() = runTest {
        coEvery { coverageRepo.get("NG") } returns coverage
        val viewModel = buildViewModel()
        viewModel.loadCoverage("NG")
        advanceUntilIdle()

        viewModel.checkCoverage(lat = 7.5, lon = 3.5)
        assertEquals(EnumCoverageStatus.INSIDE, viewModel.uiState.value.coverageStatus)

        viewModel.checkCoverage(lat = 9.0, lon = 3.5)
        assertEquals(EnumCoverageStatus.OUTSIDE, viewModel.uiState.value.coverageStatus)
    }

    @Test
    fun `a point picked before the area arrives is checked once it loads`() = runTest {
        coEvery { coverageRepo.get("NG") } returns coverage
        val viewModel = buildViewModel()

        viewModel.checkCoverage(lat = 9.0, lon = 3.5)
        assertEquals(EnumCoverageStatus.UNKNOWN, viewModel.uiState.value.coverageStatus)

        viewModel.loadCoverage("NG")
        advanceUntilIdle()
        assertEquals(EnumCoverageStatus.OUTSIDE, viewModel.uiState.value.coverageStatus)
    }

    @Test
    fun `no known area leaves the status unknown`() = runTest {
        coEvery { coverageRepo.get("BI") } returns null
        val viewModel = buildViewModel()
        viewModel.loadCoverage("BI")
        advanceUntilIdle()

        viewModel.checkCoverage(lat = -3.4, lon = 29.9)
        assertNull(viewModel.uiState.value.coverageGeoJson)
        assertEquals(EnumCoverageStatus.UNKNOWN, viewModel.uiState.value.coverageStatus)
    }

    @Test
    fun `a missing area is reported as unavailable instead of failing silently`() = runTest {
        coEvery { coverageRepo.get("BI") } returns null
        val viewModel = buildViewModel()
        viewModel.loadCoverage("BI")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.coverageUnavailable)
        assertFalse(viewModel.uiState.value.isLoadingCoverage)
    }

    @Test
    fun `a blank country code loads nothing`() = runTest {
        val viewModel = buildViewModel()
        viewModel.loadCoverage("")
        advanceUntilIdle()

        coVerify(exactly = 0) { coverageRepo.get(any()) }
        assertFalse(viewModel.uiState.value.coverageUnavailable)
    }

    @Test
    fun `permission refusal is surfaced and can be dismissed`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onPermissionDenied()

        assertEquals(
            LocationPickerViewModel.LocationProblem.PERMISSION_DENIED,
            viewModel.uiState.value.locationProblem,
        )
        viewModel.dismissLocationProblem()
        assertNull(viewModel.uiState.value.locationProblem)
    }
}
