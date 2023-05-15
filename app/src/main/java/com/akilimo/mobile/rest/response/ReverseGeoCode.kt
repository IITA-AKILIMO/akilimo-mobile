package com.akilimo.mobile.rest.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class ReverseGeoCode(
    @JsonProperty("type")
    val type: String,
    @JsonProperty("query")
    val query: List<Double>? = null,
    @JsonProperty("features")
    val mapFeatures: List<MapFeature>? = null,
    @JsonProperty("attribution")
    val attribution: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapContext(
    @JsonProperty("id")
    var id: String? = null,
    @JsonProperty("mapbox_id")
    var mapboxId: String? = null,
    @JsonProperty("text")
    var text: String? = null,
    @JsonProperty("short_code")
    var shortCode: String? = null,
    @JsonProperty("wikidata")
    var wikidata: String? = null
)


@JsonIgnoreProperties(ignoreUnknown = true)
data class MapFeature(
    @JsonProperty("id")
    var id: String? = null,
    @JsonProperty("type")
    var type: String? = null,
    @JsonProperty("place_type")
    var placeType: List<String>? = null,
    @JsonProperty("relevance")
    var relevance: Int? = null,
    @JsonProperty("properties")
    var mapProperties: MapProperties? = null,
    @JsonProperty("text")
    var text: String? = null,
    @JsonProperty("place_name")
    var placeName: String? = null,
    @JsonProperty("bbox")
    var boundingBox: List<Double>? = null,
    @JsonProperty("center")
    var center: List<Double>? = null,
    @JsonProperty("geometry")
    var geometry: MapGeometry? = null,
    @JsonProperty("context")
    var mapContext: List<MapContext>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapGeometry(
    @JsonProperty("type")
    var type: String? = null,
    @JsonProperty("coordinates")
    var coordinates: List<Double>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapProperties(
    @JsonProperty("mapbox_id")
    var mapboxId: String? = null,
    @JsonProperty("short_code")
    var shortCode: String? = null,
    @JsonProperty("wikidata")
    var wikidata: String? = null
)
