package com.akilimo.mobile.utils

import com.squareup.moshi.Moshi
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * The area where AKILIMO can compute a recommendation for one country, parsed from the GeoJSON
 * served by `GET /v1/coverage/{country}`.
 *
 * The point-in-polygon rules mirror the API's `App\Support\GeoJson` (ray casting, holes, a point
 * exactly on an edge counts as inside) so the app and the server agree on what is "outside".
 * Coordinates are `[lon, lat]` as in GeoJSON.
 */
class CoverageArea private constructor(
    /** Each polygon is a list of rings; the first ring is the outline, the others are holes. */
    private val polygons: List<List<List<DoubleArray>>>,
) {
    val minLon: Double
    val minLat: Double
    val maxLon: Double
    val maxLat: Double

    init {
        val outline = polygons.flatMap { it.firstOrNull().orEmpty() }
        minLon = outline.minOf { it[0] }
        maxLon = outline.maxOf { it[0] }
        minLat = outline.minOf { it[1] }
        maxLat = outline.maxOf { it[1] }
    }

    val centerLat: Double get() = (minLat + maxLat) / 2
    val centerLon: Double get() = (minLon + maxLon) / 2

    fun contains(lat: Double, lon: Double): Boolean {
        if (lon < minLon || lon > maxLon || lat < minLat || lat > maxLat) return false
        return polygons.any { polygonContains(it, lon, lat) }
    }

    companion object {
        private const val EPSILON = 1e-12
        private val adapter by lazy { Moshi.Builder().build().adapter(Any::class.java) }

        /** Null when the text is not GeoJSON or holds no polygon. */
        fun parse(geoJson: String): CoverageArea? {
            val root = try {
                adapter.fromJson(geoJson)
            } catch (_: Exception) {
                null
            } ?: return null

            val polygons = polygonsOf(root).filter { it.isNotEmpty() && it[0].isNotEmpty() }
            return if (polygons.isEmpty()) null else CoverageArea(polygons)
        }

        private fun polygonsOf(node: Any?): List<List<List<DoubleArray>>> {
            val obj = node as? Map<*, *> ?: return emptyList()
            return when (obj["type"]) {
                "FeatureCollection" -> (obj["features"] as? List<*>).orEmpty().flatMap { polygonsOf(it) }
                "Feature" -> polygonsOf(obj["geometry"])
                "Polygon" -> listOf(rings(obj["coordinates"]))
                "MultiPolygon" -> (obj["coordinates"] as? List<*>).orEmpty().map { rings(it) }
                "GeometryCollection" -> (obj["geometries"] as? List<*>).orEmpty().flatMap { polygonsOf(it) }
                else -> emptyList()
            }
        }

        private fun rings(node: Any?): List<List<DoubleArray>> =
            (node as? List<*>).orEmpty().map { ring ->
                (ring as? List<*>).orEmpty().mapNotNull { position ->
                    val p = position as? List<*> ?: return@mapNotNull null
                    val lon = (p.getOrNull(0) as? Number)?.toDouble() ?: return@mapNotNull null
                    val lat = (p.getOrNull(1) as? Number)?.toDouble() ?: return@mapNotNull null
                    doubleArrayOf(lon, lat)
                }
            }

        private fun polygonContains(rings: List<List<DoubleArray>>, x: Double, y: Double): Boolean {
            when (ringPosition(x, y, rings[0])) {
                RingPosition.OUTSIDE -> return false
                RingPosition.EDGE -> return true
                RingPosition.INSIDE -> Unit
            }
            // Inside a hole means outside; on a hole's edge still counts as inside
            return rings.drop(1).none { ringPosition(x, y, it) == RingPosition.INSIDE }
        }

        private enum class RingPosition { INSIDE, OUTSIDE, EDGE }

        private fun ringPosition(x: Double, y: Double, ring: List<DoubleArray>): RingPosition {
            var inside = false
            var j = ring.size - 1
            for (i in ring.indices) {
                val (xi, yi) = ring[i].let { it[0] to it[1] }
                val (xj, yj) = ring[j].let { it[0] to it[1] }

                val cross = (x - xi) * (yj - yi) - (y - yi) * (xj - xi)
                if (abs(cross) < EPSILON &&
                    x >= min(xi, xj) - EPSILON && x <= max(xi, xj) + EPSILON &&
                    y >= min(yi, yj) - EPSILON && y <= max(yi, yj) + EPSILON
                ) {
                    return RingPosition.EDGE
                }

                if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
                    inside = !inside
                }
                j = i
            }
            return if (inside) RingPosition.INSIDE else RingPosition.OUTSIDE
        }
    }
}
