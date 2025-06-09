package com.akilimo.mobile.rest.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReverseGeoCode(
    @Json(name = "type")
    val type: String,
    @Json(name = "query")
    val query: List<Double>? = null,
    @Json(name = "features")
    val mapFeatures: List<MapFeature>? = null,
    @Json(name = "attribution")
    val attribution: String? = null
)

@JsonClass(generateAdapter = true)
data class MapContext(
    @Json(name = "id")
    var id: String? = null,
    @Json(name = "mapbox_id")
    var mapboxId: String? = null,
    @Json(name = "text")
    var text: String? = null,
    @Json(name = "short_code")
    var shortCode: String? = null,
    @Json(name = "wikidata")
    var wikidata: String? = null
)

@JsonClass(generateAdapter = true)
data class MapFeature(
    @Json(name = "id")
    var id: String? = null,
    @Json(name = "type")
    var type: String? = null,
    @Json(name = "place_type")
    var placeType: List<String>? = null,
    @Json(name = "relevance")
    var relevance: Int? = null,
    @Json(name = "properties")
    var mapProperties: MapProperties? = null,
    @Json(name = "text")
    var text: String? = null,
    @Json(name = "place_name")
    var placeName: String? = null,
    @Json(name = "bbox")
    var boundingBox: List<Double>? = null,
    @Json(name = "center")
    var center: List<Double>? = null,
    @Json(name = "geometry")
    var geometry: MapGeometry? = null,
    @Json(name = "context")
    var mapContext: List<MapContext>? = null
)

@JsonClass(generateAdapter = true)
data class MapGeometry(
    @Json(name = "type")
    var type: String? = null,
    @Json(name = "coordinates")
    var coordinates: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class MapProperties(
    @Json(name = "mapbox_id")
    var mapboxId: String? = null,
    @Json(name = "short_code")
    var shortCode: String? = null,
    @Json(name = "wikidata")
    var wikidata: String? = null
)
