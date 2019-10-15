package com.iita.akilimo.entities

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
class ComputeRequest {

    @JsonProperty("country")
    var country: String? = null

    @JsonProperty("currency")
    var currency: String? = null

    @JsonProperty("lat")
    var mapLat: Double = 0.toDouble()

    @JsonProperty("lon")
    var mapLong: Double = 0.toDouble()

    @JsonProperty("area")
    var fieldArea: Double = 0.toDouble()

    @JsonProperty("areaUnits")
    var areaUnits: String? = null
    @JsonProperty("intercropType")
    var interCroppingType: String? = null

    @JsonProperty("intercrop")
    var interCrop: Boolean = false

    @JsonProperty("IC")
    var interCroppingRec: Boolean = false
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
    var costTractorPloughing: String? = null
    @JsonProperty("cost_tractor_harrowing")
    var costTractorHarrowing: String? = null
    @JsonProperty("cost_tractor_ridging")
    var costTractorRidging: String? = null

    @JsonProperty("cost_manual_ploughing")
    var costManualPloughing: String? = null
    @JsonProperty("cost_manual_harrowing")
    var costManualHarrowing: String? = null
    @JsonProperty("cost_manual_ridging")
    var costManualRidging: String? = null
    @JsonProperty("cost_weeding1")
    var costWeedingOne: String? = null
    @JsonProperty("cost_weeding2")
    var costWeedingTwo: String? = null

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
    var cassavaUnitPrice: Double = 0.toDouble()

    @JsonProperty("cassUP_m1")
    var cassUPM1: String? = null
    @JsonProperty("cassUP_m2")
    var cassUPM2: String? = null
    @JsonProperty("cassUP_p1")
    var cassUPP1: String? = null
    @JsonProperty("cassUP_p2")
    var cassUPP2: String? = null

    @JsonProperty("maizePD")
    var maizeProduceType: String? = null
    @JsonProperty("maizeUW")
    var maizeUnitWeight: String? = null
    @JsonProperty("maizeUP")
    var maizeUnitPrice: String? = null

    @JsonProperty("sweetPotatoPD")
    var sweetPotatoProduceType: String? = null
    @JsonProperty("sweetPotatoUW")
    var sweetPotatoUnitWeight: String? = null
    @JsonProperty("sweetPotatoUP")
    var sweetPotatoUnitPrice: String? = null

    @JsonProperty("maxInv")
    var maxInvestment: Double? = null

    @JsonProperty("SMS")
    var sendSms: Boolean = false
    @JsonProperty("email")
    var sendEmail: Boolean = false

    @JsonProperty("userPhoneCC")
    var mobileCountryCode: String? = null
    @JsonProperty("userPhoneNr")
    var mobileNumber: String? = null
    @JsonProperty("fullPhoneNumber")
    var fullPhoneNumber: String? = null
    @JsonProperty("userName")
    var userName: String? = null
    @JsonProperty("userEmail")
    var userEmail: String? = null
    @JsonProperty("userField")
    var fieldDescription: String? = null

    @JsonProperty("riskAtt")
    var riskAttitude: Int = 0
}