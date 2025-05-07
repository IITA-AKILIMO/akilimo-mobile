package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.threeten.bp.LocalDate

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class ComputeRequest {

    @JsonProperty("country_code")
    var countryCode: String? = null

    @JsonProperty("use_case")
    var useCase: String? = null

    @JsonProperty("currency_code")
    var currencyCode: String? = null

    @JsonProperty("map_lat")
    var mapLat: Double = 0.0

    @JsonProperty("map_long")
    var mapLong: Double = 0.0

    @JsonProperty("field_size")
    var fieldSize: Double = 0.0

    @JsonProperty("area_unit")
    var areaUnit: String = "acre"

    @JsonProperty("inter_cropped_crop")
    var interCroppedCrop: String = "NA"

    @JsonProperty("inter_cropping_maize_rec")
    var interCroppingMaizeRec: Boolean = false

    @JsonProperty("inter_cropping_potato_rec")
    var interCroppingPotatoRec: Boolean = false

    @JsonProperty("fertilizer_rec")
    var fertilizerRec: Boolean = false

    @JsonProperty("planting_practices_rec")
    var plantingPracticesRec: Boolean = false

    @JsonProperty("scheduled_planting_rec")
    var scheduledPlantingRec: Boolean = false

    @JsonProperty("scheduled_harvest_rec")
    var scheduledHarvestRec: Boolean = false

    @JsonProperty("planting_date")
    var plantingDate: LocalDate? = null

    @JsonProperty("planting_date_window")
    var plantingDateWindow: Int = 0

    @JsonProperty("harvest_date")
    var harvestDate: LocalDate? = null

    @JsonProperty("harvest_date_window")
    var harvestDateWindow: Int = 0

    @JsonProperty("fallow_type")
    var fallowType: String = "none"

    @JsonProperty("fallow_height")
    var fallowHeight: Int = 100

    @JsonProperty("fallow_green")
    var fallowGreen: Boolean = false

    @JsonProperty("problem_weeds")
    var problemWeeds: Boolean = false

    @JsonProperty("tractor_plough")
    var tractorPlough: Boolean = false

    @JsonProperty("tractor_harrow")
    var tractorHarrow: Boolean = false

    @JsonProperty("tractor_ridger")
    var tractorRidger: Boolean = false

    @JsonProperty("cost_lmo_area_basis")
    var costLmoAreaBasis: String = "areaUnit"

    @JsonProperty("cost_tractor_ploughing")
    var costTractorPloughing: Double = 0.0

    @JsonProperty("cost_tractor_harrowing")
    var costTractorHarrowing: Double = 0.0

    @JsonProperty("cost_tractor_ridging")
    var costTractorRidging: Double = 0.0

    @JsonProperty("cost_manual_ploughing")
    var costManualPloughing: Double = 0.0

    @JsonProperty("cost_manual_harrowing")
    var costManualHarrowing: Double = 0.0

    @JsonProperty("cost_manual_ridging")
    var costManualRidging: Double = 0.0

    @JsonProperty("cost_weeding_one")
    var costWeedingOne: Double = 0.0

    @JsonProperty("cost_weeding_two")
    var costWeedingTwo: Double = 0.0

    @JsonProperty("ploughing_done")
    var ploughingDone: Boolean = false

    @JsonProperty("harrowing_done")
    var harrowingDone: Boolean = false

    @JsonProperty("ridging_done")
    var ridgingDone: Boolean = false

    @JsonProperty("method_ploughing")
    var methodPloughing: String? = null

    @JsonProperty("method_harrowing")
    var methodHarrowing: String? = null

    @JsonProperty("method_ridging")
    var methodRidging: String? = null

    @JsonProperty("method_weeding")
    var methodWeeding: String? = null

    @JsonProperty("current_field_yield")
    var currentFieldYield: Double = 0.0

    @JsonProperty("current_maize_performance")
    var currentMaizePerformance: String? = null

    @JsonProperty("sell_to_starch_factory")
    var sellToStarchFactory: Boolean = false

    @JsonProperty("starch_factory_name")
    var starchFactoryName: String? = null

    @JsonProperty("cassava_produce_type")
    var cassavaProduceType: String = "roots"

    @JsonProperty("cassava_unit_weight")
    var cassavaUnitWeight: Int = 1000

    @JsonProperty("cassava_unit_price")
    var cassavaUnitPrice: Double = 0.0

    @JsonProperty("cass_up_m1")
    var cassUpM1: Double = 0.0

    @JsonProperty("cass_up_m2")
    var cassUpM2: Double = 0.0

    @JsonProperty("cass_up_p1")
    var cassUpP1: Double = 0.0

    @JsonProperty("cass_up_p2")
    var cassUpP2: Double = 0.0

    @JsonProperty("maize_produce_type")
    var maizeProduceType: String = "fresh_cob"

    @JsonProperty("maize_unit_weight")
    var maizeUnitWeight: Double = 1.0

    @JsonProperty("maize_unit_price")
    var maizeUnitPrice: Double = 230.0

    @JsonProperty("sweet_potato_produce_type")
    var sweetPotatoProduceType: String = "tubers"

    @JsonProperty("sweet_potato_unit_weight")
    var sweetPotatoUnitWeight: Double = 1000.0

    @JsonProperty("sweet_potato_unit_price")
    var sweetPotatoUnitPrice: Double = 120000.0

    @JsonProperty("max_investment")
    var maxInvestment: Double = 0.0

    @JsonProperty("risk_attitude")
    var riskAttitude: Int = 1
}