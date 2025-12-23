package com.akilimo.mobile.rest.request

import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumUseCase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class ComputeRequest(
    val farmInformation: FarmInformation,
    val interCropping: InterCropping,
    val recommendations: Recommendations,
    val planting: Planting,
    val fallow: Fallow,
    val tractorCosts: TractorCosts,
    val manualCosts: ManualCosts,
    val weedingCosts: WeedingCosts,
    val operationsDone: OperationsDone,
    val methods: Methods,
    val yieldInfo: YieldInfo,
    val cassava: CropInfo,
    val maize: CropInfo,
    val sweetPotato: CropInfo,
    val maxInvestment: Double
) {

    @JsonClass(generateAdapter = true)
    data class FarmInformation(
        @param:Json(name = "country_code")
        val countryCode: String,
        @param:Json(name = "use_case")
        val useCase: EnumUseCase,
        @param:Json(name = "currency_code")
        val currencyCode: String? = null,
        @param:Json(name = "map_lat")
        val mapLat: Double,
        @param:Json(name = "map_long")
        val mapLong: Double,
        @param:Json(name = "field_size")
        val fieldSize: Double,
        @param:Json(name = "area_unit")
        val areaUnit: EnumAreaUnit
    )

    @JsonClass(generateAdapter = true)
    data class InterCropping(
        @param:Json(name = "inter_cropped_crop")
        val interCroppedCrop: String,
        @param:Json(name = "inter_cropping_maize_rec")
        val interCroppingMaizeRec: Boolean,
        @param:Json(name = "inter_cropping_potato_rec")
        val interCroppingPotatoRec: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class Recommendations(
        @param:Json(name = "fertilizer_rec")
        val fertilizerRec: Boolean,
        @param:Json(name = "planting_practices_rec")
        val plantingPracticesRec: Boolean,
        @param:Json(name = "scheduled_planting_rec")
        val scheduledPlantingRec: Boolean,
        @param:Json(name = "scheduled_harvest_rec")
        val scheduledHarvestRec: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class Planting(
        @param:Json(name = "planting_date")
        val plantingDate: LocalDate,
        @param:Json(name = "harvest_date")
        val harvestDate: LocalDate,
        @param:Json(name = "planting_date_window")
        val plantingDateWindow: Long,
        @param:Json(name = "harvest_date_window")
        val harvestDateWindow: Long
    )

    @JsonClass(generateAdapter = true)
    data class Fallow(
        @param:Json(name = "fallow_type")
        val fallowType: String,
        @param:Json(name = "fallow_height")
        val fallowHeight: Int,
        @param:Json(name = "fallow_green")
        val fallowGreen: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class TractorCosts(
        @param:Json(name = "tractor_plough")
        val hasTractorPlough: Boolean,
        @param:Json(name = "tractor_harrow")
        val hasTractorHarrow: Boolean,
        @param:Json(name = "tractor_ridger")
        val hasTractorRidger: Boolean,
        @param:Json(name = "cost_lmo_area_basis")
        val costLmoAreaBasis: String = "areaUnit",
        @param:Json(name = "cost_tractor_ploughing")
        val costPloughing: Double,
        @param:Json(name = "cost_tractor_harrowing")
        val costHarrowing: Double,
        @param:Json(name = "cost_tractor_ridging")
        val costRidging: Double
    )

    @JsonClass(generateAdapter = true)
    data class ManualCosts(
        @param:Json(name = "cost_manual_ploughing")
        val costPloughing: Double,
        @param:Json(name = "cost_manual_harrowing")
        val costHarrowing: Double,
        @param:Json(name = "cost_manual_ridging")
        val costRidging: Double
    )

    @JsonClass(generateAdapter = true)
    data class WeedingCosts(
        @param:Json(name = "cost_weeding_one")
        val costOne: Double,
        @param:Json(name = "cost_weeding_two")
        val costTwo: Double
    )

    @JsonClass(generateAdapter = true)
    data class OperationsDone(
        @param:Json(name = "ploughing_done")
        val ploughingDone: Boolean,
        @param:Json(name = "harrowing_done")
        val harrowingDone: Boolean,
        @param:Json(name = "ridging_done")
        val ridgingDone: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class Methods(
        @param:Json(name = "method_ploughing")
        val methodPloughing: String,
        @param:Json(name = "method_harrowing")
        val methodHarrowing: String,
        @param:Json(name = "method_ridging")
        val methodRidging: String,
        @param:Json(name = "method_weeding")
        val methodWeeding: String
    )

    @JsonClass(generateAdapter = true)
    data class YieldInfo(
        @param:Json(name = "current_field_yield")
        val currentFieldYield: Double,
        @param:Json(name = "current_maize_performance")
        val currentMaizePerformance: Int,
        @param:Json(name = "sell_to_starch_factory")
        val sellToStarchFactory: Boolean,
        @param:Json(name = "starch_factory_name")
        val starchFactoryName: String
    )

    @JsonClass(generateAdapter = true)
    data class CropInfo(
        @param:Json(name = "produce_type")
        val produceType: String,
        @param:Json(name = "unit_weight")
        val unitWeight: Double,
        @param:Json(name = "unit_price")
        val unitPrice: Double = 0.0,
        @param:Json(name = "unit_price_maize1")
        val upM1: Double = 0.0,
        @param:Json(name = "unit_price_maize2")
        val upM2: Double = 0.0,
        @param:Json(name = "unit_price_potato1")
        val upP1: Double = 0.0,
        @param:Json(name = "unit_price_potato2")
        val upP2: Double = 0.0
    )
}