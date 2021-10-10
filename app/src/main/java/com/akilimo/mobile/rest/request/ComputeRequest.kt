package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.annotation.JsonProperty

class ComputeRequest {

    @JsonProperty("country")
    var country: String? = null

    @JsonProperty("useCase")
    var useCase: String? = null

    @JsonProperty("currency")
    var currency: String? = null

    @JsonProperty("lat")
    var mapLat: Double = 0.0

    @JsonProperty("lon")
    var mapLong: Double = 0.0

    @JsonProperty("area")
    var fieldArea: Double = 0.0

    @JsonProperty("areaUnits")
    var areaUnits: String? = null

    @JsonProperty("intercropType")
    var interCroppingType: String? = null

    @JsonProperty("intercrop")
    var interCrop: Boolean = false

    @JsonProperty("IC_MAIZE")
    var interCroppingMaizeRec: Boolean = false

    @JsonProperty("IC_SP")
    var interCroppingPotatoRec: Boolean = false

    @JsonProperty("FR")
    var fertilizerRec: Boolean = false

    @JsonProperty("PP")
    var plantingPracticesRec: Boolean = false

    @JsonProperty("SPP")
    var scheduledPlantingRec: Boolean = false

    @JsonProperty("SPH")
    var scheduledHarvestRec: Boolean = false

    @JsonProperty("PD")
    var plantingDate: String? = null

    @JsonProperty("PD_window")
    var plantingDateWindow: Int = 0

    @JsonProperty("HD")
    var harvestDate: String? = null

    @JsonProperty("HD_window")
    var harvestDateWindow: Int = 0

    @JsonProperty("fallowType")
    var fallowType: String? = null

    @JsonProperty("fallowHeight")
    var fallowHeight: Int = 0

    @JsonProperty("fallowGreen")
    var fallowGreen: Boolean = false

    @JsonProperty("problemWeeds")
    var problemWeeds: Boolean = false

    @JsonProperty("tractor_plough")
    var tractorPlough: Boolean = false

    @JsonProperty("tractor_harrow")
    var tractorHarrow: Boolean = false

    @JsonProperty("tractor_ridger")
    var tractorRidger: Boolean = false

    @JsonProperty("cost_LMO_areaBasis")
    var costLmoAreaBasis: String? = null

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

    @JsonProperty("cost_weeding1")
    var costWeedingOne: Double = 0.0

    @JsonProperty("cost_weeding2")
    var costWeedingTwo: Double = 0.0

    @JsonProperty("ploughing")
    var ploughingDone: Boolean = false

    @JsonProperty("harrowing")
    var harrowingDone: Boolean = false

    @JsonProperty("ridging")
    var ridgingDone: Boolean = false

    @JsonProperty("method_ploughing")
    var methodPloughing: String? = null

    @JsonProperty("method_harrowing")
    var methodHarrowing: String? = null

    @JsonProperty("method_ridging")
    var methodRidging: String? = null

    @JsonProperty("method_weeding")
    var methodWeeding: String? = null

    @JsonProperty("FCY")
    var currentFieldYield: Int = 0

    @JsonProperty("CMP")
    var currentMaizePerformance: String? = null

    @JsonProperty("saleSF")
    var sellToStarchFactory: Boolean? = null

    @JsonProperty("nameSF")
    var starchFactoryName: String? = null

    @JsonProperty("cassPD")
    var cassavaProduceType: String? = null

    @JsonProperty("cassUW")
    var cassavaUnitWeight: Int = 0

    @JsonProperty("cassUP")
    var cassavaUnitPrice: Double = 0.0

    @JsonProperty("cassUP_m1")
    var cassUPM1: Double = 0.0

    @JsonProperty("cassUP_m2")
    var cassUPM2: Double = 0.0

    @JsonProperty("cassUP_p1")
    var cassUPP1: Double = 0.0

    @JsonProperty("cassUP_p2")
    var cassUPP2: Double = 0.0

    @JsonProperty("maizePD")
    var maizeProduceType: String? = null

    @JsonProperty("maizeUW")
    var maizeUnitWeight: Int = 0

    @JsonProperty("maizeUP")
    var maizeUnitPrice: Double = 0.0

    @JsonProperty("sweetPotatoPD")
    var sweetPotatoProduceType: String? = null

    @JsonProperty("sweetPotatoUW")
    var sweetPotatoUnitWeight: Int = 0

    @JsonProperty("sweetPotatoUP")
    var sweetPotatoUnitPrice: Double = 0.0

    @JsonProperty("maxInv")
    var maxInvestment: Double? = null


    @JsonProperty("riskAtt")
    var riskAttitude: Int = 1
}
