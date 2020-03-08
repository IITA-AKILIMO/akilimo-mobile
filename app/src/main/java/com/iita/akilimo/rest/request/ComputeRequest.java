
package com.iita.akilimo.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class ComputeRequest {


    @JsonProperty("country")
    private String country;

    @JsonProperty("lat")
    private Double mapLat;

    @JsonProperty("lon")
    private Double mapLong;

    @JsonProperty("area")
    private Double fieldArea;

    @JsonProperty("areaUnits")
    private String areaUnits;

    @JsonProperty("intercrop")
    private String interCroppingType;

    @JsonProperty("IC_MAIZE")
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

    @JsonProperty("HD")
    private String harvestDate;

    @JsonProperty("PD_window")
    private int plantingDateWindow;
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
    private double costLmoAreaBasis;
    @JsonProperty("cost_tractor_ploughing")
    private double costTractorPloughing;
    @JsonProperty("cost_tractor_harrowing")
    private double costTractorHarrowing;
    @JsonProperty("cost_tractor_ridging")
    private double costTractorRidging;

    @JsonProperty("cost_manual_ploughing")
    private double costManualPloughing;
    @JsonProperty("cost_manual_harrowing")
    private double costManualHarrowing;
    @JsonProperty("cost_manual_ridging")
    private double costManualRidging;
    @JsonProperty("cost_weeding1")
    private double costWeedingOne;
    @JsonProperty("cost_weeding2")
    private double costWeedingTwo;

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
    private double cassUPM1;
    @JsonProperty("cassUP_m2")
    private double cassUPM2;
    @JsonProperty("cassUP_p1")
    private double cassUPP1;
    @JsonProperty("cassUP_p2")
    private double cassUPP2;

    @JsonProperty("maizePD")
    private String maizeProduceType;
    @JsonProperty("maizeUW")
    private int maizeUnitWeight;
    @JsonProperty("maizeUP")
    private double maizeUnitPrice;

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
    private String fieldName;
    @JsonProperty("fieldDesc")
    private String fieldDescription;

    @JsonProperty("riskAtt")
    private int riskAttitude;

    @JsonProperty("ureaavailable")
    private boolean ureaAvailable;
    @JsonProperty("ureaCostperBag")
    private double ureaCostPerBag;
    @JsonProperty("ureaBagWt")
    private int ureaBagWeight;

    @JsonProperty("MOPavailable")
    private boolean mopAvailable;
    @JsonProperty("MOPCostperBag")
    private double mopCostPerBag;
    @JsonProperty("MOPBagWt")
    private int mopBagWeight;

    @JsonProperty("DAPavailable")
    private boolean dapAvailable;
    @JsonProperty("DAPCostperBag")
    private double dapCostPerBag;
    @JsonProperty("DAPBagWt")
    private int dapBagWeight;

    @JsonProperty("TSPavailable")
    private boolean tspAvailable;
    @JsonProperty("TSPCostperBag")
    private double tspCostPerBag;
    @JsonProperty("TSPBagWt")
    private int tspBagWeight;

    @JsonProperty("Nafakaavailable")
    private boolean nafakaAvailable;
    @JsonProperty("NafakaCostperBag")
    private double nafakaCostPerBag;
    @JsonProperty("NafakaBagWt")
    private int nafakaBagWeight;

    @JsonProperty("CANavailable")
    private boolean canAvailable;
    @JsonProperty("CANCostperBag")
    private double canCostPerBag;
    @JsonProperty("CANBagWt")
    private int canBagWeight;

    @JsonProperty("SSPavailable")
    private boolean sspAvailable;
    @JsonProperty("SSPCostperBag")
    private double sspCostPerBag;
    @JsonProperty("SSPBagWt")
    private int sspBagWeight;

    @JsonProperty("NPK201010available")
    private boolean nPK201010Available;
    @JsonProperty("NPK201010CostperBag")
    private double nPK201010CostPerBag;
    @JsonProperty("NPK201010BagWt")
    private int nPK201010BagWeight;

    @JsonProperty("NPK151515available")
    private boolean nPK151515Available;
    @JsonProperty("NPK151515CostperBag")
    private double nPK151515CostPerBag;
    @JsonProperty("NPK151515BagWt")
    private int nPK151515BagWeight;

    @JsonProperty("NPK171717available")
    private boolean npK171717Available;
    @JsonProperty("NPK171717CostperBag")
    private double npK171717CostPerBag;
    @JsonProperty("NPK171717BagWt")
    private int npK171717BagWeight;
}