package com.akilimo.mobile.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverageAreaTest {

    // A 10x10 square from (0,0) to (10,10) with a 2x2 hole from (4,4) to (6,6); [lon, lat]
    private val squareWithHole = """
        {"type":"FeatureCollection","features":[{"type":"Feature","properties":{"n_cells":96},
         "geometry":{"type":"Polygon","coordinates":[
           [[0,0],[10,0],[10,10],[0,10],[0,0]],
           [[4,4],[6,4],[6,6],[4,6],[4,4]]
         ]}}]}
    """.trimIndent()

    private val area = CoverageArea.parse(squareWithHole)!!

    @Test
    fun `point inside the outline is covered`() {
        assertTrue(area.contains(lat = 2.0, lon = 2.0))
    }

    @Test
    fun `point outside the bounding box is not covered`() {
        assertFalse(area.contains(lat = 11.0, lon = 5.0))
    }

    @Test
    fun `point inside a hole is not covered`() {
        assertFalse(area.contains(lat = 5.0, lon = 5.0))
    }

    @Test
    fun `point on the outer edge counts as covered`() {
        assertTrue(area.contains(lat = 0.0, lon = 5.0))
    }

    @Test
    fun `point on a hole edge counts as covered`() {
        assertTrue(area.contains(lat = 4.0, lon = 5.0))
    }

    @Test
    fun `bounding box and centre come from the outline`() {
        assertEquals(0.0, area.minLon, 0.0)
        assertEquals(10.0, area.maxLat, 0.0)
        assertEquals(5.0, area.centerLat, 0.0)
        assertEquals(5.0, area.centerLon, 0.0)
    }

    @Test
    fun `multipolygon covers each part but not the gap between`() {
        val multi = CoverageArea.parse(
            """{"type":"MultiPolygon","coordinates":[
                [[[0,0],[1,0],[1,1],[0,1],[0,0]]],
                [[[3,3],[4,3],[4,4],[3,4],[3,3]]]
            ]}"""
        )
        assertNotNull(multi)
        assertTrue(multi!!.contains(lat = 0.5, lon = 0.5))
        assertTrue(multi.contains(lat = 3.5, lon = 3.5))
        assertFalse(multi.contains(lat = 2.0, lon = 2.0))
    }

    @Test
    fun `text without a polygon is rejected`() {
        assertNull(CoverageArea.parse("""{"type":"FeatureCollection","features":[]}"""))
        assertNull(CoverageArea.parse("""{"type":"Point","coordinates":[1,2]}"""))
        assertNull(CoverageArea.parse("not json"))
    }
}
