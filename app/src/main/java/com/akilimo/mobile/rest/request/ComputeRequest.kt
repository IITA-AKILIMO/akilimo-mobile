package com.akilimo.mobile.rest.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ComputeRequest {

    @Json(name = "country_code")
    var countryCode: String? = null

    @Json(name = "use_case")
    var useCase: String? = null

    @Json(name = "currency_code")
    var currencyCode: String? = null

    @Json(name = "map_lat")
    var mapLat: Double = 0.0

    @Json(name = "map_long")
    var mapLong: Double = 0.0

    @Json(name = "field_size")
    var fieldSize: Double = 0.0

    @Json(name = "area_unit")
    var areaUnit: String = "acre"

    @Json(name = "inter_cropped_crop")
    var interCroppedCrop: String = "NA"

    @Json(name = "inter_cropping_maize_rec")
    var interCroppingMaizeRec: Boolean = false

    @Json(name = "inter_cropping_potato_rec")
    var interCroppingPotatoRec: Boolean = false

    @Json(name = "fertilizer_rec")
    var fertilizerRec: Boolean = false

    @Json(name = "planting_practices_rec")
    var plantingPracticesRec: Boolean = false

    @Json(name = "scheduled_planting_rec")
    var scheduledPlantingRec: Boolean = false

    @Json(name = "scheduled_harvest_rec")
    var scheduledHarvestRec: Boolean = false

    @Json(name = "planting_date")
    var plantingDate: String? = null

    @Json(name = "harvest_date")
    var harvestDate: String? = null

    @Json(name = "planting_date_window")
    var plantingDateWindow: Int = 0

    @Json(name = "harvest_date_window")
    var harvestDateWindow: Int = 0

    @Json(name = "fallow_type")
    var fallowType: String = "none"

    @Json(name = "fallow_height")
    var fallowHeight: Int = 100

    @Json(name = "fallow_green")
    var fallowGreen: Boolean = false

    @Json(name = "problem_weeds")
    var problemWeeds: Boolean = false

    @Json(name = "tractor_plough")
    var tractorPlough: Boolean = false

    @Json(name = "tractor_harrow")
    var tractorHarrow: Boolean = false

    @Json(name = "tractor_ridger")
    var tractorRidger: Boolean = false

    @Json(name = "cost_lmo_area_basis")
    var costLmoAreaBasis: String = "areaUnit"

    @Json(name = "cost_tractor_ploughing")
    var costTractorPloughing: Double = 0.0

    @Json(name = "cost_tractor_harrowing")
    var costTractorHarrowing: Double = 0.0

    @Json(name = "cost_tractor_ridging")
    var costTractorRidging: Double = 0.0

    @Json(name = "cost_manual_ploughing")
    var costManualPloughing: Double = 0.0

    @Json(name = "cost_manual_harrowing")
    var costManualHarrowing: Double = 0.0

    @Json(name = "cost_manual_ridging")
    var costManualRidging: Double = 0.0

    @Json(name = "cost_weeding_one")
    var costWeedingOne: Double = 0.0

    @Json(name = "cost_weeding_two")
    var costWeedingTwo: Double = 0.0

    @Json(name = "ploughing_done")
    var ploughingDone: Boolean = false

    @Json(name = "harrowing_done")
    var harrowingDone: Boolean = false

    @Json(name = "ridging_done")
    var ridgingDone: Boolean = false

    @Json(name = "method_ploughing")
    var methodPloughing: String? = null

    @Json(name = "method_harrowing")
    var methodHarrowing: String? = null

    @Json(name = "method_ridging")
    var methodRidging: String? = null

    @Json(name = "method_weeding")
    var methodWeeding: String? = null

    @Json(name = "current_field_yield")
    var currentFieldYield: Double = 0.0

    @Json(name = "current_maize_performance")
    var currentMaizePerformance: Int = 0

    @Json(name = "sell_to_starch_factory")
    var sellToStarchFactory: Boolean = false

    @Json(name = "starch_factory_name")
    var starchFactoryName: String = "NA"

    @Json(name = "cassava_produce_type")
    var cassavaProduceType: String = "roots"

    @Json(name = "cassava_unit_weight")
    var cassavaUnitWeight: Int = 1000

    @Json(name = "cassava_unit_price")
    var cassavaUnitPrice: Double = 0.0

    @Json(name = "cass_up_m1")
    var cassUpM1: Double = 0.0

    @Json(name = "cass_up_m2")
    var cassUpM2: Double = 0.0

    @Json(name = "cass_up_p1")
    var cassUpP1: Double = 0.0

    @Json(name = "cass_up_p2")
    var cassUpP2: Double = 0.0

    @Json(name = "maize_produce_type")
    var maizeProduceType: String = "fresh_cob"

    @Json(name = "maize_unit_weight")
    var maizeUnitWeight: Double = 1.0

    @Json(name = "maize_unit_price")
    var maizeUnitPrice: Double = 230.0

    @Json(name = "sweet_potato_produce_type")
    var sweetPotatoProduceType: String = "tubers"

    @Json(name = "sweet_potato_unit_weight")
    var sweetPotatoUnitWeight: Double = 1000.0

    @Json(name = "sweet_potato_unit_price")
    var sweetPotatoUnitPrice: Double = 120000.0

    @Json(name = "max_investment")
    var maxInvestment: Double = 0.0

    @Json(name = "risk_attitude")
    var riskAttitude: Int = 1
}
