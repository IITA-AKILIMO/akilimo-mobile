
package com.iita.akilimo.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({
        "CMP",
        "FCY",
        "FR",
        "IC",
        "PP",
        "SPH",
        "SPH",
        "ureaBagWt",
        "ureaCostperBag",
        "ureaavailable",
        "MOPBagWt",
        "MOPCostperBag",
        "MOPavailable",
        "CANBagWt",
        "CANCostperBag",
        "CANavailable",
        "DAPBagWt",
        "DAPCostperBag",
        "DAPavailable",
        "NPK151515BagWt",
        "NPK151515CostperBag",
        "NPK151515available",
        "NPK171717BagWt",
        "NPK171717CostperBag",
        "NPK171717available",
        "NPK201010BagWt",
        "NPK201010CostperBag",
        "NPK201010available",
        "NafakaBagWt",
        "NafakaCostperBag",
        "Nafakaavailable",
        "SSPBagWt",
        "SSPCostperBag",
        "SSPavailable",
        "TSPBagWt",
        "TSPCostperBag",
        "TSPavailable",
        "PD",
        "PD_window",
        "HD",
        "HD_window",
        "area",
        "areaUnits",
        "cassPD",
        "cassUP",
        "cassUP_m1",
        "cassUP_m2",
        "cassUP_p1",
        "cassUP_p2",
        "cassUW",
        "cost_LMO_areaBasis",
        "cost_manual_harrowing",
        "cost_manual_ploughing",
        "cost_manual_ridging",
        "cost_tractor_harrowing",
        "cost_tractor_ploughing",
        "cost_tractor_ridging",
        "cost_weeding1",
        "cost_weeding2",
        "country",
        "email",
        "SMS",
        "fallowGreen",
        "fallowHeight",
        "fallowType",
        "harrowing",
        "intercrop",
        "lat",
        "lon",
        "maizePD",
        "maizeUP",
        "maizeUW",
        "sweetPotatoPD",
        "sweetPotatoUW",
        "sweetPotatoUP",
        "maxInv",
        "method_harrowing",
        "method_ploughing",
        "method_ridging",
        "nameSF",
        "ploughing",
        "problemWeeds",
        "ridging",
        "riskAtt",
        "saleSF",
        "tractor_harrow",
        "tractor_plough",
        "tractor_ridger",
        "userEmail",
        "userField",
        "userName",
        "userPhoneCC",
        "userPhoneNr",
        "fullPhoneNumber",
        "fertilizerList"
})
//@Builder(toBuilder = true)
//@Getter
@Data
public class ComputeRequest {

    @JsonProperty("country")
    private String country;

    @JsonProperty("lat")
    private double mapLat;

    @JsonProperty("lon")
    private double mapLong;

    @JsonProperty("area")
    private double fieldArea;

    @JsonProperty("areaUnits")
    private String areaUnits;

    @JsonProperty("intercropType")
    private String interCroppingType;

    @JsonProperty("intercrop")
    private boolean interCrop;

    @JsonProperty("IC")
    private boolean interCroppingRec;
    @JsonProperty("FR")
    private boolean fertilizerRec;
    @JsonProperty("PP")
    private boolean plantingPracticesRec;
    @JsonProperty("SPH")
    private boolean scheduledPlantingRec;
    @JsonProperty("SPH")
    private boolean scheduledHarvestRec;

    @JsonProperty("PD")
    private String plantingDate;
    @JsonProperty("PD_window")
    private int plantingDateWindow;

    @JsonProperty("HD")
    private String harvestDate;
    @JsonProperty("HD_window")
    private int harvestDateWindow;

    @JsonProperty("fallowType")
    private String fallowType;

    @JsonProperty("fallowHeight")
    private int fallowHeight;
    @JsonProperty("fallowGreen")
    private boolean fallowGreen;
    @JsonProperty("problemWeeds")
    private boolean problemWeeds;

    @JsonProperty("tractor_plough")
    private boolean tractorPlough;
    @JsonProperty("tractor_harrow")
    private boolean tractorHarrow;
    @JsonProperty("tractor_ridger")
    private boolean tractorRidger;

    @JsonProperty("cost_LMO_areaBasis")
    private String costLmoAreaBasis;
    @JsonProperty("cost_tractor_ploughing")
    private String costTractorPloughing;
    @JsonProperty("cost_tractor_harrowing")
    private String costTractorHarrowing;
    @JsonProperty("cost_tractor_ridging")
    private String costTractorRidging;

    @JsonProperty("cost_manual_ploughing")
    private String costManualPloughing;
    @JsonProperty("cost_manual_harrowing")
    private String costManualHarrowing;
    @JsonProperty("cost_manual_ridging")
    private String costManualRidging;
    @JsonProperty("cost_weeding1")
    private String costWeedingOne;
    @JsonProperty("cost_weeding2")
    private String costWeedingTwo;

    @JsonProperty("ploughing")
    private boolean ploughingDone;
    @JsonProperty("harrowing")
    private boolean harrowingDone;
    @JsonProperty("ridging")
    private boolean ridgingDone;

    @JsonProperty("method_ploughing")
    private String methodPloughing;
    @JsonProperty("method_harrowing")
    private String methodHarrowing;
    @JsonProperty("method_ridging")
    private String methodRidging;

    @JsonProperty("FCY")
    private int currentFieldYield;

    @JsonProperty("CMP")
    private String currentMaizePerformance;
    @JsonProperty("saleSF")
    private Boolean sellToStarchFactory;
    @JsonProperty("nameSF")
    private String starchFactoryName;
    @JsonProperty("cassPD")
    private String cassavaProduceType;

    @JsonProperty("cassUW")
    private int cassavaUnitWeight;
    @JsonProperty("cassUP")
    private double cassavaUnitPrice;

    @JsonProperty("cassUP_m1")
    private String cassUPM1;
    @JsonProperty("cassUP_m2")
    private String cassUPM2;
    @JsonProperty("cassUP_p1")
    private String cassUPP1;
    @JsonProperty("cassUP_p2")
    private String cassUPP2;

    @JsonProperty("maizePD")
    private String maizeProduceType;
    @JsonProperty("maizeUW")
    private String maizeUnitWeight;
    @JsonProperty("maizeUP")
    private String maizeUnitPrice;

    @JsonProperty("sweetPotatoPD")
    private String sweetPotatoProduceType;
    @JsonProperty("sweetPotatoUW")
    private String sweetPotatoUnitWeight;
    @JsonProperty("sweetPotatoUP")
    private String sweetPotatoUnitPrice;

    @JsonProperty("maxInv")
    private Double maxInvestment;

    @JsonProperty("SMS")
    private boolean sendSms;
    @JsonProperty("email")
    private boolean sendEmail;

    @JsonProperty("userPhoneCC")
    private String mobileCountryCode;
    @JsonProperty("userPhoneNr")
    private String mobileNumber;
    @JsonProperty("fullPhoneNumber")
    private String fullPhoneNumber;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("userEmail")
    private String userEmail;
    @JsonProperty("userField")
    private String fieldDescription;

    @JsonProperty("riskAtt")
    private int riskAttitude;
}