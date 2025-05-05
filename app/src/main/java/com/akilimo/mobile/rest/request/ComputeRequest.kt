package com.akilimo.mobile.rest.request

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class ComputeRequest {
    var countryCode: String = ""
    var useCase: String? = null
    var currency: String? = null
    var mapLat: Double = 0.0
    var mapLong: Double = 0.0
    var fieldArea: Double = 0.0
    var areaUnits: String? = null
    var interCroppingType: String? = null
    var interCrop: Boolean = false
    var interCroppingMaizeRec: Boolean = false
    var interCroppingPotatoRec: Boolean = false
    var fertilizerRec: Boolean = false
    var plantingPracticesRec: Boolean = false
    var scheduledPlantingRec: Boolean = false
    var scheduledHarvestRec: Boolean = false
    var plantingDate: String? = null
    var plantingDateWindow: Int = 0
    var harvestDate: String? = null
    var harvestDateWindow: Int = 0
    var fallowType: String = "none"
    var fallowHeight: Int = 100
    var fallowGreen: Boolean = false
    var problemWeeds: Boolean = false
    var tractorPlough: Boolean = false
    var tractorHarrow: Boolean = false
    var tractorRidger: Boolean = false
    var costLmoAreaBasis: String = "areaUnit"
    var costTractorPloughing: Double = 0.0
    var costTractorHarrowing: Double = 0.0
    var costTractorRidging: Double = 0.0
    var costManualPloughing: Double = 0.0
    var costManualHarrowing: Double = 0.0
    var costManualRidging: Double = 0.0
    var costWeedingOne: Double = 0.0
    var costWeedingTwo: Double = 0.0
    var ploughingDone: Boolean = false
    var harrowingDone: Boolean = false
    var ridgingDone: Boolean = false
    var methodPloughing: String? = null
    var methodHarrowing: String? = null
    var methodRidging: String? = null
    var methodWeeding: String? = null
    var currentFieldYield: Double = 0.0
    var currentMaizePerformance: String? = null
    var sellToStarchFactory: Boolean? = null
    var starchFactoryName: String? = null
    var cassavaProduceType: String? = null
    var cassavaUnitWeight: Int = 50
    var cassavaUnitPrice: Double = 0.0
    var cassUPM1: Double = 0.0
    var cassUPM2: Double = 0.0
    var cassUPP1: Double = 0.0
    var cassUPP2: Double = 0.0

    var maizeProduceType: String = "fresh_cob"
    var maizeUnitWeight: Double = 50.0
    var maizeUnitPrice: Double = 0.0

    var sweetPotatoProduceType: String = "tubers"
    var sweetPotatoUnitWeight: Double = 50.0
    var sweetPotatoUnitPrice: Double = 0.0
    var maxInvestment: Double? = null
    var riskAttitude: Int = 1
}